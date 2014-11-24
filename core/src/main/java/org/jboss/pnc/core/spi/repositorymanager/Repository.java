package org.jboss.pnc.core.spi.repositorymanager;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-23.
 */
public interface Repository {
    /**
     * Mark repository data as persistent. In case of proxy it stores new imports. Proxy repo itself can be dropped.
     */
    void persist();
}
