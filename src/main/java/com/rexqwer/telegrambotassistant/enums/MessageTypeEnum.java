package com.rexqwer.telegrambotassistant.enums;

import lombok.Getter;

@Getter
public enum MessageTypeEnum {

    UNKNOWN(1, ""),
    LOG(2, "(^)Лог, (.*)"),
    COMMAND(3, ""),
    SCHEDULED_TASK(4, ""),
    GPT_RESPONSE(5, ""),
    TELEGRAM_RESPONSE(6, "");

    private final Integer code;
    private final String pattern;

    MessageTypeEnum(Integer code, String pattern) {
        this.code = code;
        this.pattern = pattern;
    }

}
