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
package org.jboss.pnc.spi.datastore.repositories.api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

public interface SortInfo {
    enum SortingDirection {
        ASC, DESC
    }

    List<String> getFields();

    SortingDirection getDirection();

    default <T> Comparator<T> getComparator() {
        return (x, y) -> {
            Comparator<T> combinedComparator = (instance1, instance2) -> 0;
            try {
                for (String field : getFields()) {
                    String getterName = "get" + Character.toString(field.charAt(0)).toUpperCase() + field.substring(1);
                    Method toCompare = x.getClass().getDeclaredMethod(getterName, null);
                    Object v1 = toCompare.invoke(x, null);
                    Object v2 = toCompare.invoke(y, null);
                    if (v1 instanceof Comparable<?> && v2 instanceof Comparable<?>) {
                        Comparable c1 = (Comparable) v1;
                        Comparable c2 = (Comparable) v2;
                        if (getDirection() == SortingDirection.ASC) {
                            combinedComparator = combinedComparator.thenComparing((o1, o2) -> c1.compareTo(c2));
                        } else {
                            combinedComparator = combinedComparator.thenComparing((o1, o2) -> c2.compareTo(c1));
                        }
                    }
                }
            } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                throw new IllegalArgumentException("Field is not accessible via reflection", e);
            }

            return combinedComparator.compare(x, y);
        };
    }
}
