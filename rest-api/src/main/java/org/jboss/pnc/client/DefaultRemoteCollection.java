/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class DefaultRemoteCollection<T> implements RemoteCollection<T> {

    private final Logger logger = LoggerFactory.getLogger(DefaultRemoteCollection.class);

    private Function<PageParameters, Page<T>> endpoint;

    private RemoteCollectionConfig config;

    protected Page<T> currentPage;

    public DefaultRemoteCollection(Function<PageParameters, Page<T>> endpoint, RemoteCollectionConfig config) {
        this.endpoint = endpoint;
        this.config = config;
        PageParameters intialPageParameters = new PageParameters();
        intialPageParameters.setPageIndex(0);
        intialPageParameters.setPageSize(config.getPageSize());
        logger.debug("Loading first page.");
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
    public Collection<T> getAll() {
        List<T> list = new ArrayList<>();
        forEach(list::add);
        return list;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        final Iterator<T> iterator = iterator();
        while (iterator.hasNext()) {
            action.accept(iterator.next());
        }
    }

    private Page<T> loadNextPage(Function<PageParameters, Page<T>> endpoint, Page<T> currentPage) {
        int newPageIndex = currentPage.getPageIndex() + 1;
        logger.debug("Loading new page. Index {}", newPageIndex);
        PageParameters pageParametersNext = new PageParameters();
        pageParametersNext.setPageSize(currentPage.getPageSize());
        pageParametersNext.setPageIndex(newPageIndex);
        return endpoint.apply(pageParametersNext);
    }

    private class RemoteIterator implements Iterator<T> {

        private Iterator<T> iterator;

        public RemoteIterator() {
            this.iterator = currentPage.getContent().iterator();
        }

        @Override
        public boolean hasNext() {
            if (iterator.hasNext()) {
                return true;
            } else if (currentPage.getPageIndex() < currentPage.getTotalPages() - 1) {
                currentPage = loadNextPage(endpoint, currentPage);
                iterator = currentPage.getContent().iterator();
                return iterator.hasNext();
            } else {
                return false;
            }
        }

        @Override
        public T next() {
            if (hasNext()) {
                return iterator.next();
            } else {
                throw new NoSuchElementException();
            }
        }
    }
}
