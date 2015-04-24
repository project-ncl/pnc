package org.jboss.pnc.spi.datastore;

import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.User;

/**
 * Created by <a href="mailto:matejonnet@gmail.com">Matej Lazar</a> on 2014-11-24.
 */
public interface Datastore {

    void storeCompletedBuild(BuildRecord buildRecord) throws DatastoreException;

    User retrieveUserByUsername(String username);
}
