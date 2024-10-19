package com.rexqwer.telegrambotassistant.enums;

import lombok.Getter;

@Getter
public enum MessageBranchTypeEnum {

    REMINDER_BRANCH(1),
    GPT_BRANCH(2);

    private final Integer code;

    MessageBranchTypeEnum(Integer code) {
        this.code = code;
    }

}
