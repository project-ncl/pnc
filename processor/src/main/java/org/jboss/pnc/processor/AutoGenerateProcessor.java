/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.processor;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
import org.jboss.pnc.processor.annotation.AutoImplement;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

@SupportedAnnotationTypes(
        {"org.jboss.pnc.processor.annotation.AutoImplement"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class AutoGenerateProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (annotations.size() == 0) {
            return false;
        }

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(AutoImplement.class);


        List<String> uniqueIdCheckList = new ArrayList<>();

        for (Element element : elements) {
            AutoImplement autoImplement = element.getAnnotation(AutoImplement.class);

            if (element.getKind() != ElementKind.INTERFACE) {
                error("The annotation @AutoImplement can only be applied on interfaces: ",
                        element);

            } else {
                boolean error = false;

                if (uniqueIdCheckList.contains(autoImplement.as())) {
                    error("AutoImplement#as should be uniquely defined", element);
                    error = true;
                }

                error = !checkIdValidity(autoImplement.as(), element);

                if (!error) {
                    uniqueIdCheckList.add(autoImplement.as());
                    try {
                        generateClass(autoImplement, element);
                    } catch (Exception e) {
                        error(e.getMessage(), null);
                    }
                }
            }
        }
        return false;
    }

    private void generateClass(AutoImplement autoImplement, Element element)
            throws Exception {

        String pkg = getPackageName(element);

        //delegate some processing to our FieldInfo class
        FieldInfo fieldInfo = FieldInfo.get(element);

        //the target interface name
        String interfaceName = getTypeName(element);

        //using our JClass to delegate most of the string appending there
        JClass implClass = new JClass();
        implClass.definePackage(pkg);
        implClass.defineClass("public class ", autoImplement.as(), "implements " + interfaceName);

        //nested builder class
        JClass builder = null;
        String builderClassName = null;

        if (autoImplement.builder()) {
            builder = new JClass();
            builder.defineClass("public static class",
                    builderClassName = autoImplement.as() + "Builder", null);
        }

        //adding class fields
        implClass.addFields(fieldInfo.getFields());
        if (builder != null) {
            builder.addFields(fieldInfo.getFields());
        }

        //adding constructor with mandatory fields
        implClass.addConstructor(builder == null ? "public" : "private",
                fieldInfo.getMandatoryFields());
        if (builder != null) {
            builder.addConstructor("private", fieldInfo.getMandatoryFields());
        }

        //generate methods
        for (Map.Entry<String, String> entry : fieldInfo.getFields().entrySet()) {
            String name = entry.getKey();
            String type = entry.getValue();
            boolean mandatory = fieldInfo.getMandatoryFields().contains(name);

            implClass.createGetterForField(name);

            //if no builder generation specified then crete setters for non mandatory fields
            if (builder == null && !mandatory) {
                implClass.createSetterForField(name);
            }

            if (builder != null && !mandatory) {
                builder.addMethod(new JMethod()
                        .defineSignature("public", false, builderClassName)
                        .name(name)
                        .addParam(type, name)
                        .defineBody(" this." + name + " = " + name + ";"
                                + JClass.LINE_BREAK
                                + " return this;"
                        )
                );
            }
        }

        if (builder != null) {

            //generate create() method of the Builder class
            JMethod createMethod = new JMethod()
                    .defineSignature("public", true, builderClassName)
                    .name("create");


            String paramString = "(";
            int i = 0;
            for (String s : fieldInfo.getMandatoryFields()) {
                createMethod.addParam(fieldInfo.getFields().get(s), s);
                paramString += (i != 0 ? ", " : "") + s;
                i++;
            }
            paramString += ");";

            createMethod.defineBody("return new " + builderClassName
                    + paramString);

            builder.addMethod(createMethod);

            //generate build() method of the builder class.
            JMethod buildMethod = new JMethod()
                    .defineSignature("public", false, autoImplement.as())
                    .name("build");
            StringBuilder buildBody = new StringBuilder();
            buildBody.append(autoImplement.as())
                    .append(" a = new ")
                    .append(autoImplement.as())
                    .append(paramString)
                    .append(JClass.LINE_BREAK);
            for (String s : fieldInfo.getFields().keySet()) {
                if (fieldInfo.getMandatoryFields().contains(s)) {
                    continue;
                }
                buildBody.append("a.")
                        .append(s)
                        .append(" = ")
                        .append(s)
                        .append(";")
                        .append(JClass.LINE_BREAK);
            }
            buildBody.append("return a;")
                    .append(JClass.LINE_BREAK);
            buildMethod.defineBody(buildBody.toString());

            builder.addMethod(buildMethod);
            implClass.addNestedClass(builder);

        }
        //finally generate class via Filer
        generateClass(pkg + "." + autoImplement.as(), implClass.end());
    }

    private String getPackageName(Element element) {
        List<PackageElement> packageElements =
                ElementFilter.packagesIn(Arrays.asList(element.getEnclosingElement()));

        Optional<PackageElement> packageElement = packageElements.stream().findAny();
        return packageElement.isPresent() ?
                packageElement.get().getQualifiedName().toString() : null;

    }

    private void generateClass(String qfn, String end) throws IOException {
        JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qfn);
        Writer writer = sourceFile.openWriter();
        writer.write(end);
        writer.close();
    }

    /**
     * Checking if the class to be generated is a valid java identifier
     * Also the name should be not same as the target interface
     */
    private boolean checkIdValidity(String name, Element e) {
        boolean valid = true;
        for (int i = 0; i < name.length(); i++) {
            if (i == 0 ? !Character.isJavaIdentifierStart(name.charAt(i)) :
                    !Character.isJavaIdentifierPart(name.charAt(i))) {
                error("AutoImplement#as should be valid java " +
                        "identifier for code generation: " + name, e);
                valid = false;
            }
        }
        if (name.equals(getTypeName(e))) {
            error("AutoImplement#as should be different than the Interface name ", e);
        }
        return valid;
    }

    /**
     * Get the simple name of the TypeMirror
     */
    private static String getTypeName(Element e) {
        TypeMirror typeMirror = e.asType();
        String[] split = typeMirror.toString().split("\\.");
        return split.length > 0 ? split[split.length - 1] : null;
    }

    private void error(String msg, Element e) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}