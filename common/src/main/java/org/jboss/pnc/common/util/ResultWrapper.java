/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

/**
 * Simple wrapper class which contains a result and an exception. This can be useful for example when performing
 * asynchronous operations which do not immediately return, but could throw an exception.
 * 
 * @param <R> The result of the operation
 * @param <E> The exception (if any) thrown during the operation.
 */
public class ResultWrapper<R, E extends Exception> {

    private E exception;

    private R result;

    public ResultWrapper(R result) {
        this.result = result;
    }

    public ResultWrapper(R result, E exception) {
        this.result = result;
        this.exception = exception;
    }

    /** Returns null if no exception was thrown */
    /**
     * @return
     */
    public E getException() {
        return exception;
    }

    public R getResult() {
        return result;
    }

}