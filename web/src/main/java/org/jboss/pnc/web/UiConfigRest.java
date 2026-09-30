/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.web;

import org.jboss.pnc.common.json.GlobalModuleGroup;
import org.jboss.pnc.common.json.moduleconfig.UIModuleConfig;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 12/6/16 Time: 2:32 PM
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UiConfigRest {

    @JsonUnwrapped
    private GlobalModuleGroup globalConfig;

    @JsonUnwrapped
    private UIModuleConfig uiModuleConfig;

    private String internalScmAuthority;

}
