package org.jboss.pnc.rest.notifications;

/**
 * Notification mechanism for Web Sockets. All implementation details should be placed in AttachedClient.
 */
public interface Notifier {

    void attachClient(AttachedClient attachedClient);

    void detachClient(AttachedClient attachedClient);

    int getAttachedClientsCount();

    void sendMessage(Object message);
}
