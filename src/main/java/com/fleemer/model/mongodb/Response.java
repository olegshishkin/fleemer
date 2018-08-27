package com.fleemer.model.mongodb;

import lombok.Data;

@Data
public class Response {
    private long contentLength;
    private int statusCode;
}
