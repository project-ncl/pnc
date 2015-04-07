package org.jboss.pnc.rest.notifications.websockets;

import org.jboss.pnc.rest.notifications.AttachedClient;
import org.jboss.pnc.rest.notifications.Notifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Notification mechanism for Web Sockets. All implementation details should be placed in AttachedClient.
 */
@ApplicationScoped
public class DefaultNotifier implements Notifier {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private Set<AttachedClient> attachedClients = Collections.synchronizedSet(new HashSet<>());

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        scheduler.scheduleAtFixedRate(this::cleanUp, 1, 1, TimeUnit.HOURS);
    }

    @Override
    public void attachClient(AttachedClient attachedClient) {
        attachedClients.add(attachedClient);
    }

    @Override
    public void detachClient(AttachedClient attachedClient) {
        attachedClients.remove(attachedClient);
    }

    @Override
    public int getAttachedClientsCount() {
        return attachedClients.size();
    }

    @Override
    public void sendMessage(Object message) {
        for(Iterator<AttachedClient> attachedClientIterator = attachedClients.iterator(); attachedClientIterator.hasNext();) {
            AttachedClient client = attachedClientIterator.next();
            if(client.isEnabled()) {
                try {
                     client.sendMessage(message);
                } catch (Exception e) {
                    logger.error("Notification client threw an error, removing it", e);
                    attachedClientIterator.remove();
                }
            }
        }
    }

    public void cleanUp() {
        for(Iterator<AttachedClient> attachedClientIterator = attachedClients.iterator(); attachedClientIterator.hasNext();) {
            AttachedClient client = attachedClientIterator.next();
            if(!client.isEnabled()) {
                attachedClientIterator.remove();
            }
        }
    }
}
