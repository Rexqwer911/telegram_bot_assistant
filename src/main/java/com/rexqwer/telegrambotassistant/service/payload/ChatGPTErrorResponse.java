package com.rexqwer.telegrambotassistant.service.payload;

import lombok.Data;

@Data
public class ChatGPTErrorResponse {
    private ErrorDetailsDTO error;
    @Data
    public static class ErrorDetailsDTO {
        private String message;
        private String type;
        private String param;
        private String code;
    }
}
