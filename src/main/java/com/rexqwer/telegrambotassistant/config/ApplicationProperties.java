package com.rexqwer.telegrambotassistant.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private Telegram telegram;
    private Gpt gpt;

    @Data
    public static class Telegram {
        private String token;
        private String username;
    }

    @Data
    public static class Gpt {
        private String token;
    }
}
