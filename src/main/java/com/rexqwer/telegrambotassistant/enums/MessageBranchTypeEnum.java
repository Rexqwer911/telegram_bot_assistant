package com.rexqwer.telegrambotassistant.enums;

public enum MessageBranchTypeEnum {

    REMINDER_BRANCH(1),
    GPT_BRANCH(2);

    private final Integer code;

    MessageBranchTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
