package com.zosh.webSocket;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class ForecastSocket {

    @Autowired
    private SimpMessagingTemplate template;

    public void send(Long productId, double value) {
        template.convertAndSend("/topic/forecast/" + productId, value);
    }
}