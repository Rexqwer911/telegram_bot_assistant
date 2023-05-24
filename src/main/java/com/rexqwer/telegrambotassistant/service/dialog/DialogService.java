package com.rexqwer.telegrambotassistant.service.dialog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class DialogService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Pattern commandPattern = Pattern.compile("\\((.*?)\\)");

    public DefaultMutableTreeNode findNode(DefaultMutableTreeNode rootNode, MessageBranch messageBranch) {

        // Проверяем, содержит ли корневой узел искомый объект
        if (!rootNode.isRoot()) {

            int lastMessageIdx = messageBranch.getLastMessageIdx();

            int level = rootNode.getLevel();

            if (lastMessageIdx < level) {
                return null;
            }

            DialogTreeBranch treeBranch = (DialogTreeBranch) rootNode.getUserObject();

            if (treeBranch.getAction() == null) {
                return null;
            }

            if (messageEquals(treeBranch, messageBranch, lastMessageIdx)) {
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) rootNode.getParent();
                boolean branchCorrect = true;
                for (int i = 1; i < level; i++) {
                    DialogTreeBranch dialogTreeBranch = (DialogTreeBranch) parent.getUserObject();
                    if (!dialogTreeBranch.getAction().equals("val") &&
                            !dialogTreeBranch.getAction().equals(
                            messageBranch.getMessages().get(
                                    messageBranch.getLastMessageIdx() - i).getText())) {
                        branchCorrect = false;
                    }
                    parent = (DefaultMutableTreeNode) parent.getParent();
                }
                if (branchCorrect) {
                    return rootNode;
                }
            }
        }

        // Проходим по всем потомкам корневого узла
        int childCount = rootNode.getChildCount();
        for (int i = 0; i < childCount; i++) {
            DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);

            // Рекурсивно вызываем поиск для каждого потомка
            DefaultMutableTreeNode foundNode = findNode(childNode, messageBranch);
            if (foundNode != null) {
                return foundNode; // Найден искомый узел
            }
        }

        return null; // Ветка не найдена
    }

    private boolean messageEquals(DialogTreeBranch treeBranch, MessageBranch messageBranch, int lastMessageIdx) {
        if (treeBranch.getCommand() != null) {
            return true;
        } else {
            return treeBranch.getAction().equals("val") || treeBranch.getAction().equals(
                    messageBranch.getMessageByIdx(lastMessageIdx).getText());
        }
    }

    public DialogStructure buildDialogStructure(String filePath) {

        DialogStructure rootTree = new DialogStructure();

        DefaultMutableTreeNode rootNode = null;
        DefaultMutableTreeNode prevNode = null;

        try (BufferedReader reader = IOUtils.toBufferedReader(new InputStreamReader(new ClassPathResource(filePath).getInputStream()))) {
            String line;

            while ((line = reader.readLine()) != null) {

                //Проверка пустого уровня
                String lineTest = "" + line;
                lineTest = lineTest.trim();
                if (lineTest.isEmpty()) {
                    continue;
                }

                //Установка рута
                if (rootNode == null) {
                    rootNode = new DefaultMutableTreeNode(buildTreeBranch(line.trim()));
                    continue;
                }

                //Проверка на строку дефолтных действий
                if (line.trim().startsWith("(default)")) {
                    int jsonSplitIdx = line.trim().indexOf("->");

                    String json = line.trim().substring(10, jsonSplitIdx);
                    JsonNode jsonNode = objectMapper.readTree(("{" + json.trim() + "}"));
                    rootTree.setDefaultText(jsonNode.get("text").asText());

                    String commandLine = line.trim().substring(jsonSplitIdx);
                    Matcher matcher = commandPattern.matcher(commandLine);
                    List<String> methods = new ArrayList<>();
                    while (matcher.find()) {
                        methods.add(matcher.group(1));
                    }
                    Assert.notNull(methods.get(0), "Дефолтная команда не может быть null");
                    rootTree.setDefaultCommand(methods.get(0));
                    continue;
                }

                //Получаем текущую строку и ее уровень
                DefaultMutableTreeNode currentNode = new DefaultMutableTreeNode(buildTreeBranch(line.trim()));
                int level = 0;
                int index = 0;
                while (index < line.length() && line.charAt(index) == '\t') {
                    level++;
                    index++;
                }

                //Добавляем ветки дерева
                if (prevNode == null) {
                    rootNode.add(currentNode);
                } else {
                    int diff = prevNode.getLevel() - level + 1;
                    for (int i = 0; i < diff; i++) {
                        prevNode = (DefaultMutableTreeNode) prevNode.getParent();
                    }
                    prevNode.add(currentNode);
                }
                prevNode = currentNode;
            }
        } catch (Exception e) {
            e.printStackTrace();
            rootTree = null;
        }

        //Assertions

        Assert.notNull(rootTree, "");

        rootTree.setTree(rootNode);

        return rootTree;
    }

    private DialogTreeBranch buildTreeBranch(String line) throws JsonProcessingException {
        int jsonSplitIdx = line.indexOf("->");
        String json;
        List<String> methods = new ArrayList<>();
        if (jsonSplitIdx >= 0) {
            json = line.substring(0, jsonSplitIdx).trim();
            Matcher matcher = commandPattern.matcher(line.substring(jsonSplitIdx));
            while (matcher.find()) {
                methods.add(matcher.group(1));
            }
        } else {
            json = "" + line;
        }

        JsonNode jsonNode = objectMapper.readTree(("{" + json + "}"));
        List<String> buttonList = new ArrayList<>();
        JsonNode buttons = jsonNode.get("buttons");
        if (buttons != null) {
            buttons.forEach(jsonNode1 -> buttonList.add(jsonNode1.asText()));
        }

        return DialogTreeBranch.builder()
                .action(jsonNode.get("action") != null ? jsonNode.get("action").asText() : null)
                .command(jsonNode.get("command") != null ? jsonNode.get("command").asText() : null)
                .replyText(jsonNode.get("text") != null ? jsonNode.get("text").asText() : null)
                .validator(jsonNode.get("validator") != null ? jsonNode.get("validator").asText() : null)
                .buttons(buttonList)
                .methods(methods)
                .build();
    }
}
