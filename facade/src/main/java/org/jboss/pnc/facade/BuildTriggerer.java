/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade;

import java.util.Optional;
import java.util.OptionalInt;

import org.jboss.pnc.dto.requests.GroupBuildRequest;
import org.jboss.pnc.spi.BuildOptions;
import org.jboss.pnc.spi.exception.BuildConflictException;
import org.jboss.pnc.spi.exception.CoreException;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface BuildTriggerer {

    int triggerBuild(int buildConfigId, OptionalInt rev, BuildOptions buildOptions)
            throws BuildConflictException, CoreException;

    int triggerGroupBuild(int groupConfigId, Optional<GroupBuildRequest> revs, BuildOptions buildOptions)
            throws BuildConflictException, CoreException;
    
}
