package com.rexqwer.telegrambotassistant.event;

import com.rexqwer.telegrambotassistant.config.ApplicationProperties;
import com.rexqwer.telegrambotassistant.event.completion.IncomingMessageEvent;
import com.rexqwer.telegrambotassistant.event.completion.OutgoingMessageEvent;
import com.rexqwer.telegrambotassistant.event.deletion.SendMessageDeletionEvent;
import com.rexqwer.telegrambotassistant.service.MessageProcessingService;
import com.rexqwer.telegrambotassistant.service.ScheduledService;
import com.rexqwer.telegrambotassistant.service.TelegramBotComponent;
import com.rexqwer.telegrambotassistant.service.VoiceMessageService;
import com.rexqwer.telegrambotassistant.service.message.MessageResponseService;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationEventListener {

    private final ApplicationProperties applicationProperties;
    private final MessageProcessingService messageProcessingService;
    private final MessageResponseService messageResponseService;
    private final TelegramBotComponent telegramBotComponent;
    private final VoiceMessageService voiceMessageService;
    private final ScheduledService scheduledService;

    @Async
    @EventListener
    public void handleIncomingMessageEvent(IncomingMessageEvent event) {
        messageProcessingService.processMessage(event.getUpdate());
    }

    @Async
    @EventListener
    public void handleInsistCompletedEvent(InsistCompletedEvent event) {
        scheduledService.processInsistCompleted(event.getTgIdMessage());
    }

    @EventListener
    public void handleTelegramBotSendMessageEvent(SendMessageEvent event) {
        if (event.getMessageId() != null) {
            telegramBotComponent.sendMessage(event.getMessage(), event.getChatId(), event.getReplyKeyboard(), event.getMessageId());
        } else {
            telegramBotComponent.sendMessage(event.getMessage(), event.getChatId(), event.getReplyKeyboard());
        }
    }

    @EventListener
    public void handleTelegramBotSendMessageDeletionEvent(SendMessageDeletionEvent event) {
        telegramBotComponent.deleteMessage(event.getPgMessageId(), event.getChatId(), event.getMessageId());
    }

    @Async
    @EventListener
    public void handleOutgoingMessageCompletedEvent(OutgoingMessageEvent event) {
        if (event.getMessageId() != null) {
            messageResponseService.processOutgoingMessageResponse(event.getMessage(), event.getMessageId());
        } else {
            messageResponseService.processOutgoingMessageResponse(event.getMessage());
        }
    }

    @Async
    @EventListener
    public void handleDownloadVoiceMessageEvent(TelegramBotDownloadVoiceMessageEvent event) {
        String fileUrl = telegramBotComponent.getFileUrl(event.getFileId());
        String oggFileName = applicationProperties.getTelegram().getVoice().getDownloadPath() + event.getFileName();
        String wavFileName = applicationProperties.getTelegram().getVoice().getWavPath() + event.getFileName();
        try {
            voiceMessageService.downloadFile(fileUrl, oggFileName + ".ogg");
            voiceMessageService.convertWav(oggFileName + ".ogg", wavFileName + ".wav");
        } catch (IOException e) {
            log.error("Error downloading file: " + event.getFileName(), e);
        }
    }
}

