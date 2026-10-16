/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
}
