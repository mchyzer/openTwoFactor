/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: TwoFactorClientWs.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client.ws;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.openTwoFactor.client.contentType.TfClientRestContentType;
import org.openTwoFactor.client.corebeans.TwoFactorResponseBeanBase;
import org.openTwoFactor.client.exceptions.TwoFactorClientWsException;
import org.openTwoFactor.client.util.TwoFactorClientConfig;
import org.openTwoFactor.client.util.TwoFactorClientLog;
import org.openTwoFactor.client.util.TwoFactorClientUtils;
import org.openTwoFactor.clientExt.edu.internet2.middleware.morphString.Crypto;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.Credentials;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.Header;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.HttpClient;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.HttpMethodBase;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.HttpStatus;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.auth.AuthScope;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.methods.PostMethod;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.methods.StringRequestEntity;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.params.DefaultHttpParams;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.params.HttpMethodParams;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.protocol.Protocol;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.protocol.SecureProtocolSocketFactory;
import org.openTwoFactor.clientExt.org.apache.commons.logging.Log;


/**
 * this is the client that all requests go through.  if you add an instance field, make sure to add to copyFrom()
 * @param <T> is the result type
 */
public class TwoFactorClientWs<T extends TwoFactorResponseBeanBase> {
  
  /**
   * 
   */
  private HttpMethodBase method;
  
  /** */
  private String response;
  
  /**
   * logger
   */
  private static Log LOG = TwoFactorClientUtils.retrieveLog(TwoFactorClientWs.class);

  /**
   * 
   */
  public TwoFactorClientWs() {
  }
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentRequest = null;
  
  /** keep a reference to the most recent for testing */
  public static String mostRecentResponse = null;

  /** keep a reference to the most recent for testing */
  public static int mostRecentHttpStatusCode = -1;

  /** keep a reference to the most recent for testing */
  public static HttpMethodBase mostRecentHttpMethod = null;

  /**
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @param asacRestContentType 
   * @param expectedResultClass is the class that the result should be
   * @param asacRestHttpMethod 
   * @param params are params for the WS
   * @return the response object
   */
  public T executeService(final String urlSuffix, final Object toSend, 
      final String labelForLog, final String clientVersion,  
      TfClientRestContentType asacRestContentType, 
      Class<? extends TwoFactorResponseBeanBase> expectedResultClass,
      TfClientRestHttpMethod asacRestHttpMethod, Map<String, String> params)  {
    
    String url = TwoFactorClientConfig.retrieveConfig().propertyValueStringRequired("twoFactorClient.webService.url");
    
    //copy the standardApi client ws instance to this
    
    //if not last connection then throw exception if not success.  If last connection then return the object
    return executeServiceHelper(url, 
        urlSuffix, toSend, labelForLog, clientVersion, asacRestContentType, expectedResultClass,
        asacRestHttpMethod, params);
    
  }

