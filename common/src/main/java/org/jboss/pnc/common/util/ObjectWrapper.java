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

}
