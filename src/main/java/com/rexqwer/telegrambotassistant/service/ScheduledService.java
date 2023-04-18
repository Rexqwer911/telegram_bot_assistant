package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.ScheduledTask;
import com.rexqwer.telegrambotassistant.enums.ScheduledTaskTypeEnum;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.ScheduledTaskRepository;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScheduledService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ScheduledTaskRepository scheduledTaskRepository;

    private final MessageService messageService;

    /**
     * Шедулер для получения заданий и запуска их выполнения
     */
    @Scheduled(cron="0/10 * * * * *")
    public void fetchTasks() {
        List<ScheduledTask> tasks = scheduledTaskRepository.findAllByNextStartTimeIsLessThanEqualAndActiveTrue(LocalDateTime.now());
        tasks.forEach(scheduledTask -> scheduledTask.setActive(false));
        scheduledTaskRepository.saveAll(tasks).forEach(scheduledTask -> Mono.just(scheduledTask).subscribe(this::processScheduledTask));
    }

//    @Scheduled(cron="0/10 * * * * *")
//    public void testSend() {
//        applicationEventPublisher.publishEvent(new SendMessageEvent("тест", "846152189"));
//    }

    private void processScheduledTask(ScheduledTask scheduledTask) {
        if (scheduledTask.getScheduledTaskType().getCode().equals(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_DAILY.getCode())) {

            scheduledTask.setNextStartTime(getNextStartTime(scheduledTask.getCronPattern()));
            scheduledTask.setActive(true);
            scheduledTaskRepository.save(scheduledTask);

            applicationEventPublisher.publishEvent(new SendMessageEvent(scheduledTask.getMessage().getText(), scheduledTask.getMessage().getChatId()));
        } else if (scheduledTask.getScheduledTaskType().getCode().equals(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG.getCode())) {

            scheduledTask.setNextStartTime(getNextStartTime(scheduledTask.getCronPattern()));
            scheduledTask.setActive(true);
            scheduledTaskRepository.save(scheduledTask);

            List<Message> todayRecords = messageService.findAllLogsOnToday();

            if (todayRecords.size() == 0) {
                applicationEventPublisher.publishEvent(new SendMessageEvent("Привет! Не увидел сегодня записей о том че ты сделал по работе :(\nМожет напишешь? Мне много не надо...", scheduledTask.getMessage().getChatId()));
            } else {
                if (ChronoUnit.HOURS.between(todayRecords.stream().findFirst().get().getCreatedAt(), LocalDateTime.now()) > 2) {
                    applicationEventPublisher.publishEvent(new SendMessageEvent("Привет! Ты мне конечно писал сегодня, но, я подумал, может, тебе есть, что добавить...", scheduledTask.getMessage().getChatId()));
                } else {
                    applicationEventPublisher.publishEvent(new SendMessageEvent("Привет! Ты красавчик, всё заполнил, но, если есть желание, можешь еще дополнить.", scheduledTask.getMessage().getChatId()));
                }
            }
        }
    }

    public Optional<ScheduledTask> findById(Long id) {
        return scheduledTaskRepository.findById(id);
    }

    public ScheduledTask saveTask(ScheduledTask scheduledTask) {
        return scheduledTaskRepository.save(scheduledTask);
    }

    public List<Long> findActiveTasks(Long userId) {
        return scheduledTaskRepository.selectActiveTaskIdsForUser(userId);
    }

    private LocalDateTime getNextStartTime(String cronPattern) {
        CronExpression cronExpression = CronExpression.parse(cronPattern);
        return cronExpression.next(LocalDateTime.now());
    }
}
