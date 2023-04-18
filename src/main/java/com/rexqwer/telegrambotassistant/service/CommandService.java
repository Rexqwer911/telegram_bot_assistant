package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommandService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void processCommand(Message message) {

        switch (message.getText()) {
            case "/start" -> {
                //Создаем объект будущей клавиатуры и выставляем нужные настройки
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                replyKeyboardMarkup.setOneTimeKeyboard(true); //скрываем после использования


                //Создаем список с рядами кнопок
                ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
                //Создаем один ряд кнопок и добавляем его в список
                KeyboardRow keyboardRow = new KeyboardRow();
                keyboardRows.add(keyboardRow);
                //Добавляем одну кнопку с текстом "Просвяти" наш ряд
                keyboardRow.add(new KeyboardButton("Помощь"));
                //добавляем лист с одним рядом кнопок в главный объект
                replyKeyboardMarkup.setKeyboard(keyboardRows);
                applicationEventPublisher.publishEvent(new SendMessageEvent("Вечер в хату, \nЧасик в радость, \nЧифирок в сладость", message.getChatId(), replyKeyboardMarkup));
            }
            case "/help" -> {
                //Создаем объект будущей клавиатуры и выставляем нужные настройки
                ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
                replyKeyboardMarkup.setResizeKeyboard(true); //подгоняем размер

                replyKeyboardMarkup.setOneTimeKeyboard(true); //скрываем после использования


                //Создаем список с рядами кнопок
                ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
                //Создаем один ряд кнопок и добавляем его в список
                KeyboardRow keyboardRow = new KeyboardRow();
                keyboardRows.add(keyboardRow);
                //Добавляем одну кнопку с текстом "Просвяти" наш ряд
                keyboardRow.add(new KeyboardButton("Помощь2"));
                //добавляем лист с одним рядом кнопок в главный объект
                replyKeyboardMarkup.setKeyboard(keyboardRows);
                applicationEventPublisher.publishEvent(new SendMessageEvent(
                        "Короче, Меченый, я тебя спас " +
                                "и в благородство играть не буду: выполнишь для меня пару заданий — и мы в расчете. " +
                                "Заодно посмотрим, как быстро у тебя башка после амнезии прояснится. " +
                                "А по твоей теме постараюсь разузнать. Хрен его знает, на кой ляд тебе этот Стрелок сдался, " +
                                "но я в чужие дела не лезу, хочешь убить, значит есть за что...",
                        message.getChatId(), replyKeyboardMarkup));
            }
            default ->
                    applicationEventPublisher.publishEvent(new SendMessageEvent("Какая-то странная команда, ничё не понял", message.getChatId()));
        }
    }
}
