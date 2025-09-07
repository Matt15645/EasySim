package com.stock_management.subscribe_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stock_management.subscribe_service.dto.EmailRequest;
import com.stock_management.subscribe_service.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Controller Tests")
class EmailControllerTest {

    private MockMvc mockMvc;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailController emailController;

    private ObjectMapper objectMapper;
    private EmailRequest validEmailRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(emailController).build();

        validEmailRequest = EmailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .content("Test Content")
                .isHtml(false)
                .build();
    }

    @Test
    @DisplayName("發送郵件成功 - 應該返回200狀態碼")
    void shouldSendEmailSuccessfully() throws Exception {
        // given
        doNothing().when(emailService).sendEmail(any(EmailRequest.class));

        // when & then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmailRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("test@example.com")));
    }

    @Test
    @DisplayName("發送HTML郵件成功 - 應該返回200狀態碼")
    void shouldSendHtmlEmailSuccessfully() throws Exception {
        // given
        EmailRequest htmlEmailRequest = EmailRequest.builder()
                .to("html@example.com")
                .subject("HTML Test")
                .content("<h1>Hello</h1><p>HTML content</p>")
                .isHtml(true)
                .build();

        doNothing().when(emailService).sendEmail(any(EmailRequest.class));

        // when & then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(htmlEmailRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("html@example.com")));
    }

    @Test
    @DisplayName("發送測試郵件成功 - 應該返回200狀態碼")
    void shouldSendTestEmailSuccessfully() throws Exception {
        // given
        doNothing().when(emailService).sendTestEmail(anyString());

        // when & then
        mockMvc.perform(post("/api/email/test")
                        .param("email", "testuser@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("testuser@example.com")));
    }

    @Test
    @DisplayName("健康檢查 - 應該返回200狀態碼")
    void shouldReturnHealthCheckSuccessfully() throws Exception {
        mockMvc.perform(get("/api/email/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Subscribe Service is running"));
    }

    @Test
    @DisplayName("郵件服務異常 - 發送郵件時應該返回500狀態碼")
    void shouldReturnInternalServerErrorWhenEmailServiceFails() throws Exception {
        // given
        doThrow(new RuntimeException("Email service error"))
                .when(emailService).sendEmail(any(EmailRequest.class));

        // when & then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validEmailRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Email service error")));
    }

    @Test
    @DisplayName("郵件服務異常 - 發送測試郵件時應該返回500狀態碼")
    void shouldReturnInternalServerErrorWhenTestEmailServiceFails() throws Exception {
        // given
        doThrow(new RuntimeException("Test email service error"))
                .when(emailService).sendTestEmail(anyString());

        // when & then
        mockMvc.perform(post("/api/email/test")
                        .param("email", "testuser@example.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Test email service error")));
    }

    @Test
    @DisplayName("無效JSON - 應該返回400狀態碼")
    void shouldReturnBadRequestForInvalidJson() throws Exception {
        // when & then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("invalid json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("缺少Content-Type - 應該返回415狀態碼")
    void shouldReturnUnsupportedMediaTypeWithoutContentType() throws Exception {
        // when & then
        mockMvc.perform(post("/api/email/send")
                        .content(objectMapper.writeValueAsString(validEmailRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("缺少測試郵件參數 - 應該返回400狀態碼")
    void shouldReturnBadRequestWhenTestEmailParameterMissing() throws Exception {
        // when & then
        mockMvc.perform(post("/api/email/test"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("空的測試郵件參數 - 應該正常處理")
    void shouldHandleEmptyTestEmailParameter() throws Exception {
        // given
        doNothing().when(emailService).sendTestEmail(anyString());

        // when & then
        mockMvc.perform(post("/api/email/test")
                        .param("email", ""))
                .andExpect(status().isOk());
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

        doNothing().when(emailService).sendEmail(any(EmailRequest.class));

        // when & then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(singleRecipientRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("user1@example.com")));
    }

    @Test
    @DisplayName("長主題和內容郵件發送")
    void shouldSendEmailWithLongSubjectAndContent() throws Exception {
        // given
        StringBuilder longSubject = new StringBuilder("Long Subject: ");
        StringBuilder longContent = new StringBuilder("Long Content: ");
        
        for (int i = 0; i < 100; i++) {
            longSubject.append("This is a very long subject line part ").append(i).append(" ");
            longContent.append("This is line ").append(i).append(" of a very long email content. ");
        }

        EmailRequest longEmailRequest = EmailRequest.builder()
                .to("longcontent@example.com")
                .subject(longSubject.toString())
                .content(longContent.toString())
                .isHtml(false)
                .build();

        doNothing().when(emailService).sendEmail(any(EmailRequest.class));

        // when & then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(longEmailRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("longcontent@example.com")));
    }

    @Test
    @DisplayName("特殊字符郵件發送")
    void shouldSendEmailWithSpecialCharacters() throws Exception {
        // given
        EmailRequest specialCharRequest = EmailRequest.builder()
                .to("special@example.com")
                .subject("特殊字符測試 - Special Characters Test 🚀")
                .content("這是一個包含特殊字符的測試郵件：中文、emoji 🎉、符號 @#$%^&*()")
                .isHtml(true)
                .build();

        doNothing().when(emailService).sendEmail(any(EmailRequest.class));

        // when & then
        mockMvc.perform(post("/api/email/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(specialCharRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("special@example.com")));
    }
}
