/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Announcement banner. This banner is used to notify users about important information.
 *
 * @author jbrazdil
 */
@ToString
@Getter
@Setter
public class Banner {

    /**
     * Banner text. The text is plain text that is shown to users.
     */
    private String banner;
}
