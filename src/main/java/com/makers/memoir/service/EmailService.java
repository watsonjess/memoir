package com.makers.memoir.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

        try {
            helper.setFrom("memoir.app.noreply@gmail.com", "Memoir");
        } catch (java.io.UnsupportedEncodingException e) {
            helper.setFrom("memoir.app.noreply@gmail.com");
        }

        helper.setText(
                "<html><body style='font-family:Georgia,serif;background-color:#f8f5f0;padding:40px;'>" +
                        "<div style='max-width:600px;margin:0 auto;'>" +
                        "<h2 style='font-size:2rem;font-weight:bold;border-bottom:2px solid #000;padding-bottom:16px;'>Your Weekly Memoir Newsletter is here!</h2>" +
                        "<p style='color:#333;line-height:1.8;font-size:1rem;'>This week's memories have been compiled into your group newsletter. Open the attachment to relive the moments your group captured this week.</p>" +
                        "<p style='color:#333;line-height:1.8;font-size:1rem;'>Every week, Memoir brings together the everyday moments that make life worth remembering.</p>" +
                        "<hr style='border:none;border-top:1px solid #ccc;margin:32px 0;'/>" +
                        "<p style='color:#888;font-size:0.85rem;'>You are receiving this because you are a member of a Memoir group. If you have any questions, please contact your group admin.</p>" +
                        "</div>" +
                        "</body></html>",
                true
        );

        helper.addAttachment("memoir-newsletter.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));

        mailSender.send(message);
    }
}