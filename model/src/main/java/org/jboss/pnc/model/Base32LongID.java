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
import javax.persistence.Convert;
import javax.persistence.Embeddable;

import org.jboss.pnc.common.pnc.LongBase32IdConverter;
import org.jboss.pnc.model.utils.Base32LongIDConverter;

@Embeddable
public class Base32LongID implements Serializable {
    @Column(name = "id", nullable = false, updatable = false)
    private long id;

    protected Base32LongID() {
    }

    public Base32LongID(String id) {
        this.id = LongBase32IdConverter.toLong(Objects.requireNonNull(id));
    }

    public Base32LongID(long id) {
        this.id = id;
    }

    public String getId() {
        return LongBase32IdConverter.toString(id);
    }

    public long getLongId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Base32LongID))
            return false;
        Base32LongID that = (Base32LongID) o;
        return getLongId() == that.getLongId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLongId());
    }

    @Override
    public String toString() {
        return getId();
    }
}
