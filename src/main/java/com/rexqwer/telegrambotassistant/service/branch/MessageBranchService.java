package com.rexqwer.telegrambotassistant.service.branch;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.MessageBranchRepository;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Slf4j
public class MessageBranchService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageBranchRepository messageBranchRepository;
    private final MessageService messageService;

    public MessageBranchService(ApplicationEventPublisher applicationEventPublisher, MessageBranchRepository messageBranchRepository, MessageService messageService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.messageBranchRepository = messageBranchRepository;
        this.messageService = messageService;
    }

    public MessageBranch processMessageBranch(Message message) {
        MessageBranch activeBranch = findActiveBranch(message);
        activeBranch.setClosed(false);
        activeBranch.setCreatedAt(message.getCreatedAt());
        activeBranch = messageBranchRepository.save(activeBranch);

        message.setMessageBranch(activeBranch);
        message = messageService.saveMessage(message);

        List<Message> messages = activeBranch.getMessages();
        messages.add(message);
        return messageBranchRepository.save(activeBranch);
    }

    private MessageBranch findActiveBranch(Message message) {
        List<MessageBranch> activeBranches = messageBranchRepository.findAllByClosedFalse();
        int activeBranchesCount = activeBranches.size();
        if (activeBranchesCount == 0) {
            return new MessageBranch();
        } else if (activeBranchesCount == 1) {
            return activeBranches.get(0);
        } else {
            applicationEventPublisher.publishEvent(new SendMessageEvent("Обнаружено более одной ветки сообщений. " +
                    "Они будут закрыты, создана новая ветка.", message.getChatId()));
            Mono.just(activeBranches).subscribe(this::deleteBranches);
            return new MessageBranch();
        }
    }

    private void deleteBranches(List<MessageBranch> branches) {
        messageBranchRepository.deleteAll(branches);
    }
}
