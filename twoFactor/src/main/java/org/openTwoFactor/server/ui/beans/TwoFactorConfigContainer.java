package org.openTwoFactor.server.ui.beans;

import org.openTwoFactor.server.config.TwoFactorServerConfig;

/**
 * misc config things needed from JSP
 * @author mchyzer
 *
 */
public class TwoFactorConfigContainer {

  /**
   * is opt some enabled
   * @return true if opt some enabled
   */
  public boolean isOptSomeEnabledUi() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.optSomeEnabledUi", false);
  }
  
  /**
   * 
   * @return the app url base
   */
  public String getAppUrlBase() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.appUrlBase");
  }
  
  /**
   * 
   * @return the app url friendly
   */
  public String getAppUrlFriendly() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.appUrlFriendly");
  }
  
  /**
   * 
   * @return if auto call / text is enabled via web service
   */
  public boolean isEnableAutoCallText() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.enableAutoCallText", true);
  }

  /**
   * 
   * @return if allow admins to generate codes for users
   */
  public boolean isAllowAdminsToGenerateCodesforUsers() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.allowAdminsToGenerateCodesforUsers", true);
  }
  
}
