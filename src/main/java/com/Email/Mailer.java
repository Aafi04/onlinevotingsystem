package com.Email;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Mailer {
    public static void sendMail() {

        String to = "abc@mail.com";
        String subject = "nill";
        String msg = "test";
        // Retrieve email username and password from system properties.
        // These can be set via -Dmail.username=your_username -Dmail.password=your_password
        // or loaded from a properties file and set into System properties programmatically.
        final String mailUser = System.getProperty("mail.username");
        final String mailPass = System.getProperty("mail.password");

        // Validate that credentials are provided
        if (mailUser == null || mailUser.isEmpty() || mailPass == null || mailPass.isEmpty()) {
            throw new IllegalStateException("Email credentials (mail.username and mail.password) must be set as system properties.");
        }

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
                        return new PasswordAuthentication(mailUser, mailPass);
                    }
                });
//2nd step)compose message
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(mailUser));
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