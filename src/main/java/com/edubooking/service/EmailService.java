package com.edubooking.service;

public interface EmailService {
    void sendEmail(String to, String subject, String message);
}