package org.jboss.pnc.spi.notifications;

public interface MessageCallback {

    void failed(AttachedClient attachedClient, Throwable throwable);

    void successful(AttachedClient attachedClient);

}
