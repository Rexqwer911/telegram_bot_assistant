package com.rexqwer.telegrambotassistant.event;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Getter
public class SendMessageEvent {
    private final String message;
    private final String chatId;
    private final ReplyKeyboard replyKeyboard;

    public SendMessageEvent(String message, String chatId, ReplyKeyboard replyKeyboard) {
        this.message = message;
        this.chatId = chatId;
        this.replyKeyboard = replyKeyboard;
    }

    public SendMessageEvent(String message, String chatId) {
        this.message = message;
        this.chatId = chatId;
        this.replyKeyboard = null;
    }
}
