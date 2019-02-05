/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.client;

import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultRemoteCollection<T> implements RemoteCollection<T> {

    private Function<PageParameters, Page<T>> endpoint;

    Page<T> currentPage;

    public DefaultRemoteCollection(Function<PageParameters, Page<T>> endpoint) {
        this.endpoint = endpoint;
        PageParameters intialPageParameters = new PageParameters();
        intialPageParameters.setPageIndex(0);
        intialPageParameters.setPageSize(100); //TODO configurable
        currentPage = endpoint.apply(intialPageParameters);
    }

    @Override
    public int size() {
        return currentPage.getTotalHits();
    }

    @Override
    public Iterator<T> iterator() {
        return new RemoteIterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        while (iterator().hasNext()) {
            action.accept(iterator().next());
        }
    }

    private Page<T> loadNextPage(Function<PageParameters, Page<T>> endpoint, Page<T> currentPage) {
        PageParameters pageParametersNext = new PageParameters();
        pageParametersNext.setPageSize(currentPage.getPageSize());
        pageParametersNext.setPageIndex(currentPage.getPageIndex() + 1);
        return endpoint.apply(pageParametersNext);
    }

    private class RemoteIterator implements Iterator<T> {

        @Override
        public boolean hasNext() {
            if (currentPage.getContent().iterator().hasNext()) {
                return true;
            } else if (currentPage.getPageIndex() < currentPage.getTotalPages()) {
                currentPage = loadNextPage(endpoint, currentPage);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public T next() {
            if (hasNext()) {
                return currentPage.getContent().iterator().next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
