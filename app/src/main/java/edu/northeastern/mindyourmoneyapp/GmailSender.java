package edu.northeastern.mindyourmoneyapp;

import java.util.Properties;
import java.util.Random;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class GmailSender {

    public static void sendEmail(final String recipientEmail,final String otp) {

        final String senderEmail = "mindyourmoneygroup5@gmail.com";
        final String senderPassword = "utbh iomb wbru fhxy";
        final String subject = "OTP to Reset Your Password";
        final String messagebody = "Hello,\n\nWe received a request to reset your password.\n" +
                "Your One-Time Password (OTP) is: " + otp + "\n\n" +
                "Please enter this OTP in the app to reset your password.\n\n";

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");

                    Session session = Session.getInstance(props, new Authenticator() {
                        @Override
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(senderEmail, senderPassword);
                        }
                    });

                    Message message = new MimeMessage(session);
                    message.setFrom(new InternetAddress(senderEmail));
                    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                    message.setSubject(subject);
                    message.setText(messagebody);

                    Transport.send(message);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // ensures a 6-digit number
        return String.valueOf(otp);
    }

}
