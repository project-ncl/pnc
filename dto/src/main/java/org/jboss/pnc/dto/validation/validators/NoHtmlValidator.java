/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.validation.validators;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * Implementation for the annotation NoHtml to verify that the String doesn't contain HTML tags. This is useful to
 * prevent XSS attacks.
 *
 * It should work out of the box with Hibernate Validator
 *
 * Copied from: https://stackoverflow.com/a/68888601/2907906
 */
public class NoHtmlValidator implements ConstraintValidator<NoHtml, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        return value == null || Jsoup.isValid(value, Safelist.none());
    }
}
