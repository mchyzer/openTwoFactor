/**
 * @author mchyzer
 * $Id: TwoFactorRestServlet.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.j2ee;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.log4j.NDC;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.daemon.DaemonController;
import org.openTwoFactor.server.subject.TfSubjectIdResolver;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.corebeans.TfResultProblem;
import org.openTwoFactor.server.ws.corebeans.TwoFactorResponseBeanBase;
import org.openTwoFactor.server.ws.rest.TfRestContentType;
import org.openTwoFactor.server.ws.rest.TfRestHttpMethod;
import org.openTwoFactor.server.ws.rest.TfRestInvalidRequest;
import org.openTwoFactor.server.ws.rest.TfWsVersion;
import org.openTwoFactor.server.ws.security.WsTfCustomAuthentication;
import org.openTwoFactor.server.ws.security.WsTfDefaultAuthentication;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;



/**
 *
 */
@SuppressWarnings("serial")
public class TwoFactorRestServlet extends HttpServlet {

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    super.init();
    DaemonController.scheduleJobsOnce();

  }

  /**
   * retrieve the subject logged in to web service
   * 
   * @return the subject
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
  public static String retrievePrincipalLoggedIn() {
    String authenticationClassName = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "ws.security.authentication.class");

    if (StringUtils.isBlank(authenticationClassName)) {
      authenticationClassName = WsTfDefaultAuthentication.class.getName();
    }
    
    String userIdLoggedIn = null;

    //this is for container auth (or custom auth, non-rampart)
    //get an instance
    Class<? extends WsTfCustomAuthentication> theClass = TwoFactorServerUtils
        .forName(authenticationClassName);

    WsTfCustomAuthentication wsAuthentication = TwoFactorServerUtils.newInstance(theClass);

    userIdLoggedIn = wsAuthentication
        .retrieveLoggedInSubjectId(TwoFactorFilterJ2ee.retrieveHttpServletRequest());

    // cant be blank!
    if (StringUtils.isBlank(userIdLoggedIn)) {
      //server is having trouble if got this far, but also the user's fault
      throw new TfRestInvalidRequest("No user is logged in");
    }

    //see if we need to resolve the subject id
    if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.resolveOnWsLogin", true)) {
      userIdLoggedIn = TfSubjectIdResolver.resolveSubjectId(userIdLoggedIn);
    }
    
    //puts it in the log4j ndc context so userid is logged
    if (NDC.getDepth() == 0) {
      StringBuilder ndcBuilder = new StringBuilder("< ");
      ndcBuilder.append(userIdLoggedIn).append(" - ");
      HttpServletRequest request = TwoFactorFilterJ2ee.retrieveHttpServletRequest();
      if (request != null) {
        ndcBuilder.append(request.getRemoteAddr());
      }
      ndcBuilder.append(" >");
      NDC.push(ndcBuilder.toString());
    }
    
    return userIdLoggedIn;

  }

  /**
   * authorization.  make sure the logged in principal is in the authz group
   * for the tf server.  maybe check IP address too
   */
  public static void assertLoggedOnPrincipalTfServer() {
    String principal = retrievePrincipalLoggedIn();
    if (StringUtils.isBlank(principal)) {
      throw new TfRestInvalidRequest("No logged in user to WS!");
    }
    
    Map<String, String> userToNetworks = TwoFactorServerConfig.retrieveConfig().tfServerAuthz();
    
    if (!userToNetworks.containsKey(principal)) {
      StringBuilder error = new StringBuilder("User '" + principal + "' is not allowed to use the web service: "
          + userToNetworks.size() + " users are allowed: ");
      for (String userName: userToNetworks.keySet()) {
        error.append(", ").append(userName);
      }
      throw new TfRestInvalidRequest(error.toString());
    }
    
    String networks = userToNetworks.get(principal);
    
    if (!StringUtils.isBlank(networks)) {

      HttpServletRequest httpServletRequest = TwoFactorFilterJ2ee.retrieveHttpServletRequest();
      
      String clientIpAddress = httpServletRequest.getRemoteAddr();

      //if its equal, thats ok, might be ipv6
      if (!StringUtils.equals(networks, clientIpAddress)) {

        if (!TwoFactorServerUtils.ipOnNetworks(clientIpAddress, networks)) {

          throw new TfRestInvalidRequest("User '" + principal + "' is not allowed to use the web service from ip address: " + clientIpAddress);
          
        }
      }
    }
    
  }
  
  
  /** when this servlet was started */
  private static long startupTime = System.currentTimeMillis();

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorRestServlet.class);

  /**
   * @return the startupTime
   */
  public static long getStartupTime() {
    return TwoFactorRestServlet.startupTime;
  }

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings("unchecked")
  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // if the WS should run in this env
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.runWs", true)) {
      throw new RuntimeException("WS doesnt run in this env per: twoFactorServer.runWs");
    }
    
    long serviceStarted = System.nanoTime();

    request = new TfHttpServletRequest(request);
    TwoFactorFilterJ2ee.assignHttpServletRequest(request);
    
    TwoFactorFilterJ2ee.assignHttpServlet(this);
    List<String> urlStrings = null;
    StringBuilder warnings = new StringBuilder();

    TwoFactorResponseBeanBase twoFactorResponseBeanBase = null;
    
    //we need something here if errors, so default to xhtml
    TfRestContentType wsRestContentType = TfRestContentType.json;
    TfRestContentType.assignContentType(wsRestContentType);

    boolean logRequestsResponses = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.ws.log.requestsResponses", false);

    boolean indent = false;
    
    String body = null;
    
    try {
      
      if (TwoFactorServerUtils.booleanValue(request.getParameter("indent"), false)) {
        indent = true;
      }
      
      //init params (if problem, exception will be thrown)
      request.getParameterMap();
      
      urlStrings = extractUrlStrings(request);
      int urlStringsLength = TwoFactorServerUtils.length(urlStrings);

      //get the body and convert to an object
      body = TwoFactorServerUtils.toString(request.getReader());

      TfWsVersion clientVersion = null;

      //get the method and validate (either from object, or HTTP method
      TfRestHttpMethod tfRestHttpMethod = null;
      {
        String methodString = request.getMethod();
        tfRestHttpMethod = TfRestHttpMethod.valueOfIgnoreCase(methodString, true);
      }
      
      //if there are other content types, detect them here
      boolean foundContentType = false;
      if (request.getRequestURI().endsWith(".xml")) {
        wsRestContentType = TfRestContentType.xml;
        foundContentType = true;
      } else if (request.getRequestURI().endsWith(".json")) {
        wsRestContentType = TfRestContentType.json;
        foundContentType = true;
      }
      TfRestContentType.assignContentType(wsRestContentType);

      if (foundContentType && urlStringsLength > 0) {
        
        String lastUrlString = urlStrings.get(urlStringsLength-1);
        if (lastUrlString.endsWith("." + wsRestContentType.name())) {
          lastUrlString = lastUrlString.substring(0, lastUrlString.length()-(1+wsRestContentType.name().length()));
        }
        urlStrings.set(urlStringsLength-1, lastUrlString);
      }
      
      if (urlStringsLength == 0) {
        
        if (tfRestHttpMethod != TfRestHttpMethod.GET) {
          throw new TfRestInvalidRequest("Cant have non-GET method for default resource: " + tfRestHttpMethod);
        }
        
        if (foundContentType) {
          
          //twoFactorResponseBeanBase = new AsasDefaultVersionResourceContainer();
          throw new TfRestInvalidRequest("Invalid request");
        }
        //twoFactorResponseBeanBase = new AsasDefaultResourceContainer();
        throw new TfRestInvalidRequest("Invalid request");

      }
        
      if (!foundContentType) {
        throw new TfRestInvalidRequest("Request must end in .json or .xml: " + request.getRequestURI());
      }
      
      //first see if version
      clientVersion = TfWsVersion.valueOfIgnoreCase(TwoFactorServerUtils.popUrlString(urlStrings), true);

      TfWsVersion.assignCurrentClientVersion(clientVersion, warnings);
      
//      WsRequestBean requestObject = null;
//
//      if (!StringUtils.isBlank(body)) {
//        requestObject = (WsRequestBean) wsRestRequestContentType.parseString(body,
//            warnings);
//      }
//      
//      //might be in params (which might not be in body
//      if (requestObject == null) {
//        //might be in http params...
//        requestObject = (WsRequestBean) GrouperServiceUtils.marshalHttpParamsToObject(
//            request.getParameterMap(), request, warnings);
//
//      }
                  
      twoFactorResponseBeanBase = tfRestHttpMethod.service(urlStrings, request.getParameterMap(), body);
    } catch (TfRestInvalidRequest arir) {

      twoFactorResponseBeanBase = new TfResultProblem();
      String error = arir.getMessage() + ", " + requestDebugInfo(request);

      //this is a user error, but an error nonetheless
      LOG.error(error, arir);

      twoFactorResponseBeanBase.setErrorMessage(error + TwoFactorServerUtils.getFullStackTrace(arir));
      twoFactorResponseBeanBase.setSuccess(false);

    } catch (RuntimeException e) {

      //this is not a user error, is a big problem

      twoFactorResponseBeanBase = new TfResultProblem();
      LOG.error("Problem with request: " + requestDebugInfo(request), e);
      twoFactorResponseBeanBase.setErrorMessage("Problem with request: "
          + requestDebugInfo(request) + ",\n" + TwoFactorServerUtils.getFullStackTrace(e));
      twoFactorResponseBeanBase.setSuccess(false);

    }
    
    //set http status code, content type, and write the response
    StringBuilder urlBuilder = null;
    String responseString = null;
    String responseStringForLog = null;

    try {
      { 
        urlBuilder = new StringBuilder();
        {
          String url = request.getRequestURL().toString();
          url = TwoFactorServerUtils.prefixOrSuffix(url, "?", true);
          urlBuilder.append(url);
        }
        //lets put the params back on (the ones we expect)
        Map<String, String> paramMap = request.getParameterMap();
        boolean firstParam = true;
        for (String paramName : paramMap.keySet()) {
          if (firstParam) {
            urlBuilder.append("?");
          } else {
            urlBuilder.append("&");
          }
          firstParam = false;
          
          urlBuilder.append(TwoFactorServerUtils.escapeUrlEncode(paramName))
            .append("=").append(TwoFactorServerUtils.escapeUrlEncode(paramMap.get(paramName)));
          
        }
        
      }
      if (warnings.length() > 0) {
        twoFactorResponseBeanBase.appendWarning(warnings.toString());
      }

      {
        Set<String> unusedParams = ((TfHttpServletRequest)request).unusedParams();
        //add warnings about unused params
        if (TwoFactorServerUtils.length(unusedParams) > 0) {
          for (String unusedParam : unusedParams) {
            twoFactorResponseBeanBase.appendWarning("Unused HTTP param: " + unusedParam);
          }
        }
      }     
      
      //structure name
      //twoFactorResponseBeanBase.setStructureName(TwoFactorServerUtils.structureName(twoFactorResponseBeanBase.getClass()));
      
      //headers should be there by now
      //set the status code
      //response.setStatus(twoFactorResponseBeanBase.getResponseMeta().getHttpStatusCode());
      response.setStatus(200);

      String restCharset = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.restHttpContentTypeCharset", "UTF-8");
      String responseContentType = wsRestContentType.getContentType();
      
      if (!TwoFactorServerUtils.isBlank(restCharset)) {
        responseContentType += "; charset=" + restCharset;
      }
      
      response.setContentType(responseContentType);

      //temporarily set to uuid, so we can time the content generation
      long millisUuid = -314253647586987L;
      
      //twoFactorResponseBeanBase.getResponseMeta().setMillis(millisUuid);
      
      responseString = wsRestContentType.writeString(twoFactorResponseBeanBase);
      
      if (indent) {
        responseString = wsRestContentType.indent(responseString);
        if (logRequestsResponses) {
          responseStringForLog = responseString;
        }
      } else {
        if (logRequestsResponses) {
          responseStringForLog = wsRestContentType.indent(responseString);
        }
      }
      
      responseString = TwoFactorServerUtils.replace(responseString, Long.toString(millisUuid), Long.toString(((System.nanoTime()-serviceStarted) / 1000000)));
      
      try {
        response.getWriter().write(responseString);
      } catch (IOException ioe) {
        throw new RuntimeException(ioe);
      }
      
    } catch (RuntimeException re) {
      //problem!
      LOG.error("Problem with request: " + requestDebugInfo(request), re);
    } finally {

      TwoFactorServerUtils.closeQuietly(response.getWriter());
      TfWsVersion.removeCurrentClientVersion();
      TfRestContentType.clearContentType();

    }
    
    HttpSession httpSession = request.getSession(false);
    if (httpSession != null) {
      httpSession.invalidate();
    }
    
    //lets write the response to file
    if (logRequestsResponses) {
      //make a file:
      StringBuilder fileContents = new StringBuilder();
      Date currentDate = new Date();
      fileContents.append("Timestamp: ").append(currentDate).append("\n");
      fileContents.append("Millis: ").append(Long.toString(((System.nanoTime()-serviceStarted) / 1000000))).append("\n");
      fileContents.append("URL: ").append(urlBuilder).append("\n");
      fileContents.append("Request body: ").append(body).append("\n\n");
      fileContents.append("Response body: ").append(responseStringForLog).append("\n\n");
      String tempDirLocation = TwoFactorServerUtils.tempFileDirLocation();
      
      File logdir = new File(tempDirLocation + "wsLogs");
      TwoFactorServerUtils.mkdirs(logdir);
      
      long myRequestIndex = 0;
      synchronized(TwoFactorRestServlet.class) {
        myRequestIndex = ++requestIndex;
      }
      
      String usernameForFilePath = "";
      
      //maybe we want userids in the file name
      if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.ws.log.requestsResponsesLogSubjectId", false)) {
        
        String username = request.getParameter("username");
        
        if (!StringUtils.isBlank(username)) {
          String subjectAttributeName = TwoFactorServerConfig.retrieveConfig()
              .propertyValueString("twoFactorServer.ws.log.requestsResponsesLogSubjectAttribute");
          
          //maybe we are using netId instead of an opaque id
          if (!StringUtils.isBlank(subjectAttributeName)) {
            
            try {
              Subject subject = SourceManager.getInstance()
                  .getSource(TfSourceUtils.SOURCE_NAME).getSubjectByIdOrIdentifier(username, false);

              if (subject != null) {
                String attributeValue = subject.getAttributeValue(subjectAttributeName);
                if (!StringUtils.isBlank(attributeValue)) {
                  usernameForFilePath = "_" + TwoFactorServerUtils.validFileName(attributeValue);
                }
              }
              
            } catch (RuntimeException re) {
              LOG.error("Error finding subject: " + username, re);
            }
            
          }
          if (StringUtils.isBlank(usernameForFilePath) ) {
            usernameForFilePath = "_" + TwoFactorServerUtils.validFileName(username);
          }
          
        }
        
      }
      
      String logfileName = tempDirLocation + "wsLogs" + File.separator + TwoFactorServerUtils.timestampToFileString(currentDate) + "_" + myRequestIndex + usernameForFilePath + ".txt";
      File file = new File(logfileName);
      file.createNewFile();
      TwoFactorServerUtils.saveStringIntoFile(file, fileContents.toString());
      
    }
    
  }

  /** unique id for requests */
  private static long requestIndex = 0;
  
  /**
   * for error messages, get a detailed report of the request
   * @param request
   * @return the string of descriptive result
   */
  public static String requestDebugInfo(HttpServletRequest request) {
    StringBuilder result = new StringBuilder();
    result.append(" uri: ").append(request.getRequestURI());
    result.append(", HTTP method: ").append(((TfHttpServletRequest)request).getOriginalMethod());
    if (!TwoFactorServerUtils.isBlank(request.getParameter("method"))) {
      result.append(", HTTP param method: ").append(request.getParameter("method"));
    }
    result.append(", decoded url strings: ");
    List<String> urlStrings = extractUrlStrings(request);
    int urlStringsLength = TwoFactorServerUtils.length(urlStrings);
    if (urlStringsLength == 0) {
      result.append("[none]");
    } else {
      for (int i = 0; i < urlStringsLength; i++) {
        result.append(i).append(": '").append(urlStrings.get(i)).append("'");
        if (i != urlStringsLength - 1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }

  /**
   * take a request and get the list of url strings for the rest web service
   * @see #extractUrlStrings(String)
   * @param request is the request to get the url strings out of
   * @return the list of url strings
   */
  private static List<String> extractUrlStrings(HttpServletRequest request) {
    String requestResourceFull = request.getRequestURI();
    return extractUrlStrings(requestResourceFull);
  }

  /**
   * <pre>
   * take a request uri and break up the url strings not including the app name or servlet
   * this does not include the url params (if applicable)
   * if the input is: grouper-ws/servicesRest/xhtml/v1_3_000/groups/members
   * then the result is a list of size 2: {"group", "members"}
   * 
   * </pre>
   * @param requestResourceFull
   * @return the url strings
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = TwoFactorServerUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();

    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(TwoFactorServerUtils.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
