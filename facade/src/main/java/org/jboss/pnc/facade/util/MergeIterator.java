/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator that merges two sorted iterators.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class MergeIterator<T> implements Iterator<T> {

    private final Iterator<T> itA;
    private final Iterator<T> itB;
    private final Comparator<T> comparator;
    private T objectA;
    private T objectB;

    public MergeIterator(Iterator<T> a, Iterator<T> b, Comparator<T> comparator) {
        this.itA = a;
        this.itB = b;
        this.comparator = comparator;
        if (itA.hasNext()) {
            objectA = itA.next();
        }
        if (itB.hasNext()) {
            objectB = itB.next();
        }
    }

    @Override
    public boolean hasNext() {
        return objectA != null || objectB != null || itA.hasNext() || itB.hasNext();
    }

    @Override
    public T next() {
        T object;
        if (objectA != null && objectB != null) {
            if (comparator.compare(objectA, objectB) < 0) {
                object = objectA;
                objectA = provideNext(itA);
            } else {
                object = objectB;
                objectB = provideNext(itB);
            }
        } else if (objectA != null) {
            object = objectA;
            objectA = provideNext(itA);
        } else if (objectB != null) {
            object = objectB;
            objectB = provideNext(itB);
        } else {
            throw new NoSuchElementException();
        }
        return object;
    }

    private T provideNext(Iterator<T> it) {
        if (it.hasNext()) {
            return it.next();
        } else {
            return null;
        }
    }

}
