package org.jboss.pnc.rest.provider;

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
