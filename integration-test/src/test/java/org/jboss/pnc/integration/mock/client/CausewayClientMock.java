/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.integration.mock.client;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.api.causeway.dto.untag.UntagRequest;
import org.jboss.pnc.causewayclient.CausewayClient;

@ApplicationScoped
public class CausewayClientMock implements CausewayClient {

    @Override
    public boolean untagBuild(UntagRequest untagRequest, String authHeaderValue) {
        return true;
    }

}
