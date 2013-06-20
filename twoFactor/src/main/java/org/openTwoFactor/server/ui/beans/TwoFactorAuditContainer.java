/**
 * @author mchyzer
 * $Id: TwoFactorAuditContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import java.util.ArrayList;
import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorAuditView;



/**
 * container bean for audit screen
 */
public class TwoFactorAuditContainer {

  /** user string for the user who's audits they are */
  private String userString;
  
  /**
   * user string for the user who's audits they are
   * @return the userString
   */
  public String getUserString() {
    return this.userString;
  }
  
  /**
   * user string for the user who's audits they are
   * @param userString1 the userString to set
   */
  public void setUserString(String userString1) {
    this.userString = userString1;
  }

  /**
   * audits for a user or current user
   */
  private List<TwoFactorAuditView> twoFactorAuditViews = new ArrayList<TwoFactorAuditView>();
  /**
   * total count of user audits
   */
  private int twoFactorAuditViewsTotalCount = -1;

  /**
   * displayed count of user audits
   */
  private int twoFactorAuditViewsDisplayedCount = -1;
  
  /**
   * displayed count of user audits
   * @return the twoFactorAuditViewsDisplayedCount
   */
  public int getTwoFactorAuditViewsDisplayedCount() {
    return this.twoFactorAuditViewsDisplayedCount;
  }
  
  /**
   * displayed count of user audits
   * @param twoFactorAuditViewsDisplayedCount1 the twoFactorAuditViewsDisplayedCount to set
   */
  public void setTwoFactorAuditViewsDisplayedCount(int twoFactorAuditViewsDisplayedCount1) {
    this.twoFactorAuditViewsDisplayedCount = twoFactorAuditViewsDisplayedCount1;
  }

  /**
   * audits for a user or current user
   * @return the twoFactorAuditViews
   */
  public List<TwoFactorAuditView> getTwoFactorAuditViews() {
    return this.twoFactorAuditViews;
  }

  /**
   * total count of user audits
   * @return the twoFactorAuditViewsTotalCount
   */
  public int getTwoFactorAuditViewsTotalCount() {
    return this.twoFactorAuditViewsTotalCount;
  }

  /**
   * audits for a user or current user
   * @param twoFactorAuditViews1 the twoFactorAuditViews to set
   */
  public void setTwoFactorAuditViews(List<TwoFactorAuditView> twoFactorAuditViews1) {
    this.twoFactorAuditViews = twoFactorAuditViews1;
  }

  /**
   * total count of user audits
   * @param twoFactorAuditViewsCount1 the twoFactorAuditViewsTotalCount to set
   */
  public void setTwoFactorAuditViewsTotalCount(int twoFactorAuditViewsCount1) {
    this.twoFactorAuditViewsTotalCount = twoFactorAuditViewsCount1;
  }

}
