package org.jboss.pnc.rest.facade.mappers.api;

import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public interface EntityMapper<DB extends GenericEntity<?>, Rest, Ref> {

    DB toEntity(Rest buildConfiguration);

    Ref toRef(DB buildConfiguration);

    Rest toRest(DB buildConfiguration);

}
