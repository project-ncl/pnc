/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import javax.ws.rs.ClientErrorException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RemoteResourceNotFoundException extends RemoteResourceException {

    public RemoteResourceNotFoundException(ClientErrorException e) {
        super(e);
    }
}
