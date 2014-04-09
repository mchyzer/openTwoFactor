/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.ui.beans;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openTwoFactor.server.beans.TwoFactorReport;
import org.openTwoFactor.server.beans.TwoFactorReportRollup;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class TwoFactorReportStat {

  /**
   * calculate stats for this report
   * @param twoFactorDaoFactory
   * @param allReports 
   * @param allRollups 
   * @param subjectStringsNotOptedIn not null to get the strings
   */
  public void calculateStats(TwoFactorDaoFactory twoFactorDaoFactory, Map<String, TwoFactorReport> allReports,
      Map<String, TwoFactorReportRollup> allRollups, Set<String> subjectStringsNotOptedIn, Source subjectSource) {
    
    Set<String> reportNameSystems = new HashSet<String>();
    
    switch (this.twoFactorReport.getReportTypeEnum()) {
      case group: 
        reportNameSystems.add(this.twoFactorReport.getReportNameSystem());
        break;
      case rollup:
        List<TwoFactorReportRollup> childRollups = TwoFactorReportRollup.retrieveChildRollups(allRollups, this.twoFactorReport.getUuid());
        for (TwoFactorReportRollup twoFactorReportRollup : childRollups) {
          TwoFactorReport theTwoFactorReport = allReports.get(twoFactorReportRollup.getChildReportUuid());
          reportNameSystems.add(theTwoFactorReport.getReportNameSystem());
        }
        break;
      default:
        throw new RuntimeException("not expecting " + this.twoFactorReport.getReportTypeEnum());
    }
    
    this.total = twoFactorDaoFactory.getTwoFactorReportData().retrieveTotalCountByReportSystemNames(reportNameSystems);
    int countOptedIn = twoFactorDaoFactory.getTwoFactorReportData().retrieveOptedInCountByReportSystemNames(reportNameSystems);
    
    this.totalNotOptedIn = this.total == 0 ? 0 : (this.total - countOptedIn);
    
    if (subjectStringsNotOptedIn != null) {
      
      List<String> loginidsNotOptedIn = twoFactorDaoFactory.getTwoFactorReportData().retrieveUsersNotOptedInPageByReportSystemNames(reportNameSystems);
      if (TwoFactorServerUtils.length(loginidsNotOptedIn) > 0) {
        Map<String, Subject> subjectMap = TfSourceUtils.retrieveSubjectsByIdsOrIdentifiers(subjectSource, loginidsNotOptedIn, true);
        for (String loginid : TwoFactorServerUtils.nonNull(subjectMap).keySet()) {
          Subject subject = subjectMap.get(loginid);
          subjectStringsNotOptedIn.add(TfSourceUtils.subjectDescription(subject, loginid));
        }
      }
    }
  }
  
  /**
   * total in report
   */
  private int total;
  
  /**
   * total not opted in
   */
  private int totalNotOptedIn;

  /**
   * two factor report to show
   */
  private TwoFactorReport twoFactorReport;

  /**
   * percentage of people opted in
   * @return the percentOptedIn
   */
  public String getPercentOptedIn() {
    
    if (this.total == 0) {
      return "-";
    }
    
    if (this.totalNotOptedIn == 0) {
      return "100%";
    }
    
    if (this.total <= this.totalNotOptedIn) {
      return "0%";
    }
    
    double percentDouble = 100 * (((double)(this.total - this.totalNotOptedIn)) / this.total);
    int percent = (int)Math.round(percentDouble);
    return percent + "%";
  }

  /**
   * total in report
   * @return the total
   */
  public int getTotal() {
    return this.total;
  }

  /**
   * total not opted in
   * @return the totalNotOptedIn
   */
  public int getTotalNotOptedIn() {
    return this.totalNotOptedIn;
  }

  /**
   * total in report
   * @param total1 the total to set
   */
  public void setTotal(int total1) {
    this.total = total1;
  }

  /**
   * total not opted in
   * @param totalNotOptedIn1 the totalNotOptedIn to set
   */
  public void setTotalNotOptedIn(int totalNotOptedIn1) {
    this.totalNotOptedIn = totalNotOptedIn1;
  }

  /**
   * two factor report to show
   * @return the twoFactorReport
   */
  public TwoFactorReport getTwoFactorReport() {
    return this.twoFactorReport;
  }

  /**
   * two factor report to show
   * @param twoFactorReport1 the twoFactorReport to set
   */
  public void setTwoFactorReport(TwoFactorReport twoFactorReport1) {
    this.twoFactorReport = twoFactorReport1;
  }

}
