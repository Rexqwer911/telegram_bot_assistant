package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.config.ApplicationProperties;
import com.rexqwer.telegrambotassistant.domain.MessageBranch;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.enums.RoleEnum;
import com.rexqwer.telegrambotassistant.repository.RoleRepository;
import com.rexqwer.telegrambotassistant.repository.UserRepository;
import com.rexqwer.telegrambotassistant.service.payload.ChatGPTErrorResponse;
import com.rexqwer.telegrambotassistant.service.payload.ContentLengthExceededException;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatGPTService {

    private final ApplicationProperties applicationProperties;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public Mono<ChatCompletionResult> executeGPTRequest(MessageBranch messageBranch, @Nullable Integer maxTokens) {

        ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                .messages(prepareChatList(messageBranch))
                .model("gpt-3.5-turbo")
                .maxTokens(maxTokens == null ? 2000 : maxTokens)
                .logitBias(new HashMap<>())
                .user(messageBranch.getUser().getUserName())
                .presencePenalty(0d)
                .frequencyPenalty(0d)
                .temperature(0d)
                .n(1)
                .build();

        WebClient webClient = WebClient.builder()
                .baseUrl("https://api.openai.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, applicationProperties.getGpt().getToken())
                .build();

        log.info("gpt request");

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(completionRequest)
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.BAD_REQUEST) {
                        return clientResponse.bodyToMono(ChatGPTErrorResponse.class).flatMap(chatGPTErrorResponse -> {
                            if (chatGPTErrorResponse.getError().getCode().equals("context_length_exceeded")) {
                                Integer length = calculateTokenDifference(chatGPTErrorResponse.getError().getMessage());
                                if (length != null) {
                                    log.error("Превысили количество токенов");
                                    return Mono.error(new ContentLengthExceededException(length));
                                }
                            }
                            return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("Received HTTP error: " + errorBody);
                                return Mono.error(new RuntimeException("Received HTTP error"));
                            });
                        });
                    } else {
                        return clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                            log.error("Received HTTP error: " + errorBody);
                            return Mono.error(new RuntimeException("Received HTTP error"));
                        });
                    }})
                .bodyToMono(ChatCompletionResult.class);
    }

    private List<ChatMessage> prepareChatList(MessageBranch messageBranch) {
        return messageBranch.getMessages().stream().map(message -> {
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRole("user");
            chatMessage.setContent(message.getText());
            return chatMessage;
        }).toList();
    }

    public User findOrCreateGptUser() {
        String userGpt = "gpt";
        Optional<User> byTgId = userRepository.findByTgId(userGpt);
        if (byTgId.isPresent()) {
            return byTgId.get();
        } else {
            User newUser = new User();
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setTgId(userGpt);
            newUser.setFirstName(userGpt);
            newUser.setLastName(userGpt);
            newUser.setUserName(userGpt);
            newUser.setRoles(Collections.singleton(roleRepository.findByCode(RoleEnum.SYSTEM.getCode())));
            return userRepository.save(newUser);
        }
    }

    private Integer calculateTokenDifference(String input) {
        String res = input.replaceAll("[^\\d\\s]", "");
        String[] numberStrings = res.trim().split("\\s+");
        if (numberStrings.length == 4 && numberStrings[3].equals("2000")) {
            return Integer.parseInt(numberStrings[0]) - Integer.parseInt(numberStrings[2]);
        } else {
            return null;
        }
    }
}

