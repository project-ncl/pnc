package org.jboss.pnc.rest.model;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@Builder
public class ProjectRef {

    private final int id; // Note int instead of Integer - Ref always reference existing entity.

    @JsonPOJOBuilder(withPrefix = "")
    public static final class ProjectRefBuilder {
    }
}
