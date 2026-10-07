/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ServiceAccountClientConfig {
    public static enum Mode {
        KEYCLOAK, LDAP
    }

    private Mode mode;

    @JsonCreator
    public ServiceAccountClientConfig(@JsonProperty("mode") Mode mode) {
        this.mode = mode;
    }
}
