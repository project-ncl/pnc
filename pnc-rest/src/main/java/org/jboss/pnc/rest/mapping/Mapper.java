package org.jboss.pnc.rest.mapping;

import com.google.common.base.Preconditions;
import org.dozer.DozerBeanMapper;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;

@ApplicationScoped
public class Mapper {

    private org.dozer.Mapper mapper = new DozerBeanMapper(Arrays.asList("mapping.xml"));

    public <T> T mapTo(T sourceObject, T destinationObject) {
        Preconditions.checkArgument(sourceObject != null, "Source Object can't be null");
        Preconditions.checkArgument(destinationObject != null, "Destination Object can't be null");
        mapper.map(sourceObject, destinationObject);
        return destinationObject;
    }

    public <T> T mapTo(Object objectToBeMapped, Class<T> destinationClass) {
        if(objectToBeMapped == null) {
            return null;
        }
        return mapper.map(objectToBeMapped, destinationClass);
    }
}
