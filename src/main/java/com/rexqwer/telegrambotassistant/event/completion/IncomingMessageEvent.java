package com.rexqwer.telegrambotassistant.event.completion;

import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;

@Getter
public class IncomingMessageEvent {

    private final Update update;

    public IncomingMessageEvent(Update update) {
        this.update = update;
    }
}
