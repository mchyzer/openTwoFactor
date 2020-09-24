/**
 * @author mchyzer
 * $Id: TwoFactorAuthorizationInterface.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server;

import java.util.Set;


/**
 * authorization interface for administrative actions.
 */
public interface TwoFactorAuthorizationInterface {

  /**
   * lite admins can help people who requested help from a friend
   * @return the list of users
   */
  public Set<String> adminLiteUserIds();

  /**
   * admin24s can input data and get a code
   * @return the set of admins
   */
  public Set<String> admin24UserIds();
  
  /**
   * admins can view audits of users, opt people out, untrust browsers, etc
   * @return the set of admins
   */
  public Set<String> adminUserIds();
  
  /**
   * you probably do not want to set this in production, and if so, do so temporarily
   * @return the userIds who can backdoor as other users
   */
  public Set<String> adminUserIdsWhoCanBackdoorAsOtherUsers();
  
  /**
   * admin user ids who can send email to other users
   * @return the set of user ids
   */
  public Set<String> adminUserIdsWhoCanEmailAllUsers();
  
  /**
   * admin user ids who can import serials of fobs in the admin console
   * @return the set of user ids
   */
  public Set<String> adminUserIdsWhoCanImportSerials();
  
  /**
   * admin user ids who can admin reports
   * @return the set of user ids
   */
  public Set<String> adminUserIdsWhoCanAdminReports();

  /**
   * admin user ids who can see admin reports
   * @return the set of user ids
   */
  public Set<String> adminUserIdsWhoCanSeeAdminReports();
}
