/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.coordinator;

import org.jboss.pnc.enums.ResultStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@Builder
public final class Result {

    private final String id;

    private final ResultStatus status;

    private final String message;

    public Result(String id, ResultStatus status) {
        this.id = id;
        this.status = status;
        this.message = "";
    }

    public Result(String id, ResultStatus status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    public boolean isSuccess() {
        return status.isSuccess();
    }

}
