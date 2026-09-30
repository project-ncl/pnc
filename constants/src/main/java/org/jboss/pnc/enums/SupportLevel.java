/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * Contains the various possible support levels, such as UNRELEASED, SUPPORTED, EOL, etc..
 *
 * Rome wasn't built in a day, nor is PNC. This feature will come in near future.
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum SupportLevel {
    UNRELEASED, EARLYACCESS, SUPPORTED, EXTENDED_SUPPORT, EOL

}
