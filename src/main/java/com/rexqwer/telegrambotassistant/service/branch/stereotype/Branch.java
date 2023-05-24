package com.rexqwer.telegrambotassistant.service.branch.stereotype;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;

public interface Branch {

    void processNewBranch(Message message);
    void processNewMessage(MessageBranch messageBranch);
    void branchSource(Message message);

    void closeBranch(Message message);
}
