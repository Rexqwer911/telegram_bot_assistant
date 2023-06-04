package com.rexqwer.telegrambotassistant.service;

import com.rexqwer.telegrambotassistant.config.ApplicationProperties;
import com.rexqwer.telegrambotassistant.domain.User;
import com.rexqwer.telegrambotassistant.event.SendMessageEvent;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoiceMessageService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationProperties applicationProperties;

    public void downloadVoiceMessage(String fileId, User user, String chatId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String fileName = user.getFirstName() + "_" + formatter.format(LocalDateTime.now());
        applicationEventPublisher.publishEvent(new TelegramBotDownloadVoiceMessageEvent(fileId, fileName, chatId));
    }

    public void processVoiceMessage(String fileName, String fileUrl, String chatId) {

        String oggFilePath = applicationProperties.getTelegram().getVoice().getDownloadPath() + fileName + ".ogg";
        String wavFilePath = applicationProperties.getTelegram().getVoice().getWavPath() + fileName + ".wav";
        String txtFilePath = applicationProperties.getTelegram().getVoice().getDecryptedPath();// + fileName + ".txt";

        boolean ok = true;

        try {
            downloadFile(fileUrl, oggFilePath);
            log.info("Скачали ogg файл {}", fileName);
        } catch (IOException e) {
            log.error("Ошибка при скачивании файла: " + fileName, e);
            ok = false;
        }

        if (ok) {
            try {
                convertWav(oggFilePath, wavFilePath);
                log.info("Конвертировали в wav файл {}", fileName);
            } catch (EncoderException e) {
                if (e.getMessage().contains("/tmp/jave/ffmpeg-amd64-3.3.1")) {
                    log.info("Попытка копировать исполняемый файл ffmpeg");
                    boolean res = copyFfmpeg();
                    if (res) {
                        try {
                            convertWav(oggFilePath, wavFilePath);
                        } catch (EncoderException er) {
                            log.error("Ошибка при конвертации ogg файла {} в wav: {}", fileName, er.getMessage(), er);
                            ok = false;
                        }
                    } else {
                        ok = false;
                    }
                } else {
                    log.error("Ошибка при конвертации ogg файла {} в wav: {}", fileName, e.getMessage(), e);
                    ok = false;
                }
            }
        }

        if (ok) {
            try {
                //decryptVoiceMessage(wavFilePath, txtFilePath);
                decryptVoiceMessageWhisper(wavFilePath, txtFilePath);
                log.info("Расшифровали wav файл {}", fileName);
            } catch (IOException | InterruptedException e) {
                log.error("Ошибка при расшифровке файла: " + fileName, e);
                ok = false;
            }
        }

        if (ok) {
            try {
                String content = Files.readString(Paths.get(txtFilePath + fileName + ".txt"), StandardCharsets.UTF_8);
                log.info("Вычитали расшифрованное сообщение {}", content);
                applicationEventPublisher.publishEvent(new SendMessageEvent(content, chatId));
            } catch (IOException e) {
                log.error("Не удалось вычитать расшифрованный файл txt {}", fileName, e);
            }

        }
    }

    private void downloadFile(String url, String filePath) throws IOException {
        URL fileUrl = new URL(url);
        try (InputStream inputStream = fileUrl.openStream();
             ReadableByteChannel readableByteChannel = Channels.newChannel(inputStream);
             FileOutputStream outputStream = new FileOutputStream(filePath)
        ) {
            outputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        }
    }

    private void convertWav(String inputFilePath, String outputFilePath) throws EncoderException {
        File inputFile = new File(inputFilePath);
        File outputFile = new File(outputFilePath);
        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(inputFile), outputFile, getEncodingAttributes());
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

    private void decryptVoiceMessageWhisper(String wavFilePath, String txtFilePath) throws IOException, InterruptedException {
        String command = "whisper " + wavFilePath +" --model " +
                applicationProperties.getTelegram().getVoice().getModel() +
                " --language Russian -o "+ txtFilePath +" -f txt";

        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("bash", "-c", command);

        Process process = processBuilder.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            log.info("Успешно расшифровали голосовое сообщение {}", txtFilePath);
        } else {
            log.error("Command execution failed with exit code: " + exitCode);
        }
    }

    private boolean copyFfmpeg() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cp",
                    applicationProperties.getTelegram().getVoice().getFfmpegPath(),
                    "/tmp/jave/");
            Process process = processBuilder.start();
            process.waitFor();
            log.info("Успешно переместили исполняемый файл ffmpeg");
            return true;
        } catch (InterruptedException | IOException e) {
            log.error("Не удалось копировать файл ffmpeg");
            return false;
        }
    }
}
