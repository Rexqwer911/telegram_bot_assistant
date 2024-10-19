package com.rexqwer.telegrambotassistant.enums;

import lombok.Getter;

@Getter
public enum ScheduledTaskTypeEnum {

    DAILY(1),
    WEEKDAYS(2),
    WEEKDAYS_LOG(3),
    WEEKLY(4);

    private final Integer code;

    ScheduledTaskTypeEnum(Integer code) {
        this.code = code;
    }

}
