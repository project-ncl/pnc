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
public class JMethod {
    private StringBuilder builder = new StringBuilder();
    private boolean firstParam = true;
    private boolean forInterface;

    public JMethod forInterface() {
        forInterface = true;
        return this;
    }

    public JMethod defineSignature(String accessModifier, boolean asStatic, String returnType) {
        builder.append(forInterface ? "" : accessModifier)
                .append(asStatic? " static ": " ")
                .append(returnType)
                .append(" ");
        return this;
    }

    public JMethod name(String name) {
        builder.append(name)
                .append("(");
        return this;
    }

    public JMethod addParam(String type, String identifier) {
        if (!firstParam) {
            builder.append(", ");
        } else {
            firstParam = false;
        }
        builder.append(type)
                .append(" ")
                .append(identifier);

        return this;
    }

    public JMethod defineBody(String body) {
        if (forInterface) {
            throw new IllegalArgumentException("Interface cannot define a body");
        }
        builder.append(") {")
                .append(JClass.LINE_BREAK)
                .append(body)
                .append(JClass.LINE_BREAK)
                .append("}")
                .append(JClass.LINE_BREAK);
        return this;
    }

    public String end() {
        return forInterface ? ");" : builder.toString();
    }
}