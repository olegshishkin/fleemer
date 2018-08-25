package com.fleemer.service.implementation;

import com.fleemer.model.Confirmation;
import com.fleemer.service.ConfirmationService;
import com.fleemer.service.MailService;
import com.fleemer.service.exception.ServiceException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailServiceImpl implements MailService {
    private static final String EMAIL_TEMPLATE = "mail";

    @Value("${spring.mail.username}")
    private String senderAddress;
    private final TemplateEngine engine;
    private final JavaMailSender mailSender;
    private final ConfirmationService service;

    @Autowired
    public MailServiceImpl(TemplateEngine engine, JavaMailSender mailSender, ConfirmationService service) {
        this.engine = engine;
        this.mailSender = mailSender;
        this.service = service;
    }

    @Override
    public void send(String to, String subject, String baseUrl, String token) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        int multipartMode = MimeMessageHelper.MULTIPART_MODE_NO;
        MimeMessageHelper helper = new MimeMessageHelper(message, multipartMode, StandardCharsets.UTF_8.name());
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariable("url", baseUrl + "/user/create/confirm?email=" + to + "&token=" + token);
        helper.setTo(to);
        String html = engine.process(EMAIL_TEMPLATE, context);
        helper.setText(html, true);
        helper.setSubject(subject);
        helper.setFrom(senderAddress);
        mailSender.send(message);
    }

    @Override
    public boolean verify(String email, String token) throws ServiceException {
        Optional<Confirmation> optional = service.findByPersonEmail(email);
        if (!optional.isPresent()) {
            return false;
        }
        Confirmation confirmation = optional.get();
        if (confirmation.isEnabled() || !confirmation.getToken().equals(token)) {
            return false;
        }
        confirmation.setEnabled(true);
        return service.save(confirmation) != null;
    }
}
