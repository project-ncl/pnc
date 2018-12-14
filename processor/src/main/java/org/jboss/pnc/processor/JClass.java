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
import java.util.*;

/**
 * This class only works if we add elements in proper sequence.
 */

public class JClass {
    public static final String LINE_BREAK = System.getProperty("line.separator");
    private StringBuilder builder = new StringBuilder();
    private String className;
    private Map<String, String> fields = new LinkedHashMap<>();

    public JClass() {

    }


    public JClass definePackage(String packageName) {
        if (packageName != null) {
            builder.append("package ")
                    .append(packageName)
                    .append(";")
                    .append(LINE_BREAK);
        }
        return this;
    }

    public JClass addImport(String importPackage) {
        builder.append("import ")
                .append(importPackage)
                .append(";");
        return this;
    }

    public JClass defineClass(String startPart, String name, String extendPart) {
        className = name;
        builder.append(LINE_BREAK).append(LINE_BREAK)
                .append(startPart)
                .append(" ")
                .append(name);
        if (extendPart != null) {
            builder.append(" ")
                    .append(extendPart);
        }

        builder.append(" {")
                .append(LINE_BREAK);
        return this;
    }

    public JClass addFields(LinkedHashMap<String, String> identifierToTypeMap) {
        for (Map.Entry<String, String> entry : identifierToTypeMap.entrySet()) {
            addField(entry.getValue(), entry.getKey());
        }
        return this;
    }

    public JClass addField(String type, String identifier) {
        fields.put(identifier, type);
        builder.append("private ")
                .append(type)
                .append(" ")
                .append(identifier)
                .append(";")
                .append(LINE_BREAK);

        return this;
    }


    public JClass addConstructor(String accessModifier, List<String> fieldsToBind) {
        builder.append(LINE_BREAK)
                .append(accessModifier)
                .append(" ")
                .append(className)
                .append("(");

        for (int i = 0; i < fieldsToBind.size(); i++) {
            if (i > 0) {
                builder.append(", ");
            }
            String name = fieldsToBind.get(i);
            builder.append(fields.get(name))
                    .append(" ")
                    .append(name);
        }
        builder.append(") {");
        for (int i = 0; i < fieldsToBind.size(); i++) {
            builder.append(LINE_BREAK);

            String name = fieldsToBind.get(i);
            builder.append("this.")
                    .append(name)
                    .append(" = ")
                    .append(name)
                    .append(";");
        }
        builder.append(LINE_BREAK);
        builder.append("}");
        builder.append(LINE_BREAK);

        return this;

    }

    public JClass addConstructor(String accessModifier, boolean bindFields) {
        addConstructor(accessModifier,
                bindFields ? new ArrayList(fields.keySet()) :
                        new ArrayList<>());
        return this;
    }

    public JClass addMethod(JMethod method) {
        builder.append(LINE_BREAK)
                .append(method.end())
                .append(LINE_BREAK);
        return this;
    }

    public JClass addNestedClass(JClass jClass) {
        builder.append(LINE_BREAK);
        builder.append(jClass.end());
        builder.append(LINE_BREAK);
        return this;
    }

    public JClass createSetterForField(String name) {
        if (!fields.containsKey(name)) {
            throw new IllegalArgumentException("Field not found for setter: " + name);
        }
        addMethod(new JMethod()
                .defineSignature("public", false, "void")
                .name("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1))
                .defineBody(" this." + name + " = " + name + ";"));
        return this;
    }

    public JClass createGetterForField(String name) {
        if (!fields.containsKey(name)) {
            throw new IllegalArgumentException("Field not found for Getter: " + name);
        }
        addMethod(new JMethod()
                .defineSignature("public", false, fields.get(name))
                .name("get" + Character.toUpperCase(name.charAt(0)) + name.substring(1))
                .defineBody(" return this." + name + ";"));
        return this;
    }

    public String end() {
        builder.append(LINE_BREAK + "}");
        return builder.toString();

    }
}