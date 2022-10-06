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
package org.jboss.pnc.coordinator.builder;

import org.slf4j.MDC;

import java.util.Map;
import java.util.Objects;

class MDCAwareElement<E> {
    private final E element;
    private final Map<String, String> contextMap;

    public MDCAwareElement(E element) {
        contextMap = MDC.getCopyOfContextMap();
        this.element = element;
    }

    public E get() {
        return element;
    }

    public Map<String, String> getContextMap() {
        return contextMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MDCAwareElement<?> that = (MDCAwareElement<?>) o;
        return element.equals(that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(element);
    }

    @Override
    public String toString() {
        return "Element:" + element + "; contextMap:" + contextMap;
    }
}
