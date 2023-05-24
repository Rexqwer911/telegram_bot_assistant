package com.rexqwer.telegrambotassistant.service.branch.stereotype;

import com.rexqwer.telegrambotassistant.domain.Message;

public interface Reminder {

    void remindList(Message message);

    void deactivateReminds(Message message);
}
