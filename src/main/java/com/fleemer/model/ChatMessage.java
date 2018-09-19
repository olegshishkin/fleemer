package com.fleemer.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChatMessage implements Serializable {
    private long senderId;
    private String senderNickname;
    private long receiverId;
    private LocalDateTime time;
    private String content;
}
