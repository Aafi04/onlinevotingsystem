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

        // Read credentials from system properties or environment variables
        // For example, run with: -Dmail.user=your_email@gmail.com -Dmail.pass=your_app_password
        final String user = System.getProperty("mail.user");
        final String pass = System.getProperty("mail.pass");

        if (user == null || user.isEmpty() || pass == null || pass.isEmpty()) {
            System.err.println("Error: Email username or password not set.");
            System.err.println("Please provide credentials via system properties, e.g.,");
            System.err.println("java -Dmail.user=your_email@gmail.com -Dmail.pass=your_app_password com.Email.Mailer");
            return; // Exit if credentials are not available
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