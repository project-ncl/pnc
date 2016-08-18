package org.jboss.pnc.datastore.limits;
/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pageable implementation that supports cursored (offset / limit based) pagination rather than
 * the index / size based.
 *
 * @author Alex Creasy
 */
public class CursoredPageRequest implements Pageable {

    private final int offset;
    private final int limit;
    private final Sort sort;

    public CursoredPageRequest(int offset, int limit, Sort sort) {
        if (offset < 0) {
            throw new IllegalArgumentException("Offset must not be less than zero!");
        }

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must not be less than one!");
        }


        this.offset = offset;
        this.limit = limit;
        this.sort = sort;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public Pageable next() {
        return new CursoredPageRequest(offset + limit, limit, sort);
    }

    @Override
    public Pageable first() {
        return new CursoredPageRequest(0, limit, sort);
    }

    @Override
    public int getPageNumber() {
        return (int) Math.ceil(offset / limit);
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    @Override
    public Pageable previousOrFirst() {
        int newOffset;

        if (offset < limit) {
            newOffset = 0;
        } else {
            newOffset = offset - limit;
        }

        return new CursoredPageRequest(newOffset, limit, sort);
    }

    @Override
    public boolean hasPrevious() {
        return offset - limit > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CursoredPageRequest that = (CursoredPageRequest) o;

        if (offset != that.offset) return false;
        if (limit != that.limit) return false;
        return sort != null ? sort.equals(that.sort) : that.sort == null;

    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + limit;
        result = 31 * result + (sort != null ? sort.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CursoredPageRequest{" +
                "offset=" + offset +
                ", limit=" + limit +
                ", sort=" + sort +
                '}';
    }
}
