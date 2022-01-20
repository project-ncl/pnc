/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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