package com.rexqwer.telegrambotassistant.event;

import com.rexqwer.telegrambotassistant.event.completion.IncomingMessageEvent;
import com.rexqwer.telegrambotassistant.event.completion.OutgoingMessageEvent;
import com.rexqwer.telegrambotassistant.service.message.MessageProcessingService;
import com.rexqwer.telegrambotassistant.service.message.MessageResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationEventListener {

    private final MessageProcessingService messageProcessingService;
    private final MessageResponseService messageResponseService;

    @Async
    @EventListener
    public void handleIncomingMessageEvent(IncomingMessageEvent event) {
        messageProcessingService.processMessage(event.getUpdate());
    }

    @Async
    @EventListener
    public void handleOutgoingMessageCompletedEvent(OutgoingMessageEvent event) {
        messageResponseService.processOutgoingMessageResponse(event.getMessage());
    }
}

