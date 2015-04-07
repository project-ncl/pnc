package org.jboss.pnc.integration.websockets;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ClientEndpoint
public class NotificationCollector {

    private List<String> messages = new ArrayList<>();

    @OnMessage
    public void onMessage(String message) {
        messages.add(message);
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void clear() {
        messages.clear();
    }

    public void awaitForAtLestOneMessage() throws InterruptedException {
        if(messages.isEmpty()) {
        }
    }

}
