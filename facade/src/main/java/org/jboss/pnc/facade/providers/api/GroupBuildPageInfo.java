/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.rest.api.parameters.GroupBuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import lombok.Builder;
import lombok.Data;

/**
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
public class GroupBuildPageInfo {

    private int pageIndex;
    private int pageSize;
    private String sort;
    private String q;
    private boolean latest;

    public static GroupBuildPageInfo toGroupBuildPageInfo(
            PageParameters pageParams,
            GroupBuildsFilterParameters filterParameters) {
        return GroupBuildPageInfo.builder()
                .pageIndex(pageParams.getPageIndex())
                .pageSize(pageParams.getPageSize())
                .sort(pageParams.getSort())
                .q(pageParams.getQ())
                .latest(filterParameters.isLatest())
                .build();
    }
}
