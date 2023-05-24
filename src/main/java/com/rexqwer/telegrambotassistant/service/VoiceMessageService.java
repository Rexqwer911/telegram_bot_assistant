package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.event.TelegramBotDownloadVoiceMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceMessageService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void downloadVoiceMessage(String fileId, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String fileName = user.getFirstName() + "_" + formatter.format(LocalDateTime.now());
        applicationEventPublisher.publishEvent(new TelegramBotDownloadVoiceMessageEvent(fileId, fileName));
    }

    public void downloadFile(String url, String filePath) throws IOException {
        URL fileUrl = new URL(url);
        try (InputStream inputStream = fileUrl.openStream();
             ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
             FileOutputStream outputStream = new FileOutputStream(filePath);
        ) {
            outputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    public void convertWav(String inputFilePath, String outputFilePath) {
        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputFilePath);
        try {
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(inputFile), outputFile, getEncodingAttributes());
            System.out.println("Конвертация завершена.");
        } catch (EncoderException e) {
            log.error("Не удалось конвертировать голосовое сообщение в wav", e);
        }
    }

    private EncodingAttributes getEncodingAttributes() {
        AudioAttributes audioAttributes = new AudioAttributes();
        audioAttributes.setCodec("pcm_s16le");
        audioAttributes.setBitRate(128000);
        audioAttributes.setChannels(2);
        audioAttributes.setSamplingRate(44100);

        EncodingAttributes encodingAttributes = new EncodingAttributes();
        encodingAttributes.setInputFormat("ogg");
        encodingAttributes.setOutputFormat("wav");
        encodingAttributes.setAudioAttributes(audioAttributes);

        return encodingAttributes;
    }
}
