package com.rexqwer.telegrambotassistant.config;


import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableConfigurationProperties({ApplicationProperties.class})
@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackages = "com.rexqwer.telegrambotassistant.repository")
public class ApplicationConfiguration {
}
