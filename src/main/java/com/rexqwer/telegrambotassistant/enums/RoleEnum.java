package com.rexqwer.telegrambotassistant.enums;

public enum RoleEnum {

    DEFAULT(1),
    ADMIN(2);

    private final Integer code;

    RoleEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }
}
