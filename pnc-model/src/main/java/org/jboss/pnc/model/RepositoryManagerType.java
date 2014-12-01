package org.jboss.pnc.model;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-25.
 * 
 * This class maps the different type of Repository Managers, whether they are Maven, NPM, CocoaPod repositories, Docker
 * registries, etc
 */
@Entity
@Table(name = "repository_manager_type")
@NamedQuery(name = "RepositoryManagerType.findAll", query = "SELECT r FROM RepositoryManagerType r")
public enum RepositoryManagerType {

    MAVEN,

    DOCKER_REGISTRY,

    NPM,

    COCOA_POD;
}
