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
package org.jboss.pnc.facade.util.labels;

import java.util.EnumSet;

/**
 * The class implementing this interface is able to add (remove) new (old) label to (from) the set of active labels and
 * update the label history. Such a class complies with the rules of adding (removing) label for its entity type.
 */
public interface LabelModifier<L extends Enum<L>> {

    void validateAndAddLabel(L label, EnumSet<L> activeLabels);

    void validateAndRemoveLabel(L label, EnumSet<L> activeLabels);

    void addLabel(L label, EnumSet<L> activeLabels);

    void removeLabel(L label, EnumSet<L> activeLabels);
}
