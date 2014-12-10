package org.jboss.pnc.core.builder.operationHandlers;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-10.
 */
public class ChainBuilder {

    private OperationHandler rootHandler;
    private OperationHandler last;

    public ChainBuilder addNext(OperationHandler nextHandler) {
        if (rootHandler == null) {
            last = rootHandler = nextHandler;
        } else {
            last.next(nextHandler);
            last = nextHandler;
        }
        return this;
    }

    public OperationHandler build() {
        return rootHandler;
    }
}
