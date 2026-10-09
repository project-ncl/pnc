/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildPushReport;
import org.jboss.pnc.spi.datastore.repositories.BuildPushReportRepository;

@Stateless
public class BuildPushReportRepositoryImpl extends AbstractRepository<BuildPushReport, Base32LongID>
        implements BuildPushReportRepository {

    public BuildPushReportRepositoryImpl() {
        super(BuildPushReport.class, Base32LongID.class);
    }
}