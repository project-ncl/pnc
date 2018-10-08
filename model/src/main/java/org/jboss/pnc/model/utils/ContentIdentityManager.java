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
package org.jboss.pnc.model.utils;

import org.jboss.pnc.model.ProductVersion;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Component that contains the rules for generating various content ID's which are used to uniquely associate content stored in
 * external services with builds, build-sets, products, etc.
 */
public class ContentIdentityManager {

    private static final String PRODUCT_CONTENT_ID_FORMAT = "product_%s_%s";

    private static final String BUILD_SET_CONTENT_ID_FORMAT = "set_%s_%s";

    private static final String BUILD_CONTENT_ID_FORMAT = "build_%s_%s";

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd.HHmm";

    public static String getProductContentId(ProductVersion productVersion) {
        if (productVersion == null) {
            return null;
        }

        return String.format(PRODUCT_CONTENT_ID_FORMAT, safeIdPart(productVersion.getProduct().getName()),
                safeIdPart(productVersion.getVersion()));
    }

    public static String getBuildContentId(String identifier) {
        String timestamp = generateTimestamp();
        return String.format(BUILD_CONTENT_ID_FORMAT, safeIdPart(identifier), timestamp);
    }

    private static String generateTimestamp() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }

    /**
     * Sift out spaces, pipe characters and colons (things that don't play well in URLs) from the project name, and convert them
     * to dashes. This is only for naming repositories, so an approximate match to the project in question is fine.
     */
    private static String safeIdPart(String name) {
        return name.replaceAll("\\W+", "-").replaceAll("[|:]+", "-");
    }

}
