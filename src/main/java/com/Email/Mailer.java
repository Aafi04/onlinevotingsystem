package com.Email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Mailer {

    private static String emailUser;
    private static String emailPass;

    static {
        Properties emailConfig = new Properties();
        try (InputStream input = Mailer.class.getClassLoader().getResourceAsStream("email.properties")) {
            if (input == null) {
                System.err.println("ERROR: email.properties not found in the classpath. Please ensure it's available.");
                throw new RuntimeException("Email configuration file (email.properties) not found.");
            }
            emailConfig.load(input);

            emailUser = emailConfig.getProperty("mail.username");
            emailPass = emailConfig.getProperty("mail.password");

            if (emailUser == null || emailPass == null) {
                System.err.println("ERROR: 'mail.username' or 'mail.password' not found in email.properties.");
                throw new RuntimeException("Missing required email credentials in email.properties.");
            }

        } catch (IOException ex) {
            System.err.println("ERROR: Failed to load email.properties: " + ex.getMessage());
            throw new RuntimeException("Failed to load email configuration.", ex);
        } catch (Exception ex) {
            System.err.println("ERROR: An unexpected error occurred during email configuration initialization: " + ex.getMessage());
            throw new RuntimeException("Failed to initialize email configuration.", ex);
        }
    }

    public static void sendMail() {

        String to = "abc@mail.com";
        String subject = "nill";
        String msg = "test";
        final String user = emailUser; // Credentials loaded from external properties
        final String pass = emailPass; // Credentials loaded from external properties


//1st step) Get the session object
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp@gmail.com");//change accordingly
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props,
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(user, pass);
                    }
                });
//2nd step)compose message
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(user));
            message.setRecipient(Message.RecipientType.TO,new InternetAddress(to));
            message.setSubject(subject);
            message.setText(msg);

            //3rd step)send message
            Transport.send(message);

            System.out.println("Done");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String []args){
        sendMail();
    }
}