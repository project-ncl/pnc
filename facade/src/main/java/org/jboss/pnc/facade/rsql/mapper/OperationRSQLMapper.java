/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.model.Operation;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class OperationRSQLMapper extends GenericOperationRSQLMapper<Operation> {

    public OperationRSQLMapper() {
        super(Operation.class);
    }

}
