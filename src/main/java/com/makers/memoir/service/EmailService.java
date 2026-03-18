package com.makers.memoir.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendNewsletterEmail(String toEmail, String subject, byte[] pdfBytes) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText("Your weekly Memoir newsletter is attached!", false);
        helper.setFrom("your.email@gmail.com");

        // Attach the PDF
        helper.addAttachment("memoir-newsletter.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));

        mailSender.send(message);
    }
}