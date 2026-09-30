/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/11/16 Time: 1:06 PM
 *
 *
 * WARNING: This class is used in REST API too. Create a separate *Rest class if you wish to add here something that
 * should not be sent via rest
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SshCredentials {
    private String command;
    private String password;
}
