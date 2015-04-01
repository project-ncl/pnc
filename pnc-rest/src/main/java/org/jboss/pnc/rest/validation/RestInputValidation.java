package org.jboss.pnc.rest.validation;

import java.io.Serializable;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.pnc.common.Identifiable;

public class RestInputValidation {

    public static Response badRequest = Response.status(Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).build();

    public static void validateIdIsNull(Identifiable<? extends Serializable> identObj) throws BadRequestException {
        if (identObj.getId() != null) {
            throw new BadRequestException("Invalid request: id must be null for this operation");
            
        }
    }
    
}
