/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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

            for (ExecutableElement restApiMethod : ElementFilter.methodsIn(endpointApi.getEnclosedElements())) {
                logger.info("Processing method " + restApiMethod.getSimpleName());

                if (restApiMethod.getKind() == ElementKind.METHOD) {
                    //NCL-5221
                    //skip endpoint methods annotated with @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
                    Consumes consumesAnnotation = restApiMethod.getAnnotation(Consumes.class);
                    if (consumesAnnotation != null && restApiMethod.getAnnotation(PATCH.class) != null) {
                        List<String> mediaTypes = Arrays.asList(consumesAnnotation.value());
                        if (mediaTypes.stream().anyMatch(type -> type.equals(MediaType.APPLICATION_JSON_PATCH_JSON)))
                            continue;
                    }
                    String parametersList = getParameters(restApiMethod);
                    TypeMirror returnType = restApiMethod.getReturnType();
                    TypeMirror returnGeneric = null; //TODO check for null before adding a generic (currently no such case)
                    if (returnType instanceof DeclaredType) {
                        if (((DeclaredType)returnType).getTypeArguments().size() > 0) {
                            returnGeneric = ((DeclaredType)returnType).getTypeArguments().get(0);
                        }
                    }

                    //startsWith because off the generics
                    if (ClassName.get(returnType).toString().startsWith(ClassName.get("org.jboss.pnc.dto.response", "Page").toString())) {
                        //Page result

                        List<VariableElement> defaultParameters = new ArrayList<>();
                        List<String> endpointParameters = new ArrayList<>();
                        boolean hasPageParameters = false;

                        ClassName returnClass = ClassName.get("org.jboss.pnc.client", "RemoteCollection");

                        for (VariableElement parameter : restApiMethod.getParameters()) {
                            final String parameterName = ClassName.get(parameter.asType()).toString();
                            switch (parameterName) {
                                case "org.jboss.pnc.rest.api.parameters.PageParameters":
                                    hasPageParameters = true;
                                    endpointParameters.add("pageParameters");
                                    break;
                                case "org.jboss.pnc.rest.api.parameters.PaginationParameters":
                                    hasPageParameters = false;
                                    endpointParameters.add("pageParameters");
                                    break;
                                default:
                                    defaultParameters.add(parameter);
                                    endpointParameters.add(parameter.getSimpleName().toString());
                                    break;
                            }
                        }

                        MethodSpec.Builder defaultMethod = beginMethod(restApiMethod);
                        for (VariableElement parameter : defaultParameters) {
                            defaultMethod.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
                        }
                        String setSortAndQuery = "";
                        if (hasPageParameters) {
                            ParameterizedTypeName stringOptional = ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(String.class));
                            defaultMethod.addParameter(stringOptional, "sort");
                            defaultMethod.addParameter(stringOptional, "query");
                            setSortAndQuery = "setSortAndQuery(pageParameters, sort, query);";
                        }
                        String endpointInvokeParameters = endpointParameters.stream()
                                .collect(Collectors.joining(", "));

                        defaultMethod.returns(ParameterizedTypeName.get(returnClass, TypeName.get(returnGeneric)))
                                .addStatement("PageReader pageLoader = new PageReader<>((pageParameters) -> { " + setSortAndQuery + " return getEndpoint()." + restApiMethod.getSimpleName() + "(" + endpointInvokeParameters + ");}, getRemoteCollectionConfig())")
                                .addStatement("return pageLoader.getCollection()");
                        MethodSpec methodSpec = completeMethod(defaultMethod);
                        methods.add(methodSpec);

                        //without sort and query
                        if (hasPageParameters) {
                            MethodSpec.Builder simpleMethod = beginMethod(restApiMethod);
                            simpleMethod.returns(ParameterizedTypeName.get(returnClass, TypeName.get(returnGeneric)));
                            for (VariableElement parameter : defaultParameters) {
                                simpleMethod.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
                            }

                            List<String> defaultMethodParameters = defaultParameters.stream()
                                    .map(p -> p.getSimpleName().toString())
                                    .collect(Collectors.toList());
                            defaultMethodParameters.add("Optional.empty()");
                            defaultMethodParameters.add("Optional.empty()");

                            String defaultMethodParametersString = defaultMethodParameters.stream()
                                    .collect(Collectors.joining(", "));

                            simpleMethod.addStatement("return " + restApiMethod.getSimpleName() + "(" + defaultMethodParametersString + ")");
                            methods.add(completeMethod(simpleMethod));
                        }

                    } else if (ClassName.get(returnType).toString().startsWith(ClassName.get("org.jboss.pnc.dto.response", "Singleton").toString())) {
                        //single result
                        MethodSpec.Builder methodBuilder = beginMethod(restApiMethod);
                        addDefaultParameters(restApiMethod, methodBuilder);
                        if (restApiMethod.getAnnotation(GET.class) != null) {
                            methodBuilder
                                    .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(returnGeneric)))
                                    .addStatement("return Optional.of(getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ").getContent())") //TODO check for empty Singleton
                                    .nextControlFlow("catch ($T e)", NotFoundException.class)
                                    .addStatement("return Optional.empty()");
                        } else if (restApiMethod.getAnnotation(POST.class) != null) {
                            methodBuilder
                                    .returns(TypeName.get(returnGeneric))
                                    .addStatement("return getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ").getContent()");
                        }
                        MethodSpec methodSpec = completeMethod(methodBuilder);
                        methods.add(methodSpec);
                    } else if (ClassName.get(returnType).toString().equals("java.lang.String")) {
                        //string response
                        MethodSpec.Builder methodBuilder = beginMethod(restApiMethod);
                        addDefaultParameters(restApiMethod, methodBuilder);
                        methodBuilder
                                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(String.class)))
                                .addStatement("return Optional.ofNullable(getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + "))")
                                .nextControlFlow("catch ($T e)", NotFoundException.class)
                                .addStatement("return Optional.empty()");
                        MethodSpec methodSpec = completeMethod(methodBuilder);
                        methods.add(methodSpec);
                    } else if (ClassName.get(returnType).toString().equals("javax.ws.rs.core.StreamingOutput")) {
                        //string response
                        MethodSpec.Builder methodBuilder = beginMethod(restApiMethod);
                        addDefaultParameters(restApiMethod, methodBuilder);
                        methodBuilder
                                .returns(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(InputStream.class)))
                                .addStatement("return Optional.ofNullable(getInputStream(\"" + restApiMethod.getAnnotation(Path.class).value() + "\", " + parametersList + "))")
                                .nextControlFlow("catch ($T e)", NotFoundException.class)
                                .addStatement("return Optional.empty()");
                        MethodSpec methodSpec = completeMethod(methodBuilder);
                        methods.add(methodSpec);
                    } else if (ClassName.get(returnType).toString().equals("void")) {
                        //void response
                        MethodSpec.Builder methodBuilder = beginMethod(restApiMethod);
                        addDefaultParameters(restApiMethod, methodBuilder);
                        methodBuilder
                                .addException(ClassName.get("org.jboss.pnc.client", "RemoteResourceNotFoundException"))
                                .returns(TypeName.get(void.class))
                                .addStatement("getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ")")
                                .nextControlFlow("catch ($T e)", NotFoundException.class)
                                .addStatement("throw new RemoteResourceNotFoundException(e)");
                        MethodSpec methodSpec = completeMethod(methodBuilder);
                        methods.add(methodSpec);
                    } else {
                        //any other return types
                        MethodSpec.Builder methodBuilder = beginMethod(restApiMethod);
                        addDefaultParameters(restApiMethod, methodBuilder);
                        methodBuilder
                                .returns(TypeName.get(returnType))
                                .addStatement("return getEndpoint()." + restApiMethod.getSimpleName() + "(" + parametersList + ")")
                                .nextControlFlow("catch ($T e)", NotFoundException.class)
                                .addStatement("throw new RemoteResourceNotFoundException(e)");
                        MethodSpec methodSpec = completeMethod(methodBuilder);
                        methods.add(methodSpec);
                    }
                }
            }

            ClassName restInterfaceClassName = ClassName.get("org.jboss.pnc.rest.api.endpoints", restInterfaceName);

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get("org.jboss.pnc.client","Configuration"), "configuration")
                    .addStatement("super(configuration, " + restInterfaceName + ".class)")
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

    private MethodSpec completeMethod(MethodSpec.Builder methodBuilder) {
        return methodBuilder
                                .nextControlFlow("catch ($T e)", WebApplicationException.class)
                                .addStatement("throw new RemoteResourceException(readErrorResponse(e), e)")
                                .endControlFlow()
                                .build();
    }

    private MethodSpec.Builder beginMethod(ExecutableElement restApiMethod) {
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(restApiMethod.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .addException(ClassName.get("org.jboss.pnc.client", "RemoteResourceException"));
        methodBuilder.beginControlFlow("try");
        return methodBuilder;
    }

    private void addDefaultParameters(ExecutableElement restApiMethod, MethodSpec.Builder builder) {
        for (VariableElement parameter : restApiMethod.getParameters()) {
            builder.addParameter(TypeName.get(parameter.asType()), parameter.getSimpleName().toString());
        }
    }

    private String getParameters(ExecutableElement restApiMethod) {
        List<String> parameters = new ArrayList<>();
        for (VariableElement parameter : restApiMethod.getParameters()) {
            parameters.add(parameter.getSimpleName().toString());
        }
        return parameters.stream().collect(Collectors.joining(", "));
    }
}
