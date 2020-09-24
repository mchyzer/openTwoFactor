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
 * built in implmentation of the administrative actions interface, who 
 * is an admin, etc.  Just check the config file
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
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#admin24UserIds()
   */
  @Override
  public Set<String> admin24UserIds() {

    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.admins24");
    
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

  /**
   * @see TwoFactorAuthorizationInterface#adminUserIdsWhoCanEmailAllUsers()
   */
  @Override
  public Set<String> adminUserIdsWhoCanEmailAllUsers() {

    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsAllowedToActAsOtherUsers = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToEmailAllUsers");
    
    if (TwoFactorServerUtils.isBlank(adminsAllowedToActAsOtherUsers)) {
      return new HashSet<String>();
    }

    return TwoFactorServerUtils.splitTrimToSet(adminsAllowedToActAsOtherUsers, ",");
  }

  /**
   * @see TwoFactorAuthorizationInterface#adminUserIdsWhoCanImportSerials()
   */
  @Override
  public Set<String> adminUserIdsWhoCanImportSerials() {
    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsAllowedImportFobSerials = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToImportFobSerialNumbers");
    
    if (TwoFactorServerUtils.isBlank(adminsAllowedImportFobSerials)) {
      return new HashSet<String>();
    }

    return TwoFactorServerUtils.splitTrimToSet(adminsAllowedImportFobSerials, ",");
  }

  /**
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#adminUserIdsWhoCanAdminReports()
   */
  public Set<String> adminUserIdsWhoCanAdminReports() {
    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsAllowedAdminReports = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToAdminReports");
    
    if (TwoFactorServerUtils.isBlank(adminsAllowedAdminReports)) {
      return new HashSet<String>();
    }

    return TwoFactorServerUtils.splitTrimToSet(adminsAllowedAdminReports, ",");
  }

  /**
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#adminUserIdsWhoCanSeeAdminReports()
   */
  public Set<String> adminUserIdsWhoCanSeeAdminReports() {
    // if using the default config file based authz, this is the comma separated list of admin ids
    String adminsAllowedAdminReports = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToSeeAdminReports");
    
    if (TwoFactorServerUtils.isBlank(adminsAllowedAdminReports)) {
      return new HashSet<String>();
    }

    return TwoFactorServerUtils.splitTrimToSet(adminsAllowedAdminReports, ",");
  }

  /**
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#adminLiteUserIds()
   */
  public Set<String> adminLiteUserIds() {
    // if using the default config file based authz, this is the comma separated list of admin ids
    String liteADmins = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.liteAdmins");
    
    if (TwoFactorServerUtils.isBlank(liteADmins)) {
      return new HashSet<String>();
    }

    return TwoFactorServerUtils.splitTrimToSet(liteADmins, ",");
  }



}
