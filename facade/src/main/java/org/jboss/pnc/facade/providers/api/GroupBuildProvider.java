/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.GroupBuild;
import org.jboss.pnc.dto.GroupBuildRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.BuildConfigSetRecord;

public interface GroupBuildProvider extends Provider<Base32LongID, BuildConfigSetRecord, GroupBuild, GroupBuildRef> {

    Page<GroupBuild> getGroupBuilds(GroupBuildPageInfo groupBuildPageInfo, String groupConfigId);

    void cancel(String id);

    boolean delete(String id, String callback);
}
