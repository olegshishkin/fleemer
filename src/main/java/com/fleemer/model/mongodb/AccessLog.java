package com.fleemer.model.mongodb;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "log")
@Data
public class AccessLog implements Serializable {
    @Id
    private String id;

    @Field("timeStamp")
    private Date time;

    private String serverName;

    @Field("remote")
    private RemoteClient remoteClient;

    private Request request;

    private Response response;
}
