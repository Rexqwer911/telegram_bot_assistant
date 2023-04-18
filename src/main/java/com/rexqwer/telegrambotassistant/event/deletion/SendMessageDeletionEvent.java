package com.rexqwer.telegrambotassistant.event.deletion;

import lombok.Getter;

@Getter
public class SendMessageDeletionEvent {
    private final Long pgMessageId;
    private final String chatId;
    private final String messageId;

    public SendMessageDeletionEvent(Long pgMessageId, String chatId, String messageId) {
        this.pgMessageId = pgMessageId;
        this.chatId = chatId;
        this.messageId = messageId;
    }
}
