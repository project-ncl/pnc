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
package org.jboss.pnc.model.utils;

import org.jboss.pnc.common.pnc.LongBase32IdConverter;

/**
 * Component that contains the rules for generating various content ID's which are used to uniquely associate content
 * stored in external services with builds, build-sets, products, etc.
 */
public class ContentIdentityManager {

    public static String getBuildContentId(String buildRecordId) {
        if (buildRecordId == null)
            throw new IllegalArgumentException("Null is not a valid build record ID");

        return "build-" + buildRecordId;
    }
}
