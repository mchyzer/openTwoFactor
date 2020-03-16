/*
 * @author mchyzer
 */
package org.openTwoFactor.server.email;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.morphString.Morph;


/**
 * use this chaining utility to send email
 */
public class TwoFactorEmail {

  /**
   * 
   */
  private List<File> attachments = new ArrayList<File>();
  
  /**
   * List of blind carbon copy addresses to send the email to.
   */
  private Set<String> bccAddresses = new LinkedHashSet<String>();

  /**
   * List of carbon copy addresses to send the email to.
   */
  private Set<String> ccAddresses = new LinkedHashSet<String>();

  /**
   * keep list emails (max 100) if testing...
   */
  private static List<TwoFactorEmail> testingEmails = new ArrayList<TwoFactorEmail>();
  
  /**
   * 
   * @return the list of emails
   */
  public static List<TwoFactorEmail> testingEmails() {
    return testingEmails;
  }
  
  /** keep count for testing */
  public static long testingEmailCount = 0;
  
  /**
   * List of addresses to send the email to.
   */
  private Set<String> toAddresses = new LinkedHashSet<String>();
  
  /** subject of email */
  private String subject;
  
  /**
   * email address this is from, there is a default if this is not filled in
   */
  private String from;
  
  /** body of email (currently HTML is not supported, only plain text) */
  private String body;
  
  /**
   * Add a blind carbon copy address to the email.
   * Use either a single adress or a comma delimited list of addresses.
   * @param bcc is the email or comma separated list of emails to add.
   * @return the email.
   */
  public TwoFactorEmail addBcc(String bcc) {
    addAddress(this.bccAddresses, bcc);
    return this;
  }

  /**
   * add attachment
   * @param file
   * @return this for chaining
   */
  public TwoFactorEmail addAttachment(File file) {
    this.attachments.add(file);
    return this;
  }
  
  /**
   * Add a carbon copy address to the email.
   * Use either a single adress or a comma delimited list of addresses.
   * @param cc is the email or comma separated list of emails to add.
   * @return the email.
   */
  public TwoFactorEmail addCc(String cc) {
    addAddress(this.ccAddresses, cc);
    return this;
  }

  /**
   * Add a to address to the email.
   * Use either a single adress or a comma delimited list of addresses.
   * @param to is the email or comma separated list of emails to add.
   * @return the email.
   */
  public TwoFactorEmail addTo(String to) {
    addAddress(this.toAddresses, to);
    return this;
  }

  /**
   * Add an email or a list of comma delimited emails to a collection.
   * @param collection is the collection to add the email or emails to.
   * @param addressesToAdd is the address or adresses to add to te collection.
   */
  private void addAddress(Set<String> collection, String addressesToAdd) {
    if (StringUtils.isEmpty(addressesToAdd)) {
      return;
    }
    addressesToAdd = StringUtils.replace(addressesToAdd, ";", ",");

    Set<String> emails = TwoFactorServerUtils.splitTrimToSet(addressesToAdd, ",");
    collection.addAll(emails);
  }

  /**
   * All of the email addresses to which this email will be
   * blind carbon copied.
   * @return the addresses.
   */
  public Set<String> getBccAddresses() {
    return this.bccAddresses;
  }

  /**
   * All of the email addresses to which this email will be
   * carbon copied.
   * @return the addresses.
   */
  public Set<String> getCcAddresses() {
    return this.ccAddresses;
  }

  /**
   * All of the email addresses to which this email will be
   * sent.
   * @return the addresses.
   */
  public Set<String> getToAddresses() {
    return this.toAddresses;
  }

  /**
   * subject of email
   * @return subject
   */
  public String getSubject() {
    return this.subject;
  }

  /**
   * email address this is from
   * @return from
   */
  public String getFrom() {
    return this.from;
  }

  /**
   * body of email (currently HTML is not supported, only plain text)
   * @return body
   */
  public String getBody() {
    return this.body;
  }

  /**
   * 
   */
  public TwoFactorEmail() {
    //empty 
  }
  
  /**
   * set subject
   * @param theSubject
   * @return this for chaining
   */
  public TwoFactorEmail assignSubject(String theSubject) {
    this.subject = theSubject;
    return this;
  }
  
  /**
   * set the body
   * @param theBody
   * @return this for chaining
   */
  public TwoFactorEmail assignBody(String theBody) {
    this.body = theBody;
    return this;
  }
 
  /**
   * set the from address
   * @param theFrom
   * @return the from address
   */
  public TwoFactorEmail assignFrom(String theFrom) {
    this.from = theFrom;
    return this;
  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorEmail.class);

  
  /**
   * try an email
   * @param args
   */
  public static void main(String[] args) {
    new TwoFactorEmail().assignBody("hey").assignSubject("subject").addTo("mchyzer@yahoo.com").send();
  }

  /**
   * from in email address
   * @param theFrom
   */
  public void setFrom(String theFrom) {
    this.from = theFrom;
  }
  
