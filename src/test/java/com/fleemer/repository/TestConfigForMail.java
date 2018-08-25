package com.fleemer.repository;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@TestConfiguration
@PropertySource(value={"classpath:application.properties"})
public class TestConfigForMail {
    @Bean
    public JavaMailSender mailSender() {
        return new JavaMailSenderImpl();
    }
}
