package com.fleemer.model.mongodb;

import lombok.Data;

@Data
public class AccessStats {
    private String date;
    private String ip;
    private String host;
    private String uri;
    private String agent;
    private int count;
}
