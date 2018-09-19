package com.fleemer.model.mongodb;

import java.io.Serializable;
import lombok.Data;

@Data
public class Request implements Serializable {
    private String uri;
    private String protocol;
    private String method;
    private String sessionId;
    private String userAgent;
    private String referer;
}
