/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface RemoteCollection<T> extends Iterable<T> {
    int size();

    /**
     * Reads all pages and returns all elements.
     */
    Collection<T> getAll();

    static <T> RemoteCollection<T> empty() {
        return new RemoteCollection() {
            @Override
            public int size() {
                return 0;
            }

            @Override
            public Collection<T> getAll() {
                return Collections.emptyList();
            }

            @Override
            public Iterator<T> iterator() {
                return new Iterator() {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }

                    @Override
                    public T next() {
                        throw new NoSuchElementException();
                    }
                };
            }
        };
    }
}
