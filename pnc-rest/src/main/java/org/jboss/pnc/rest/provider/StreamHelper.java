package org.jboss.pnc.rest.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Stream;

public class StreamHelper {

    public static <T> Stream<T> nullableStreamOf(Collection<T> nullableCollection) {
        if(nullableCollection == null) {
            return Collections.<T>emptyList().stream();
        }
        return nullableCollection.stream();
    }

}
