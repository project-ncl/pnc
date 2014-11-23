package org.jboss.pnc.core;

import org.jboss.pnc.core.spi.repositorymanager.RepositoryManager;
import org.jboss.pnc.model.BuildType;

import javax.enterprise.context.ApplicationScoped;


/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
@ApplicationScoped
public class RepositoryManagerFactory {

    public RepositoryManager getRepositoryManager(BuildType buildType) {
        return null;//TODO
    }
}
