/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth.keycloakutil.util;

import java.util.Iterator;
import java.util.LinkedHashMap;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class Headers implements Iterable<Header> {

    private LinkedHashMap<String, Header> headers = new LinkedHashMap<>();

    public void add(String header, String value) {
        headers.put(header.toLowerCase(), new Header(header, value));
    }

    public boolean addIfMissing(String header, String value) {
        String key = header.toLowerCase();
        if (!headers.containsKey(key)) {
            headers.put(key, new Header(header, value));
            return true;
        }
        return false;
    }

    public boolean contains(String header) {
        String key = header.toLowerCase();
        return headers.containsKey(key);
    }

    public Header get(String header) {
        return headers.get(header.toLowerCase());
    }

    @Override
    public Iterator<Header> iterator() {
        return headers.values().iterator();
    }
}
