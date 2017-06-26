/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

package org.jboss.pnc.rest.endpoint;

import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.rest.provider.AbstractProvider;
import org.jboss.pnc.rest.provider.collection.CollectionInfo;
import org.jboss.pnc.rest.restmodel.GenericRestEntity;
import org.jboss.pnc.rest.restmodel.response.Page;
import org.jboss.pnc.rest.restmodel.response.Singleton;
import org.jboss.pnc.rest.validation.exceptions.ValidationException;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 * Abstract endpoint class providing common functionality
 *
 * @author Sebastian Laskawiec
 */
public class AbstractEndpoint<DBEntity extends GenericEntity<Integer>, RESTEntity extends GenericRestEntity<Integer>> {

    protected AbstractProvider<DBEntity, RESTEntity> basicProvider;

    @Deprecated
    public AbstractEndpoint() {
    }

    public AbstractEndpoint(AbstractProvider<DBEntity, RESTEntity> basicProvider) {
        this.basicProvider = basicProvider;
    }

    public Response getAll(int pageIndex, int pageSize, String sortingRsql, String rsql) {
        return fromCollection(basicProvider.getAll(pageIndex, pageSize, sortingRsql, rsql));
    }

    public Response getSpecific(@NotNull Integer id) {
        return fromSingleton(basicProvider.getSpecific(id));
    }

    public Response createNew(RESTEntity restEntity, UriInfo uriInfo) throws ValidationException {
        int id = basicProvider.store(restEntity);
        UriBuilder uriBuilder = UriBuilder.fromUri(uriInfo.getRequestUri()).path("{id}");
        return Response.created(uriBuilder.build(id)).entity(new Singleton(basicProvider.getSpecific(id))).build();
    }

    public Response update(Integer id, RESTEntity restEntity) throws ValidationException {
        basicProvider.update(id, restEntity);
        return Response.ok().build();
    }

    protected Response delete(Integer id) throws ValidationException {
        basicProvider.delete(id);
        return Response.ok().build();
    }

    protected <T> Response fromCollection(CollectionInfo<T> collection) {
        Page<T> pageForResponse = new Page<>(collection);
        if(collection == null || collection.getContent().size() == 0) {
            return Response.noContent().entity(pageForResponse).build();
        }
        return Response.ok().entity(pageForResponse).build();
    }

    protected <T> Response fromSingleton(T singleton) {
        if(singleton == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(new Singleton(null)).build();
        }
        return Response.ok().entity(new Singleton(singleton)).build();
    }

    protected Response fromEmpty() {
        return Response.ok().build();
    }

}
