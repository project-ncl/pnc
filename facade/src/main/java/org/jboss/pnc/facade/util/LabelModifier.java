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
package org.jboss.pnc.facade.util;

import org.jboss.pnc.api.enums.LabelOperation;
import org.jboss.pnc.facade.validation.InvalidLabelOperationException;

import java.util.EnumSet;

/**
 * The class implementing this interface is able to add (remove) new (old) label to (from) the set of active labels.
 * Such a class complies with the rules of adding (removing) label for the given entity type E.
 *
 * @param <E> entity
 */
public interface LabelModifier<E extends Enum<E>> {

    void addLabel(E label, EnumSet<E> labels) throws InvalidLabelOperationException;

    void removeLabel(E label, EnumSet<E> labels) throws InvalidLabelOperationException;

    default void checkLabelIsNotPresent(E label, EnumSet<E> labels) {
        if (labels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    labels,
                    LabelOperation.ADDED,
                    "label already present in the set of active labels");
        }
    }

    default void checkLabelIsPresent(E label, EnumSet<E> labels) {
        if (!labels.contains(label)) {
            throw new InvalidLabelOperationException(
                    label,
                    labels,
                    LabelOperation.REMOVED,
                    "no such label present in the set of active labels");
        }
    }
}
