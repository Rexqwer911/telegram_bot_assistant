package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.config.ApplicationProperties;
import com.rexqwer.telegrambotassistant.event.InsistCompletedEvent;
import com.rexqwer.telegrambotassistant.event.completion.IncomingMessageEvent;
import com.rexqwer.telegrambotassistant.event.completion.OutgoingMessageEvent;
import com.rexqwer.telegrambotassistant.event.deletion.MessageDeletedEvent;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;


@Component
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class TelegramBotComponent extends TelegramLongPollingBot {

    private final ApplicationProperties applicationProperties;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final String botToken;

    public TelegramBotComponent(ApplicationProperties applicationProperties,
                                ApplicationEventPublisher applicationEventPublisher) {
        super(applicationProperties.getTelegram().getToken());
        this.applicationProperties = applicationProperties;
        this.applicationEventPublisher = applicationEventPublisher;
        this.botToken = applicationProperties.getTelegram().getToken();
    }

    @Override
    public String getBotUsername() {
        return applicationProperties.getTelegram().getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String callbackData = callbackQuery.getData();
            Integer messageId = callbackQuery.getMessage().getMessageId();
            if (callbackData.equals("completedInsist")) {
                applicationEventPublisher.publishEvent(new InsistCompletedEvent(messageId.toString()));
            }
            AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
            answerCallbackQuery.setCallbackQueryId(callbackQuery.getId());
            try {
                execute(answerCallbackQuery);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            applicationEventPublisher.publishEvent(new IncomingMessageEvent(update));
        }
    }

    public void sendMessage(String text, String chatId, ReplyKeyboard replyKeyboard, Long messageId) {
        try {
            SendMessage response = new SendMessage();
            response.setText(text);
            response.setChatId(chatId);
            if (replyKeyboard != null) {
                response.setReplyMarkup(replyKeyboard);
            }
            Message execute = execute(response);
            applicationEventPublisher.publishEvent(new OutgoingMessageEvent(execute, messageId));
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения ботом. Message: {}, chatId: {}, replyMarkup: {}", text, chatId, replyKeyboard, e);
        }
    }

    public void sendMessage(String text, String chatId, ReplyKeyboard replyKeyboard) {
        try {
            SendMessage response = new SendMessage();
            response.setText(text);
            response.setChatId(chatId);
            if (replyKeyboard != null) {
                response.setReplyMarkup(replyKeyboard);
            }
            Message execute = execute(response);
            applicationEventPublisher.publishEvent(new OutgoingMessageEvent(execute));
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения ботом. Message: {}, chatId: {}, replyMarkup: {}", text, chatId, replyKeyboard, e);
        }
    }

    public void deleteMessage(Long pgMessageId, String chatId, String messageId) {
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
    public String getFileUrl(String fileId) {
        try {
            return execute(GetFile.builder().fileId(fileId).build()).getFileUrl(botToken);
        } catch (Exception e) {
            log.error("Ошибка при получении url файла", e);
            return null;
        }
    }
}
