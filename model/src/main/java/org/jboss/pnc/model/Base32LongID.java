/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
        stream.defaultWriteObject();
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
        stream.defaultReadObject();
        this.id = stream.readLong();
    }
}
