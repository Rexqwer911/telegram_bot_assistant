package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.config.ApplicationProperties;
import com.theokanning.openai.completion.CompletionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatGPTService {

    private final ApplicationProperties applicationProperties;

    public Mono<CompletionResult> executeGPTRequest(String prompt) {

        String json = String.format("""
            {
                "model": "text-davinci-003",
                "prompt": "%s",
                "echo": true,
                "max_tokens": 4000
            }""", prompt);

        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, applicationProperties.getGpt().getToken())
                .build();

        return webClient.post()
                .uri("/v1/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(json)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse ->
                        clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Received HTTP error: " + errorBody);
                            return Mono.error(new RuntimeException("Received HTTP error"));
                        }))
                .bodyToMono(CompletionResult.class);
    }
}

