/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.builddriver;

import java.io.Serializable;
import java.util.Optional;

import org.jboss.pnc.enums.BuildStatus;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-12-18.
 */
public interface BuildDriverResult extends Serializable {

    BuildStatus getBuildStatus();

    Optional<String> getOutputChecksum();

}
