/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.causewayclient;

import org.jboss.pnc.api.causeway.dto.untag.UntagRequest;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface CausewayClient {

    boolean untagBuild(UntagRequest untagRequest, String authHeaderValue);
}
