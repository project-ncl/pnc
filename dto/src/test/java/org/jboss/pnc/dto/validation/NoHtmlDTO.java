/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.validation;

import org.jboss.pnc.dto.validation.constraints.NoHtml;

public class NoHtmlDTO {

    @NoHtml
    public String test;
}
