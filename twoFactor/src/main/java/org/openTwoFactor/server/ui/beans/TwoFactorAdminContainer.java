/**
 * @author mchyzer
 * $Id: TwoFactorAdminContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;



/**
 * admin data for the admin console
 */
public class TwoFactorAdminContainer {

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
