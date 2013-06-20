
/*
 * @author mchyzer
 * $Id: WsTfCustomAuthentication.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.security;

import javax.servlet.http.HttpServletRequest;

import org.openTwoFactor.server.ws.rest.TfRestInvalidRequest;



/**
 * <pre>
 * implement this interface and provide the class to the classpath and grouper-ws.properties
 * to override the default of httpServletRequest.getUserPrincipal();
 * for non-Rampart authentication
 * 
 * if user is not found, throw a runtime exception.  Could be WsInvalidQueryException
 * which is a type of runtime exception (experiment and see what you want the response to 
 * look like)
 * 
 * </pre>
 */
public interface WsTfCustomAuthentication {
  
  /**
   * retrieve the current username (subjectId) from the request object.
   * @param httpServletRequest
   * @return the logged in username (subjectId)
   * @throws TfRestInvalidRequest if there is a problem
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
    throws TfRestInvalidRequest;
  
}
