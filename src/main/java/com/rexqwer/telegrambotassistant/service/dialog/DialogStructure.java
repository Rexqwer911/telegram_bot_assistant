package com.rexqwer.telegrambotassistant.service.dialog;

import lombok.Data;

import javax.swing.tree.DefaultMutableTreeNode;

@Data
public class DialogStructure {
    private DefaultMutableTreeNode tree;
    private String defaultText;
    private String defaultCommand;
}
