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
 * $Id: TfValidatePassword.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client.api;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openTwoFactor.client.contentType.TfClientRestContentType;
import org.openTwoFactor.client.corebeans.TfCheckPasswordResponse;
import org.openTwoFactor.client.util.TwoFactorClientLog;
import org.openTwoFactor.client.util.TwoFactorClientUtils;
import org.openTwoFactor.client.ws.TfClientRestHttpMethod;
import org.openTwoFactor.client.ws.TwoFactorClientWs;




/**
 * class to call the two factor web service
 */
public class TfValidatePassword extends AsacApiRequestBase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TwoFactorClientLog.assignDebugToConsole(true);
    TfCheckPasswordResponse tfCheckPasswordResponse = 
      new TfValidatePassword().assignUsername("jsmith").assignBrowserUserAgent("userAgent")
        .assignDebug(true).assignIndent(true).assignRequireReauth(true)
        .assignRequireTwoFactor(true).assignServiceId("serviceId")
        .assignServiceName("serviceName").assignSpRequiredFactors("spRequiredFactors")
        .assignTrustedBrowser(true).assignTwoFactorPass("123456")
        .assignUserBrowserUuid("userBrowserUuid").assignUserIpAddress("1.2.3.4")
        .assignUsername("jsmith").execute();
    System.out.println(tfCheckPasswordResponse);
  }
  
  /**
   * xml or json, defaults to json 
   * @param format1
   * @return this for chaining
   */
  public TfValidatePassword assignFormat(String format1) {
    this.setContentType(TfClientRestContentType.valueOfIgnoreCase(format1, false));
    if (this.getContentType() == null ) {
      this.setContentType(TfClientRestContentType.json);
    }
    return this;
  }
  
  /**
   * whatever is sent from browser.  Note, if this changes, the twoFactorService could invalidate the uuid...
   */
  private String browserUserAgent;
  /**
   * true|false If true or if a param is set on the server, then a debug message will be returned
   */
  private Boolean debug;
  /**
   * true|false if the service requires reauth
   */
  private Boolean requireReauth;
  /**
   * true|false if the service is requiring two factor
   */
  private Boolean requireTwoFactor;
  /**
   * is some unchanging ID of the service which is requesting authentication
   */
  private String serviceId;
  /**
   * is a friendly service name requesting authentication that could be shown on screen, this could change
   */
  private String serviceName;
  /**
   * ABC,DEF,GHI Comma separated factors required from the SP, note this could
   * mean that the service requires two factor, or
   */
  private String spRequiredFactors;
  /**
   * true|false depending on what the user selected
   */
  private Boolean trustedBrowser;
  /**
   * the two factor password unencrypted that the user entered.
   * This could be blank or omitted if the user did not enter one
   */
  private String twoFactorPass;
  /**
   * value is a uuid which is assigned to the user e.g. by cookie, unencrypted
   */
  private String userBrowserUuid;
  /**
   * 1.2.3.4 or ipv6, this is generally used for logging
   */
  private String userIpAddress;
  /**
   * username that the user logged in with
   */
  private String username;
  
  /**
   * @see org.openTwoFactor.client.api.AsacApiRequestBase#assignIndent(boolean)
   */
  @Override
  public TfValidatePassword assignIndent(boolean indent1) {
    return (TfValidatePassword)super.assignIndent(indent1);
  }

  /**
   * validate this call
   */
  private void validate() {
  }
  
  /**
   * execute the call and return the results.  If there is a problem calling the service, an
   * exception will be thrown
   * 
   * @return the results
   */
  public TfCheckPasswordResponse execute() {
    
    this.validate();

    TfCheckPasswordResponse tfCheckPasswordResponse = null;

    TwoFactorClientWs<TfCheckPasswordResponse> standardApiClientWs = new TwoFactorClientWs<TfCheckPasswordResponse>();
    
    //kick off the web service
    String urlSuffix = "/v1/validatePassword." + this.getContentType().name();
    
    Map<String, String> params = new LinkedHashMap<String, String>();
    if (!TwoFactorClientUtils.isBlank(this.browserUserAgent)) {
      params.put("browserUserAgent", this.browserUserAgent);
    }
    if (this.debug != null) {
      params.put("debug", this.debug ? "true" : "false");
    }
    if (this.requireReauth != null) {
      params.put("requireReauth", this.requireReauth ? "true" : "false");
    }
    if (this.requireTwoFactor != null) {
      params.put("requireTwoFactor", this.requireTwoFactor ? "true" : "false");
    }
    if (!TwoFactorClientUtils.isBlank(this.serviceId)) {
      params.put("serviceId", this.serviceId);
    }
    if (!TwoFactorClientUtils.isBlank(this.serviceName)) {
      params.put("serviceName", this.serviceName);
    }
    if (!TwoFactorClientUtils.isBlank(this.spRequiredFactors)) {
      params.put("spRequiredFactors", this.spRequiredFactors);
    }
    if (this.trustedBrowser != null) {
      params.put("trustedBrowser", this.trustedBrowser ? "true" : "false");
    }
    if (!TwoFactorClientUtils.isBlank(this.twoFactorPass)) {
      params.put("twoFactorPass", this.twoFactorPass);
    }
    if (!TwoFactorClientUtils.isBlank(this.userBrowserUuid)) {
      params.put("userBrowserUuid", this.userBrowserUuid);
    }
    if (!TwoFactorClientUtils.isBlank(this.userIpAddress)) {
      params.put("userIpAddress", this.userIpAddress);
    }
    if (!TwoFactorClientUtils.isBlank(this.username)) {
      params.put("username", this.username);
    }
    
    tfCheckPasswordResponse =
      standardApiClientWs.executeService(urlSuffix, null, "defaultVersionResource", null,
          this.getContentType(), TfCheckPasswordResponse.class, TfClientRestHttpMethod.POST, params);
    
    return tfCheckPasswordResponse;
    
  }

  /**
   * whatever is sent from browser.  Note, if this changes, the twoFactorService could invalidate the uuid...
   * @param browserUserAgent1 the browserUserAgent to set
   * @return the request for chaining
   */
  public TfValidatePassword assignBrowserUserAgent(String browserUserAgent1) {
    this.browserUserAgent = browserUserAgent1;
    return this;
  }

  /**
   * true|false If true or if a param is set on the server, then a debug message will be returned
   * @param debug1 the debug to set
   * @return this for chaining
   */
  public TfValidatePassword assignDebug(Boolean debug1) {
    this.debug = debug1;
    return this;
  }

  /**
   * true|false if the service requires reauth
   * @param requireReauth1 the requireReauth to set
   * @return the request for chaining
   */
  public TfValidatePassword assignRequireReauth(Boolean requireReauth1) {
    this.requireReauth = requireReauth1;
    return this;
  }

  /**
   * true|false if the service is requiring two factor
   * @param requireTwoFactor1 the requireTwoFactor to set
   * @return the request for chaining
   */
  public TfValidatePassword assignRequireTwoFactor(Boolean requireTwoFactor1) {
    this.requireTwoFactor = requireTwoFactor1;
    return this;
  }

  /**
   * is some unchanging ID of the service which is requesting authentication
   * @param serviceId1 the serviceId to set
   * @return the request for chaining
   */
  public TfValidatePassword assignServiceId(String serviceId1) {
    this.serviceId = serviceId1;
    return this;
  }

  /**
   * is a friendly service name requesting authentication that could be shown on screen, this could change
   * @param serviceName1 the serviceName to set
   * @return the request for chaining
   */
  public TfValidatePassword assignServiceName(String serviceName1) {
    this.serviceName = serviceName1;
    return this;
  }

  /**
   * ABC,DEF,GHI Comma separated factors required from the SP, note this could
   * mean that the service requires two factor, or
   * @param spRequiredFactors1 the spRequiredFactors to set
   * @return the request for chaining
   */
  public TfValidatePassword assignSpRequiredFactors(String spRequiredFactors1) {
    this.spRequiredFactors = spRequiredFactors1;
    return this;
  }

  /**
   * true|false depending on what the user selected
   * @param trustedBrowser1 the trustedBrowser to set
   * @return the request for chaining
   */
  public TfValidatePassword assignTrustedBrowser(Boolean trustedBrowser1) {
    this.trustedBrowser = trustedBrowser1;
    return this;
  }

  /**
   * the two factor password unencrypted that the user entered.
   * This could be blank or omitted if the user did not enter one
   * @param twoFactorPassUnencrypted1 the twoFactorPass to set
   * @return the request for chaining
   */
  public TfValidatePassword assignTwoFactorPass(String twoFactorPassUnencrypted1) {
    this.twoFactorPass = twoFactorPassUnencrypted1;
    return this;
  }

  /**
   * value is a uuid which is assigned to the user e.g. by cookie, unencrypted
   * @param userBrowserUuid1 the userBrowserUuid to set
   * @return the request for chaining
   */
  public TfValidatePassword assignUserBrowserUuid(String userBrowserUuid1) {
    this.userBrowserUuid = userBrowserUuid1;
    return this;
  }

  /**
   * 1.2.3.4 or ipv6, this is generally used for logging
   * @param userIpAddress1 the userIpAddress to set
   * @return the request for chaining
   */
  public TfValidatePassword assignUserIpAddress(String userIpAddress1) {
    this.userIpAddress = userIpAddress1;
    return this;
  }

  /**
   * username that the user logged in with
   * @param username1 the username to set
   * @return the request for chaining
   */
  public TfValidatePassword assignUsername(String username1) {
    this.username = username1;
    return this;
  }
  
}
