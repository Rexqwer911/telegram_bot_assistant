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
        private Voice voice;
    }

    @Data
    public static class Voice {
        private Boolean active;
        private String url;
    }

    @Data
    public static class Gpt {
        private String token;
    }
}
