/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.providers.api;

import lombok.Data;
import org.jboss.pnc.rest.api.parameters.BuildsBuildConfigFilterParameters;
import org.jboss.pnc.rest.api.parameters.BuildsFilterParameters;
import org.jboss.pnc.rest.api.parameters.PageParameters;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class BuildPageInfo {

    private final int pageIndex;
    private final int pageSize;
    private final String sort;
    private final String q;
    private final boolean latest;
    private final boolean running;
    private final String buildConfigName;

    public static BuildPageInfo toBuildPageInfo(PageParameters page, BuildsBuildConfigFilterParameters builds) {
        return new BuildPageInfo(
                page.getPageIndex(),
                page.getPageSize(),
                page.getSort(),
                page.getQ(),
                false,
                false,
                builds.getBuildConfigName());
    }

    public static BuildPageInfo toBuildPageInfo(PageParameters page, BuildsFilterParameters builds) {
        return new BuildPageInfo(
                page.getPageIndex(),
                page.getPageSize(),
                page.getSort(),
                page.getQ(),
                builds.isLatest(),
                builds.isRunning(),
                builds.getBuildConfigName());
    }
}
