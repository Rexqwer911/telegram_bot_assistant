package com.rexqwer.telegrambotassistant.enums;

public enum MessageBranchTypeEnum {

    SCHEDULED_BRANCH(1);

    private final Integer code;

    MessageBranchTypeEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

}
