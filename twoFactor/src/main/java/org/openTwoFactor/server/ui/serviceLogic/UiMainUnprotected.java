/**
 * @author mchyzer
 * $Id: UiMainUnprotected.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.ui.UiServiceLogicBase;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * methods that are used from the UI which arent authenticated
 */
public class UiMainUnprotected extends UiServiceLogicBase {

  
  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void index(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer.retrieveFromRequest();
    
    showJsp("index.jsp");
  }

  /**
   * logout
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer.retrieveFromRequest();
    
    //see if cookies should be removed
    String cookiePrefix = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.logout.cookie.prefix");
    if (!StringUtils.isBlank(cookiePrefix)) {
      String[] cookiePrefixes = TwoFactorServerUtils.splitTrim(cookiePrefix, ",");
      for (String theCookiePrefix : cookiePrefixes) {
        TwoFactorFilterJ2ee.removeCookiesByPrefix(theCookiePrefix);
      }
    }

    
    String logoutUrl = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.logoutUrl");
    
    //redirect to logout url
    try {
      httpServletResponse.sendRedirect(logoutUrl);
    } catch (IOException ioe) {
      throw new RuntimeException("Problem redirecting to url: " + logoutUrl);
    }
  }

}
