/**
 * @author mchyzer
 * $Id: TwoFactorAdminContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.TwoFactorAuthorizationInterface;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.cache.TwoFactorCache;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.util.TfSourceUtils;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;



/**
 * admin data for the admin console
 */
public class TwoFactorAdminContainer {

  /**
   * number of users emailed
   */
  private int adminEmailNumberOfUsers;

  
  
  /**
   * number of users emailed
   * @return number of users
   */
  public int getAdminEmailNumberOfUsers() {
    return this.adminEmailNumberOfUsers;
  }


  /**
   * number of users emailed
   * @param adminEmailNumberOfUsers1
   */
  public void setAdminEmailNumberOfUsers(int adminEmailNumberOfUsers1) {
    this.adminEmailNumberOfUsers = adminEmailNumberOfUsers1;
  }

  /**
   * report bean
   *
   */
  public static class TwoFactorAdminReportBean {
    
    /** opted in users */
    private int optedInUsers;

    /** opted out users */
    private int optedOutUsers;

    /**
     * opted in users
     * @return opted in users
     */
    public int getOptedInUsers() {
      return this.optedInUsers;
    }

    /**
     * opted in users
     * @param optedInUsers1
     */
    public void setOptedInUsers(int optedInUsers1) {
      this.optedInUsers = optedInUsers1;
    }

    /**
     * opted out users
     * @return opted out users
     */
    public int getOptedOutUsers() {
      return this.optedOutUsers;
    }

    /**
     * opted out users
     * @param optedOutUsers1
     */
    public void setOptedOutUsers(int optedOutUsers1) {
      this.optedOutUsers = optedOutUsers1;
    }
    
    
  }
  
  /**
   * cache the logins in a hash cache.  Key is the realm, and principal
   */
  private static TwoFactorCache<Boolean, TwoFactorAdminReportBean> twoFactorAdminReportBeanCache = new TwoFactorCache<Boolean, TwoFactorAdminReportBean>(
      TwoFactorAdminContainer.class.getName() + ".report");

  /**
   * get the report bean
   * @return bean
   */
  public TwoFactorAdminReportBean getTwoFactorAdminReportBean() {
    TwoFactorAdminReportBean twoFactorAdminReportBean = twoFactorAdminReportBeanCache.get(Boolean.TRUE);
    if (twoFactorAdminReportBean == null) {
      synchronized (TwoFactorAdminContainer.class) {
        twoFactorAdminReportBean = twoFactorAdminReportBeanCache.get(Boolean.TRUE);
        if (twoFactorAdminReportBean == null) {
          twoFactorAdminReportBean = new TwoFactorAdminReportBean();
          twoFactorAdminReportBean.setOptedInUsers(TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveCountOfOptedInUsers());
          twoFactorAdminReportBean.setOptedOutUsers(TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveCountOfOptedOutUsers());
          twoFactorAdminReportBeanCache.put(Boolean.TRUE, twoFactorAdminReportBean);
          
        }
      }
    }
    return twoFactorAdminReportBean;
  }
  
  
  /**
   * 
   * @return loginid this user is acting as (if different than self)
   */
  public String getActingAsLoginid() {
    String actingAs = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    String original = TwoFactorFilterJ2ee.retrieveUserIdFromRequestOriginalNotActAs();
    if (!StringUtils.isBlank(actingAs) && !StringUtils.equals(actingAs, original)) {
      return actingAs;
    }
    return null;
  }
  
  /**
   * if the logged in user can backdoor as other users
   * @return if logged in user can backdoor
   */
  public boolean isCanLoggedInUserBackdoor() {
    return TwoFactorFilterJ2ee.allowedToActAsOtherUsers();
  }

  private static TwoFactorCache<String, Boolean> adminEmailCache = new TwoFactorCache<String, Boolean>(TwoFactorAdminContainer.class.getName() + ".adminEmailCache", 100, false, 120, 120, false);

  
  /**
   * if the logged in user can send email to all users
   * @return if the logged in user can send email to ll users
   */
  public boolean isCanLoggedInUserEmailAll() {

    String originalLoginid = TwoFactorFilterJ2ee.retrieveUserIdFromRequestOriginalNotActAs();
    
    //see if it is in cache
    Boolean isAdminEmail = adminEmailCache.get(originalLoginid);
    if (isAdminEmail != null) {
      return isAdminEmail;
    }

    TwoFactorAuthorizationInterface twoFactorAuthorizationInterface = TwoFactorServerConfig.retrieveConfig().twoFactorAuthorization();

    Set<String> adminUserIdsWhoCanEmailAllUsersSet = twoFactorAuthorizationInterface.adminUserIdsWhoCanEmailAllUsers();

    Source theSource = TfSourceUtils.mainSource();

    isAdminEmail = TfSourceUtils.subjectIdOrNetIdInSet(theSource, originalLoginid, adminUserIdsWhoCanEmailAllUsersSet);
  
  
    adminEmailCache.put(originalLoginid, isAdminEmail);
    
    return isAdminEmail;

  }
  
  /**
   * subject operating on
   */
  private Subject subjectOperatingOn;
  
  /**
   * subject operating on
   * @return subject operating on
   */
  public Subject getSubjectOperatingOn() {
    return this.subjectOperatingOn;
  }

  /**
   * subject operating on
   * @param subjectOperatingOn1
   */
  public void setSubjectOperatingOn(Subject subjectOperatingOn1) {
    this.subjectOperatingOn = subjectOperatingOn1;
  }

  /**
   * the user being operated on
   */
  private TwoFactorUser twoFactorUserOperatingOn;
  
  /**
   * the user being operated on
   * @return the twoFactorUserOperatingOn
   */
  public TwoFactorUser getTwoFactorUserOperatingOn() {
    return this.twoFactorUserOperatingOn;
  }
  
  /**
   * the user being operated on
   * @param twoFactorUserOperatingOn1 the twoFactorUserOperatingOn to set
   */
  public void setTwoFactorUserOperatingOn(TwoFactorUser twoFactorUserOperatingOn1) {
    this.twoFactorUserOperatingOn = twoFactorUserOperatingOn1;
  }

  /**
   * userId that the admin is operating on
   */
  private String userIdOperatingOn;
  
  /**
   * userId that the admin is operating on
   * @return the userIdOperatingOn
   */
  public String getUserIdOperatingOn() {
    return this.userIdOperatingOn;
  }
  
  /**
   * userId that the admin is operating on
   * @param userIdOperatingOn1 the userIdOperatingOn to set
   */
  public void setUserIdOperatingOn(String userIdOperatingOn1) {
    this.userIdOperatingOn = userIdOperatingOn1;
  }

  
}
