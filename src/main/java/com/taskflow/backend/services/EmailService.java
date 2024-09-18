package com.taskflow.backend.services;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendActivationEmail(String toEmail, String activationLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("taskflowpw2024@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Activate Your TaskFlow Account");
        message.setText("""
                        Thank you for registering with TaskFlow!
                        
                        Please click the link below to activate your account:
                        """ +
                        activationLink + "\n\n" +
                        "If you did not register for this account, please ignore this email.");

        mailSender.send(message);
    }
}
