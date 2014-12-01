package org.jboss.pnc.model;

/**
 * This class maps the different type of Repository Managers, whether they are Maven, NPM, CocoaPod repositories, Docker
 * registries, etc
 */
public enum RepositoryManagerType {
    MAVEN,
    DOCKER_REGISTRY,
    NPM,
    COCOA_POD
}
