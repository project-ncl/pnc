package org.jboss.pnc.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 * 
 * This class indicates the genesis of an artifact, whether is was imported or built internally
 */
@Entity
@Table(name = "artifact_status")
@NamedQuery(name = "ArtifactStatus.findAll", query = "SELECT a FROM ArtifactStatus a")
public enum ArtifactStatus {

    BINARY_IMPORTED,

    BINARY_BUILT;
}
