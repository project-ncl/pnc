package org.jboss.pnc.rest.mapping;

import org.dozer.DozerBeanMapper;

import javax.enterprise.context.ApplicationScoped;
import java.util.Arrays;

@ApplicationScoped
public class Mapper {

    private org.dozer.Mapper mapper = new DozerBeanMapper(Arrays.asList("mapping.xml"));

    public <T> T mapTo(Object objectToBeMapped, Class<T> destinationClass) {
        if(objectToBeMapped == null) {
            return null;
        }
        return mapper.map(objectToBeMapped, destinationClass);
    }
}
