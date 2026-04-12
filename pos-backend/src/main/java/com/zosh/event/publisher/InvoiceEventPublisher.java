package com.zosh.event.publisher;

import com.zosh.event.InvoiceEmailRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishInvoiceEmailRequested(InvoiceEmailRequestedEvent event) {
        applicationEventPublisher.publishEvent(event);
        log.debug("InvoiceEmailRequestedEvent published for invoice ID: {}", event.getInvoiceId());
    }
}
