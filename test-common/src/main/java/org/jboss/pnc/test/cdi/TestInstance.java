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
package org.jboss.pnc.test.cdi;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class TestInstance<T> implements Instance<T> {

    Collection<T> objects;

    public TestInstance(T... objects) {
        this.objects = Arrays.asList(objects);
    }

    public TestInstance(Collection<T> objects) {
        this.objects = new ArrayList<>(objects);
    }

    @Override
    public Instance<T> select(Annotation... qualifiers) {
        List<T> filteredObjects = objects.stream().filter(object -> {
            for (Annotation a : qualifiers) {
                if (object.getClass().isAnnotationPresent(a.annotationType())) {
                    return true;
                }
            }
            return false;
        }).collect(Collectors.toList());
        return new TestInstance<>(filteredObjects);
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> subtype, Annotation... qualifiers) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> subtype, Annotation... qualifiers) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public boolean isUnsatisfied() {
        return objects.isEmpty();
    }

    @Override
    public boolean isAmbiguous() {
        return objects.size() > 1;
    }

    @Override
    public void destroy(T instance) {
    }

    @Override
    public Iterator<T> iterator() {
        return objects.iterator();
    }

    @Override
    public T get() {
        return objects.iterator().next();
    }
}
