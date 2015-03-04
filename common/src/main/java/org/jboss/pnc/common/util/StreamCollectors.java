package org.jboss.pnc.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collector;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2015-02-17.
 */
public class StreamCollectors {

    public static <T> Collector<T, List<T>, T> singletonCollector() {
        return Collector.of(
                ArrayList::new,
                List::add,
                (left, right) -> {
                    left.addAll(right);
                    return left;
                },
                list -> {
                    if (list.size() == 0) {
                        return null;
                    }
                    if (list.size() > 1) {
                        throw new IllegalStateException();
                    }
                    return list.get(0);
                }
        );
    }
}
