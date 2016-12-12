/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorReport;
import org.openTwoFactor.server.daemon.TfReportConfig;
import org.openTwoFactor.server.daemon.TfReportData;
import org.openTwoFactor.server.dao.TwoFactorReportDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorReportDao implements TwoFactorReportDao {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorReport retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorReport twoFactorReport = HibernateSession.byHqlStatic().createQuery(
        "select tfr from TwoFactorReport as tfr where tfr.uuid = :theUuid")
        .setString("theUuid", uuid).uniqueResult(TwoFactorReport.class);
    return twoFactorReport;
  }

  /**
   * @see TwoFactorReportDao#store(TwoFactorReport)
   */
  @Override
  public void store(TwoFactorReport twoFactorReport) {
    if (twoFactorReport == null) {
      throw new NullPointerException("twoFactorReport is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorReport);
    
  }

  /**
   * @see TwoFactorReportDao#delete(TwoFactorReport)
   */
  @Override
  public void delete(TwoFactorReport twoFactorReport) {
    if (twoFactorReport == null) {
      throw new NullPointerException("twoFactorReport is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorReport);
    
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveByParentReportUuid(java.lang.String)
   */
  @Override
  public List<TwoFactorReport> retrieveByParentReportUuid(String parentReportUuid) {
    List<TwoFactorReport> twoFactorReports = HibernateSession.byHqlStatic().createQuery(
        "select tfr from TwoFactorReport as tfr, TwoFactorReportRollup tfrr where tfr.uuid = tfrr.childReportUuid "
        + " and tfrr.parentReportUuid = :parentReportUuid order by tfr.reportNameSystem ")
        .setString("parentReportUuid", parentReportUuid).list(TwoFactorReport.class);
    return twoFactorReports;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveByUserUuid(java.lang.String)
   */
  @Override
  public List<TwoFactorReport> retrieveByUserUuid(String userUuid) {
    List<TwoFactorReport> twoFactorReports = HibernateSession.byHqlStatic().createQuery(
        "select tfr from TwoFactorReport as tfr, TwoFactorReportPrivilege tfrp where tfr.uuid = tfrp.reportUuid "
        + " and tfrr.parentReportUuid = theUserUuid order by tfr.reportNameSystem ")
        .setString("theUserUuid", userUuid).list(TwoFactorReport.class);
    return twoFactorReports;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveAll()
   */
  @Override
  public List<TwoFactorReport> retrieveAll() {
    List<TwoFactorReport> twoFactorReports = HibernateSession.byHqlStatic().createQuery(
        "select tfr from TwoFactorReport as tfr order by tfr.reportNameFriendly ")
        .list(TwoFactorReport.class);
    return twoFactorReports;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveReportByConfig(org.openTwoFactor.server.daemon.TfReportConfig)
   */
  public TfReportData retrieveReportByConfig(TfReportConfig tfReportConfig) {
    
    TfReportData tfReportData = new TfReportData();

    //lets parse the query:
    String headersString = tfReportConfig.getQuery();
    headersString = headersString.substring("select ".length());
    
    headersString = headersString.substring(0, headersString.toLowerCase().indexOf(" from "));
    
    List<String> headers = TwoFactorServerUtils.splitTrimToList(headersString, ", ");
    
    List<String[]> data = HibernateSession.bySqlStatic().listSelect(String[].class, tfReportConfig.getQuery(), null);

    tfReportData.setHeaders(headers);
    tfReportData.setData(data);
    return tfReportData;
    
  }

}
