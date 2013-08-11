/**
 * @author mchyzer
 * $Id: TfCheckPasswordRequest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.corebeans;

import edu.internet2.middleware.subject.Source;



/**
 * method chaining request for easy testing
 */
public class TfCheckPasswordRequest {

  /**
   * subject source
   */
  private Source subjectSource;

  
  
  /**
   * subject source
   * @return source
   */
  public Source getSubjectSource() {
    return this.subjectSource;
  }


  
  /**
   * subject source
   * @param subjectSource1
   * @return this for chaining
   */
  public TfCheckPasswordRequest assignSubjectSource(Source subjectSource1) {
    this.subjectSource = subjectSource1;
    return this;
  }

  /**
   * client to WS source IP address
   */
  private String clientSourceIpAddress;
  
  /**
   * client to WS username
   */
  private String clientUsername;
  
  
  /**
   * @param clientSourceIpAddress1 the clientSourceIpAddress to set
   * @return this for chaining
   */
  public TfCheckPasswordRequest assignClientSourceIpAddress(String clientSourceIpAddress1) {
    this.clientSourceIpAddress = clientSourceIpAddress1;
    return this;
  }

  
  /**
   * @param clientUsername1 the clientUsername to set
   * @return this for chaining
   */
  public TfCheckPasswordRequest assignClientUsername(String clientUsername1) {
    this.clientUsername = clientUsername1;
    return this;
  }

  /**
   * client to WS source IP address
   * @return the clientSourceIpAddress
   */
  public String getClientSourceIpAddress() {
    return this.clientSourceIpAddress;
  }
  
  /**
   * client to WS username
   * @return the clientUsername
   */
  public String getClientUsername() {
    return this.clientUsername;
  }

  /**
   * true|false If true or if a param is set on the server, then a debug message will be returned
   */
  private Boolean debug;
  
  
  /**
   * true|false If true or if a param is set on the server, then a debug message will be returned
   * @return the debug
   */
  public Boolean getDebug() {
    return this.debug;
  }


  
  /**
   * true|false If true or if a param is set on the server, then a debug message will be returned
   * @param debug1 the debug to set
   * @return this for chaining
   */
  public TfCheckPasswordRequest assignDebug(Boolean debug1) {
    this.debug = debug1;
    return this;
  }

  /**
   * value is a uuid which is assigned to the user e.g. by cookie, unencrypted
   */
  private String userBrowserUuid;
  
  /**
   * value is a uuid which is assigned to the user e.g. by cookie, unencrypted
   * @return the userBrowserUuid
   */
  public String getUserBrowserUuid() {
    return this.userBrowserUuid;
  }

  
  /**
   * value is a uuid which is assigned to the user e.g. by cookie, unencrypted
   * @param userUuidUnencrypted1 the userBrowserUuid to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignUserBrowserUuidUnencrypted(String userUuidUnencrypted1) {
    this.userBrowserUuid = userUuidUnencrypted1;
    return this;
  }

  /**
   * is some unchanging ID of the service which is requesting authentication
   */
  private String serviceId;
  
  
  /**
   * is some unchanging ID of the service which is requesting authentication
   * @return the serviceId
   */
  public String getServiceId() {
    return this.serviceId;
  }

  
  /**
   * is some unchanging ID of the service which is requesting authentication
   * @param serviceId1 the serviceId to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignServiceId(String serviceId1) {
    this.serviceId = serviceId1;
    return this;
  }

  /**
   * is a friendly service name requesting authentication that could be shown on screen, this could change
   */
  private String serviceName;

  
  /**
   * is a friendly service name requesting authentication that could be shown on screen, this could change
   * @return the serviceName
   */
  public String getServiceName() {
    return this.serviceName;
  }

  
  /**
   * is a friendly service name requesting authentication that could be shown on screen, this could change
   * @param serviceName1 the serviceName to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignServiceName(String serviceName1) {
    this.serviceName = serviceName1;
    return this;
  }

  /**
   * true|false if the service requires reauth
   */
  private Boolean requireReauth;
  
  
  /**
   * true|false if the service requires reauth
   * @return the requireReauth
   */
  public Boolean getRequireReauth() {
    return this.requireReauth;
  }

  
  /**
   * true|false if the service requires reauth
   * @param requireReauth1 the requireReauth to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignRequireReauth(Boolean requireReauth1) {
    this.requireReauth = requireReauth1;
    return this;
  }

  /**
   * whatever is sent from browser.  Note, if this changes, the twoFactorService could invalidate the uuid...
   */
  private String browserUserAgent;
  
  
  /**
   * whatever is sent from browser.  Note, if this changes, the twoFactorService could invalidate the uuid...
   * @return the browserUserAgent
   */
  public String getBrowserUserAgent() {
    return this.browserUserAgent;
  }

  
  /**
   * whatever is sent from browser.  Note, if this changes, the twoFactorService could invalidate the uuid...
   * @param browserUserAgent1 the browserUserAgent to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignBrowserUserAgent(String browserUserAgent1) {
    this.browserUserAgent = browserUserAgent1;
    return this;
  }

  /**
   * 1.2.3.4 or ipv6, this is generally used for logging
   */
  private String userIpAddress;
  
