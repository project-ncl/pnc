/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Operation;
import org.jboss.pnc.dto.OperationRef;
import org.jboss.pnc.model.Base32LongID;

public interface OperationProvider<DB extends org.jboss.pnc.model.Operation, DTO extends Operation>
        extends Provider<Base32LongID, DB, DTO, OperationRef> {

}
