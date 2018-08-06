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
public class RepositoryConfigurationRef {

    private final int id; // Note int instead of Integer - Ref always reference existing entity.

    private final String internalUrl;

    private final String externalUrl;

    private final boolean preBuildSyncEnabled;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class RepositoryConfigurationRefBuilder {
    }
}
