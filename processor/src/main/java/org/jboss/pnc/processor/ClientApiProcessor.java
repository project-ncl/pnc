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

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.jboss.pnc.processor.annotation.ClientApi;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.ws.rs.GET;
import java.io.IOException;
import java.util.Set;

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

//        CtClass ctClass = new CtPrimitiveType();

        for (Element element : restApiInterfaces) {
            for (ExecutableElement restApiMethod : ElementFilter.methodsIn(element.getEnclosedElements())) {
                if (restApiMethod.getAnnotation(GET.class) != null) {
                    MethodSpec main = MethodSpec.methodBuilder(restApiMethod.getSimpleName().toString())
                            .addModifiers(Modifier.PUBLIC)
    //                        .returns(restApiMethod.getReturnType())
                            .addParameter(String[].class, "args")
                            .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                            .build();


                }

            /*    if (restApiMethod.getKind() == ElementKind.METHOD) {
                    TypeMirror returnType = restApiMethod.getReturnType();


                    MethodSpec.Builder builder = MethodSpec.methodBuilder(restApiMethod.getSimpleName().toString())
                            //                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                            .returns(Response.class);


                    for (TypeParameterElement typeParameter : restApiMethod.getTypeParameters()) {
                        typeParameter.
                    }
                    for (VariableElement parameter : restApiMethod.getParameters()) {
                        DeclaredType type = (DeclaredType)parameter.asType();
                        TypeVisitor<?, ?> visitor = new TypeVisitor<Object, Object>() {

                            @Override
                            public Object visit(TypeMirror t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visit(TypeMirror t) {
                                return null;
                            }

                            @Override
                            public Object visitPrimitive(PrimitiveType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitNull(NullType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitArray(ArrayType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitDeclared(DeclaredType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitError(ErrorType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitTypeVariable(TypeVariable t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitWildcard(WildcardType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitExecutable(ExecutableType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitNoType(NoType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitUnknown(TypeMirror t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitUnion(UnionType t, Object o) {
                                return null;
                            }

                            @Override
                            public Object visitIntersection(IntersectionType t, Object o) {
                                return null;
                            }
                        };
                        type.accept(visitor);

                    }

//                            .addParameter(String[].class, "args")
//                            .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
//                            .build();
                }*/
            }
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
