/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.model;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;

/**
 * This class represents the combination of an ID and table revision. It is used as the primary key in audit tables.
 *
 */
public class IdRev implements Serializable {

    private static final long serialVersionUID = 0L;

    @Column(name = "id")
    public Integer id;

    @Column(name = "rev")
    public Integer rev;

    public IdRev() {

    }

    public IdRev(Integer id, Integer rev) {
        this.id = id;
        this.rev = rev;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRev() {
        return rev;
    }

    public void setRev(Integer rev) {
        this.rev = rev;
    }

    public String toString() {
        return "Id: " + id + ", Rev: " + rev;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, rev);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof IdRev))
            return false;
        IdRev other = (IdRev) obj;
        if (id == null) {
            if (other.getId() != null)
                return false;
        } else if (!id.equals(other.getId()))
            return false;
        if (rev == null) {
            if (other.getRev() != null)
                return false;
        } else if (!rev.equals(other.getRev()))
            return false;
        return true;
    }
}
