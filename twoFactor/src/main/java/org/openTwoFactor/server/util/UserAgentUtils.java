/**
 * @author mchyzer
 * $Id: UserAgentUtils.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;

import eu.bitwalker.useragentutils.UserAgent;
import eu.bitwalker.useragentutils.Version;


/**
 * run the main after updating the userAgentUtils to process browsers again
 */
public class UserAgentUtils {


  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(UserAgentUtils.class);

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    if (args.length != 1 || (!StringUtils.equalsIgnoreCase(args[0], "read")) && !StringUtils.equalsIgnoreCase(args[0], "write")) {
      System.out.println("Pass in arg: read|write");
      return;
    }
    
    //loop through and fix all user agents
    List<TwoFactorUserAgent> twoFactorUserAgents = TwoFactorDaoFactory.getFactory().getTwoFactorUserAgent().retrieveAll();
    
    StringBuilder report = new StringBuilder();
    
    for (TwoFactorUserAgent twoFactorUserAgent : twoFactorUserAgents) {
            
      TwoFactorUserAgent twoFactorUserAgentFromDb = (TwoFactorUserAgent)twoFactorUserAgent.clone();
      
      twoFactorUserAgent.dbVersionReset();
      twoFactorUserAgent.calculateBrowserFields();
      if (twoFactorUserAgent.dbNeedsUpdate()) {
        StringBuilder diffs = new StringBuilder();
        diffs.append("UserAgent diff " + StringUtils.abbreviate(twoFactorUserAgent.getUserAgent(), 20));
        
        if (!StringUtils.equals(twoFactorUserAgent.getOperatingSystem(), twoFactorUserAgentFromDb.getOperatingSystem())) {
          
          diffs.append(", OS from: " + twoFactorUserAgentFromDb.getOperatingSystem() + ", and now: " + twoFactorUserAgent.getOperatingSystem());
          
        }
        
        if (twoFactorUserAgent.getMobile() != twoFactorUserAgentFromDb.getMobile()) {

          diffs.append(", mobileFrom: " + twoFactorUserAgentFromDb.getMobile() + ", and now: " + twoFactorUserAgent.getMobile());

        }

        if (!StringUtils.equals(twoFactorUserAgent.getBrowser(), twoFactorUserAgentFromDb.getBrowser())) {

          diffs.append(", browser: " + twoFactorUserAgentFromDb.getBrowser() + ", and now: " + twoFactorUserAgent.getBrowser());

        }

        if (!TwoFactorServerUtils.equals(twoFactorUserAgent.getVersionNumber(), twoFactorUserAgentFromDb.getVersionNumber())) {

          diffs.append(", versionNumber: " + twoFactorUserAgentFromDb.getVersionNumber() + ", and now: " + twoFactorUserAgent.getVersionNumber());

        }
        report.append(diffs).append("\n");
        if (StringUtils.equalsIgnoreCase(args[0], "write")) {
          twoFactorUserAgent.store(TwoFactorDaoFactory.getFactory(), false);
        }

      }
      
    }
    
    System.out.println(report);
    
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
        StringBuilder result = new StringBuilder(userAgent.getBrowser().getName());
        
        Version version = userAgent == null ? null : userAgent.getBrowserVersion();
        
        if (version != null) {
          if (!StringUtils.isBlank(version.getMajorVersion())) {
            
            //version might already be in there
            if (result.indexOf(version.getMajorVersion()) == -1) {
            
              result.append(" ").append(version.getMajorVersion());
              if (!StringUtils.isBlank(version.getMinorVersion()) && !StringUtils.equals("0", version.getMinorVersion())) {
                result.append(".").append(version.getMinorVersion());
              }
            }
          }
        }
        return result.toString();
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
