package org.jboss.pnc.restclient.websocket;

import org.jboss.pnc.client.RemoteResourceException;

/**
 * The Supplier uses secondary means (f.e REST) to retrieve relevant information that WS message would contain.
 * Additionally, the supplier must be reusable.
 */
@FunctionalInterface
public interface FallbackRequestSupplier<T> {
    T get() throws RemoteResourceException;
}
