package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
import com.rexqwer.telegrambotassistant.event.TelegramBotDownloadVoiceMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service

public class VoiceMessageService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final WebClient whisperWebClient;

    public VoiceMessageService(ApplicationEventPublisher applicationEventPublisher,
                               @Qualifier(value = "whisperWebClient") WebClient whisperWebClient) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.whisperWebClient = whisperWebClient;
    }

    public void downloadVoiceMessage(String fileId, User user, String chatId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String fileName = user.getFirstName() + "_" + formatter.format(LocalDateTime.now());
        applicationEventPublisher.publishEvent(new TelegramBotDownloadVoiceMessageEvent(fileId, fileName, chatId));
    }

    public void processVoiceMessage(String fileUrl, String fileName, String chatId) {
        try {
            URL url = new URL(fileUrl);
            ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
            InputStream inputStream = Channels.newInputStream(readableByteChannel);

            MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();

            ByteArrayResource byteArrayResource = new ByteArrayResource(inputStream.readAllBytes()) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            formData.add("audio_file", byteArrayResource);

            String response = whisperWebClient
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("task", "transcribe")
                            .queryParam("language", "ru")
                            .queryParam("encode", "true")
                            .queryParam("output", "txt")
                            .queryParam("word_timestamps", "false")
                            .build())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .headers(httpHeaders -> {
                        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                        httpHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
                    })
                    .body(BodyInserters.fromMultipartData(formData))
                    .retrieve()
                    .onStatus(HttpStatus::isError, resp -> resp.bodyToMono(String.class)
                            .map(errorResponse -> {
                                log.error("Ошибка при выполнении запроса к сервису распознавания голоса: {}", errorResponse);
                                return new Exception("Ошибка при выполнении запроса к сервису распознавания голоса");
                            }))
                    .bodyToMono(String.class)
                    .block();

            log.info("Ответ whisper {}", response);
            applicationEventPublisher.publishEvent(new SendMessageEvent(response, chatId));
        } catch (Exception e) {
            log.error("Ошибка при конвертации голосового сообщения", e);
            applicationEventPublisher.publishEvent(new SendMessageEvent("Произошла ошибка при расшифровке голосового сообщения", chatId));
        }
    }
}
