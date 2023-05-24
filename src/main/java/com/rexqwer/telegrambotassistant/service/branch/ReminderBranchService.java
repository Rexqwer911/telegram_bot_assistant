package com.rexqwer.telegrambotassistant.service.branch;

import com.rexqwer.telegrambotassistant.domain.Message;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.domain.ScheduledTask;
import com.rexqwer.telegrambotassistant.enums.MessageBranchTypeEnum;
import com.rexqwer.telegrambotassistant.enums.ScheduledTaskTypeEnum;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.repository.MessageBranchTypeRepository;
import com.rexqwer.telegrambotassistant.repository.ScheduledTaskTypeRepository;
import com.rexqwer.telegrambotassistant.service.ScheduledService;
import com.rexqwer.telegrambotassistant.service.UserService;
import com.rexqwer.telegrambotassistant.service.branch.stereotype.Branch;
import com.rexqwer.telegrambotassistant.service.branch.stereotype.Reminder;
import com.rexqwer.telegrambotassistant.service.dialog.DialogService;
import com.rexqwer.telegrambotassistant.service.dialog.DialogStructure;
import com.rexqwer.telegrambotassistant.service.dialog.DialogTreeBranch;
import com.rexqwer.telegrambotassistant.service.message.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import javax.swing.tree.DefaultMutableTreeNode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReminderBranchService implements Branch, Reminder {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final MessageBranchTypeRepository messageBranchTypeRepository;
    private final MessageService messageService;
    private final MessageBranchService messageBranchService;
    private final ScheduledService scheduledService;
    private final UserService userService;
    private final DialogStructure reminderDialogStructure;
    private final DialogService dialogService;
    private final ScheduledTaskTypeRepository scheduledTaskTypeRepository;

    @Override
    public void processNewBranch(Message message) {
        branchSource(
                messageService.assignMessageBranch(
                        message,
                        messageBranchService.processNewMessageBranch(
                                message,
                                messageBranchTypeRepository.findByCode(
                                        MessageBranchTypeEnum.REMINDER_BRANCH.getCode()))));
    }

    @Override
    public void processNewMessage(MessageBranch messageBranch) {

        Message lastMessage = messageBranch.getMessageByIdx(messageBranch.getLastMessageIdx());
        if (lastMessage.getText().equals("Отмена")) {
            try {
                executeMethod(reminderDialogStructure.getDefaultCommand(),
                        this.getClass(),
                        lastMessage);
            } catch (Exception e) {
                log.error("Не удалось выполнить дефолтную команду возврата", e);
            }
        } else {
            DefaultMutableTreeNode node = dialogService.findNode(reminderDialogStructure.getTree(), messageBranch);
            if (node != null) {
                DialogTreeBranch currentDialogTreeBranch = (DialogTreeBranch) node.getUserObject();

                if (currentDialogTreeBranch.getValidator() != null) {
                    //validate
                    // если валидация сфейлилась, делаем return
                }

                if (currentDialogTreeBranch.getReplyText() != null) {
                    applicationEventPublisher.publishEvent(new SendMessageEvent(
                            currentDialogTreeBranch.getReplyText(),
                            messageBranch.getChatId(),
                            ReplyKeyboardMarkup.builder()
                                    .resizeKeyboard(true)
                                    .keyboard(currentDialogTreeBranch.getButtons()
                                            .stream()
                                            .map(s -> new KeyboardRow(Collections.singletonList(new KeyboardButton(s))))
                                            .toList())
                                    .build()));
                }

                if (!currentDialogTreeBranch.getMethods().isEmpty()) {
                    int executedMethods = 0;
                    for(String methodName : currentDialogTreeBranch.getMethods()) {
                        try {
                            executeMethod(methodName,
                                    this.getClass(),
                                    messageBranch.getMessages().get(messageBranch.getLastMessageIdx()));
                            executedMethods = executedMethods + 1;
                        } catch (Exception e) {
                            log.error("Ошибка при выполнении метода {}", methodName, e);
                            break;
                        }
                    }
                    if (currentDialogTreeBranch.getMethods().size() != executedMethods) {
                        log.error("Не выполнены все методы пайплайна!!!");
                        applicationEventPublisher.publishEvent(new SendMessageEvent(
                                "Не выполнены все методы пайплайна!!!",
                                messageBranch.getChatId(),
                                ReplyKeyboardRemove.builder().removeKeyboard(true).build()));
                        try {
                            executeMethod(reminderDialogStructure.getDefaultCommand(),
                                    this.getClass(),
                                    messageBranch.getMessages().get(messageBranch.getLastMessageIdx()));
                        } catch (Exception e) {
                            log.error("Не удалось выполнить дефолтную команду возврата", e);
                        }
                    }
                }
            } else {
                applicationEventPublisher.publishEvent(new SendMessageEvent(
                        reminderDialogStructure.getDefaultText(),
                        messageBranch.getChatId(),
                        ReplyKeyboardRemove.builder().removeKeyboard(true).build()));
                try {
                    executeMethod(reminderDialogStructure.getDefaultCommand(),
                            this.getClass(),
                            messageBranch.getMessages().get(messageBranch.getLastMessageIdx()));
                } catch (Exception e) {
                    log.error("Не удалось выполнить дефолтную команду возврата", e);
                }
            }
        }
    }

    private void executeMethod(String methodName, Class<?> cl, Object parameter) throws Exception {
        try {
            cl.getMethod(methodName, parameter.getClass()).invoke(this, parameter);
        } catch (Exception e) {
            log.error("Не удалось вызвать метод {}", methodName, e);
            throw new Exception("Не удалось выполнить метод " + methodName);
        }
    }

    @Override
    public void branchSource(Message message) {
        ArrayList<String> commands = new ArrayList<>();
        commands.add("Создать новое напоминание");
        commands.add("Список напоминаний");
        commands.add("Отключить напоминание");
        commands.add("Выход");

        ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
                .resizeKeyboard(true)
                .keyboard(commands.stream()
                        .map(s -> new KeyboardRow(Collections.singletonList(new KeyboardButton(s))))
                        .toList())
                .build();

        applicationEventPublisher.publishEvent(new SendMessageEvent(
                "Панель управления напоминаниями",
                message.getChatId(), replyKeyboardMarkup));
    }

    @Override
    public void closeBranch(Message message) {
        messageBranchService.closeMessageBranch(message);
    }

    @Override
    public void remindList(Message message) {
        List<ScheduledTask> activeTasks = scheduledService.findActiveTasks(message.getUser());
        if (activeTasks.isEmpty()) {
            applicationEventPublisher.publishEvent(new SendMessageEvent("У вас пока нет активных напоминаний", message.getChatId()));
        } else {
            activeTasks.stream().map(
                    activeTask -> "Задание "
                            .concat(activeTask.getId().toString())
                            .concat(", тип - ")
                            .concat(activeTask.getScheduledTaskType().getValue())
                            .concat(", текст напоминания - ")
                            .concat(activeTask.getMessage().getText())
            ).forEach(s -> applicationEventPublisher.publishEvent(
                    new SendMessageEvent(s, message.getChatId())));
        }
    }

    @Override
    public void deactivateReminds(Message message) {

        List<Long> tasksIds = parseTasks(message.getText());

        if (tasksIds != null) {
            int deactivateCount = 0;
            for (Long taskId : tasksIds) {
                Optional<ScheduledTask> byId = scheduledService.findById(taskId);
                if (byId.isPresent()) {
                    ScheduledTask scheduledTask = byId.get();
                    if (scheduledTask.getMessage().getUser().getId().equals(message.getUser().getId()) ||
                            message.getUser().getRoles().contains(userService.getAdminRole())) {
                        scheduledTask.setActive(false);
                        scheduledService.saveTask(scheduledTask);
                        applicationEventPublisher.publishEvent(new SendMessageEvent("Задание " + taskId + " отключено.", message.getChatId()));
                        deactivateCount = deactivateCount + 1;
                    } else {
                        applicationEventPublisher.publishEvent(new SendMessageEvent("Вы не можете отключить выполнение задания %s, оно не ваше.", message.getChatId()));
                    }
                } else {
                    applicationEventPublisher.publishEvent(new SendMessageEvent("Задание " + taskId + " отсутствует.", message.getChatId()));
                }
            }
            applicationEventPublisher.publishEvent(new SendMessageEvent("Отключили " + deactivateCount + " заданий.", message.getChatId()));
            branchSource(message);
        } else {
            applicationEventPublisher.publishEvent(new SendMessageEvent(defaultErrorMessage(), message.getChatId()));
            branchSource(message);
        }
    }

    public void createRemind(Message message) throws Exception {

        DefaultMutableTreeNode currentNode = dialogService.findNode(reminderDialogStructure.getTree(), message.getMessageBranch());
        int level = currentNode.getLevel() - 2;

        String scheduleS = message.getMessageBranch().getMessageByIdx(message.getMessageBranch().getLastMessageIdx() - level).getText();

        String textPattern = message.getMessageBranch().getMessageByIdx(message.getMessageBranch().getLastMessageIdx() - 2).getText();
        String cronPattern;
        ScheduledTaskTypeEnum schedule;
        String logMessage = "";
        boolean insistent = message.getText().equals("Да");
        switch (scheduleS) {
            case "По будням" -> {
                if (message.getMessageBranch().getMessageByIdx(
                        message.getMessageBranch().getLastMessageIdx() - 1)
                        .getText().equals("Лог")) {
                    schedule = ScheduledTaskTypeEnum.WEEKDAYS_LOG;
                    logMessage = "(проверка логов)";
                } else {
                    schedule = ScheduledTaskTypeEnum.WEEKDAYS;
                }
                cronPattern = getCronPatternWeekDays(textPattern);
            }
            case "Еженедельно" -> {
                schedule = ScheduledTaskTypeEnum.WEEKLY;
                String day = message.getMessageBranch().getMessageByIdx(message.getMessageBranch().getLastMessageIdx() - level + 1).getText().toLowerCase();
                Map<String, String> days = new HashMap<>();
                days.put("понедельник", "1");
                days.put("вторник", "2");
                days.put("среда", "3");
                days.put("четверг", "4");
                days.put("пятница", "5");
                days.put("суббота", "6");
                days.put("воскресенье", "0");
                String dayPattern = days.get(day);
                Assert.notNull(dayPattern,"Не определен день недели");
                cronPattern = getCronPatternWeekly(textPattern, dayPattern);
            }
            case "Ежедневное" -> {
                schedule = ScheduledTaskTypeEnum.DAILY;
                cronPattern = getCronPatternDaily(textPattern);
            }
            default -> {
                log.error("Не удалось определить период");
                throw new Exception("Не удалось определить период для создания задания");
            }
        }

        Long taskId = createNewScheduledTask(message.getMessageBranch().getMessageByIdx(
                message.getMessageBranch().getLastMessageIdx() - 1),
                schedule, cronPattern, insistent);
        String callback = "Задание " + taskId + " создано и запущено выполнение " + scheduleS.toLowerCase() + " " + logMessage;
        applicationEventPublisher.publishEvent(new SendMessageEvent(callback.trim(), message.getChatId()));
    }

    private List<Long> parseTasks(String input) {

        //Очищаем ввод от мусора
        if (input == null || input.isEmpty()) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (Character.isDigit(ch) || ch == ',') {
                sb.append(ch);
            }
        }

        String text = sb.toString();

        //Формируем из полученной строки List
        List<Long> resultList = new ArrayList<>();
        if (!text.isEmpty()) {
            String[] numberStrings = text.split(",");
            for (String numberString : numberStrings) {
                try {
                    long number = Long.parseLong(numberString);
                    resultList.add(number);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return resultList;
        } else {
            return null;
        }
    }
    private String defaultErrorMessage() {
        return "Не смог распознать команду. Возвращаемся к началу.";
    }
    private String getCronPatternDaily(String text) {
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

    private String getCronPatternWeekly(String text, String weekday) {
        String[] parts = text.split(":");
        String hours = parts[0];
        String minutes = parts[1].substring(0, 2);
        return "0 " + minutes + " " + hours + " * * " + weekday;
    }

    private Long createNewScheduledTask(Message message, ScheduledTaskTypeEnum scheduledTaskTypeEnum, String cronPattern, Boolean insistent) {

        CronExpression cronExpression = CronExpression.parse(cronPattern);
        LocalDateTime nextStartTime = cronExpression.next(LocalDateTime.now());

        ScheduledTask scheduledTask = new ScheduledTask();
        scheduledTask.setActive(true);
        scheduledTask.setScheduledTaskType(scheduledTaskTypeRepository.findByCode(scheduledTaskTypeEnum.getCode()));
        scheduledTask.setUser(message.getUser());
        scheduledTask.setCronPattern(cronPattern);
        scheduledTask.setInsistent(insistent);
        scheduledTask.setMessage(message);
        scheduledTask.setNextStartTime(nextStartTime);
        return scheduledService.saveTask(scheduledTask).getId();
    }
//
//
//
//    public void processNewMessage(MessageBranch messageBranch) {
//
//        List<Message> messages = messageBranch.getMessages();
//
//        Message message = messages.get(messageBranch.getMessages().size() - 1);
//
//        if (message.getText().equals("Создать новое напоминание")) {
//
//            ArrayList<KeyboardRow> keyboardRows = new ArrayList<>();
//
//            KeyboardRow keyboardRow = new KeyboardRow();
//            keyboardRow.add(new KeyboardButton("Ежедневное напоминание"));
//            keyboardRow.add(new KeyboardButton("Напоминание логирования по будням"));
//            keyboardRows.add(keyboardRow);
//
//            keyboardRow = new KeyboardRow();
//            keyboardRow.add(new KeyboardButton("Вернуться"));
//            keyboardRows.add(keyboardRow);
//
//            ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder()
//                    .resizeKeyboard(true)
//                    .oneTimeKeyboard(false)
//                    .keyboard(keyboardRows)
//                    .build();
//
//            applicationEventPublisher.publishEvent(new SendMessageEvent(
//                    "Выберите тип создаваемого напоминания:",
//                    message.getChatId(),
//                    replyKeyboardMarkup));
//        } else if (message.getText().equals("Список напоминаний")) {
//            applicationEventPublisher.publishEvent(new SendMessageEvent(
//                    "Ваши активные задания: " + scheduledService.findActiveTasks(message.getUser().getId()),
//                    message.getChatId()));
//        } else if (message.getText().equals("Отключить напоминание")) {
//
//        } else if (message.getText().equals("Вернуться")) {
//            applicationEventPublisher.publishEvent(new SendMessageEvent(
//                    "Вышли из панели управления заданиями по расписанию",
//                    message.getChatId(),
//                    ReplyKeyboardRemove.builder().removeKeyboard(true).build()));
//            messageBranch.setClosed(true);
//            messageBranchRepository.save(messageBranch);
//        } else {
//            if (messages.size() > 1) {
//                Message prevMessage = messages.get(messages.size() - 2);
//                if (prevMessage.getText().equals("Создать новое напоминание")) {
//                    if (message.getText().equals("Ежедневное напоминание")) {
//                        String cronPattern = getCronPatternEveryDay("21:00");
//                        String text = "Напоминание логов";
//                        Long taskId = createNewScheduledTask(text, message.getChatId(), message.getUser(), ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG, cronPattern);
//                        String callback = "Задание по будням (проверка логов) " + taskId + " создано и запущено выполнение по расписанию";
//                        applicationEventPublisher.publishEvent(new SendMessageEvent(callback, message.getChatId()));
//                    } else if (message.getText().equals("Напоминание логирования по будням")) {
//                        String cronPattern = getCronPatternWeekDays("21:00");
//                        String text = "Напоминание логов";
//                        Long taskId = createNewScheduledTask(text, message.getChatId(), message.getUser(), ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG, cronPattern);
//                        String callback = "Задание по будням (проверка логов) " + taskId + " создано и запущено выполнение по расписанию";
//                        applicationEventPublisher.publishEvent(new SendMessageEvent(callback, message.getChatId()));
//                    } else {
//                        applicationEventPublisher.publishEvent(new SendMessageEvent(
//                                "Пока что не существует других типов напоминаний. Сорян.",
//                                message.getChatId()));
//                    }
//                } else if (prevMessage.getText().equals("Отключить напоминание")) {
//                    long taskId;
//                    try {
//                        taskId = Long.parseLong(message.getText());
//                        Optional<ScheduledTask> byId = scheduledService.findById(taskId);
//                        if (byId.isPresent()) {
//                            ScheduledTask scheduledTask = byId.get();
//                            if (scheduledTask.getUser().getId().equals(message.getUser().getId()) || message.getUser().getRoles().contains(userService.getAdminRole())) {
//                                scheduledTask.setActive(false);
//                                scheduledService.saveTask(scheduledTask);
//                                applicationEventPublisher.publishEvent(new SendMessageEvent("Задание " + taskId + " отключено.", message.getChatId()));
//                            } else {
//                                applicationEventPublisher.publishEvent(new SendMessageEvent("Вы не можете отключить выполнение этого задания, оно не ваше.", message.getChatId()));
//                            }
//                        } else {
//                            applicationEventPublisher.publishEvent(new SendMessageEvent("Задание " + taskId + " отсутствует.", message.getChatId()));
//                        }
//                    } catch (Exception e) {
//                        applicationEventPublisher.publishEvent(new SendMessageEvent("Эй чорт, введи нормальное значение", message.getChatId()));
//                    }
//                } else {
//
//                }
//            } else {
//
//            }
//        }
//    }

//    private void registerNewScheduledTask(String text, String chatId, User user) {
//        text = text.substring(13);
//        String callback;
//        if (text.startsWith(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION.getPeriodicity())) {
//            try {
//                text = text.substring(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION.getPeriodicity().length() + 2);
//                String cronPattern = getCronPatternEveryDay(text);
//                text = text.substring(7);
//                Long taskId = createNewScheduledTask(text, chatId, user, ScheduledTaskTypeEnum.REGULAR_NOTIFICATION, cronPattern);
//                callback = "Задание " + taskId + " создано и запущено выполнение по расписанию";
//            } catch (Exception e) {
//                callback = "Братан, я сломался, ты мне чет не то прислал :(";
//            }
//        } else if (text.startsWith(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG.getPeriodicity())) {
//            try {
//                text = text.substring(ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG.getPeriodicity().length() + 2);
//                String cronPattern = getCronPatternWeekDays(text);
//                text = text.substring(7);
//                Long taskId = createNewScheduledTask(text, chatId, user, ScheduledTaskTypeEnum.REGULAR_NOTIFICATION_WEEKDAYS_LOG, cronPattern);
//                callback = "Задание по будням (проверка логов) " + taskId + " создано и запущено выполнение по расписанию";
//            } catch (Exception e) {
//                callback = "Братан, я сломался, ты мне чет не то прислал :(";
//            }
//        } else {
//            ScheduledTaskTypeEnum[] values = ScheduledTaskTypeEnum.values();
//            List<String> periodicities = new ArrayList<>();
//            for (ScheduledTaskTypeEnum scheduledTaskTypeEnum : values) {
//                periodicities.add(scheduledTaskTypeEnum.getPeriodicity());
//            }
//            callback = "Не опознан тип задания по расписанию. Доступные значения: " + periodicities;
//        }
//        applicationEventPublisher.publishEvent(new SendMessageEvent(callback, chatId));
//    }

}
