package com.rexqwer.telegrambotassistant.service.branch;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.domain.reference.MessageBranchType;
import com.rexqwer.telegrambotassistant.enums.MessageBranchTypeEnum;
import com.rexqwer.telegrambotassistant.repository.MessageBranchTypeRepository;
import com.rexqwer.telegrambotassistant.service.branch.scheduled.ScheduledMessageBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageBranchSelector {

    private final ScheduledMessageBranchService scheduledMessageBranchService;
    private final MessageBranchTypeRepository messageBranchTypeRepository;
    private MessageBranchType undefinedType;

    @PostConstruct
    public void init() {
        undefinedType = messageBranchTypeRepository.findByCode(MessageBranchTypeEnum.UNDEFINED.getCode());
    }

    public void process(Message message) {


        MessageBranch messageBranch = scheduledMessageBranchService.processMessageBranch(message);

        Message updatedMessage = messageBranch.getMessages().stream()
                .filter(m -> m.getId().equals(message.getId()))
                .findFirst()
                .orElseThrow();

//        if (messageBranch.getMessages().size() == 1) {
//            if (messageBranch.getMessageBranchType() == null) {
//                messageBranch.setMessageBranchType(undefinedType);
//            }
//        } else {
//
//        }
    }
}
