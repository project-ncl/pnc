package org.jboss.pnc.datastore;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
class Sequences {
    static AtomicInteger buildRecordSequence = new AtomicInteger(0);

    static AtomicInteger buildConfigSetRecordSequence = new AtomicInteger(0);;
}
