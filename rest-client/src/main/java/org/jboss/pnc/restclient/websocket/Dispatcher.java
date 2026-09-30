/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.restclient.websocket;

import java.util.function.Consumer;

/**
 * Dispatchers are labeled Consumers of generic String WebSocket messages.
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
public interface Dispatcher extends Consumer<String> {
}
