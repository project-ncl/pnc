package org.jboss.pnc.rest.notifications.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.rest.notifications.OutputConverter;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class JSonOutputConverter implements OutputConverter {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String apply(Object objectToBeConverted) {
        if(objectToBeConverted != null) {
            try {
                return mapper.writeValueAsString(objectToBeConverted);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Could not convert object to JSON", e);
            }
        }
        return "{}";
    }
}
