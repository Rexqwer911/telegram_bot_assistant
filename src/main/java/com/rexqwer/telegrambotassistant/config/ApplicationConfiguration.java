package com.rexqwer.telegrambotassistant.config;


import com.rexqwer.telegrambotassistant.service.dialog.DialogService;
import com.rexqwer.telegrambotassistant.service.dialog.DialogStructure;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableConfigurationProperties({ApplicationProperties.class})
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "com.rexqwer.telegrambotassistant.repository")
public class ApplicationConfiguration {

    private final DialogService dialogService;

    @Bean("reminderDialogStructure")
    public DialogStructure reminderDialogStructure() {
        return dialogService.buildDialogStructure("branches/reminder.txt");
    }

}
