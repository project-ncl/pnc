/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.rsql;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public class RSQLSelectorPath {

    private final String element;
    private final RSQLSelectorPath next;

    private RSQLSelectorPath(String element, RSQLSelectorPath next) {
        this.element = element;
        this.next = next;
    }

    public static RSQLSelectorPath get(String selector) {
        String[] fields = selector.split("\\.");
        RSQLSelectorPath next = null;
        for (int i = fields.length - 1; i >= 0; i--) {
            next = new RSQLSelectorPath(fields[i], next);
        }
        return next;
    }

    public RSQLSelectorPath next() {
        if (next == null) {
            throw new RSQLException("Another element in the RSQL selector expected after " + element);
        }
        return next;
    }

    public boolean isFinal() {
        return next == null;
    }

    public String getElement() {
        return element;
    }

}
