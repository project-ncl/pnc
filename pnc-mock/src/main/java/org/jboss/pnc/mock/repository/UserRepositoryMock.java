/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.User;
import org.jboss.pnc.spi.datastore.repositories.UserRepository;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 9/22/16 Time: 12:06 PM
 */
public class UserRepositoryMock extends IntIdRepositoryMock<User> implements UserRepository {
}
