/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.ui.beans;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorReport;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * objects for viewing reports
 */
public class TwoFactorViewReportContainer {

  /**
   * child reports to this report
   */
  private List<TwoFactorReportStat> childReportStats;
  
  /**
   * child reports to this report
   * @return the childReports
   */
  public List<TwoFactorReportStat> getChildReportStats() {
    return this.childReportStats;
  }
  
  /**
   * child reports to this report
   * @param childReports1 the childReports to set
   */
  public void setChildReportStats(List<TwoFactorReportStat> childReports1) {
    this.childReportStats = childReports1;
  }

  /**
   * if there are child reports
   * @return if there are child reports
   */
  public boolean isHasChildReports() {
    return TwoFactorServerUtils.length(this.childReportStats) > 0;
  }
  
  /**
   * stat on the main report
   */
  private TwoFactorReportStat mainReportStat;
  
  /**
   * stat on the main report
   * @return the mainReportStat
   */
  public TwoFactorReportStat getMainReportStat() {
    return this.mainReportStat;
  }

  
  /**
   * stat on the main report
   * @param mainReportStat1 the mainReportStat to set
   */
  public void setMainReportStat(TwoFactorReportStat mainReportStat1) {
    this.mainReportStat = mainReportStat1;
  }

  /**
   * reports the logged in user is allowed to view
   */
  private List<TwoFactorReport> reportsAllowedToView;
  
  /**
   * reports the logged in user is allowed to view
   * @return the reportsAllowedToView
   */
  public List<TwoFactorReport> getReportsAllowedToView() {
    return this.reportsAllowedToView;
  }
  
  /**
   * reports the logged in user is allowed to view
   * @param reportsAllowedToView1 the reportsAllowedToView to set
   */
  public void setReportsAllowedToView(List<TwoFactorReport> reportsAllowedToView1) {
    this.reportsAllowedToView = reportsAllowedToView1;
  }

  /**
   * if should show report
   * @return the showReport
   */
  public boolean isShowReport() {
    return this.mainReportStat != null;
  }
  
  /**
   * subject descriptions not opted in
   */
  private List<String> subjectDescriptionsNotOptedIn;

  
  /**
   * subject descriptions not opted in
   * @return the subjectDescriptionsNotOptedIn
   */
  public List<String> getSubjectDescriptionsNotOptedIn() {
    return this.subjectDescriptionsNotOptedIn;
  }

  /**
   * subject descriptions not opted in
   * @param subjectDescriptionsNotOptedIn1 the subjectDescriptionsNotOptedIn to set
   */
  public void setSubjectDescriptionsNotOptedIn(List<String> subjectDescriptionsNotOptedIn1) {
    this.subjectDescriptionsNotOptedIn = subjectDescriptionsNotOptedIn1;
  }
  
}
