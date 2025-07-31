package com.training.warehouse.service.impl;

import com.training.warehouse.service.MailService;
import lombok.RequiredArgsConstructor;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendAsync() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'sendAsync'");
    }

}