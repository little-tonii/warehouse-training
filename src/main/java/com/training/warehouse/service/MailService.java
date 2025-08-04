package com.training.warehouse.service;

public interface MailService {
    void sendAsync(String email, String subject, String message);
}