  /**
   * 1.2.3.4 or ipv6, this is generally used for logging
   * @return the userIpAddress
   */
  public String getUserIpAddress() {
    return this.userIpAddress;
  }

  
  /**
   * 1.2.3.4 or ipv6, this is generally used for logging
   * @param userIpAddress1 the userIpAddress to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignUserIpAddress(String userIpAddress1) {
    this.userIpAddress = userIpAddress1;
    return this;
  }


  /**
   * the two factor password unencrypted that the user entered.
   * This could be blank or omitted if the user did not enter one
   */
  private String twoFactorPass;
  
  
  /**
   * the two factor password unencrypted that the user entered.
   * This could be blank or omitted if the user did not enter one
   * @return the twoFactorPass
   */
  public String getTwoFactorPass() {
    return this.twoFactorPass;
  }

  
  /**
   * the two factor password unencrypted that the user entered.
   * This could be blank or omitted if the user did not enter one
   * @param twoFactorPassUnencrypted1 the twoFactorPass to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignTwoFactorPassUnencrypted(String twoFactorPassUnencrypted1) {
    this.twoFactorPass = twoFactorPassUnencrypted1;
    return this;
  }


  /**
   * username that the user logged in with
   */
  private String username;
  
  /**
   * username that the user logged in with
   * @return the username
   */
  public String getUsername() {
    return this.username;
  }

  
  /**
   * username that the user logged in with
   * @param username1 the username to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignUsername(String username1) {
    this.username = username1;
    return this;
  }



  /**
   * original username that the user logged in with
   */
  private String originalUsername;
  
  /**
   * original username that the user logged in with
   * @return the original username
   */
  public String getOriginalUsername() {
    return this.originalUsername;
  }

  
  /**
   * original username that the user logged in with
   * @param username1 the username to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignOriginalUsername(String username1) {
    this.originalUsername = username1;
    return this;
  }


  /**
   * true|false depending on what the user selected
   */
  private Boolean trustedBrowser;
  
  /**
   * true|false depending on what the user selected
   * @return the trustedBrowser
   */
  public Boolean getTrustedBrowser() {
    return this.trustedBrowser;
  }

  
  /**
   * true|false depending on what the user selected
   * @param trustedBrowser1 the trustedBrowser to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignTrustedBrowser(Boolean trustedBrowser1) {
    this.trustedBrowser = trustedBrowser1;
    return this;
  }

  
  /**
   * ABC,DEF,GHI Comma separated factors required from the SP, note this could
   * mean that the service requires two factor, or
   */
  private String spRequiredFactors;
  
  /**
   * ABC,DEF,GHI Comma separated factors required from the SP, note this could
   * mean that the service requires two factor, or
   * @return the spRequiredFactors
   */
  public String getSpRequiredFactors() {
    return this.spRequiredFactors;
  }

  
  /**
   * ABC,DEF,GHI Comma separated factors required from the SP, note this could
   * mean that the service requires two factor, or
   * @param spRequiredFactors1 the spRequiredFactors to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignSpRequiredFactors(String spRequiredFactors1) {
    this.spRequiredFactors = spRequiredFactors1;
    return this;
  }

  /**
   * true|false if the service is requiring two factor
   */
  private Boolean requireTwoFactor;
  
  
  /**
   * true|false if the service is requiring two factor
   * @return the requireTwoFactor
   */
  public Boolean getRequireTwoFactor() {
    return this.requireTwoFactor;
  }

  
  /**
   * true|false if the service is requiring two factor
   * @param requireTwoFactor1 the requireTwoFactor to set
   * @return the request for chaining
   */
  public TfCheckPasswordRequest assignRequireTwoFactor(Boolean requireTwoFactor1) {
    this.requireTwoFactor = requireTwoFactor1;
    return this;
  }


}
