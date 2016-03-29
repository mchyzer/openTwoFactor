/**
 * @author tacketal
 * $Id: TwoFactorNexmo.java,v 1.1 2014/02/28 06:02:51 tacketal Exp $
 */
package org.openTwoFactor.server.contact;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.openTwoFactor.server.config.TwoFactorServerConfig;

import com.nexmo.messaging.sdk.NexmoSmsClient;
import com.nexmo.messaging.sdk.NexmoSmsClientSSL;
import com.nexmo.messaging.sdk.SmsSubmissionResult;
import com.nexmo.messaging.sdk.messages.TextMessage;



/**
 * two factor contact interface for twilio
 */
public class TwoFactorNexmo implements TwoFactorContactInterface {
  
  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#text(java.lang.String, java.lang.String)
   */
  @Override
  public void text(String phoneNumber, String text) {
    
    String accountKey = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.nexmo.key");
    String accountSecret = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.nexmo.secret");
    String fromNumber = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.nexmo.fromNumber");
    
    // Create a client for submitting to Nexmo
	NexmoSmsClient client = null;
    try {
        client = new NexmoSmsClientSSL(accountKey, accountSecret);
    } catch (Exception e) {
        System.err.println("Failed to instanciate a Nexmo Client");
        e.printStackTrace();
        throw new RuntimeException("Failed to instanciate a Nexmo Client");
    }
    
    // create text message
    // format the outgoing number to international format 
    phoneNumber = phoneNumber.replaceAll("\\+", "");
    phoneNumber = phoneNumber.replaceAll("-", "");
    phoneNumber = phoneNumber.replaceAll("\\(", "");
    phoneNumber = phoneNumber.replaceAll("\\)", "");
    phoneNumber = phoneNumber.replaceAll(" ", "");
    TextMessage txtMsg = new TextMessage(fromNumber, phoneNumber, text);
    
    // Use the Nexmo client to submit the Text Message ...
	SmsSubmissionResult[] results = null;
    try {
        results = client.submitMessage(txtMsg);
    } catch (Exception e) {
        System.err.println("Failed to communicate with the Nexmo Client");
        e.printStackTrace();
        throw new RuntimeException("Failed to communicate with the Nexmo Client");
    }

    // Evaluate the results of the submission attempt ...
    /*System.out.println("... Message submitted in [ " + results.length + " ] parts");
    for (int i=0;i<results.length;i++) {
        System.out.println("--------- part [ " + (i + 1) + " ] ------------");
        System.out.println("Status [ " + results[i].getStatus() + " ] ...");
        if (results[i].getStatus() == SmsSubmissionResult.STATUS_OK)
            System.out.println("SUCCESS");
        else if (results[i].getTemporaryError())
            System.out.println("TEMPORARY FAILURE - PLEASE RETRY");
        else
            System.out.println("SUBMISSION FAILED!");
        System.out.println("Message-Id [ " + results[i].getMessageId() + " ] ...");
        System.out.println("Error-Text [ " + results[i].getErrorText() + " ] ...");

        if (results[i].getMessagePrice() != null)
            System.out.println("Message-Price [ " + results[i].getMessagePrice() + " ] ...");
        if (results[i].getRemainingBalance() != null)
            System.out.println("Remaining-Balance [ " + results[i].getRemainingBalance() + " ] ...");
    }*/
    
  }

  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#voice(java.lang.String, java.lang.String)
   */
  @Override
  public void voice(String phoneNumber, String message) {
    
    String accountKey = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.nexmo.key");
    String accountSecret = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.nexmo.secret");
    String fromNumber = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.nexmo.fromNumber");
    
    // Create a client for submitting to Nexmo
	NexmoSmsClient client = null;
    try {
        client = new NexmoSmsClient("https://rest.nexmo.com/tts/xml", accountKey, accountSecret, 5000, 30000, false, "", true);
    } catch (Exception e) {
        System.err.println("Failed to instanciate a Nexmo Client");
        e.printStackTrace();
        throw new RuntimeException("Failed to instanciate a Nexmo Client");
    }
    
    // create message
    // format the outgoing number to international format 
    phoneNumber = phoneNumber.replaceAll("\\+", "");
    phoneNumber = phoneNumber.replaceAll("-", "");
    phoneNumber = phoneNumber.replaceAll("\\(", "");
    phoneNumber = phoneNumber.replaceAll("\\)", "");
    phoneNumber = phoneNumber.replaceAll(" ", "");
    TextMessage txtMsg = new TextMessage(fromNumber, phoneNumber, message);
    
    // Use the Nexmo client to submit the Text Message ...
	SmsSubmissionResult[] results = null;
    try {
        results = client.submitMessage(txtMsg);
    } catch (Exception e) {
        System.err.println("Failed to communicate with the Nexmo Client");
        e.printStackTrace();
        throw new RuntimeException("Failed to communicate with the Nexmo Client");
    }

    // Evaluate the results of the submission attempt ...
    /*System.out.println("... Message submitted in [ " + results.length + " ] parts");
    for (int i=0;i<results.length;i++) {
        System.out.println("--------- part [ " + (i + 1) + " ] ------------");
        System.out.println("Status [ " + results[i].getStatus() + " ] ...");
        if (results[i].getStatus() == SmsSubmissionResult.STATUS_OK)
            System.out.println("SUCCESS");
        else if (results[i].getTemporaryError())
            System.out.println("TEMPORARY FAILURE - PLEASE RETRY");
        else
            System.out.println("SUBMISSION FAILED!");
        System.out.println("Message-Id [ " + results[i].getMessageId() + " ] ...");
        System.out.println("Error-Text [ " + results[i].getErrorText() + " ] ...");

        if (results[i].getMessagePrice() != null)
            System.out.println("Message-Price [ " + results[i].getMessagePrice() + " ] ...");
        if (results[i].getRemainingBalance() != null)
            System.out.println("Remaining-Balance [ " + results[i].getRemainingBalance() + " ] ...");
    }*/
    
  }

}
