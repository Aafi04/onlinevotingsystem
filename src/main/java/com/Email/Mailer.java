package com.Email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mailer {
    private final Properties properties;
    private final String user;
    private final String pass;

    // Default hardcoded values for the static sendMail method
    private static final String DEFAULT_TO = "abc@mail.com";
    private static final String DEFAULT_SUBJECT = "nill";
    private static final String DEFAULT_MSG = "test";
    private static final String DEFAULT_USER = "abc@gmail.com"; // Change accordingly
    private static final String DEFAULT_PASS = "xxxxxx"; // Change
    private static final String DEFAULT_SMTP_HOST = "smtp@gmail.com"; // Change accordingly
    private static final String DEFAULT_SMTP_PORT = "587";

    /**
     * Constructor for creating a Mailer instance with custom properties and authentication.
     * This constructor is intended for testability and flexible configuration.
     *
     * @param properties Mail session properties.
     * @param user       Username for authentication.
     * @param pass       Password for authentication.
     */
    public Mailer(Properties properties, String user, String pass) {
        this.properties = properties;
        this.user = user;
        this.pass = pass;
    }

    /**
     * Sends an email using the Mailer instance's configured properties and authentication.
     *
     * @param to      Recipient email address.
     * @param subject Email subject.
     * @param msg     Email body.
     * @throws MessagingException if an error occurs during email sending.
     */
    public void sendMail(String to, String subject, String msg) throws MessagingException {
        Session session = Session.getInstance(properties,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(user));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(msg);

        Transport.send(message);
    }

    /**
     * Original static method for sending mail with hardcoded default configurations.
     * This method is preserved to maintain backward compatibility.
     *
     * @throws MessagingException if an error occurs during email sending.
     */
    public static void sendMail() throws MessagingException {
        Properties defaultProps = new Properties();
        defaultProps.put("mail.smtp.host", DEFAULT_SMTP_HOST);
        defaultProps.put("mail.smtp.auth", "true");
        defaultProps.put("mail.smtp.starttls.enable", "true");
        defaultProps.put("mail.smtp.port", DEFAULT_SMTP_PORT);

        // Create a Mailer instance with default properties and send mail
        Mailer mailer = new Mailer(defaultProps, DEFAULT_USER, DEFAULT_PASS);
        mailer.sendMail(DEFAULT_TO, DEFAULT_SUBJECT, DEFAULT_MSG);

        System.out.println("Done"); // Original System.out.println preserved for static method
    }

    public static void main(String[] args) {
        try {
            sendMail();
        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}