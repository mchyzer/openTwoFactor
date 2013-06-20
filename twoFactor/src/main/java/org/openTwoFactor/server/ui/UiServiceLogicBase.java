/**
 * 
 */
package org.openTwoFactor.server.ui;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;



/**
 * base class of service logic for UI
 * @author mchyzer
 *
 */
public class UiServiceLogicBase {

  /**
   * hand over control to a jsp in WEB-INF/jsp
   * @param jspNameInWebInfJspDir
   */
  protected void showJsp(String jspNameInWebInfJspDir) {
    
    HttpServletRequest httpServletRequest = TwoFactorFilterJ2ee.retrieveHttpServletRequest();
    HttpServletResponse httpServletResponse = TwoFactorFilterJ2ee.retrieveHttpServletResponse();
    RequestDispatcher dispatcher = httpServletRequest.getRequestDispatcher("/WEB-INF/jsp/" + jspNameInWebInfJspDir);

    if (!httpServletResponse.isCommitted()) {
      httpServletResponse.setHeader("Connection", "close");
    }
    try {
      dispatcher.forward(httpServletRequest, httpServletResponse);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
