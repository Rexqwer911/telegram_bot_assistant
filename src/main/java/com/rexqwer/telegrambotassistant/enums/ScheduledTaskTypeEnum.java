package com.rexqwer.telegrambotassistant.enums;

public enum ScheduledTaskTypeEnum {

    REGULAR_NOTIFICATION_DAILY(1, "ежедневно"),
    REGULAR_NOTIFICATION_WEEKDAYS_LOG(2, "лог будни");

    private final Integer code;
    private final String periodicity;

    ScheduledTaskTypeEnum(Integer code, String periodicity) {
        this.code = code;
        this.periodicity = periodicity;
    }

    public Integer getCode() {
        return code;
    }

    public String getPeriodicity() {
        return periodicity;
    }

}
