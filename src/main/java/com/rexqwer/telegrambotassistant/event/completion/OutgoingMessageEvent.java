package com.rexqwer.telegrambotassistant.event.completion;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Message;

@Getter
public class OutgoingMessageEvent {
    private final Message message;
    private final Long messageId;

    public OutgoingMessageEvent(Message message, Long messageId) {
        this.message = message;
        this.messageId = messageId;
    }

    public OutgoingMessageEvent(Message message) {
        this.message = message;
        this.messageId = null;
    }
}
