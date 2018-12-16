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

import org.jboss.pnc.dto.DTOEntity;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.rest.api.endpoints.BaseEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.ClientErrorException;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class ClientBase<T extends DTOEntity> {

    protected final String BASE_PATH = "/pnc-rest/rest";

    protected final ResteasyClient client;

    protected final ResteasyWebTarget target;

    protected ClientBase(ConnectionInfo connectionInfo) {
        client = new ResteasyClientBuilder().build();
        target = client.target(connectionInfo.getProtocol() + "://" + connectionInfo.getHost() + ":" + connectionInfo.getPort() + BASE_PATH);
    }

    protected abstract BaseEndpoint<T> getEndpoint();

    public Optional<T> getSpecific(String id) throws RemoteResourseReadException {
        try {
            return Optional.ofNullable(getEndpoint().getSpecific(id));
        } catch (ClientErrorException e) {
            throw new RemoteResourseReadException("Cannot get remote resource.", e);
        }
    }

    public Page<T> getAll(PageParameters parametrs) throws RemoteResourseReadException {
        try {
            return getEndpoint().getAll(parametrs);
        } catch (ClientErrorException e) {
            throw new RemoteResourseReadException("Cannot get remote resource.", e);
        }
    }

    public T createNew(T object) throws RemoteResourceCreateException {
        try {
            return getEndpoint().createNew(object);
        } catch (ClientErrorException e) {
            throw new RemoteResourceCreateException("Cannot create remote resource.", e);
        }
    }

    public T update(T object) throws RemoteResourceUpdateException, RemoteResourceNotFoundException {
        try {
            T updated = getEndpoint().update(object.getId(), object);
            if (updated == null) {
                throw new RemoteResourceNotFoundException("Could not found remote resource.");
            }
            return updated;
        } catch (ClientErrorException e) {
            throw new RemoteResourceUpdateException("Cannot updated remote resource.", e);
        }
    }

    public T delete(String id) throws RemoteResourceUpdateException, RemoteResourceNotFoundException {
        try {
            T updated = getEndpoint().deleteSpecific(id);
            if (updated == null) {
                throw new RemoteResourceNotFoundException("Could not found remote resource.");
            }
            return updated;
        } catch (ClientErrorException e) {
            throw new RemoteResourceUpdateException("Cannot updated remote resource.", e);
        }
    }
}
