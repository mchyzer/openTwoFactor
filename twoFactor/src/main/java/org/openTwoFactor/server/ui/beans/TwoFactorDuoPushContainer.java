/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.ui.beans;

import net.sf.json.JSONObject;

import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * container for stuff about duo push
 */
public class TwoFactorDuoPushContainer {

  /**
   * if duo enabled
   * @return if duo enabled
   */
  public boolean isDuoEnabled() {
    boolean duoRegisterUsers = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duo.registerUsers", true);
    return duoRegisterUsers;
  }

  /**
   * if duo web option enabled
   * @return if duo web option enabled
   */
  public boolean isAllowUsersToControlDuoWeb() {
    boolean allowUsersToControlDuoWeb = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duo.allowUsersToControlDuoWeb", true);
    return allowUsersToControlDuoWeb;
  }
  
  /**
   * duo user
   */
  private JSONObject duoUser;
  
  /**
   * duo user
   * @return the duoUser
   */
  public JSONObject getDuoUser() {
    return this.duoUser;
  }
  
  /**
   * duo user
   * @param duoUser1 the duoUser to set
   */
  public void setDuoUser(JSONObject duoUser1) {
    this.duoUser = duoUser1;
  }

  /**
   * barcode url for enrolling in push
   */
  private String barcodeUrl;
  
  /**
   * barcode url for enrolling in push
   * @return the barcodeUrl
   */
  public String getBarcodeUrl() {
    return this.barcodeUrl;
  }
  
  /**
   * barcode url for enrolling in push
   * @param barcodeUrl1 the barcodeUrl to set
   */
  public void setBarcodeUrl(String barcodeUrl1) {
    this.barcodeUrl = barcodeUrl1;
  }

  /**
   * if in process of enrolling in push
   */
  private boolean enrolling = false;
  
  /**
   * if in process of enrolling in push
   * @return the enrolling
   */
  public boolean isEnrolling() {
    return this.enrolling;
  }

  
  /**
   * if in process of enrolling in push
   * @param enrolling1 the enrolling to set
   */
  public void setEnrolling(boolean enrolling1) {
    this.enrolling = enrolling1;
  }

  
  
  /**
   * init the container
   * @param twoFactorUser
   */
  public void init(TwoFactorUser twoFactorUser) {
    
    String duoUserId = DuoCommands.retrieveDuoUserIdBySomeId(twoFactorUser.getLoginid(), false);
    
    this.duoUser = DuoCommands.retrieveDuoUserByIdOrUsername(duoUserId, true);
            
    this.enrolledInDuoPush = DuoCommands.enrolledInPush(this.duoUser);
    
    this.pushForWeb = TwoFactorServerUtils.booleanValue(twoFactorUser.getDuoPushByDefault(), false);
  }
  
  /**
   * if the user is enrolled in duo push
   */
  private boolean enrolledInDuoPush = false;
  
  /**
   * if the user is enrolled in duo push
   * @return if enrolled in duo push
   */
  public boolean isEnrolledInDuoPush() {
    return this.enrolledInDuoPush;
  }
  
  /**
   * if the user is enrolled in duo push
   * @param enrolledInDuoPush1 the enrolledInDuoPush to set
   */
  public void setEnrolledInDuoPush(boolean enrolledInDuoPush1) {
    this.enrolledInDuoPush = enrolledInDuoPush1;
  }

  /**
   * if should do push for web
   */
  private boolean pushForWeb;

  
  /**
   * if should do push for web
   * @return the pushForWeb
   */
  public boolean isPushForWeb() {
    return this.pushForWeb;
  }

  
  /**
   * if should do push for web
   * @param pushForWeb1 the pushForWeb to set
   */
  public void setPushForWeb(boolean pushForWeb1) {
    this.pushForWeb = pushForWeb1;
  }

  
}
