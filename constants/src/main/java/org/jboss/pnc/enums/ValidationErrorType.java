/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.enums;

/**
 * This enum represents various types of errors found throughout validation
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum ValidationErrorType {
    /**
     * Validated entity has invalid format (f.e. regex pattern does not match).
     */
    FORMAT,
    /**
     * Validated entity already exists.
     */
    DUPLICATION
}
