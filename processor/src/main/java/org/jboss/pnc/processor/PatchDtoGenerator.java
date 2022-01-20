/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.processor.annotation.PatchSupport;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@SupportedAnnotationTypes({ "org.jboss.pnc.processor.annotation.PatchSupport" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class PatchDtoGenerator extends AbstractProcessor {

    private static final Logger logger = Logger.getLogger(PatchDtoGenerator.class.getName());

    public PatchDtoGenerator() {
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.info("Running patch DTO annotation processor ...");
        try {
            return process0(annotations, roundEnv);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to processs annotated sources.", e);
        }
        return false;
    }

    private boolean process0(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        Set<? extends Element> dtos = roundEnv.getElementsAnnotatedWith(PatchSupport.class);
        for (Element dto : dtos) {
            if (dto.getKind() == ElementKind.CLASS) {
                generatePatchClass((TypeElement) dto);
            }
        }
        return true;
    }

    private void generatePatchClass(TypeElement dto) throws IOException {
        String dtoName = dto.getSimpleName().toString();
        String patchBuilderClassName = dtoName + "PatchBuilder";

        List<MethodSpec> methods = new ArrayList<>();

        // DTO fields
        List<VariableElement> fields = ElementFilter.fieldsIn(dto.getEnclosedElements());

        // Ref fields
        Element dtoRef = ((DeclaredType) dto.getSuperclass()).asElement();
        fields.addAll(ElementFilter.fieldsIn(dtoRef.getEnclosedElements()));

        for (VariableElement dtoField : fields) {
            Name fieldName = dtoField.getSimpleName();
            PatchSupport annotation = dtoField.getAnnotation(PatchSupport.class);
            if (annotation == null) {
                logger.info("Skipping DTO field " + fieldName);
                continue;
            }
            logger.info("Processing DTO field " + fieldName);

            for (PatchSupport.Operation operation : annotation.value()) {
                if (operation.equals(PatchSupport.Operation.ADD)) {
                    createAddMethod(methods, dtoField, patchBuilderClassName);
                } else if (operation.equals(PatchSupport.Operation.REMOVE)) {
                    createRemoveMethod(methods, dtoField, patchBuilderClassName);
                } else if (operation.equals(PatchSupport.Operation.REPLACE)) {
                    createReplaceMethod(methods, dtoField, patchBuilderClassName);
                }
            }

        }

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super(org.jboss.pnc.dto." + dtoName + ".class)")
                .build();

        TypeSpec javaPatchClass = TypeSpec.classBuilder(patchBuilderClassName)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(constructor)
                .addMethods(methods)
                .superclass(
                        ParameterizedTypeName.get(
                                ClassName.get("org.jboss.pnc.client.patch", "PatchBase"),
                                ClassName.get("org.jboss.pnc.client.patch", patchBuilderClassName),
                                TypeName.get(dto.asType())))
                .build();

        JavaFile.builder("org.jboss.pnc.client.patch", javaPatchClass).build().writeTo(processingEnv.getFiler());
    }

    private void createAddMethod(List<MethodSpec> methods, VariableElement dtoField, String patchBuilderClassName) {

        String fieldName = dtoField.getSimpleName().toString();

        String methodName = "add" + StringUtils.firstCharToUpperCase(fieldName);
        MethodSpec.Builder methodBuilder = beginMethod(methodName);
        ParameterSpec parameterSpec = ParameterSpec.builder(ParameterizedTypeName.get(dtoField.asType()), "elements")
                .build();
        methodBuilder.addParameter(parameterSpec);
        methodBuilder.returns(ClassName.get("org.jboss.pnc.client.patch", patchBuilderClassName));
        methodBuilder.addStatement("return add(elements, \"" + fieldName + "\")");

        MethodSpec methodSpec = completeMethod(methodBuilder);
        methods.add(methodSpec);
    }

    private void createRemoveMethod(List<MethodSpec> methods, VariableElement dtoField, String patchBuilderClassName) {

        String fieldName = dtoField.getSimpleName().toString();

        String methodName = "remove" + StringUtils.firstCharToUpperCase(fieldName);
        MethodSpec.Builder methodBuilder = beginMethod(methodName);
        ParameterSpec parameterSpec = ParameterSpec
                .builder(ParameterizedTypeName.get(ClassName.get(Collection.class), TypeName.get(Object.class)), "keys")
                .build();
        methodBuilder.addParameter(parameterSpec);
        methodBuilder.returns(ClassName.get("org.jboss.pnc.client.patch", patchBuilderClassName));
        methodBuilder.addStatement("return remove(keys, \"" + fieldName + "\")");

        MethodSpec methodSpec = completeMethod(methodBuilder);
        methods.add(methodSpec);
    }

    private void createReplaceMethod(List<MethodSpec> methods, VariableElement dtoField, String patchBuilderClassName) {

        String fieldName = dtoField.getSimpleName().toString();

        String methodName = "replace" + StringUtils.firstCharToUpperCase(fieldName);
        MethodSpec.Builder methodBuilder = beginMethod(methodName);
        ParameterSpec parameterSpec = ParameterSpec.builder(ParameterizedTypeName.get(dtoField.asType()), "element")
                .build();
        methodBuilder.addParameter(parameterSpec);
        methodBuilder.returns(ClassName.get("org.jboss.pnc.client.patch", patchBuilderClassName));
        methodBuilder.addStatement("return replace(element, \"" + fieldName + "\")");

        MethodSpec methodSpec = completeMethod(methodBuilder);
        methods.add(methodSpec);
    }

    private MethodSpec.Builder beginMethod(String methodName) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName)
                .addModifiers(Modifier.PUBLIC)
                .addException(ClassName.get("org.jboss.pnc.client.patch", "PatchBuilderException"));
        methodBuilder.beginControlFlow("try");
        return methodBuilder;
    }

    private MethodSpec completeMethod(MethodSpec.Builder methodBuilder) {
        return methodBuilder.nextControlFlow("catch ($T e)", Exception.class)
                .addStatement("throw new PatchBuilderException(\"Error creating patch.\", e)")
                .endControlFlow()
                .build();
    }

    public static TypeName[] getGenericTypeNames(VariableElement field) {
        TypeMirror fieldType = field.asType();
        List<? extends TypeMirror> typeArguments = ((DeclaredType) fieldType).getTypeArguments();
        TypeName[] typeNames = new TypeName[typeArguments.size()];
        for (int i = 0; i < typeArguments.size(); i++) {
            typeNames[i] = TypeName.get(typeArguments.get(i));
        }
        return typeNames;
    }
}
