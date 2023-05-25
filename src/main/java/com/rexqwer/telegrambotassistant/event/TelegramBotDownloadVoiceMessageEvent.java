package com.rexqwer.telegrambotassistant.event;

import lombok.Getter;

@Getter
public class TelegramBotDownloadVoiceMessageEvent {

    private final String fileId;
    private final String fileName;
    private final String chatId;

    public TelegramBotDownloadVoiceMessageEvent(String fileId, String fileName, String chatId) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.chatId = chatId;
    }
}
