package org.jboss.pnc.rest.utils;

public class Utility {

    @FunctionalInterface
    public interface Action {
        void doIt();
    }

    public static void performIfNotNull(boolean expression, Action action) {
        if(expression) {
            action.doIt();
        }
    }

}
