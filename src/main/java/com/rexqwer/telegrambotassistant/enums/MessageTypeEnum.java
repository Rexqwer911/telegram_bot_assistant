package com.rexqwer.telegrambotassistant.enums;

public enum MessageTypeEnum {

    /**
     * Ниже приведены паттерны, которые может обрабатывать приложение
     *
     * Лог, Сегодня сделал супчик, покушал, поспал, погулял в парке, короче норм такой день
     * Задача, 08.03.2023, USRT-123, 2ч20м, Созвон с Виктором по поводу задачи Вывод доработок на прод
     * Задача, USRT-123, 1ч, Созвон с Виктором по поводу задачи Вывод доработок на прод
     */

    UNKNOWN(1, ""),
    LOG(2, "(^)Лог, (.*)"),
    COMMAND(3, "(^)/(.*)");
//    SCHEDULED_TASK_START(3, "(^)Напоминание, (.*), \\d\\d:\\d\\d, (.*)"),
//    SCHEDULED_TASK_LIST(4, "(^)Список заданий по расписанию(\\Z)"),
//    SCHEDULED_TASK_STOP(5, "(^)Останови напоминание (\\d*)\\Z");

    private final Integer code;
    private final String pattern;

    MessageTypeEnum(Integer code, String pattern) {
        this.code = code;
        this.pattern = pattern;
    }

    public Integer getCode() {
        return code;
    }

    public String getPattern() {
        return pattern;
    }
}
