package com.fleemer.service;

import com.fleemer.service.exception.ServiceException;
import javax.mail.MessagingException;

public interface MailService {
    void send(String to, String subject, String baseUrl, String token) throws MessagingException;

    boolean verify(String email, String token) throws ServiceException;
}
