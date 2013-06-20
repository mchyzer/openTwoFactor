/**
 * 
 */
package org.openTwoFactor.server.j2ee;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.daemon.DaemonController;
import org.openTwoFactor.server.subject.TfSubjectIdResolver;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * Extend the servlet to get user info
 * 
 * @author mchyzer
 * 
 */
public class TwoFactorFilterJ2ee implements Filter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(TwoFactorFilterJ2ee.class);

  /**
   * if in request, get the start time
   * @return the start time
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return TwoFactorServerUtils.longValue(requestStartMillis, 0);
  }
  
  /**
   * 
   * @return if logged in user is allowed to act as other users
   */
  public static boolean allowedToActAsOtherUsers() {
    
    String originalLoginid = retrieveUserIdFromRequestOriginalNotActAs();
    
    //see if there is an act as user in the session
    if (!StringUtils.isBlank(originalLoginid)) {
      String adminsAllowedToActAsOtherUsers = TwoFactorServerConfig.retrieveConfig().propertyValueString(
          "twoFactorServer.adminsAllowedToActAsOtherUsers");
      
      //if there are admins configured (e.g. non prod)
      if (!StringUtils.isBlank(adminsAllowedToActAsOtherUsers)) {
        
        Set<String> adminsAllowedToActAsOtherUsersSet = TwoFactorServerUtils.splitTrimToSet(
            adminsAllowedToActAsOtherUsers, ",");

        //if the admins contains the current user
        if (adminsAllowedToActAsOtherUsersSet.contains(originalLoginid)) {
          return true;
        }
        
        //see if loginid is there
        originalLoginid = TwoFactorFilterJ2ee.retrieveUserIdFromRequestOriginalNotActAs(false);
        
        if (adminsAllowedToActAsOtherUsersSet.contains(originalLoginid)) {
          return true;
        }

      }
    }
    return false;

  }

  /**
   * retrieve the person actually logged in, do not consider who is acting as someone else
   * @return the loginid
   */
  public static String retrieveUserIdFromRequestOriginalNotActAs() {
    return retrieveUserIdFromRequestOriginalNotActAs(true);
  }

  /**
   * retrieve the person actually logged in, do not consider who is acting as someone else
   * @param fromUi
   * @return the loginid
   */
  public static String retrieveUserIdFromRequestOriginalNotActAs(boolean fromUi) {

    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    TwoFactorServerUtils
        .assertion(httpServletRequest != null,
            "HttpServletRequest is null, is the Servlet mapped in the web.xml?");
    
    Principal principal = httpServletRequest.getUserPrincipal();
    String principalName = null;
    if (principal == null) {
      principalName = httpServletRequest.getRemoteUser();
      if (TwoFactorServerUtils.isBlank(principalName)) {
        principalName = (String)httpServletRequest.getAttribute("REMOTE_USER");
      }
    } else {
      principalName = principal.getName();
    }

    //see if we need to resolve the subject id
    if (fromUi && !StringUtils.isBlank(principalName) 
        && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.resolveOnUiLogin", true)) {
      principalName = TfSubjectIdResolver.resolveSubjectId(principalName);
    }
    
    return principalName;
  }
  
  /**
   * retrieve the user principal (who is authenticated) from the (threadlocal)
   * request object
   * 
   * @return the user principal name
   */
  public static String retrieveUserIdFromRequest() {
    return retrieveUserIdFromRequest(true);
  }

  
  /**
   * retrieve the user principal (who is authenticated) from the (threadlocal)
   * request object
   * @param fromUi if this is from the UI
   * @return the user principal name
   */
  public static String retrieveUserIdFromRequest(boolean fromUi) {

    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    
    String principalName = retrieveUserIdFromRequestOriginalNotActAs(fromUi);
    
    if (allowedToActAsOtherUsers()) {
      
      String urlActAsLoginid = httpServletRequest.getParameter("tfBackdoorLoginName");

      if (httpServletRequest.getParameterMap().keySet().contains("tfBackdoorLoginName")) {

        HttpSession httpSession = httpServletRequest.getSession(true);
        
        //if there is a logind passed in the url
        if (!StringUtils.isBlank(urlActAsLoginid)) {
          
          //see if we need to resolve the subject id
          if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.resolveOnUiBackdoorLogin", true)) {
            urlActAsLoginid = TfSubjectIdResolver.resolveSubjectId(urlActAsLoginid);
          }

          httpSession.setAttribute("twoFactorActAsUserId", urlActAsLoginid);
          
        } else {
          
          httpSession.removeAttribute("twoFactorActAsUserId");
        }
        
        
      }
      HttpSession httpSession = httpServletRequest.getSession(false);
      
      //if we have a session and there is an actac in there
      if (httpSession != null) {
        
        String actAsLogind = (String)httpSession.getAttribute("twoFactorActAsUserId");
        if (!StringUtils.isBlank(actAsLogind)) {
          principalName = actAsLogind;
        }
      }
    }
    
    TwoFactorServerUtils.assertion(TwoFactorServerUtils.isNotBlank(principalName),
        "There is no user logged in, make sure the container requires authentication");
    return principalName;
  }

  /**
   * thread local for servlet
   */
  private static ThreadLocal<HttpServlet> threadLocalServlet = new ThreadLocal<HttpServlet>();

  /**
   * thread local for request
   */
  private static ThreadLocal<HttpServletRequest> threadLocalRequest = new ThreadLocal<HttpServletRequest>();

  /**
   * thread local for original request
   */
  private static ThreadLocal<HttpServletRequest> threadLocalOriginalRequest = new ThreadLocal<HttpServletRequest>();

  /**
   * thread local for request
   */
  private static ThreadLocal<Long> threadLocalRequestStartMillis = new ThreadLocal<Long>();

  /**
   * thread local for response
   */
  private static ThreadLocal<HttpServletResponse> threadLocalResponse = new ThreadLocal<HttpServletResponse>();

  /**
   * public method to get the http servlet request
   * 
   * @return the http servlet request
   */
  public static HttpServletRequest retrieveHttpServletRequest() {
    return threadLocalRequest.get();
  }

  /**
   * public method to get the original http servlet request
   * 
   * @return the original http servlet request
   */
  public static HttpServletRequest retrieveOriginalHttpServletRequest() {
    return threadLocalOriginalRequest.get();
  }

  /**
   * public method to get the http servlet
   * 
   * @return the http servlet
   */
  public static HttpServlet retrieveHttpServlet() {
    return threadLocalServlet.get();
  }

  /**
   * public method to get the http servlet
   * 
   * @param httpServlet is servlet to assign
   */
  public static void assignHttpServlet(HttpServlet httpServlet) {
    threadLocalServlet.set(httpServlet);
  }

  /**
   * public method to get the http servlet request
   * 
   * @param httpServletRequest is servlet to assign
   */
  public static void assignHttpServletRequest(HttpServletRequest httpServletRequest) {
    threadLocalRequest.set(httpServletRequest);
  }

  /**
   * public method to get the http servlet request
   * 
   * @return the http servlet request
   */
  public static HttpServletResponse retrieveHttpServletResponse() {
    return threadLocalResponse.get();
  }

  /**
   * filter method
   */
  public void destroy() {
    // not needed

  }

  /**
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    try {
  
      threadLocalOriginalRequest.set((HttpServletRequest) request);

      //wrap this for single valued params or whatever
      request = new TfHttpServletRequest((HttpServletRequest)request);
      
      //servlet will set this...
      threadLocalServlet.remove();
      threadLocalRequest.set((HttpServletRequest) request);
      threadLocalResponse.set((HttpServletResponse) response);
      threadLocalRequestStartMillis.set(System.currentTimeMillis());
    
      filterChain.doFilter(request, response);
    } catch (RuntimeException re) {
      LOG.info("error in request", re);
      throw re;
    } finally {
      threadLocalRequest.remove();
      threadLocalOriginalRequest.remove();
      threadLocalResponse.remove();
      threadLocalRequestStartMillis.remove();
      threadLocalServlet.remove();
      
    }

  }
  
  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig arg0) throws ServletException {
    try {
      DaemonController.scheduleJobsOnce();
    } catch (RuntimeException re) {
      LOG.error("error", re);
      throw re;
    }
  }


}
