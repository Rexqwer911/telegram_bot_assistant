package com.rexqwer.telegrambotassistant.event.deletion;

import lombok.Getter;

@Getter
public class MessageDeletedEvent {
    private final Long pgMessageId;

    public MessageDeletedEvent(Long pgMessageId) {
        this.pgMessageId = pgMessageId;
    }
}
