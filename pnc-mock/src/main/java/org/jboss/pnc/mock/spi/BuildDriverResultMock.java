/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.spi;

import java.util.Optional;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildDriverResultMock {

    public static final String BUILD_LOG = "The quick brown fox jumps over the lazy dog.\nFinished: SUCCESS";

    public static BuildDriverResult mockResult(BuildStatus status) {
        return new BuildDriverResult() {
            @Override
            public BuildStatus getBuildStatus() {
                return status;
            }

            @Override
            public Optional<String> getOutputChecksum() {
                return Optional.of("4678bbe366b11f7216bd03ad33f583d9");
            }
        };
    }

}
