package com.rexqwer.telegrambotassistant.event.completion;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Message;

@Getter
public class OutgoingMessageEvent {
    private final Message message;

    public OutgoingMessageEvent(Message message) {
        this.message = message;
    }
}
