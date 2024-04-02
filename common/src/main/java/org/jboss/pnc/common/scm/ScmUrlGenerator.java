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
package org.jboss.pnc.common.scm;

import javax.validation.constraints.NotNull;

public interface ScmUrlGenerator {
    /**
     * Generate a download url for a Gitweb snapshot using the scm provider url of the project and the ref
     *
     * @param scmUrl URL has to be the 'git clone' link (either http or git+ssh)
     * @param ref The ref to generate a snapshot. It can be a sha, branch, tag. If left empty, master is used
     * @return Download url
     * @throws ScmException thrown if the Url is not valid
     */
    String generateDownloadUrlWithGitweb(@NotNull String scmUrl, @NotNull String ref) throws ScmException;
}
