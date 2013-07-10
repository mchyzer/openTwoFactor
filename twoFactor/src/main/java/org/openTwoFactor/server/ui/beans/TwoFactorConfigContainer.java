package org.openTwoFactor.server.ui.beans;

import org.openTwoFactor.server.config.TwoFactorServerConfig;


public class TwoFactorConfigContainer {

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
  
}
