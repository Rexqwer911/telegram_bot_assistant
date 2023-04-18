package com.rexqwer.telegrambotassistant.service.message;

import com.rexqwer.telegrambotassistant.repository.MessageResponseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageResponseService {

    private final MessageResponseRepository messageResponseRepository;

    public void processOutgoingMessageResponse(Message tgMessage) {
        log.info("outgoing");
    }
}
