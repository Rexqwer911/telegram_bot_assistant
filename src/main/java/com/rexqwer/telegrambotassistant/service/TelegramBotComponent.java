package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.config.ApplicationProperties;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.event.completion.IncomingMessageEvent;
import com.rexqwer.telegrambotassistant.event.completion.OutgoingMessageEvent;
import com.rexqwer.telegrambotassistant.event.deletion.MessageDeletedEvent;
import com.rexqwer.telegrambotassistant.event.deletion.SendMessageDeletionEvent;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;


@Component
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class TelegramBotComponent extends TelegramLongPollingBot {

    private final ApplicationProperties applicationProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String botToken;

    @Override
    public String getBotUsername() {
        return applicationProperties.getTelegram().getUsername();
    }

    public TelegramBotComponent(ApplicationProperties applicationProperties,
                                ApplicationEventPublisher applicationEventPublisher) {
        super(applicationProperties.getTelegram().getToken());
        this.applicationProperties = applicationProperties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.botToken = applicationProperties.getTelegram().getToken();
    }

    @EventListener
    public void handleTelegramBotSendMessageEvent(SendMessageEvent event) {
        sendMessage(event.getMessage(), event.getChatId(), event.getReplyKeyboard());
    }

    private void sendMessage(String message, String chatId, ReplyKeyboard replyKeyboard) {
        try {
            SendMessage response = new SendMessage();
            response.setText(message);
            response.setChatId(chatId);
            if (replyKeyboard != null) {
                response.setReplyMarkup(replyKeyboard);
            }
            Message execute = execute(response);
            applicationEventPublisher.publishEvent(new OutgoingMessageEvent(execute));
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения ботом. Message: {}, chatId: {}, replyMarkup: {}", message, chatId, replyKeyboard, e);
        }
    }


    @EventListener
    public void handleTelegramBotSendMessageDeletionEventEvent(SendMessageDeletionEvent event) {
        deleteMessage(event.getPgMessageId(), event.getChatId(), event.getMessageId());
    }

    private void deleteMessage(Long pgMessageId, String chatId, String messageId) {
        try {
            DeleteMessage deleteMessage = new DeleteMessage();
            deleteMessage.setChatId(chatId);
            deleteMessage.setMessageId(Integer.parseInt(messageId));
            Boolean success = execute(deleteMessage);
            if (success) {
                applicationEventPublisher.publishEvent(new MessageDeletedEvent(pgMessageId));
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении сообщения ботом. chatId: {}, messageId: {}", chatId, messageId, e);
        }
    }



    @Override
    public void onUpdateReceived(Update update) {
        Mono.just(update).subscribe(u -> applicationEventPublisher.publishEvent(new IncomingMessageEvent(u)));
    }
}
