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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.jboss.pnc.processor.annotation.ClientApi;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@SupportedAnnotationTypes(
        {"org.jboss.pnc.processor.annotation.ClientApi"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClientApiProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        System.out.println(">>>>>>>>> PROCESSING ....");
        try {
            return process0(annotations, roundEnv);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
        return false;
    }

    private boolean process0(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        Set<? extends Element> restApiInterfaces = roundEnv.getElementsAnnotatedWith(ClientApi.class);

        for (Element ednpointApi : restApiInterfaces) {

            List<MethodSpec> methods = new ArrayList<>();

            for (ExecutableElement restApiMethod : ElementFilter.methodsIn(ednpointApi.getEnclosedElements())) {
                if (restApiMethod.getKind() == ElementKind.METHOD) {
                    TypeMirror returnType = restApiMethod.getReturnType();

                    MethodSpec.Builder builder = MethodSpec.methodBuilder(restApiMethod.getSimpleName().toString())
                            .addModifiers(Modifier.PUBLIC);


                    List<String> parameters = new ArrayList<>();
                    for (VariableElement parameter : restApiMethod.getParameters()) {
                        builder.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
                        parameters.add(parameter.getSimpleName().toString());
                    }
                    String parametersList = parameters.stream().collect(Collectors.joining(","));

                    builder.addException(ClassName.get("org.jboss.pnc.client", "RemoteResourseReadException"))
                            .beginControlFlow("try");

                    if (restApiMethod.getAnnotation(GET.class) != null) {
                        //startsWith beacuse off the generics
                        if (ClassName.get(returnType).toString().startsWith(ClassName.get("org.jboss.pnc.dto.response", "Page").toString())) {
                            //Collection
                            builder.returns(TypeName.get(returnType))
                                    .addStatement("return getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ")");
                        } else {
                            builder.returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(returnType)))
                                    .addStatement("return Optional.ofNullable(getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + "))");
                        }
                    }


                    MethodSpec methodSpec = builder
                            .nextControlFlow("catch ($T e)", ClientErrorException.class)
                            .addStatement("throw new RemoteResourseReadException(\"Cannot get remote resource.\", e)")
                            .endControlFlow()
                            .build();
                    methods.add(methodSpec);
                }
            }

            MethodSpec getEndpoint = MethodSpec.methodBuilder("getEndpoint")
                    .addModifiers(Modifier.PROTECTED)
                    .returns(TypeName.get(ednpointApi.asType()))
                    .addStatement("return target.proxy(" + ednpointApi.asType().toString() + ".class)")
                    .build();

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("org.jboss.pnc.client","ConnectionInfo"), "connectionInfo")
                    .addStatement("super(connectionInfo)")
                    .build();

            TypeSpec clazz = TypeSpec.classBuilder(ednpointApi.getSimpleName().toString() + "Client")
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(constructor)
                    .addMethod(getEndpoint)
                    .addMethods(methods)
                    .superclass(ClassName.get("org.jboss.pnc.client", "ClientBaseSimple"))
                    .build();

            JavaFile.builder("org.jboss.pnc.client", clazz)
                    .build()
                    .writeTo(processingEnv.getFiler());
        }


        helloWord();

        return true;
    }

    private void helloWord() throws IOException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("ProjectClientX")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile.builder("org.jboss.pnc.client", helloWorld)
                .build()
                .writeTo(processingEnv.getFiler());
    }

}
