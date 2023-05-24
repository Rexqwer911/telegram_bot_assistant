package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.ScheduledTask;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.enums.MessageTypeEnum;
import com.rexqwer.telegrambotassistant.enums.ScheduledTaskTypeEnum;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.MessageTypeRepository;
import com.rexqwer.telegrambotassistant.repository.ScheduledTaskRepository;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ScheduledTaskRepository scheduledTaskRepository;
    private final MessageService messageService;
    private final MessageTypeRepository messageTypeRepository;

    /**
     * Шедулер для получения заданий и запуска их выполнения
     */
    @Scheduled(cron="0/10 * * * * *")
    public void fetchTasks() {
        List<ScheduledTask> tasks = scheduledTaskRepository.findAllByNextStartTimeIsLessThanEqualAndActiveTrue(LocalDateTime.now());
        tasks.forEach(scheduledTask -> scheduledTask.setActive(false));
        scheduledTaskRepository.saveAll(tasks).forEach(scheduledTask -> Mono.just(scheduledTask).subscribe(this::processScheduledTask));
    }

    private void processScheduledTask(ScheduledTask scheduledTask) {
        Integer taskTypeCode = scheduledTask.getScheduledTaskType().getCode();
        boolean sendMessage = true;
        String text;
        if (taskTypeCode.equals(ScheduledTaskTypeEnum.DAILY.getCode()) ||
                taskTypeCode.equals(ScheduledTaskTypeEnum.WEEKLY.getCode()) ||
                taskTypeCode.equals(ScheduledTaskTypeEnum.WEEKDAYS.getCode())) {

            scheduledTask.setNextStartTime(getNextStartTime(scheduledTask, scheduledTask.getInsistent()));
            scheduledTask.setActive(true);
            scheduledTaskRepository.save(scheduledTask);

            text = scheduledTask.getMessage().getText();
        } else if (taskTypeCode.equals(ScheduledTaskTypeEnum.WEEKDAYS_LOG.getCode())) {

            scheduledTask.setNextStartTime(getNextStartTime(scheduledTask, scheduledTask.getInsistent()));
            scheduledTask.setActive(true);
            scheduledTaskRepository.save(scheduledTask);

            List<Message> todayRecords = messageService.findAllLogsOnToday();
            if (todayRecords.size() == 0) {
                text = "Привет! Не увидел сегодня записей о том че ты сделал по работе :(\nМожет напишешь? Мне много не надо...";
            } else {
                if (ChronoUnit.HOURS.between(todayRecords.stream().findFirst().get().getCreatedAt(), LocalDateTime.now()) > 2) {
                    text = "Привет! Ты мне конечно писал сегодня, но, я подумал, может, тебе есть, что добавить...";
                } else {
                    text = "Привет! Ты красавчик, всё заполнил, но, если есть желание, можешь еще дополнить.";
                }
            }
        } else {
            text = "Не опознан тип задания по расписанию для отправки сообщения";
            log.error(text);
            sendMessage = false;
        }
        if (sendMessage) {
            Message message = messageService.createMessage(
                    messageService.findOrCreateBotUser(),
                    messageTypeRepository.findByCode(MessageTypeEnum.TELEGRAM_RESPONSE.getCode()),
                    text,
                    scheduledTask
            );
            if (scheduledTask.getInsistent()) {
                applicationEventPublisher.publishEvent(new SendMessageEvent(text,
                        scheduledTask.getMessage().getChatId(),
                        InlineKeyboardMarkup.builder()
                                .keyboard(Collections.singleton(
                                        Collections.singletonList(InlineKeyboardButton.builder()
                                                .text("Выполнено")
                                                .callbackData("completedInsist")
                                                .build())))
                                .build(),
                        message.getId()));
            } else {
                applicationEventPublisher.publishEvent(new SendMessageEvent(text,
                        scheduledTask.getMessage().getChatId()));
            }
        }
    }

    public Optional<ScheduledTask> findById(Long id) {
        return scheduledTaskRepository.findById(id);
    }

    public ScheduledTask saveTask(ScheduledTask scheduledTask) {
        return scheduledTaskRepository.save(scheduledTask);
    }

    public List<ScheduledTask> findActiveTasks(User user) {
        return scheduledTaskRepository.findAllByUserAndActiveTrue(user);
    }

    public void processInsistCompleted(String tgIdMessage) {
        Optional<Message> byTgId = messageService.findByTgId(tgIdMessage);
        if (byTgId.isPresent()) {
            ScheduledTask scheduledTask = byTgId.get().getScheduledTask();
            if (scheduledTask != null) {
                scheduledTask.setNextStartTime(getNextStartTime(scheduledTask, false));
                scheduledTaskRepository.save(scheduledTask);
                log.info("Получили подтверждение выполнения напоминания");
            } else {
                log.error("Для сообщения с tgId {} не найдено напоминание", tgIdMessage);
            }
        } else {
            log.error("Не найдено сообщение с tgId {}", tgIdMessage);
        }
    }

    private LocalDateTime getNextStartTime(ScheduledTask scheduledTask, boolean insistently) {
        String cronPattern;
        if (insistently) {
            cronPattern = "* 0/30 * * * *";
        } else {
            cronPattern = scheduledTask.getCronPattern();
        }
        CronExpression cronExpression = CronExpression.parse(cronPattern);
        return cronExpression.next(LocalDateTime.now());
    }
}
