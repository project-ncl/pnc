/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.repositories;

import javax.ejb.Stateless;

import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableArtifactLicenseInfo;
import org.jboss.pnc.spi.datastore.repositories.DeliverableArtifactLicenseInfoRepository;

@Stateless
public class DeliverableArtifactLicenseInfoRepositoryImpl
        extends AbstractRepository<DeliverableArtifactLicenseInfo, Base32LongID>
        implements DeliverableArtifactLicenseInfoRepository {

    public DeliverableArtifactLicenseInfoRepositoryImpl() {
        super(DeliverableArtifactLicenseInfo.class, Base32LongID.class);
    }
}