  /**
   * @param url to hit, could be multiple
   * @param urlSuffix e.g. groups/aStem:aGroup/members
   * @param toSend is the bean which will transform into XML, or just a string of XML to send...
   * @param labelForLog label if the request is logged to file
   * @param clientVersion 
   * @param tfClientRestContentType 
   * @param expectedResultClass is the class which is expected
   * @param tfClientRestHttpMethod
   * @param params are params for the WS
   * @return the response object
   */
  private T executeServiceHelper(String url, String urlSuffix, Object toSend, String labelForLog, String clientVersion,
      TfClientRestContentType tfClientRestContentType, 
      Class<? extends TwoFactorResponseBeanBase> expectedResultClass, TfClientRestHttpMethod tfClientRestHttpMethod,
      Map<String, String> params)  {
    
    mostRecentHttpStatusCode = -1;
    mostRecentHttpMethod = null;
        
    String logDir = TwoFactorClientConfig.retrieveConfig().propertyValueString("twoFactorClient.logging.webService.documentDir");
    File requestFile = null;
    File responseFile = null;
    
    if (!TwoFactorClientUtils.isBlank(logDir)) {
      
      logDir = TwoFactorClientUtils.stripEnd(logDir, "/");
      logDir = TwoFactorClientUtils.stripEnd(logDir, "\\");
      Date date = new Date();
      String logName = logDir  + File.separator + "wsLog_" 
        + new SimpleDateFormat("yyyy_MM").format(date)
        + File.separator + "day_" 
        + new SimpleDateFormat("dd" + File.separator + "HH_mm_ss_SSS").format(date)
        + "_" + ((int)(1000 * Math.random())) + "_" + labelForLog;
      
      requestFile = new File(logName + "_request.log");
      
      responseFile = new File(logName + "_response.log");

      //make parents
      TwoFactorClientUtils.mkdirs(requestFile.getParentFile());
      
    }
    int[] responseCode = new int[1];
    
    //make sure right content type is in request (e.g. application/xhtml+xml
    this.method = this.method(url, urlSuffix, 
        toSend, requestFile, responseCode, clientVersion, tfClientRestContentType,
        tfClientRestHttpMethod, params);

    //make sure a request came back
    this.response = TwoFactorClientUtils.responseBodyAsString(this.method);

    mostRecentResponse = this.response;

    if (responseFile != null || TwoFactorClientLog.debugToConsole()) {
      if (responseFile != null) {
        LOG.debug("WebService: logging response to: " + TwoFactorClientUtils.fileCanonicalPath(responseFile));
      }
      
      String theResponse = this.response;
      Exception indentException = null;

      boolean isIndent = TwoFactorClientConfig.retrieveConfig().propertyValueBooleanRequired("twoFactorClient.logging.webService.indent");
      if (isIndent) {
        try {
          theResponse = tfClientRestContentType.indent(theResponse);
        } catch (Exception e) {
          indentException = e;
        }
      }
      
      StringBuilder headers = new StringBuilder();

      headers.append("HTTP/1.1 ").append(responseCode[0]).append(" ").append(HttpStatus.getStatusText(responseCode[0])).append("\n");
      
      for (Header header : this.method.getResponseHeaders()) {
        String name = header.getName();
        String value = header.getValue();
        
        //dont allow cookies to go to logs
        if (TwoFactorClientUtils.equals(name, "Set-Cookie")) {
          value = value.replaceAll("JSESSIONID=(.*)?;", "JSESSIONID=xxxxxxxxxxxx;");
        }
        headers.append(name).append(": ").append(value).append("\n");
      }
      headers.append("\n");
      String theResponseTotal = headers + theResponse;
      if (responseFile != null) {
        TwoFactorClientUtils.saveStringIntoFile(responseFile, theResponseTotal);
      }
      if (TwoFactorClientLog.debugToConsole()) {
        System.err.println("\n################ RESPONSE START " + (isIndent ? "(indented) " : "") + "###############\n");
        System.err.println(theResponseTotal);
        System.err.println("\n################ RESPONSE END ###############\n\n");
      }
      if (indentException != null) {
        throw new RuntimeException("Problems indenting " + tfClientRestContentType 
            + " (is it valid?), turn off the indenting in the " +
            "twoFactor.client.properties: twoFactorClient.logging.webService.indent", indentException);
      }
    }

    @SuppressWarnings("unchecked")
    T resultObject = (T)tfClientRestContentType.parseString(expectedResultClass, this.response, new StringBuilder());
    
    //see if problem
    if (!TwoFactorClientUtils.isBlank(resultObject.getErrorMessage()) ||
        resultObject.getSuccess() == null || !resultObject.getSuccess()) {
      throw new TwoFactorClientWsException(resultObject, resultObject.getErrorMessage());
    }

    return resultObject;
  }

//  /**
//   * if failure, handle it
//   * @param responseContainer is the object that everything marshaled to
//   * @param resultMetadataHolders
//   * @param resultMessage
//   * @throws GcWebServiceError if there is a problem
//   */
//  public void handleFailure(Object responseContainer, ResultMetadataHolder[] resultMetadataHolders, String resultMessage) {
//    // see if request worked or not
//    if (!this.success) {
//      StringBuilder error = new StringBuilder("Bad response from web service: resultCode: " + this.resultCode
//        + ", " + resultMessage);
//      int errorIndex = 0;
//      for (int i=0;i<TwoFactorClientUtils.length(resultMetadataHolders);i++) {
//        try {
//          WsResultMeta resultMetadata = resultMetadataHolders[i].getResultMetadata();
//          if (!TwoFactorClientUtils.equals(resultMetadata.getSuccess(), "T")) {
//            error.append("\nError ").append(errorIndex).append(", result index: ").append(i).append(", code: ").append(resultMetadata.getResultCode())
//              .append(", message: ").append(resultMetadata.getResultMessage());
//            errorIndex++;
//          }
//        } catch (Exception e) {
//          //object not there
//          LOG.debug("issue with error message: ", e);
//        }
//      }
//      throw new GcWebServiceError(responseContainer, error.toString());
//    }
//
//  }
  
  
  /**
   * http client
   * @return the http client
   */
  @SuppressWarnings({ "deprecation", "unchecked" })
  private static HttpClient httpClient() {
    
    //see if invalid SSL
    String httpsSocketFactoryName = TwoFactorClientConfig.retrieveConfig().propertyValueString("twoFactorClient.https.customSocketFactory");
    
    //is there overhead here?  should only do this once?
    //perhaps give a custom factory
    if (!TwoFactorClientUtils.isBlank(httpsSocketFactoryName)) {
      Class<? extends SecureProtocolSocketFactory> httpsSocketFactoryClass = TwoFactorClientUtils.forName(httpsSocketFactoryName);
      SecureProtocolSocketFactory httpsSocketFactoryInstance = TwoFactorClientUtils.newInstance(httpsSocketFactoryClass);
      Protocol easyhttps = new Protocol("https", httpsSocketFactoryInstance, 443);
      Protocol.registerProtocol("https", easyhttps);
    }
    
    HttpClient httpClient = new HttpClient();

    DefaultHttpParams.getDefaultParams().setParameter(
        HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

    httpClient.getParams().setAuthenticationPreemptive(true);
    
    int soTimeoutMillis = TwoFactorClientConfig.retrieveConfig().propertyValueIntRequired(
        "twoFactorClient.webService.httpSocketTimeoutMillis");
    
    httpClient.getParams().setSoTimeout(soTimeoutMillis);
    httpClient.getParams().setParameter(HttpMethodParams.HEAD_BODY_CHECK_TIMEOUT, soTimeoutMillis);
    
    int connectionManagerMillis = TwoFactorClientConfig.retrieveConfig().propertyValueIntRequired(
        "twoFactorClient.webService.httpConnectionManagerTimeoutMillis");
    
    httpClient.getParams().setConnectionManagerTimeout(connectionManagerMillis);

    String user = TwoFactorClientConfig.retrieveConfig().propertyValueStringRequired("twoFactorClient.webService.login");
    
    LOG.debug("WebService: connecting as user: '" + user + "'");
    
    boolean disableExternalFileLookup = TwoFactorClientConfig.retrieveConfig().propertyValueBooleanRequired(
        "encrypt.disableExternalFileLookup");
    
    //lets lookup if file
    String wsPass = TwoFactorClientConfig.retrieveConfig().propertyValueStringRequired("twoFactorClient.webService.password");
    String wsPassFromFile = TwoFactorClientUtils.readFromFileIfFile(wsPass, disableExternalFileLookup);

    String passPrefix = null;

    if (!TwoFactorClientUtils.equals(wsPass, wsPassFromFile)) {

      passPrefix = "WebService pass: reading encrypted value from file: " + wsPass;

      String encryptKey = TwoFactorClientUtils.encryptKey();
      wsPass = new Crypto(encryptKey).decrypt(wsPassFromFile);
      
    } else {
      passPrefix = "WebService pass: reading scalar value from standardApi.client.properties";
    }
    
    if (TwoFactorClientConfig.retrieveConfig().propertyValueBoolean("twoFactorClient.logging.logMaskedPassword", false)) {
      LOG.debug(passPrefix + ": " + TwoFactorClientUtils.repeat("*", wsPass.length()));
    }

    Credentials defaultcreds = new UsernamePasswordCredentials(user, wsPass);

    //set auth scope to null and negative so it applies to all hosts and ports
    httpClient.getState().setCredentials(new AuthScope(null, -1), defaultcreds);

    return httpClient;
  }

  /**
   * @param url is the url to use
   * @param suffix e.g. groups/aStem:aGroup/members
   * @param webServiceVersion
   * @param asacRestHttpMethod
   * @return the method
   */
  private HttpMethodBase method(String url, String suffix, String webServiceVersion, TfClientRestHttpMethod asacRestHttpMethod) {
    
    url = TwoFactorClientUtils.stripEnd(url, "/");
    
    webServiceVersion = TwoFactorClientUtils.stripStart(webServiceVersion, "/");
    webServiceVersion = TwoFactorClientUtils.stripEnd(webServiceVersion, "/");

    suffix = TwoFactorClientUtils.trim(suffix);
    suffix = TwoFactorClientUtils.stripStart(suffix, "/");
        
    if (suffix != null && suffix.startsWith(".") && TwoFactorClientUtils.isBlank(webServiceVersion) ) {
      url = url + suffix;
    } else {
      url = url + (TwoFactorClientUtils.isBlank(webServiceVersion) ? "" : ("/" + webServiceVersion)) 
          + (TwoFactorClientUtils.isBlank(suffix) ? "" :  ("/" + suffix));
    }

    LOG.debug("WebService: connecting to URL: '" + url + "'");

    //URL e.g. http://localhost:8093/standardApi-ws/servicesRest/v1_3_000/...
    //NOTE: aStem:aGroup urlencoded substitutes %3A for a colon
    HttpMethodBase httpMethodBase = asacRestHttpMethod.httpMethod(url);

    //no keep alive so response is easier to indent for tests
    httpMethodBase.setRequestHeader("Connection", "close");
    
    return httpMethodBase;
  }

  /**
   * @param url to use
   * @param urlSuffix to put on end of base url, e.g. groups/aStem:aGroup/members
   * @param objectToMarshall is the bean to convert to XML, or it could be a string of xml
   * @param logFile if not null, log the contents of the request there
   * @param responseCode array of size one to get the response code back
   * @param clientVersion 
   * @param asacRestContentType
   * @param asacRestHttpMethod 
   * @param params
   * @return the post method
   */
  private HttpMethodBase method(String url, 
      String urlSuffix, Object objectToMarshall, File logFile, 
      int[] responseCode, String clientVersion, TfClientRestContentType asacRestContentType,
      TfClientRestHttpMethod asacRestHttpMethod,
      Map<String, String> params)  {
    
    mostRecentHttpStatusCode = -1;
    mostRecentHttpMethod = null;
    
    try {
      
      HttpClient httpClient = httpClient();
  
      HttpMethodBase theMethod = method(url, urlSuffix, clientVersion, asacRestHttpMethod);
  
      String requestDocument = null;
      
      if (objectToMarshall != null) {
        requestDocument = objectToMarshall instanceof String ? (String)objectToMarshall : asacRestContentType.writeString(objectToMarshall);
      }
      
      if (theMethod instanceof EntityEnclosingMethod) {
        //text/xml
        //text/x-json
        //
        if (!TwoFactorClientUtils.isBlank(requestDocument)) {
          ((EntityEnclosingMethod)theMethod).setRequestEntity(new StringRequestEntity(requestDocument, 
              asacRestContentType.getContentType(), "UTF-8"));
        }
      }
      
      if (TwoFactorClientUtils.length(params) > 0) {
        
        if (!(theMethod instanceof PostMethod)) {
          throw new RuntimeException("If sending params, it must be a POST");
        }

        for (String key : params.keySet()) {
          ((PostMethod)theMethod).setParameter(key, params.get(key));
        }
              
      }
      
      String requestParamsString = "";

      if (TwoFactorClientUtils.length(params) > 0) {
        
        StringBuilder paramsString = new StringBuilder();
        for (String key : params.keySet()) {
          
          if (paramsString.length() > 0) {
            paramsString.append("&");
          }
          
          paramsString.append(TwoFactorClientUtils.escapeUrlEncode(key))
            .append("=").append(TwoFactorClientUtils.escapeUrlEncode(params.get(key)));
          
        }
        requestParamsString = paramsString.toString();
      }

      if (logFile != null || TwoFactorClientLog.debugToConsole()) {
        if (logFile != null) {
          LOG.debug("WebService: logging request to: " + TwoFactorClientUtils.fileCanonicalPath(logFile));
        }
        String theRequestDocument = TwoFactorClientUtils.trimToEmpty(requestDocument);
        boolean isIndent = false;
        Exception indentException = null;
        if (!TwoFactorClientUtils.isBlank(theRequestDocument)) {
          isIndent = TwoFactorClientConfig.retrieveConfig().propertyValueBooleanRequired("twoFactorClient.logging.webService.indent");
          if (isIndent) {
            try {
              theRequestDocument = TwoFactorClientUtils.indent(theRequestDocument, true);
            } catch (Exception e) {
              indentException = e;
            }
          }
        }
        if (TwoFactorClientUtils.length(params) > 0) {
          
          theRequestDocument += requestParamsString;
        }
        
        StringBuilder headers = new StringBuilder();
  //      POST /standardApi-ws/servicesRest/v1_4_000/subjects HTTP/1.1
  //      Connection: close
  //      Authorization: Basic bWNoeXplcjpEaxxxxxxxxxx==
  //      User-Agent: Jakarta Commons-HttpClient/3.1
  //      Host: localhost:8090
  //      Content-Length: 226
  //      Content-Type: text/xml; charset=UTF-8
        headers.append(asacRestHttpMethod.name()).append(" ")
          .append(theMethod.getURI().getPathQuery()).append(" HTTP/1.1\n");
        headers.append("Connection: close\n");
        headers.append("Authorization: Basic xxxxxxxxxxxxxxxx\n");
        headers.append("User-Agent: Jakarta Commons-HttpClient/3.1\n");
        headers.append("Host: ").append(theMethod.getURI().getHost()).append(":")
          .append(theMethod.getURI().getPort()).append("\n");
        if (theMethod instanceof EntityEnclosingMethod) {
          headers.append("Content-Length: ").append(
              ((EntityEnclosingMethod)theMethod).getRequestEntity().getContentLength()).append("\n");
          headers.append("Content-Type: ").append(
              ((EntityEnclosingMethod)theMethod).getRequestEntity().getContentType()).append("\n");
        }
        headers.append("\n");
        
        String theRequest = headers + theRequestDocument;
        if (logFile != null) {
          TwoFactorClientUtils.saveStringIntoFile(logFile, theRequest);
        }
        if (TwoFactorClientLog.debugToConsole()) {
          System.err.println("\n################ REQUEST START " + (isIndent ? "(indented) " : "") + "###############\n");
          System.err.println(theRequest);
          System.err.println("\n################ REQUEST END ###############\n\n");
        }
        if (indentException != null) {
          throw new RuntimeException("Problems indenting xml (is it valid?), turn off the indenting in the " +
          		"standardApi.client.properties: twoFactorClient.logging.webService.indent", indentException);
        }
      }
      
      mostRecentRequest = requestDocument;
      
      if (!TwoFactorClientUtils.isBlank(requestParamsString)) {
        mostRecentRequest += requestParamsString;
      }
      
      
      int responseCodeInt = httpClient.executeMethod(theMethod);

      mostRecentHttpStatusCode = responseCodeInt;
      mostRecentHttpMethod = theMethod;
      
      if (responseCode != null && responseCode.length > 0) {
        responseCode[0] = responseCodeInt;
      }
      
      return theMethod;
    } catch (Exception e) {
      
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      
      throw new RuntimeException("Problem in url: " + url, e);
    }
  }
  

}
