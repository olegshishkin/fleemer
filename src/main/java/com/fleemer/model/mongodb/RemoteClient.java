package com.fleemer.model.mongodb;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
public class RemoteClient {
    private String host;

    @Field("addr")
    private String ip;

    @Field("user")
    private String username;
}
