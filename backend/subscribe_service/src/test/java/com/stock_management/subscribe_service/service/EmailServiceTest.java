package com.stock_management.subscribe_service.service;

import com.stock_management.subscribe_service.dto.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Service Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    private EmailRequest validEmailRequest;

    @BeforeEach
    void setUp() {
        validEmailRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .isHtml(false)
                .build();
    }

    @Test
    @DisplayName("發送郵件成功 - 純文字內容")
    void shouldSendTextEmailSuccessfully() throws Exception {
        // given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(validEmailRequest);

        // then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("發送郵件成功 - HTML內容")
    void shouldSendHtmlEmailSuccessfully() throws Exception {
        // given
        EmailRequest htmlEmailRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("HTML Test Subject")
                .content("<h1>Hello World</h1><p>This is a test email</p>")
                .isHtml(true)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(htmlEmailRequest);

        // then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("發送測試郵件成功")
    void shouldSendTestEmailSuccessfully() throws Exception {
        // given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendTestEmail("testuser@example.com");

        // then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("郵件伺服器異常 - 應該拋出RuntimeException")
    void shouldThrowExceptionWhenMailServerFails() throws Exception {
        // given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        // when & then
        assertThatThrownBy(() -> emailService.sendEmail(validEmailRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("郵件發送失敗");
    }

    @Test
    @DisplayName("創建郵件訊息失敗 - 應該拋出RuntimeException")
    void shouldThrowExceptionWhenCreateMessageFails() {
        // given
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Create message failed"));

        // when & then
        assertThatThrownBy(() -> emailService.sendEmail(validEmailRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("郵件發送失敗");
    }

    @Test
    @DisplayName("發送測試郵件時郵件伺服器異常 - 應該拋出RuntimeException")
    void shouldThrowExceptionWhenTestEmailFails() throws Exception {
        // given
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));

        // when & then
        assertThatThrownBy(() -> emailService.sendTestEmail("test@example.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("郵件發送失敗");
    }

    @Test
    @DisplayName("單一收件者郵件發送")
    void shouldSendEmailToSingleRecipient() throws Exception {
        // given
        EmailRequest singleRecipientRequest = EmailRequest.builder()
                .to("user1@example.com")
                .subject("Single Recipient Test")
                .content("This email is sent to a single recipient")
                .isHtml(false)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(singleRecipientRequest);

        // then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("長內容郵件發送")
    void shouldSendLongContentEmail() throws Exception {
        // given
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("This is line ").append(i).append(" of a very long email content. ");
        }

        EmailRequest longContentRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("Long Content Email")
                .content(longContent.toString())
                .isHtml(false)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(longContentRequest);

        // then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("特殊字符郵件發送")
    void shouldSendEmailWithSpecialCharacters() throws Exception {
        // given
        EmailRequest specialCharRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("特殊字符測試 - Special Characters Test 🚀")
                .content("這是一個包含特殊字符的測試郵件：中文、emoji 🎉、符號 @#$%^&*()")
                .isHtml(false)
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(specialCharRequest);

        // then
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }
}
