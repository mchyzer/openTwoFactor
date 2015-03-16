/**
 * @author mchyzer
 * $Id: TwoFactorSmsmatrix.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.contact;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * two factor contact interface for smsmatrix
 */
public class TwoFactorSmsmatrix implements TwoFactorContactInterface {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    String userText = "Your verification code is: 1, 2, 3, 4, 5, 6.  " +
      "Again, your verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      "verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      "verification code is: 1, 2, 3, 4, 5, 6.  ";
    
    new TwoFactorSmsmatrix().text("215 880 9847", userText);
    
  }
  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorAudit.class);

  /**
   * username from config
   * @return username from config
   */
  private String smsmatrixUser() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.smsmatrix.user");
  }

  /**
   * pass from config
   * @return pass from config
   */
  private String smsmatrixPass() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.smsmatrix.pass");
  }
  
  /**
   * fromNumber from config, not required
   * @return fromNumber from config
   */
  private String smsmatrixFromNumber() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.smsmatrix.fromNumber");
  }
  
  /**
   * text url from config
   * @return text url from config
   */
  private String smsmatrixTextUrl() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.smsmatrix.textUrl");
  }
  
  /**
   * text to speach url from config
   * @return text to speach url from config
   */
  private String smsmatrixTextToSpeachUrl() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.smsmatrix.textToSpeechUrl");
  }
  
  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#text(java.lang.String, java.lang.String)
   */
  @Override
  public void text(String phoneNumber, String text) {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    String responseString = null;
    Integer statusCode = null;
    try {

      if (LOG.isDebugEnabled()) {
        debugMap.put("operation", "smsmatrixText");
        debugMap.put("phoneNumber", phoneNumber);
        debugMap.put("textLength", StringUtils.defaultString(text).length());
      }

      HttpClient client = new HttpClient();
  
      PostMethod postMethod = new PostMethod(smsmatrixTextUrl());
      
      postMethod.addParameter("username", smsmatrixUser());
      postMethod.addParameter("password", smsmatrixPass());
      postMethod.addParameter("phone", phoneNumber);
      postMethod.addParameter("txt", text);
  
      client.executeMethod(postMethod);

      statusCode = postMethod.getStatusCode();
      if (LOG.isDebugEnabled()) {
        debugMap.put("status", statusCode);
      }
      
      responseString = postMethod.getResponseBodyAsString();
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("response", responseString);
      }
      
      if (postMethod.getStatusCode() != 200) {
        throw new RuntimeException("smsmatrix status was not 200: " + postMethod.getStatusCode());
      }
      
    } catch (Exception e) {
      throw new RuntimeException("Error with smsmatrix: '" + phoneNumber + "', " + responseString, e);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(TwoFactorServerUtils.mapToString(debugMap));
      }
    }


    
  }

  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#voice(java.lang.String, java.lang.String)
   */
  @Override
  public void voice(String phoneNumber, String message) {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    String responseString = null;
    Integer statusCode = null;
    try {

      if (LOG.isDebugEnabled()) {
        debugMap.put("operation", "smsmatrixTextToSpeach");
        debugMap.put("phoneNumber", phoneNumber);
        debugMap.put("textLength", StringUtils.defaultString(message).length());
      }

      HttpClient client = new HttpClient();
  
      PostMethod postMethod = new PostMethod(smsmatrixTextToSpeachUrl());
      
      postMethod.addParameter("username", smsmatrixUser());
      postMethod.addParameter("password", smsmatrixPass());
      postMethod.addParameter("phone", phoneNumber);

      
      String smsmatrixFromNumber = smsmatrixFromNumber();
      if (!StringUtils.isBlank(smsmatrixFromNumber)) {
        postMethod.addParameter("callerid", smsmatrixFromNumber);
      }
      //    String userText = "Your verification code is: 1, 2, 3, 4, 5, 6.  " +
      //      "Again, your verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      //      "verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      //      "verification code is: 1, 2, 3, 4, 5, 6.  ";
      postMethod.addParameter("txt", message);
  
      client.executeMethod(postMethod);

      statusCode = postMethod.getStatusCode();
      if (LOG.isDebugEnabled()) {
        debugMap.put("status", statusCode);
      }
      
      responseString = postMethod.getResponseBodyAsString();
      
      if (LOG.isDebugEnabled()) {
        debugMap.put("response", responseString);
      }
      
      if (postMethod.getStatusCode() != 200) {
        throw new RuntimeException("smsmatrix status was not 200: " + postMethod.getStatusCode());
      }
      
    } catch (Exception e) {
      throw new RuntimeException("Error with smsmatrix: '" + phoneNumber + "', " + responseString, e);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(TwoFactorServerUtils.mapToString(debugMap));
      }
    }
    
  }

}
