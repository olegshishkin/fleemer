package com.fleemer.service;

import javax.mail.MessagingException;

public interface MailService {
    void send(String from, String to, String subject, String text) throws MessagingException;
}
