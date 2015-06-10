package org.jboss.pnc.datastore.predicates.rsql;

/**
 * Thrown when for some reason a field can not be selected or converted (for example RSQL uses some fields which do not exist)
 */
public class RSQLConverterException extends RuntimeException {

    public RSQLConverterException(String message, Exception e) {
        super(message, e);
    }
}
