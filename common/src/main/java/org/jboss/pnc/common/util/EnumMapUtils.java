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

import java.util.EnumMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
public class EnumMapUtils {

    /**
     * Function creates {@code EnumMap} and initializes it with entries ({@code k}, {@code v}), where {@code k} acquires
     * all enum constants from enum {@code K} and {@code v} is the default value provided by the supplier.
     *
     * @param keyType class of which are all keys in the {@code EnumMap}
     * @param defaultValueSupplier supplies default value for every key
     * @return {@code EnumMap} as described above
     * @param <K> type of keys in the resulting {@code EnumMap}
     * @param <V> type of values in the resulting {@code EnumMap}
     */
    public static <K extends Enum<K>, V> EnumMap<K, V> initEnumMapWithDefaultValue(
            Class<K> keyType,
            Supplier<V> defaultValueSupplier) {
        EnumMap<K, V> enumMap = new EnumMap<>(keyType);
        Stream.of(keyType.getEnumConstants()).forEach(enumConst -> enumMap.put(enumConst, defaultValueSupplier.get()));
        return enumMap;
    }
}
