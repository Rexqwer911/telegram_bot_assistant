package com.rexqwer.telegrambotassistant.service.dialog;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DialogTreeBranch {
    private String action;
    private String command;
    private String replyText;
    private String validator;
    private List<String> buttons = new ArrayList<>();
    private List<String> methods;
}
