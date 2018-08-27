package com.fleemer.model.mongodb;

import lombok.Data;

@Data
public class Request {
    private String uri;
    private String protocol;
    private String method;
    private String sessionId;
    private String userAgent;
    private String referer;
}
