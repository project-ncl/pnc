/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
