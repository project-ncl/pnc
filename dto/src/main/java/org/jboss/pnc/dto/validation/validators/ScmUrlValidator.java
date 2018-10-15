/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.dto.validation.validators;

import org.apache.commons.validator.routines.UrlValidator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com
 * Date: 3/12/16
 * Time: 5:01 PM
 */
public class ScmUrlValidator implements ConstraintValidator<ScmUrl, String>{

    @Override
    public void initialize(ScmUrl constraintAnnotation) {}

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isValid(value);
    }


    /******************************************************
     * STATIC:
     ******************************************************/
    private static final char USERNAME_INDICATOR = '@';
    private static final Pattern USERNAME_PATTERN = Pattern.compile("[\\.\\-_%\\w]+", Pattern.UNICODE_CHARACTER_CLASS);
    private static final UrlValidator validator = new UrlValidator(new String[]{"http", "https", "git", "ssh", "git+ssh"});

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
        return Stream.of(usernameParts)
                .allMatch(p -> USERNAME_PATTERN.matcher(p).matches());
    }

    /**
     * Returns a String with username.
     * '@' character or whatever indicator of it being username is included in the returned value
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
