package org.openTwoFactor.server.poc;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class EmailPoc {

  /**
   * @param args
   */
  public static void main(String[] args) {

    //new TwoFactorEmail().addTo("mchyzer@isc.upenn.edu")
    //  .assignSubject("subject").assignBody("body " + System.nanoTime()).send();
    sendEmail();

    long start = System.nanoTime();
    
    sendEmail();
    //new TwoFactorEmail().addTo("mchyzer@isc.upenn.edu")
    //  .assignSubject("subject").assignBody("body " + System.nanoTime()).send();

    System.out.println("Mail took: " + ((System.nanoTime() - start) / 1000000L) + "ms");

  }

  public static void sendEmail() {
    // Recipient's email ID needs to be mentioned.
    String to = "mchyzer@isc.upenn.edu";

    // Sender's email ID needs to be mentioned
    String from = "groupersystem@gmail.com";

    // Assuming you are sending email from localhost
    String host = "smtp.gmail.com";

    // Get system properties
    Properties properties = System.getProperties();

    properties.put("mail.smtp.auth", "true");
    properties.put("mail.smtp.starttls.enable", "true");
    properties.put("mail.smtp.port", "587");
    
    // Setup mail server
    properties.setProperty("mail.smtp.host", host);

    // Get the default Session object.
    Session session = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication("groupersystem@gmail.com", "GrouperSystem1");
        
      }
      });

    try{
       // Create a default MimeMessage object.
       MimeMessage message = new MimeMessage(session);

       // Set From: header field of the header.
       message.setFrom(new InternetAddress(from));

       // Set To: header field of the header.
       message.addRecipient(Message.RecipientType.TO,
                                new InternetAddress(to));

       // Set Subject: header field
       message.setSubject("This is the Subject Line!");

       // Now set the actual message
       message.setText("This is actual message: " + System.nanoTime());

       // Send message
       Transport.send(message);
       System.out.println("Sent message successfully....");
    }catch (MessagingException mex) {
       mex.printStackTrace();
    }

  }
  
}
