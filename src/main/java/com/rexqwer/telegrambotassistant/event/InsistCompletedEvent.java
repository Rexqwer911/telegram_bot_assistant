package com.rexqwer.telegrambotassistant.event;

import lombok.Getter;

@Getter
public class InsistCompletedEvent {
    private final String tgIdMessage;

    public InsistCompletedEvent(String tgIdMessage) {
        this.tgIdMessage = tgIdMessage;
    }
}