  /**
   * send the email
   */
  public void send() {
    
    try {

      //mail.smtp.server = whatever.school.edu
      //#mail.from.address = noreply@school.edu
      String smtpServer = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server");
      if (StringUtils.isBlank(smtpServer)) {
        throw new RuntimeException("You need to specify the from smtp server mail.smtp.server in grouper.properties");
      }
      
      String theFrom = StringUtils.defaultIfEmpty(this.from, TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.from.address"));
      if (!StringUtils.equals("testing", smtpServer) && StringUtils.isBlank(theFrom)) {
        throw new RuntimeException("You need to specify the from email address mail.from.address in grouper.properties");
      }
      
      String subjectPrefix = StringUtils.defaultString(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.subject.prefix"));
      
      final String SMTP_USER = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.user"); 
      
      String smtpPass = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.pass"); 
      
      final String SMTP_PASS = StringUtils.isBlank(smtpPass) ? null : Morph.decryptIfFile(smtpPass);
      
      Properties properties = System.getProperties();
      
      properties.put("mail.host", smtpServer);
      properties.put("mail.transport.protocol", "smtp");
      
      Authenticator authenticator = null;
  
      //this has never been tested... :)
      if (!StringUtils.isBlank(SMTP_USER)) {
        properties.setProperty("mail.smtp.submitter", SMTP_USER);
        properties.setProperty("mail.smtp.auth", "true");
        
        authenticator = new Authenticator() {
          @Override
          protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
          }
        };
      }
      
      boolean useSsl = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.smtp.ssl", false);
      if (useSsl) {
        
        properties.put("mail.smtp.starttls.enable","true");
        properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        properties.put("mail.smtp.socketFactory.fallback", "false");
        
      }

      if (LOG.isDebugEnabled()) {
        properties.put("mail.smtp.debug", "true");
      }
      
      //leave blank for default (probably 25), if ssl is true, default is 465, else specify
      String port = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.port");
      if (!StringUtils.isBlank(port)) {
        properties.put("mail.smtp.socketFactory.port", port);
      } else {
        if (useSsl) {
          properties.put("mail.smtp.socketFactory.port", "465");
        }
      }

      
      Session session = Session.getInstance(properties, authenticator);
      Message message = new MimeMessage(session);
      
      boolean hasRecipient = false;
// TODO
//      this.toAddresses = new HashSet<String>();
//      this.toAddresses.add("mchyzer@upenn.edu");
      if (TwoFactorServerUtils.length(this.toAddresses) > 0) {
        
        for (String aTo : this.toAddresses) {
          if (!StringUtils.isBlank(aTo)) {
            hasRecipient = true;
            message.addRecipient(RecipientType.TO, new InternetAddress(aTo));
          }
        }
        
      }
      
      if (TwoFactorServerUtils.length(this.ccAddresses) > 0) {
        
        for (String aCc : this.ccAddresses) {
          if (!StringUtils.isBlank(aCc)) {
            hasRecipient = true;
            message.addRecipient(RecipientType.CC, new InternetAddress(aCc));
          }
        }
        
      }
      
      if (TwoFactorServerUtils.length(this.bccAddresses) > 0) {
        
        for (String aBcc : this.bccAddresses) {
          if (!StringUtils.isBlank(aBcc)) {
            hasRecipient = true;
            message.addRecipient(RecipientType.BCC, new InternetAddress(aBcc));
          }
        }
        
      }
      
      if (!hasRecipient) {
        LOG.debug("Cant find recipient for email");
        return;
      }
      
      if (!StringUtils.isBlank(theFrom)) {
        message.addFrom(new InternetAddress[] { new InternetAddress(theFrom) });
      }
      
      String theSubject = StringUtils.defaultString(subjectPrefix) + this.subject;
      message.setSubject(theSubject);
      
      
      testingEmailCount++;
      
      //if you dont have a server, but want to test, then set this
      if (!StringUtils.equals("testing", smtpServer)) {
        // Deal with the attachments.
        if (this.attachments.size() == 0) {
          message.setContent(this.body, "text/plain");
        } else {

          MimeBodyPart messagePart = new MimeBodyPart();
          messagePart.setContent(this.getBody(), "text/plain");

          Multipart entireMessage = new MimeMultipart();
          entireMessage.addBodyPart(messagePart);

          MimeBodyPart mimeBodyPart = null;
          for (File attachmentFile : this.attachments) {
            mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setDataHandler(new DataHandler(new FileDataSource(attachmentFile
                .getAbsolutePath())));
            mimeBodyPart.setFileName(attachmentFile.getName());
            entireMessage.addBodyPart(mimeBodyPart);
          }
          message.setContent(entireMessage);
        }

        Transport.send(message);
      } else {
        LOG.error("Not sending email since smtp server is 'testing'. \nFROM: " + theFrom + "\nSUBJECT: " + theSubject + "\nBODY: " + this.body + "\n");
        synchronized (TwoFactorEmail.class) {
          
          testingEmails.add(this);
          while (testingEmails.size() > 100) {
            testingEmails.remove(0);
          }
          
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  
}
