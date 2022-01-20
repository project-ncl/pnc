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
package org.jboss.pnc.common.util;

import java.util.Collection;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamHelper {

    public static <T> Stream<T> nullableStreamOf(Collection<T> nullableCollection) {
        if (nullableCollection == null) {
            return Stream.empty();
        }
        return nullableCollection.stream();
    }

    public static <T> Stream<T> nullableStreamOf(Iterable<T> nullableIterable) {
        if (nullableIterable == null || !nullableIterable.iterator().hasNext()) {
            return Stream.empty();
        }
        return StreamSupport.stream(nullableIterable.spliterator(), false);
    }

}
