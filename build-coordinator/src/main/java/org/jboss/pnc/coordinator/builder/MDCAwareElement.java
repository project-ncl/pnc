/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.coordinator.builder;

import java.util.Map;
import java.util.Objects;

import org.slf4j.MDC;

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
