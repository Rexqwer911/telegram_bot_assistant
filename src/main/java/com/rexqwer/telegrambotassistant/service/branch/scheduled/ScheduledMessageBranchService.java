package com.rexqwer.telegrambotassistant.service.branch.scheduled;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.ScheduledTask;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.enums.ScheduledTaskTypeEnum;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.MessageBranchRepository;
import com.rexqwer.telegrambotassistant.repository.ScheduledTaskTypeRepository;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import com.rexqwer.telegrambotassistant.service.ScheduledService;
import com.rexqwer.telegrambotassistant.service.UserService;
import com.rexqwer.telegrambotassistant.service.branch.MessageBranchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ScheduledMessageBranchService extends MessageBranchService implements ScheduledBranch {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ScheduledService scheduledService;
    private final MessageService messageService;
    private final ScheduledTaskTypeRepository scheduledTaskTypeRepository;
    private final UserService userService;

    public ScheduledMessageBranchService(ApplicationEventPublisher applicationEventPublisher, MessageBranchRepository messageBranchRepository, ApplicationEventPublisher applicationEventPublisher1, ScheduledService scheduledService, MessageService messageService, ScheduledTaskTypeRepository scheduledTaskTypeRepository, UserService userService) {
        super(applicationEventPublisher, messageBranchRepository, messageService);
        this.applicationEventPublisher = applicationEventPublisher1;
        this.scheduledService = scheduledService;
        this.messageService = messageService;
        this.scheduledTaskTypeRepository = scheduledTaskTypeRepository;
        this.userService = userService;
    }

    @Override
    public void registerNewScheduledTask(String text, String chatId, User user) {
        text = text.substring(13);
        String callback;
        if (text.startsWith(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_DAILY.getPeriodicity())) {
            try {
                text = text.substring(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_DAILY.getPeriodicity().length() + 2);
                String cronPattern = getCronPatternEveryDay(text);
                text = text.substring(7);
                Long taskId = createNewScheduledTask(text, chatId, user, ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_DAILY, cronPattern);
                callback = "Задание " + taskId + " создано и запущено выполнение по расписанию";
            } catch (Exception e) {
                callback = "Братан, я сломался, ты мне чет не то прислал :(";
            }
        } else if (text.startsWith(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG.getPeriodicity())) {
            try {
                text = text.substring(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG.getPeriodicity().length() + 2);
                String cronPattern = getCronPatternWeekDays(text);
                text = text.substring(7);
                Long taskId = createNewScheduledTask(text, chatId, user, ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG, cronPattern);
                callback = "Задание по будням (проверка логов) " + taskId + " создано и запущено выполнение по расписанию";
            } catch (Exception e) {
                callback = "Братан, я сломался, ты мне чет не то прислал :(";
            }
        } else {
            ScheduledTaskTypeEnum[] values = ScheduledTaskTypeEnum.values();
            List<String> periodicities = new ArrayList<>();
            for (ScheduledTaskTypeEnum scheduledTaskTypeEnum : values) {
                periodicities.add(scheduledTaskTypeEnum.getPeriodicity());
            }
            callback = "Не опознан тип задания по расписанию. Доступные значения: " + periodicities.toString();
        }
        applicationEventPublisher.publishEvent(new SendMessageEvent(callback, chatId));
    }

    @Override
    public void deactivateScheduledTask(String text, User user, String chatId) {
        long taskId;
        try {
            taskId = Long.parseLong(text.substring(21));
        } catch (Exception e) {
            applicationEventPublisher.publishEvent(new SendMessageEvent("Чет не могу распарсить номер задания, проверь что ты мне норм данные дал пж", chatId));
            return;
        }
        Optional<ScheduledTask> byId = scheduledService.findById(taskId);
        if (byId.isPresent()) {
            ScheduledTask scheduledTask = byId.get();
            if (scheduledTask.getUser().getId().equals(user.getId()) || user.getRoles().contains(userService.getAdminRole())) {
                scheduledTask.setActive(false);
                scheduledService.saveTask(scheduledTask);
                applicationEventPublisher.publishEvent(new SendMessageEvent("Задание " + taskId + " отключено.", chatId));
            } else {
                applicationEventPublisher.publishEvent(new SendMessageEvent("Вы не можете отключить выполнение этого задания, оно не ваше.", chatId));
            }
        } else {
            applicationEventPublisher.publishEvent(new SendMessageEvent("Задание " + taskId + " отсутствует.", chatId));
        }
    }

    @Override
    public void sendActiveScheduledTasksToUser(User user, String chatId) {
        applicationEventPublisher.publishEvent(new SendMessageEvent("Ваши активные задания: " + scheduledService.findActiveTasks(user.getId()), chatId));
    }



    private String getCronPatternEveryDay(String text) {
        String[] parts = text.split(":");
        String hours = parts[0];
        String minutes = parts[1].substring(0, 2);
        return "0 " + minutes + " " + hours + " * * *";
    }

    private String getCronPatternWeekDays(String text) {
        String[] parts = text.split(":");
        String hours = parts[0];
        String minutes = parts[1].substring(0, 2);
        return "0 " + minutes + " " + hours + " * * MON-FRI";
    }

    private Long createNewScheduledTask(String text, String chatId, User user, ScheduledTaskTypeEnum scheduledTaskTypeEnum, String cronPattern) {

        CronExpression cronExpression = CronExpression.parse(cronPattern);
        LocalDateTime nextStartTime = cronExpression.next(LocalDateTime.now());

        Message message = new Message();
        message.setCreatedAt(LocalDateTime.now());
        message.setChatId(chatId);
        message.setText(text);
        message = messageService.saveMessage(message);


        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setActive(true);
        scheduledTask.setScheduledTaskType(scheduledTaskTypeRepository.findByCode(scheduledTaskTypeEnum.getCode()));
        scheduledTask.setUser(user);
        scheduledTask.setCronPattern(cronPattern);
        scheduledTask.setMessage(message);
        scheduledTask.setNextStartTime(nextStartTime);
        return scheduledService.saveTask(scheduledTask).getId();
    }
}
