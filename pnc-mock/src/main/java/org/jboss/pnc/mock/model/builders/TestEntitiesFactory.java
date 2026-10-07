/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.model.builders;

import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.User;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class TestEntitiesFactory {
    public static User newUser() {
        return User.Builder.newBuilder().id(1).username("medusa").firstName("Medusa").lastName("Poseidon's").build();
    }

    public static BuildConfigurationSet newBuildConfigurationSet() {
        return BuildConfigurationSet.Builder.newBuilder().id(1).name("test-build-configuration-set-1").build();
    }

}
