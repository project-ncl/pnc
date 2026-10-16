/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-09.
 */
public class ObjectWrapper<T> {
    private T obj;

    public ObjectWrapper() {
    }

    public ObjectWrapper(T obj) {
        this.obj = obj;
    }

    public void set(T obj) {
        this.obj = obj;
    }

    public T get() {
        return obj;
    }

    public Boolean isSet() {
        return obj != null;
    }
}
