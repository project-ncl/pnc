package org.jboss.pnc.rest.provider;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamHelper {

    public static <T> Stream<T> nullableStreamOf(Collection<T> nullableCollection) {
        if(nullableCollection == null) {
            return Stream.empty();
        }
        return nullableCollection.stream();
    }

    public static <T> Stream<T> nullableStreamOf(Iterable<T> nullableIterable) {
        if(nullableIterable == null || !nullableIterable.iterator().hasNext()) {
            return Stream.empty();
        }
        return StreamSupport.stream(nullableIterable.spliterator(), false);
    }

}
