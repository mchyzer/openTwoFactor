/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorReportRollup;
import org.openTwoFactor.server.dao.TwoFactorReportRollupDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorReportRollupDao implements TwoFactorReportRollupDao {

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportRollupDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorReportRollup retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorReportRollup twoFactorReportRollup = HibernateSession.byHqlStatic().createQuery(
        "select tfrr from TwoFactorReportRollup as tfrr where tfrr.uuid = :theUuid")
        .setString("theUuid", uuid).uniqueResult(TwoFactorReportRollup.class);
    return twoFactorReportRollup;
  }

  /**
   * @see TwoFactorReportRollupDao#store(TwoFactorReportRollup)
   */
  @Override
  public void store(TwoFactorReportRollup twoFactorReportRollup) {
    if (twoFactorReportRollup == null) {
      throw new NullPointerException("twoFactorReportRollup is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorReportRollup);
    
  }

  /**
   * @see TwoFactorReportRollupDao#delete(TwoFactorReportRollup)
   */
  @Override
  public void delete(TwoFactorReportRollup twoFactorReportRollup) {
    if (twoFactorReportRollup == null) {
      throw new NullPointerException("twoFactorReportRollup is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorReportRollup);
    
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportRollupDao#retrieveAll()
   */
  public List<TwoFactorReportRollup> retrieveAll() {
    List<TwoFactorReportRollup> twoFactorReportRollups = HibernateSession.byHqlStatic().createQuery(
        "select tfrr from TwoFactorReportRollup as tfrr, TwoFactorReport as parentReport, TwoFactorReport as childReport "
        + " where tfrr.parentReportUuid = parentReport.uuid and tfrr.childReportUuid = childReport.uuid "
        + " order by parentReport.reportNameFriendly, childReport.reportNameFriendly ")
        .list(TwoFactorReportRollup.class);
    return twoFactorReportRollups;
  }

}
