package org.jboss.pnc.common.util;


/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-09.
 */
public class BooleanWrapper {
    boolean b;

    public BooleanWrapper(boolean b) {
        this.b = b;
    }

    public void set(boolean b) {
        this.b = b;
    }

    public boolean get() {
        return b;
    }

}
