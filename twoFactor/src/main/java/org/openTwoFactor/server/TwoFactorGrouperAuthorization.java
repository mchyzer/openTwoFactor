/**
 * @author mchyzer
 * $Id: TwoFactorAuthorization.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;



/**
 * built in implmentation of the administrative actions interface, who 
 * is an admin, etc.  Just check the config file
 */
public class TwoFactorGrouperAuthorization implements TwoFactorAuthorizationInterface {

  /**
   * failsafe cache will be alive if connection to authz interface fails
   * key is the group name in grouper
   */
  private static Map<String, Set<String>> failsafeCache = new HashMap<String, Set<String>>();
  
  /**
   * expirable cache of security (5 minutes)
   */
  private static ExpirableCache<String, Set<String>> expirableCache = null;
  
  /**
   * init the expirable cache
   * @return the cache
   */
  private static ExpirableCache<String, Set<String>> expirableCache() {
    if (expirableCache == null) {
      expirableCache = new ExpirableCache<String, Set<String>>(TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.twoFactorAuthzGrouperTimeoutMinutes"));
    }
    return expirableCache;
  }
  
  /**
   * get list of users from authz query
   * @param groupName
   * @return list of users
   */
  private static Set<String> retrieveUserIdsFromAuthz(String groupName) {
    
    String query = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.twoFactorAuthzGrouperAuthorizationQuery");
    
    List<String> results = HibernateSession.bySqlStatic().listSelect(String.class, query, TwoFactorServerUtils.toListObject(groupName));
            
    return new LinkedHashSet<String>(results);
  }
  
  
  /**
   * get list of users from authz query
   * @param groupName
   * @return list of users
   */
  private static Set<String> retrieveUserIdsFromAuthzOrCache(String groupName) {

    if (TwoFactorServerUtils.isBlank(groupName)) {
      return new HashSet<String>();
    }

    Set<String> result = expirableCache().get(groupName);
    if (result != null) {
      return result;
    }
    try {
      
      result = retrieveUserIdsFromAuthz(groupName);

      //put in caches
      expirableCache.put(groupName, result);
      failsafeCache.put(groupName, result);
      
    } catch (RuntimeException e) {
      result = failsafeCache.get(groupName);
      //if nothing in failsafe we done
      if (result == null) {
        throw e;
      }
      //log if not error
      LOG.error("Error retrieving group: '" + groupName + "'", e);
    }
    
    return result;
  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorGrouperAuthorization.class);
  
  /**
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#adminUserIds()
   */
  @Override
  public Set<String> adminUserIds() {

    // if using the default config file based authz, this is the comma separated list of admin ids
    String groupName = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.admins");
    
    return retrieveUserIdsFromAuthzOrCache(groupName);
  }

  /**
   * @see TwoFactorAuthorizationInterface#adminUserIdsWhoCanBackdoorAsOtherUsers()
   */
  @Override
  public Set<String> adminUserIdsWhoCanBackdoorAsOtherUsers() {

    
    // if using the default config file based authz, this is the comma separated list of admin ids
    String groupName = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToActAsOtherUsers");

    return retrieveUserIdsFromAuthzOrCache(groupName);
  }

  /**
   * @see TwoFactorAuthorizationInterface#adminUserIdsWhoCanEmailAllUsers()
   */
  @Override
  public Set<String> adminUserIdsWhoCanEmailAllUsers() {

    // if using the default config file based authz, this is the comma separated list of admin ids
    String groupName = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToEmailAllUsers");
    
    return retrieveUserIdsFromAuthzOrCache(groupName);
  }

  /**
   * @see TwoFactorAuthorizationInterface#adminUserIdsWhoCanImportSerials()
   */
  @Override
  public Set<String> adminUserIdsWhoCanImportSerials() {
    // if using the default config file based authz, this is the comma separated list of admin ids
    String groupName = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToImportFobSerialNumbers");
    
    return retrieveUserIdsFromAuthzOrCache(groupName);
  }

  /**
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#adminUserIdsWhoCanAdminReports()
   */
  public Set<String> adminUserIdsWhoCanAdminReports() {
    // if using the default config file based authz, this is the comma separated list of admin ids
    String groupName = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToAdminReports");
    
    return retrieveUserIdsFromAuthzOrCache(groupName);
  }

  /**
   * @see org.openTwoFactor.server.TwoFactorAuthorizationInterface#adminUserIdsWhoCanSeeAdminReports()
   */
  public Set<String> adminUserIdsWhoCanSeeAdminReports() {
    // if using the default config file based authz, this is the comma separated list of admin ids
    String groupName = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "twoFactorServer.adminsAllowedToSeeAdminReports");
    
    return retrieveUserIdsFromAuthzOrCache(groupName);
  }

}
