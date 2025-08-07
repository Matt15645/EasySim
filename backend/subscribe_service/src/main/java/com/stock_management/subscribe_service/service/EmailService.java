package com.stock_management.subscribe_service.service;

import com.stock_management.subscribe_service.dto.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendEmail(EmailRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(request.getTo());
            helper.setSubject(request.getSubject());
            helper.setText(request.getContent(), request.isHtml());
            
            mailSender.send(message);
            log.info("郵件發送成功至: {}", request.getTo());
        } catch (Exception e) {
            log.error("郵件發送失敗: {}", e.getMessage());
            throw new RuntimeException("郵件發送失敗", e);
        }
    }
    
    public void sendTestEmail(String email) {
        EmailRequest request = EmailRequest.builder()
                .to(email)
                .subject("Stock Management System - 測試郵件")
                .content("<h1>Hello!</h1><p>這是來自 Subscribe Service 的測試郵件</p><p>時間: " + 
                        java.time.LocalDateTime.now() + "</p>")
                .isHtml(true)
                .build();
        
        sendEmail(request);
    }
}
