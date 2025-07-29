package com.training.warehouse.service;

public interface MailService {
    void sendMail(String to, String subject,String body);
}