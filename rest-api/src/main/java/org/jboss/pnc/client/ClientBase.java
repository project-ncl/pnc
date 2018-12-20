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
import org.jboss.pnc.dto.response.Singleton;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import javax.ws.rs.core.Response;

import java.util.Arrays;

import static java.net.HttpURLConnection.HTTP_NO_CONTENT;
import static java.net.HttpURLConnection.HTTP_OK;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public abstract class ClientBase {

    protected final String BASE_PATH = "/pnc-rest/rest";

    protected final ResteasyClient client;

    protected final ResteasyWebTarget target;

    protected ClientBase(ConnectionInfo connectionInfo) {
        client = new ResteasyClientBuilder().build();
        target = client.target(connectionInfo.getProtocol() + "://" + connectionInfo.getHost() + ":" + connectionInfo.getPort() + BASE_PATH);
    }

    protected Page readPageResponse(Response response) throws RemoteResourceException {
        int status = response.getStatus();
        if (status == HTTP_OK || status == HTTP_NO_CONTENT) {
            Page page = response.readEntity(Page.class);
            return page;
        } else {
            throw new RemoteResourceException("Received invalid response status.", status);
        }
    }

    protected <T> T readSingletonResponse(Response response, Integer... expectedStatus) throws RemoteResourceException {
        int status = response.getStatus();
        if (Arrays.asList(expectedStatus).contains(status)) {
            Singleton<T> singleton = response.readEntity(Singleton.class);
            return singleton.getContent();
        } else {
            throw new RemoteResourceException("Received invalid response status.", status);
        }
    }

    protected void validateUpdate(Response response) throws RemoteResourceNotFoundException, RemoteResourceException {
        int status = response.getStatus();
        if (status == HTTP_NO_CONTENT) {
            throw new RemoteResourceNotFoundException("No entity found.");
        }
        if (status != HTTP_OK) {
            throw new RemoteResourceException("Error updating remote resource.", status);
        }
    }

    protected void validateDelete(Response response) throws RemoteResourceException {
        int status = response.getStatus();
        if (status != HTTP_OK) {
            throw new RemoteResourceException("Error deleting remote resource.", status);
        }
    }


    /*
  public Page<Project> getAll(PageParameters pageParameters) throws RemoteResourceException {
    Response response = null;
    try {
      response = getEndpoint().getAll(pageParameters);
      return readPageResponse(response);
    } catch (ClientErrorException e) {
      throw new RemoteResourceException(e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  public Project createNew(Project project) throws RemoteResourceException {
    Response response = null;
    try {
      response = getEndpoint().createNew(project);
      return readSingletonResponse(response);
    } catch (ClientErrorException e) {
      throw new RemoteResourceException(e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

     */
}
