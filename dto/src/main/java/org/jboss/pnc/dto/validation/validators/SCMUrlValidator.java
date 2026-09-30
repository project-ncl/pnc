/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.validation.validators;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.routines.UrlValidator;
import org.jboss.pnc.dto.validation.constraints.SCMUrl;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 3/12/16 Time: 5:01 PM
 */
public class SCMUrlValidator implements ConstraintValidator<SCMUrl, String> {

    @Override
    public void initialize(SCMUrl constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isValid(value);
    }

    /******************************************************
     * STATIC:
     ******************************************************/
    private static final char USERNAME_INDICATOR = '@';
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[\\.\\-_%\\w]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final UrlValidator validator = new UrlValidator(
            new String[] { "http", "https", "git", "ssh", "git+ssh" });

    public static boolean isValid(String url) {
        if (url == null || "".equals(url)) {
            return true;
        }

        if (hasNoProtocol(url)) {
            url = prepareDefaultProtocolUrl(url);
        }

        String username = getUsername(url);
        if (username != null) {
            if (!isValidUsername(username)) {
                return false;
            }
            url = url.replaceFirst(username, "");
        }
        return validator.isValid(url);
    }

    private static String prepareDefaultProtocolUrl(String url) {
        url = url.replaceFirst(":", "/");
        url = "ssh://" + url;
        return url;
    }

    private static boolean hasNoProtocol(String url) {
        return !url.contains("://");
    }

    private static boolean isValidUsername(String username) {
        username = username.replaceFirst("" + USERNAME_INDICATOR, "");
        String[] usernameParts = username.split(":");
        return Stream.of(usernameParts).allMatch(p -> USERNAME_PATTERN.matcher(p).matches());
    }

    /**
     * Returns a String with username. '@' character or whatever indicator of it being username is included in the
     * returned value
     *
     * @param url url to extract username from
     * @return username if username is specified in the url, null otherwise
     */
    private static String getUsername(String url) {
        int atIndex = url.lastIndexOf(USERNAME_INDICATOR);
        if (atIndex < 0) {
            return null;
        }
        String username = url.substring(0, atIndex + 1);
        int slashIndex = username.lastIndexOf('/');
        if (slashIndex > 0) {
            username = username.substring(slashIndex + 1);
        }
        return username;
    }
}
