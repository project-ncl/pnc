/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dingroguclient;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class DingroguBuildPushDTO {
    String causewayUrl;
    String orchUrl;
    String operationId;
    String buildId;
    String username;
    String tagPrefix;
    boolean reimport;
}
