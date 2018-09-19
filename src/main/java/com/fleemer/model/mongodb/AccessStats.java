package com.fleemer.model.mongodb;

import java.io.Serializable;
import lombok.Data;

@Data
public class AccessStats implements Serializable {
    private String date;
    private String ip;
    private String host;
    private String uri;
    private String agent;
    private int count;
}
