package org.jboss.pnc.model.utils;

import lombok.Getter;
import lombok.Setter;
import org.jboss.pnc.model.GenericEntity;

import javax.persistence.*;
import javax.validation.constraints.Size;

/**
 * The class that contains configuration of the SCM repositories.
 *
 * @author Jakub Bartecek
 */
@Entity
public class RepositoryConfiguration implements GenericEntity<Integer> {

    private static final long serialVersionUID = 4248038054068607536L;

    private static final String SEQ_NAME = "repository_configuration_id_seq";

    @Id
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    private Integer id;

    @Size(max = 255)
    @Column(unique = true, nullable = false, updatable = false)
    @Getter
    @Setter
    private String internalScmRepoUrl;

    @Size(max = 255)
    @Getter
    @Setter
    private String externalScmRepoUrl;

    /**
     * Declares whether the pre-build repository synchronization should happen or not.
     */
    @Getter
    @Setter
    private Boolean preBuildSyncEnabled;

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryConfiguration)) return false;

        RepositoryConfiguration that = (RepositoryConfiguration) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public static class Builder {
        private Integer id;

        private String internalScmRepoUrl;

        private String externalScmRepoUrl;

        private Boolean preBuildSyncEnabled;

        private static Builder newBuilder() {
            return new Builder();
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public RepositoryConfiguration build() {
            RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
            repositoryConfiguration.setId(id);
            repositoryConfiguration.setInternalScmRepoUrl(internalScmRepoUrl);
            repositoryConfiguration.setExternalScmRepoUrl(externalScmRepoUrl);
            repositoryConfiguration.setPreBuildSyncEnabled(preBuildSyncEnabled);
        }

        public Builder internalScmRepoUrl(String internalScmRepoUrl) {
            this.internalScmRepoUrl = internalScmRepoUrl;
            return this;
        }

        public Builder externalScmRepoUrl(String externalScmRepoUrl) {
            this.externalScmRepoUrl = externalScmRepoUrl;
            return this;
        }

        public Builder preBuildSyncEnabled(Boolean preBuildSyncEnabled) {
            this.preBuildSyncEnabled = preBuildSyncEnabled;
            return this;
        }
    }
}
