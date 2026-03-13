package com.Email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.mail.MessagingException;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.*;

public class MailerTest {

    private Properties baseProps;
    private String testUser;
    private String testPass;

    @BeforeEach
    void setUp() {
        // Base properties for testing. We'll modify these to simulate various failure scenarios.
        baseProps = new Properties();
        baseProps.put("mail.smtp.auth", "true");
        baseProps.put("mail.smtp.starttls.enable", "true");
        baseProps.put("mail.transport.protocol", "smtp");
        // Set short timeouts to fail fast during connection attempts
        baseProps.put("mail.smtp.connectiontimeout", "1000"); // 1 second timeout
        baseProps.put("mail.smtp.timeout", "1000"); // 1 second timeout

        testUser = "testuser@example.com";
        testPass = "testpass";
    }

    @Test
    void testSendMail_invalidSmtpHost_shouldThrowMessagingException() {
        Properties invalidHostProps = (Properties) baseProps.clone();
        // Use a non-existent domain to simulate an unknown host error or network issue
        invalidHostProps.put("mail.smtp.host", "nonexistent.invalid.domain");
        invalidHostProps.put("mail.smtp.port", "25"); // Standard port

        Mailer mailer = new Mailer(invalidHostProps, testUser, testPass);

        MessagingException thrown = assertThrows(MessagingException.class, () ->
                mailer.sendMail("recipient@example.com", "Test Subject", "Test Body"));

        // Expect messages indicating connection failure or unknown host
        String message = thrown.getMessage().toLowerCase();
        assertTrue(message.contains("could not connect to smtp host") ||
                   message.contains("unknown host") ||
                   message.contains("connect failed"),
                   "Expected connection/host error, but got: " + thrown.getMessage());
    }

    @Test
    void testSendMail_invalidSmtpPort_shouldThrowMessagingException() {
        Properties invalidPortProps = (Properties) baseProps.clone();
        // Use localhost but an invalid/unassigned port to simulate connection refusal
        invalidPortProps.put("mail.smtp.host", "localhost");
        invalidPortProps.put("mail.smtp.port", "1"); // Port 1 is usually privileged and not used by SMTP

        Mailer mailer = new Mailer(invalidPortProps, testUser, testPass);

        MessagingException thrown = assertThrows(MessagingException.class, () ->
                mailer.sendMail("recipient@example.com", "Test Subject", "Test Body"));

        // Expect messages indicating connection refusal or timeout
        String message = thrown.getMessage().toLowerCase();
        assertTrue(message.contains("could not connect to smtp host") ||
                   message.contains("connection refused") ||
                   message.contains("connect failed"),
                   "Expected connection refused or timeout error, but got: " + thrown.getMessage());
    }

    @Test
    void testSendMail_authenticationRequiredButNoAuthProvided_shouldThrowMessagingException() {
        Properties authRequiredProps = (Properties) baseProps.clone();
        authRequiredProps.put("mail.smtp.auth", "true");
        // To simulate this, we need to try connecting to a host that *would* require auth
        // but provide empty credentials. Using a non-existent host here might mask the auth error
        // with a connection error. For a true unit test of auth failure without a real server,
        // one would typically mock `Transport` or `Session`.
        // Given no external dependencies, we rely on `javax.mail`'s internal behavior.
        // We'll use a local IP that's unlikely to have an SMTP server, ensuring a connection attempt.
        authRequiredProps.put("mail.smtp.host", "127.0.0.1"); // Localhost, but no SMTP server running
        authRequiredProps.put("mail.smtp.port", "25");

        Mailer mailer = new Mailer(authRequiredProps, "", ""); // Empty user/pass

        MessagingException thrown = assertThrows(MessagingException.class, () ->
                mailer.sendMail("recipient@example.com", "Test Subject", "Test Body"));

        // The specific error message can vary (e.g., connection refused, or an auth error if it gets that far).
        // We check for general connection/auth failure indicators.
        String message = thrown.getMessage().toLowerCase();
        assertTrue(message.contains("could not connect to smtp host") ||
                   message.contains("authentication failed") ||
                   message.contains("connect failed") ||
                   message.contains("connection refused"),
                   "Expected connection or authentication error, but got: " + thrown.getMessage());
    }
}