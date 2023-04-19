package com.rexqwer.telegrambotassistant.enums;

public enum MessageBranchTypeEnum {

    UNDEFINED(1),
    SCHEDULED_BRANCH(2);

    private final Integer code;

    MessageBranchTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
