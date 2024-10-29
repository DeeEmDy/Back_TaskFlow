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




/*


//   use mimeMessageHelper porque necesitamos enviar un correo electrónico con contenido HTML. y con simpleMailMessage no se puede enviar contenido HTML.

 package com.taskflow.backend.services;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendActivationEmail(String toEmail, String activationLink) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("taskflowpw2024@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Activación de tu Cuenta en TaskFlow");

            // Leer el archivo HTML como una cadena
            String htmlContent = new String(Files.readAllBytes(Paths.get("src/main/resources/templates/activationEmailTemplate.html")));

            // Reemplazar el marcador de enlace de activación con el enlace real
            htmlContent = htmlContent.replace("{{activationLink}}", activationLink);

            helper.setText(htmlContent, true); // 'true' para indicar que el contenido es HTML

            mailSender.send(message);
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            // Manejo de errores
        }
    }
}
    

 */