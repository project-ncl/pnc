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
package org.jboss.pnc.client;

import java.util.Optional;
import org.jboss.pnc.dto.response.ErrorResponse;

import javax.ws.rs.WebApplicationException;

import lombok.Getter;

/**
 * Client exception, which indicates a failure of the request to the server.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @author Jakub Bartecek
 */
public class RemoteResourceException extends ClientException {

    @Getter
    private final int status;

    private final ErrorResponse response;

    public RemoteResourceException(Throwable cause) {
        super(cause);
        this.status = -1;
        this.response = null;
    }

    public RemoteResourceException(WebApplicationException cause) {
        super(cause);
        this.status = cause.getResponse().getStatus();
        this.response = null;
    }

    public RemoteResourceException(ErrorResponse response, WebApplicationException cause) {
        super(response == null ? cause.getMessage() : response.getErrorMessage(), cause);
        this.status = cause.getResponse().getStatus();
        this.response = response;
    }

    public RemoteResourceException(String message, int status) {
        super(message + " status: " + status);
        this.status = status;
        this.response = null;
    }

    public Optional<ErrorResponse> getResponse() {
        return Optional.ofNullable(response);
    }

}
