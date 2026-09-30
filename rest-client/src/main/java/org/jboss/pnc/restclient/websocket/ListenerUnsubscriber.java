/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.restclient.websocket;

/**
 * It's labeled Runnable which is used to remove WebSocket listeners.
 *
 * Should be used after listener is no longer needed.
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
public interface ListenerUnsubscriber extends Runnable {
}
