package com.rexqwer.telegrambotassistant.event;

import lombok.Getter;

@Getter
public class DisableUserTasksEvent {

    private final String chatId;

    public DisableUserTasksEvent(String chatId) {
        this.chatId = chatId;
    }
}
