package org.jboss.pnc.rest.notifications;

/**
 * A generic WS client.
 */
public interface AttachedClient {

    /**
     * Returns <code>true</code> if enabled.
     */
    boolean isEnabled();

    /**
     * Sends a message to the client
     *
     * @param messageBody Message body - depends on implementation how to deal with it.
     */
    void sendMessage(Object messageBody) throws Exception;
}
