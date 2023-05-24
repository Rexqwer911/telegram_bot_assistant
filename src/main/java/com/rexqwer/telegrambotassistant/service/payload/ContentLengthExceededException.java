package com.rexqwer.telegrambotassistant.service.payload;

public class ContentLengthExceededException extends Exception {

    private final Integer contentLength;

    public ContentLengthExceededException(Integer contentLength) {
        this.contentLength = contentLength;
    }

    public Integer getContentLength() {
        return contentLength;
    }
}
