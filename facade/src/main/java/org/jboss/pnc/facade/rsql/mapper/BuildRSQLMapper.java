/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql.mapper;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.jboss.pnc.facade.rsql.RSQLException;
import org.jboss.pnc.facade.rsql.converter.Base32EncodedLongValueConverter;
import org.jboss.pnc.facade.rsql.converter.ValueConverter;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecord_;
import org.jboss.pnc.model.GenericEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class BuildRSQLMapper extends AbstractRSQLMapper<Base32LongID, BuildRecord> {

    private static final Logger logger = LoggerFactory.getLogger(BuildRSQLMapper.class);

    public BuildRSQLMapper() {
        super(BuildRecord.class);
    }

    @Override
    protected SingularAttribute<BuildRecord, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "buildConfigRevision":
                throw new RSQLException(
                        "RSQL selector 'buildConfigRevision' is hard or impossible to implement for Builds");
            case "environment":
                throw new RSQLException("RSQL selector 'environment' is hard or impossible to implement for Builds");
            case "user":
                return BuildRecord_.user;
            case "groupBuild":
                return BuildRecord_.buildConfigSetRecord;
            case "productMilestone":
                return BuildRecord_.productMilestone;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<BuildRecord, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<BuildRecord, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return BuildRecord_.id;
            case "submitTime":
                return BuildRecord_.submitTime;
            case "startTime":
                return BuildRecord_.startTime;
            case "endTime":
                return BuildRecord_.endTime;
            case "status":
                return BuildRecord_.status;
            case "buildContentId":
                return BuildRecord_.buildContentId;
            case "temporaryBuild":
                return BuildRecord_.temporaryBuild;
            case "scmUrl":
                return BuildRecord_.scmRepoURL;
            case "scmTag":
                return BuildRecord_.scmTag;
            case "scmRevision":
                return BuildRecord_.scmRevision;
            case "buildOutputChecksum":
                return BuildRecord_.buildOutputChecksum;
            default:
                return null;
        }
    }

    @Override
    public ValueConverter getValueConverter(String name) {
        switch (name) {
            case "id":
                logger.debug("Using custom value converter ...");
                return new Base32EncodedLongValueConverter();
            default:
                return super.getValueConverter(name);
        }

    }

}
