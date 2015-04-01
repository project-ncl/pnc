package org.jboss.pnc.rest.validation;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Exception which occurs when an invalid REST request is received.
 *
 */
public class BadRequestException extends WebApplicationException {

    private static final long serialVersionUID = 1L;

    private ErrorMessage errorMessage;

    public BadRequestException(String msg) {
        errorMessage = new ErrorMessage(msg);
    }

    public Response getResponse() {
        return Response.status(Status.BAD_REQUEST).entity(errorMessage)
                .type(MediaType.APPLICATION_JSON).build();
    }
}