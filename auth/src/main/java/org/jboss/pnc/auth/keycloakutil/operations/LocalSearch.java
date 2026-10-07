/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.auth.keycloakutil.operations;

import java.util.LinkedList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class LocalSearch {

    private List<ObjectNode> items;

    public LocalSearch(List<ObjectNode> items) {
        this.items = items;
    }

    public ObjectNode exactMatchOne(String value, String... attrs) {

        List<ObjectNode> matched = new LinkedList<>();

        for (ObjectNode item : items) {
            for (String attr : attrs) {
                JsonNode node = item.get(attr);
                if (node != null && node.asText().equals(value)) {
                    matched.add(item);
                    break;
                }
            }
        }

        if (matched.size() == 0) {
            return null;
        }

        if (matched.size() > 1) {
            throw new RuntimeException("More than one match");
        }

        return matched.get(0);
    }
}
