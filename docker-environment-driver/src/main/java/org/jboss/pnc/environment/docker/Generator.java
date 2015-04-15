package org.jboss.pnc.environment.docker;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import javax.enterprise.context.ApplicationScoped;

/**
 * Generates unique container IDs
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@ApplicationScoped
public class Generator {

    private final String CONTAINER_ID_PREFIX = "PNC-"
            + UUID.randomUUID().toString() + "-";

    private final AtomicInteger atomicInt = new AtomicInteger();

    /**
     * @return New unique container id
     */
    public String generateContainerId() {
        return CONTAINER_ID_PREFIX + atomicInt.incrementAndGet();
    }

}
