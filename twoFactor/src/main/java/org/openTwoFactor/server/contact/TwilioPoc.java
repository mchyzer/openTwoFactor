package org.openTwoFactor.server.contact;


import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;
import com.twilio.sdk.verbs.TwiMLException;

/**
 * (267) 297-1922
 * ACe36b52e662f9f6349be3aac02b4fd0e1
 */
public class TwilioPoc {

  /** */
  public static final String ACCOUNT_SID = "ACe36b52e662f9f6349be3aac02b4fd0e1";

  /** */
  public static final String AUTH_TOKEN = "aa374dff47e9xxxxxxxx82159ddcaf8";

  /**
   * 
   * @param args
   * @throws TwilioRestException
   * @throws TwiMLException
   */
  public static void main(final String[] args) throws TwilioRestException, TwiMLException {

    // Create a rest client
    final TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);

    // Get the main account (The one we used to authenticate the client)
    final Account mainAccount = client.getAccount();

    // Make a call
    final CallFactory callFactory = mainAccount.getCallFactory();
    final Map<String, String> callParams = new HashMap<String, String>();
    callParams.put("To", "2154001116"); // Replace with a valid phone number
    callParams.put("From", "(267) 297-1922"); // Replace with a valid phone number in your account
    String userText = "Your verification code is: 1, 2, 3, 4, 5, 6.  " +
    		"Again, your verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
    		"verification code is: 1, 2, 3, 4, 5, 6.  ";

    try {
      userText = URLEncoder.encode(userText, "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    callParams.put("Url", "http://twimlets.com/message?Message%5B0%5D=" + userText);
    final Call call = callFactory.create(callParams);
    System.out.println(call.getSid());

    // Send an sms
//    final SmsFactory smsFactory = mainAccount.getSmsFactory();
//    final Map<String, String> smsParams = new HashMap<String, String>();
//    smsParams.put("To", "2154001116"); // Replace with a valid phone number
//    smsParams.put("From", "(267) 297-1922"); // Replace with a valid phone number in your account
//    smsParams.put("Body", "This is a test message!");
//    smsFactory.create(smsParams);

    // Make a raw HTTP request to the api... note, this is deprecated style
    //final TwilioRestResponse resp = client.request("/2010-04-01/Accounts", "GET", null);
    //if (!resp.isError()) {
    //  System.out.println(resp.getResponseText());
    //}
  }
}