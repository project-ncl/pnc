/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadEnvProperty {

    private static final Logger log = LoggerFactory.getLogger(ReadEnvProperty.class);

    /**
     * If propertyName has no value (either specified in system property or environment property), then just return the
     * default value. System property value has priority over environment property value.
     *
     * If value can't be parsed, just return the default value.
     *
     * @param propertyName property name to check the value
     * @param defaultValue default value to use
     *
     * @return value from property, or default value
     */
    public int getIntValueFromPropertyOrDefault(String propertyName, int defaultValue) {

        int value = defaultValue;

        String valueEnv = System.getenv(propertyName);
        String valueSys = System.getProperty(propertyName);

        try {
            if (valueSys != null) {
                value = Integer.parseInt(valueSys);
            } else if (valueEnv != null) {
                value = Integer.parseInt(valueEnv);
            }
            return value;
        } catch (NumberFormatException e) {
            log.warn(
                    "Could not parse the '" + propertyName + "' system property. Using default value: " + defaultValue);
            return value;
        }
    }
}
