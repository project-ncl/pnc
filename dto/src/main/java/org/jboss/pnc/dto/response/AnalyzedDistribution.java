/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import java.util.Date;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

/**
 * This DTO provides information about the distribution which was analyzed by deliverable analyzer operation.
 */
@Value
@Builder
@Jacksonized
public class AnalyzedDistribution {

    /**
     * The url of the distribution analyzed
     */
    String distributionUrl;

    /**
     * The time this distribution was first analyzed
     */
    Date creationTime;

    /**
     * MD5 checksum of the distribution.
     */
    String md5;

    /**
     * SHA-1 checksum of the distribution.
     */
    String sha1;

    /**
     * SHA-256 checksum of the distribution.
     */
    String sha256;

}
