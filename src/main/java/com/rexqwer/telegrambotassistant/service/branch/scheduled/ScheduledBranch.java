package com.rexqwer.telegrambotassistant.service.branch.scheduled;

import com.rexqwer.telegrambotassistant.domain.User;

public interface ScheduledBranch {

    void registerNewScheduledTask(String text, String chatId, User user);

    void deactivateScheduledTask(String text, User user, String chatId);

    void sendActiveScheduledTasksToUser(User user, String chatId);
}
