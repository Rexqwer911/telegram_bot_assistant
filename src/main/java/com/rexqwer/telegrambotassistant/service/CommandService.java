package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.service.branch.ChatGPTMessageBranchService;
import com.rexqwer.telegrambotassistant.service.branch.ReminderBranchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ReminderBranchService reminderBranchService;
    private final ChatGPTMessageBranchService chatGPTMessageBranchService;

    public void processCommand(Message message) {

        switch (message.getText()) {
            case "/start" -> {
                applicationEventPublisher.publishEvent(new SendMessageEvent(
            "Вечер в хату, \nЧасик в радость, \nЧифирок в сладость",
                        message.getChatId(),
                        ReplyKeyboardRemove.builder().removeKeyboard(true).build()));
            }
            case "/help" -> {
                applicationEventPublisher.publishEvent(new SendMessageEvent(
                        "Короче, Меченый, я тебя спас " +
                                "и в благородство играть не буду: выполнишь для меня пару заданий — и мы в расчете. " +
                                "Заодно посмотрим, как быстро у тебя башка после амнезии прояснится. " +
                                "А по твоей теме постараюсь разузнать. Хрен его знает, на кой ляд тебе этот Стрелок сдался, " +
                                "но я в чужие дела не лезу, хочешь убить, значит есть за что...",
                        message.getChatId(), InlineKeyboardMarkup.builder()
                        .keyboard(Collections.singleton(
                                Collections.singletonList(InlineKeyboardButton.builder().text("Ага").callbackData("aga")
                                        .build())))
                        .build()));
            }
            case "/reminder" -> reminderBranchService.processNewBranch(message);
            case "/gpt" -> chatGPTMessageBranchService.processNewGptMessageBranch(message);
            default -> applicationEventPublisher.publishEvent(
                    new SendMessageEvent("Какая-то странная команда, ничё не понял", message.getChatId()));
        }
    }
}
