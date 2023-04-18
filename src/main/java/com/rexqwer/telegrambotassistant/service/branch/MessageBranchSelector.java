package com.rexqwer.telegrambotassistant.service.branch;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.service.branch.scheduled.ScheduledMessageBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageBranchSelector {

    private final ScheduledMessageBranchService scheduledMessageBranchService;

    public void process(Message message) {

        MessageBranch messageBranch = scheduledMessageBranchService.processMessageBranch(message);

        log.debug("messagebranch");

    }
}
