/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model.utils;

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
