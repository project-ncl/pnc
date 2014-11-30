package org.jboss.pnc.model;

import java.io.StringBufferInputStream;
import java.util.List;
import java.util.UUID;

/**
 * BuildConfiguration should have links to the project being built AND the product
 * (RepositoryConfiguration) in which the build is taking place. The product
 * information will determine the in-progress repository to pull from
 * (in addition to global repos potentially available to all builds).
 * The project information is used to name the repository environment, along with a build ID
 * generated for use in the BuildConfiguration. The product information should also define
 * whether (and which) external repositories can be used for this build...or, possibly this
 * will be handled during product-specific environment setup, from which this build repository would inherit.
 */
public class BuildConfiguration {

    /**
     * Generated unique build ID.
     * TODO: UUID?
     */
    private String buildID;

    /**
     * Project being built.
     */
    private Project currentProject;

    /**
     * List of repositories that are being used.
     */
    private List<String> repositories;

    // TODO : Configure repositories.
    public BuildConfiguration (Project p)
    {
        this.currentProject = p;

        // For now use a generated buildID.
        buildID = UUID.randomUUID().toString();
    }

}
