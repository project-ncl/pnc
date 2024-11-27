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
package org.jboss.pnc.model.utils;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.boot.spi.MetadataBuilderImplementor;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.engine.config.spi.ConfigurationService;
import org.hibernate.type.StandardBasicTypes;

/**
 * Extends functions provided by Hibernate.
 *
 * @author Patrik Korytár &lt;pkorytar@redhat.com&gt;
 */
public class CustomSqlFunctionImplementor implements MetadataBuilderContributor {
    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        String dialect = (String) metadataBuilder.unwrap(MetadataBuilderImplementor.class)
                .getMetadataBuildingOptions()
                .getServiceRegistry()
                .getService(ConfigurationService.class)
                .getSettings()
                .get("hibernate.dialect");

        if (dialect == null) {
            throw new IllegalStateException("Dialect is not set");
        }

        switch (dialect) {
            case "org.hibernate.dialect.HSQLDialect":
                metadataBuilder.applySqlFunction(
                        "string_agg",
                        new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
                break;
            case "org.hibernate.dialect.PostgreSQL95Dialect":
                metadataBuilder.applySqlFunction(
                        "string_agg",
                        new StandardSQLFunction("string_agg", StandardBasicTypes.STRING));
                break;
            default:
                throw new IllegalStateException("Unsupported dialect " + dialect);
        }

        // adding parametrized expression to both select and group by clauses caused problems with binding the parameter
        // hence the function with hardcoded redhat string
        metadataBuilder.applySqlFunction(
                "redhat-locate",
                new SQLFunctionTemplate(StandardBasicTypes.INTEGER, "LOCATE('.redhat-', ?1)"));
    }
}