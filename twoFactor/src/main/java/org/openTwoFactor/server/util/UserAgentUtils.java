/**
 * @author mchyzer
 * $Id: UserAgentUtils.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;

import nl.bitwalker.useragentutils.UserAgent;

import org.apache.commons.logging.Log;


/**
 *
 */
public class UserAgentUtils {


  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(UserAgentUtils.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //request.getHeader("User-Agent")
  }
  
  /**
   * get the operating system
   * @param userAgentString
   * @return the operating system
   */
  public String operatingSystem(String userAgentString) {
    try {
      UserAgent userAgent = userAgent(userAgentString);
      if (userAgent != null && userAgent.getOperatingSystem() != null) {
        return userAgent.getOperatingSystem().getName();
      }
    } catch (Exception e) {
      LOG.info("Error looking up user agent: '" + userAgentString + "'", e);
    }
    return null;
  }

  /**
   * see if mobile device
   * @param userAgentString
   * @return if mobile or null if dont know
   */
  public Boolean mobile(String userAgentString) {
    try {
      UserAgent userAgent = userAgent(userAgentString);
      if (userAgent != null && userAgent.getOperatingSystem() != null) {
        return userAgent.getOperatingSystem().isMobileDevice();
      }
    } catch (Exception e) {
      LOG.info("Error looking up user agent: '" + userAgentString + "'", e);
    }
    return null;
    
  }

  /**
   * 
   * @param userAgentString
   * @return browser
   */
  public String browser(String userAgentString) {
    try {
      
      UserAgent userAgent = userAgent(userAgentString);
      if (userAgent != null && userAgent.getBrowser() != null) {
        return userAgent.getBrowser().getName();
      }
      
    } catch (Exception e) {
      LOG.info("Error looking up user agent: '" + userAgentString + "'", e);
    }
    return null;
  }
  
  /**
   * 
   * @param userAgentString
   * @return the user agent
   */
  private UserAgent userAgent(String userAgentString) {
    UserAgent userAgent = UserAgent.parseUserAgentString(userAgentString);
    return userAgent;
  }
  
}
