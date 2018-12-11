/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.enums;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public enum ArtifactQuality {
    /**
     * The artifact has not yet been verified or tested
     */
    NEW,
    /**
     * The artifact has been verified by an automated process, but has not yet been tested against a
     * complete product or other large set of components.
     */
    VERIFIED,
    /**
     * The artifact has passed integration testing.
     */
    TESTED,
    /**
     * The artifact should no longer be used due to lack of support and/or a better alternative
     * being available.
     */
    DEPRECATED,
    /**
     * The artifact contains a severe defect, possibly a functional or security issue.
     */
    BLACKLISTED,
    /**
     * Artifact with DELETED quality is used to show BuildRecord dependencies although the artifact
     * itself was deleted. DELETED can be set only from TEMPORARY
     */
    DELETED,
    /**
     * The artifact is from a snapshot or a Pull Request build
     */
    TEMPORARY

}
