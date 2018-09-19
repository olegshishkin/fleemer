package com.fleemer.model.mongodb;

import java.io.Serializable;
import lombok.Data;

@Data
public class Response implements Serializable {
    private long contentLength;
    private int statusCode;
}
