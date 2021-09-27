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

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.jboss.pnc.common.pnc.LongBase32IdConverter;

@Embeddable
public class Base32LongID implements Serializable {

    private static final long serialVersionUID = -3000291820607237160L;

    @Column(name = "id", nullable = false, updatable = false)
    private long id;

    private Base32LongID() {
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

    public void setId(String id) {
        this.id = LongBase32IdConverter.toLong(Objects.requireNonNull(id));
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

    /**
     * Overrides default Serializable writeObject
     *
     * @param stream
     * @throws IOException
     */
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.writeLong(id);
    }

    /**
     * Overrides default Serializable readObject
     *
     * @param stream
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.id = stream.readLong();
    }

}
