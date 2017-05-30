/**
 * @author mchyzer
 * $Id: TfCheckPasswordRequest.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client.corebeans;



/**
 * method chaining request for easy testing
 */
public class TfCheckPasswordRequest {

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
   */
  public void setDebug(Boolean debug1) {
    this.debug = debug1;
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
   */
  public void assignUserBrowserUuidUnencrypted(String userUuidUnencrypted1) {
    this.userBrowserUuid = userUuidUnencrypted1;
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
   */
  public void assignServiceId(String serviceId1) {
    this.serviceId = serviceId1;
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
   */
  public void assignServiceName(String serviceName1) {
    this.serviceName = serviceName1;
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
   */
  public void assignRequireReauth(Boolean requireReauth1) {
    this.requireReauth = requireReauth1;
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
   */
  public void assignBrowserUserAgent(String browserUserAgent1) {
    this.browserUserAgent = browserUserAgent1;
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
   */
  public void assignUserIpAddress(String userIpAddress1) {
    this.userIpAddress = userIpAddress1;
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
   */
  public void assignTwoFactorPassUnencrypted(String twoFactorPassUnencrypted1) {
    this.twoFactorPass = twoFactorPassUnencrypted1;
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
   */
  public void assignUsername(String username1) {
    this.username = username1;
  }

  /**
   * true|false if the service is being queried asynchronously from a browser (e.g. dont clear state)
   */
  private Boolean asyncAuth;
  
  /**
   * true|false if the service is being queried asynchronously from a browser (e.g. dont clear state)
   * @return the asyncAuth
   */
  public Boolean getAsyncAuth() {
    return this.asyncAuth;
  }
  
  /**
   * true|false if the service is being queried asynchronously from a browser (e.g. dont clear state)
   * @param asyncAuth1 the asyncAuth to set
   */
  public void assignAsyncAuth(Boolean asyncAuth1) {
    this.asyncAuth = asyncAuth1;
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
   */
  public void assignTrustedBrowser(Boolean trustedBrowser1) {
    this.trustedBrowser = trustedBrowser1;
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
   */
  public void assignSpRequiredFactors(String spRequiredFactors1) {
    this.spRequiredFactors = spRequiredFactors1;
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
   */
  public void assignRequireTwoFactor(Boolean requireTwoFactor1) {
    this.requireTwoFactor = requireTwoFactor1;
  }


}
