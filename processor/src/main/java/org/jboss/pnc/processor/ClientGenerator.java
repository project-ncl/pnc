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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import org.jboss.pnc.processor.annotation.Client;

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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@SupportedAnnotationTypes(
        {"org.jboss.pnc.processor.annotation.Client"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClientGenerator extends AbstractProcessor {

    private static final Logger logger = Logger.getLogger(ClientGenerator.class.getName());

    public ClientGenerator() {
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        logger.info("Running annotation processor ...");
        try {
            return process0(annotations, roundEnv);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to processs annotated sources.", e);
        }
        return false;
    }

    private boolean process0(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        Set<? extends Element> restApiInterfaces = roundEnv.getElementsAnnotatedWith(Client.class);

        for (Element endpointApi : restApiInterfaces) {

            String restInterfaceName = endpointApi.getSimpleName().toString();
            logger.info("Generating client for " + restInterfaceName);

            List<MethodSpec> methods = new ArrayList<>();
            List<MethodSpec> restMethods = new ArrayList<>();

            for (ExecutableElement restApiMethod : ElementFilter.methodsIn(endpointApi.getEnclosedElements())) {
                logger.info("Processing method " + restApiMethod.getSimpleName());

                if (restApiMethod.getKind() == ElementKind.METHOD) {
                    TypeMirror returnType = restApiMethod.getReturnType();

                    MethodSpec.Builder builder = MethodSpec.methodBuilder(restApiMethod.getSimpleName().toString())
                            .addModifiers(Modifier.PUBLIC);


                    List<String> parameters = new ArrayList<>();
                    for (VariableElement parameter : restApiMethod.getParameters()) {
                        builder.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
                        parameters.add(parameter.getSimpleName().toString());
                    }
                    String parametersList = parameters.stream().collect(Collectors.joining(", "));

                    builder.addException(ClassName.get("org.jboss.pnc.client", "RemoteResourceException"))
                            .beginControlFlow("try");

                    //startsWith because off the generics
                    if (ClassName.get(returnType).toString().startsWith(ClassName.get("org.jboss.pnc.dto.response", "Page").toString())) {
                        //Page result
                        builder.returns(TypeName.get(returnType))
                        .addStatement("return getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ")");
                    } else if (ClassName.get(returnType).toString().startsWith(ClassName.get("org.jboss.pnc.dto.response", "Singleton").toString())) {
                        //single result
                        TypeMirror singletonTypeGeneric = ((DeclaredType)returnType).getTypeArguments().get(0); //TODO some validation
                        if (restApiMethod.getAnnotation(GET.class) != null) {
                            builder
                                    .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(singletonTypeGeneric)))
                                    .addStatement("return Optional.ofNullable(getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ").getContent())");
                        } else if (restApiMethod.getAnnotation(POST.class) != null) {
                            builder.returns(TypeName.get(singletonTypeGeneric))
                                    .addStatement("return getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ").getContent()");
                        }
                    } else if (ClassName.get(returnType).toString().equals("java.lang.String")) {
                        //string response
                        builder
                                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(String.class)))
                                .addStatement("return Optional.ofNullable(getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + "))")
                                .nextControlFlow("catch ($T e)", NotFoundException.class)
                                .addStatement("return Optional.empty()");
                    } else if (ClassName.get(returnType).toString().equals("void")) {
                        //void response
                        builder
                                .addException(ClassName.get("org.jboss.pnc.client", "RemoteResourceNotFoundException"))
                                .returns(TypeName.get(void.class))
                                .addStatement("getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ")")
                                .nextControlFlow("catch ($T e)", NotFoundException.class)
                                .addStatement("throw new RemoteResourceNotFoundException(e)");
                    } else {
                        //any other return types
                        builder
                                .addException(ClassName.get("org.jboss.pnc.client", "ClientException"))
                                .returns(TypeName.get(returnType))
                                .addStatement("return getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ")")
                                .nextControlFlow("catch ($T e)", NotFoundException.class)
                                .addStatement("throw new RemoteResourceNotFoundException(e)");
                    }

                    MethodSpec methodSpec = builder
                            .nextControlFlow("catch ($T e)", ClientErrorException.class)
                            .addStatement("throw new RemoteResourceException(e)")
                            .endControlFlow()
                            .build();
                    methods.add(methodSpec);


                    MethodSpec.Builder restMethodBuilder = MethodSpec.methodBuilder(restApiMethod.getSimpleName().toString())
                            .returns(Response.class)
                            .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                            .addAnnotations(restApiMethod.getAnnotationMirrors().stream().map(mirror -> AnnotationSpec.get(mirror)).collect(Collectors.toSet()));

                    for (VariableElement parameter : restApiMethod.getParameters()) {
                        restMethodBuilder.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
                    }

                    restMethods.add(restMethodBuilder.build());
                }
            }

            ClassName restInterfaceClassName = ClassName.get("org.jboss.pnc.rest.api.endpoints", restInterfaceName);

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("org.jboss.pnc.client","ConnectionInfo"), "connectionInfo")
                    .addStatement("super(connectionInfo, " + restInterfaceName + ".class)")
                    .build();

            String clientName = restInterfaceName + "Client";
            clientName = clientName.replaceAll("Endpoint", "");

            TypeSpec javaClientClass = TypeSpec.classBuilder(clientName)
                    .addModifiers(Modifier.PUBLIC)
                    .addMethod(constructor)
                    .addMethods(methods)
                    .superclass(ParameterizedTypeName.get(
                            ClassName.get("org.jboss.pnc.client", "ClientBase"),
                            restInterfaceClassName
                    ))
                    .build();

            JavaFile.builder("org.jboss.pnc.client", javaClientClass)
                    .build()
                    .writeTo(processingEnv.getFiler());

        }
        return true;
    }
}
