package com.rexqwer.telegrambotassistant.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {

    DEFAULT(1),
    ADMIN(2),
    SYSTEM(3);

    private final Integer code;

    RoleEnum(Integer code) {
        this.code = code;
    }

}
