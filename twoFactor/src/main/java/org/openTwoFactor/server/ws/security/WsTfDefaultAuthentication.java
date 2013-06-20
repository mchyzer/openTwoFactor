
/*
 * @author mchyzer
 * $Id: WsTfDefaultAuthentication.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.security;

import javax.servlet.http.HttpServletRequest;

import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;



/**
 * default authentication for grouper if a custom one isnt specified in
 * grouper-ws.properties for non-rampart requests
 */
public class WsTfDefaultAuthentication implements WsTfCustomAuthentication {

  /**
   * 
   * @see WsTfCustomAuthentication#retrieveLoggedInSubjectId(javax.servlet.http.HttpServletRequest)
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws RuntimeException {
    
    // use this to be the user connected, or the user act-as
    String userIdLoggedIn = TwoFactorFilterJ2ee.retrieveUserIdFromRequest(false);

    return userIdLoggedIn;
  }

}
