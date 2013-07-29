/**
 * @author mchyzer
 * $Id: TwoFactorAuthorization.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server;

import java.util.HashSet;
import java.util.Set;

import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 *
 */
public class TwoFactorAuthorization implements TwoFactorAuthorizationInterface {

  /**
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#adminUserIds()
   */
  @Override
  public Set<String> adminUserIds() {

    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.admins");
    
    if (TwoFactorServerUtils.isBlank(adminsString)) {
      return new HashSet<String>();
    }

    return TwoFactorServerUtils.splitTrimToSet(adminsString, ",");
      
  }

  /**
   * @see TwoFactorAuthorizationInterface#adminUserIdsWhoCanBackdoorAsOtherUsers()
   */
  @Override
  public Set<String> adminUserIdsWhoCanBackdoorAsOtherUsers() {

    
    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsAllowedToActAsOtherUsers = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToActAsOtherUsers");
    
    if (TwoFactorServerUtils.isBlank(adminsAllowedToActAsOtherUsers)) {
      return new HashSet<String>();
    }

    return TwoFactorServerUtils.splitTrimToSet(adminsAllowedToActAsOtherUsers, ",");

  }

}
