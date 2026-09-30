/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.messaging.spi;

import java.util.Map;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public interface MessageSender {

    void init();

    void destroy();

    String getMessageSenderId();

    void sendToTopic(Message message);

    void sendToTopic(Message message, Map<String, String> headers);

    void sendToTopic(String message);

    void sendToTopic(String message, Map<String, String> headers);
}
