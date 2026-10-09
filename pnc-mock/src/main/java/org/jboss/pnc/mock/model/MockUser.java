/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.model;

import org.jboss.pnc.model.User;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class MockUser {

    public static User newTestUser(Integer id) {
        User user = User.Builder.newBuilder().id(id).firstName("Poseidon").lastName("Neptune").build();
        return user;
    }

}
