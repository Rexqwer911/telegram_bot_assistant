package com.rexqwer.telegrambotassistant.event;

import lombok.Getter;

@Getter
public class TelegramBotDownloadVoiceMessageEvent {

    private final String fileId;
    private final String fileName;

    public TelegramBotDownloadVoiceMessageEvent(String fileId, String fileName) {
        this.fileId = fileId;
        this.fileName = fileName;
    }
}
