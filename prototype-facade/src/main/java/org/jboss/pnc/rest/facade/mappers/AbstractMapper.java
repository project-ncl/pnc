package org.jboss.pnc.rest.facade.mappers;

import org.jboss.pnc.rest.facade.mappers.api.EntityMapper;
import org.jboss.pnc.model.GenericEntity;

import java.io.Serializable;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public abstract class AbstractMapper<DB extends GenericEntity<?>, Rest, Ref> implements EntityMapper<DB, Rest, Ref> {

    protected Instant toInstant(final Date time) {
        if (time != null) {
            return time.toInstant();
        }
        return null;
    }

    protected Date toDate(final Instant time) {
        if (time != null) {
            return Date.from(time);
        }
        return null;
    }

    protected <K, V> Map<K, V> toMap(final Map<K, V> map) {
        if (map != null) {
            return Collections.unmodifiableMap(map);
        } else {
            return Collections.emptyMap();
        }
    }

    protected <V> Set<V> toSet(final Set<V> set) {
        if (set != null) {
            return Collections.unmodifiableSet(set);
        } else {
            return Collections.emptySet();
        }
    }

    protected <I extends Serializable> I toId(GenericEntity<I> entity) {
        if (entity != null) {
            return entity.getId();
        }
        return null;
    }

    protected <I extends Serializable> Set<I> toIds(Collection<? extends GenericEntity<I>> entities) {
        if (entities != null) {
            return entities.stream().map(GenericEntity::getId).collect(Collectors.toSet());
        }
        return Collections.emptySet();
    }

}
