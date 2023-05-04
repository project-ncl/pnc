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
package org.jboss.pnc.facade.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * DISCLAIMER: DO NOT use this Query builder unless you have NO OTHER CHOICE. By using this class, you are highly
 * susceptible to typos and mistakes as input IS NOT validated. Use TYPED Queries JPQL, HQL or Criteria API.
 *
 * @author Jan Michalov
 */
public class NativeQueryBuilder {

    private final List<String> selects;
    private final List<String> froms;
    private final List<String> joins;
    private final List<String> wheres;
    private final List<String> orderBys;
    private Integer limit;
    private Integer offset;

    private NativeQueryBuilder() {
        selects = new ArrayList<>();
        froms = new ArrayList<>();
        joins = new ArrayList<>();
        wheres = new ArrayList<>();
        orderBys = new ArrayList<>();
    }

    public NativeQueryBuilder select(String field) {
        selects.add(field);
        return this;
    }

    public NativeQueryBuilder select(String table, String field) {
        selects.add(table(table) + field);
        return this;
    }

    public NativeQueryBuilder select(String table, String field, String alias) {
        selects.add(table(table) + field + alias(alias));
        return this;
    }

    public NativeQueryBuilder requiresSelect(String table, String field, String alias) {
        String selectClause = table(table) + field + alias(alias);
        if (!selects.contains(selectClause))
            selects.add(selectClause);
        return this;
    }

    private static String table(String table) {
        return table == null || table.isEmpty() ? "" : table + '.';
    }

    private static String alias(String alias) {
        return alias == null || alias.isEmpty() ? "" : " AS " + alias;
    }

    public NativeQueryBuilder from(String table) {
        froms.add(table);
        return this;
    }

    public NativeQueryBuilder from(String table, String alias) {
        froms.add(table + alias(alias));
        return this;
    }

    public NativeQueryBuilder join(String joinType, String table, String alias, String onClause) {
        joins.add(joinType + " JOIN " + table + alias(alias) + on(onClause));
        return this;
    }

    private static String on(String onClause) {
        return onClause == null || onClause.isBlank() ? "" : " ON " + onClause;
    }

    public NativeQueryBuilder requiresJoin(String joinType, String table, String alias, String onClause) {
        String joinClause = joinType + " JOIN " + table + alias(alias) + on(onClause);

        if (!joins.contains(joinClause))
            joins.add(joinClause);

        return this;
    }

    public NativeQueryBuilder where(String condition) {
        wheres.add(condition);
        return this;
    }

    public NativeQueryBuilder orderBy(String... orderFields) {
        orderBys.addAll(Arrays.asList(orderFields));
        return this;
    }

    public NativeQueryBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public NativeQueryBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    public static NativeQueryBuilder builder() {
        return new NativeQueryBuilder();
    }

    public String build() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder queryString = new StringBuilder();

        if (!selects.isEmpty()) {
            queryString.append("SELECT ").append(String.join(", ", selects));
        }
        if (!froms.isEmpty()) {
            queryString.append(" FROM ").append(String.join(", ", froms));
        }
        if (!joins.isEmpty()) {
            queryString.append(" ").append(String.join(" ", joins));
        }
        if (!wheres.isEmpty()) {
            queryString.append(" WHERE ").append(String.join(" AND ", wheres));
        }
        if (!orderBys.isEmpty()) {
            queryString.append(" ORDER BY ").append(String.join(", ", orderBys));
        }
        if (limit != null) {
            queryString.append(" LIMIT ").append(limit);
        }
        if (offset != null) {
            queryString.append(" OFFSET ").append(offset);
        }

        return queryString.toString();
    }
}
