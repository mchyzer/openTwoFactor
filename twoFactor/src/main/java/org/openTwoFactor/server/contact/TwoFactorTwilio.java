/**
 * @author mchyzer
 * $Id: TwoFactorTwilio.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.contact;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.openTwoFactor.server.config.TwoFactorServerConfig;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.factory.SmsFactory;
import com.twilio.sdk.resource.instance.Account;



/**
 * two factor contact interface for twilio, this is generally the primary method
 */
public class TwoFactorTwilio implements TwoFactorContactInterface {

  /**
   * 
   * @return the account
   */
  Account twilioAccount() {
    
    String accountSid = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.twilio.sid");
    String accountAuthToken = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.twilio.authToken");
    
    // Create a rest client
    TwilioRestClient client = new TwilioRestClient(accountSid, accountAuthToken);

    // Get the main account (The one we used to authenticate the client)
    Account mainAccount = client.getAccount();
    
    return mainAccount;
  }
  
  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#text(java.lang.String, java.lang.String)
   */
  @Override
  public void text(String phoneNumber, String text) {
    
    SmsFactory smsFactory = twilioAccount().getSmsFactory();
    Map<String, String> smsParams = new HashMap<String, String>();
    smsParams.put("To", phoneNumber);
    smsParams.put("From", TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.twilio.fromNumber"));
    smsParams.put("Body", text);
    try {
      smsFactory.create(smsParams);
    } catch(TwilioRestException tre) {
      throw new RuntimeException("Error sending text to: " + phoneNumber, tre);
    }
    
  }

  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#voice(java.lang.String, java.lang.String)
   */
  @Override
  public void voice(String phoneNumber, String message) {
    
    final CallFactory callFactory = twilioAccount().getCallFactory();
    final Map<String, String> callParams = new HashMap<String, String>();
    callParams.put("To", phoneNumber); // Replace with a valid phone number
    callParams.put("From", TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.twilio.fromNumber"));
    //    String userText = "Your verification code is: 1, 2, 3, 4, 5, 6.  " +
    //      "Again, your verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
    //      "verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
    //      "verification code is: 1, 2, 3, 4, 5, 6.  ";
    
    try {
      message = URLEncoder.encode(message, "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    //TODO change this to be something local for privacy reasons
    callParams.put("Url", "http://twimlets.com/message?Message%5B0%5D=" + message);

    try {
      callFactory.create(callParams);
    } catch (TwilioRestException tre) {
      throw new RuntimeException("Error sending voice call to: " + phoneNumber, tre);
    }
    
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    String userText = "Your verification code is: 1, 2, 3, 4, 5, 6.  " +
      "Again, your verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      "verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      "verification code is: 1, 2, 3, 4, 5, 6.  ";
    
    new TwoFactorTwilio().voice("215 880 9847", userText);
    
  }

}
