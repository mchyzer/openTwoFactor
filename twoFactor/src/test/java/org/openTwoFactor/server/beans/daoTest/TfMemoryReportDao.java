/**
 * @author mchyzer
 * $Id: TfMemoryUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorReport;
import org.openTwoFactor.server.dao.TwoFactorReportDao;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * 
 */
public class TfMemoryReportDao implements TwoFactorReportDao {

  /**
   * users
   */
  public static List<TwoFactorReport> reports = Collections.synchronizedList(new ArrayList<TwoFactorReport>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#delete(TwoFactorReport)
   */
  @Override
  public void delete(TwoFactorReport twoFactorReport) {
    Iterator<TwoFactorReport> iterator = reports.iterator();
    while (iterator.hasNext()) {
      TwoFactorReport current = iterator.next();
      if (current == twoFactorReport || StringUtils.equals(twoFactorReport.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#store(TwoFactorReport)
   */
  @Override
  public void store(TwoFactorReport twoFactorReport) {
    if (StringUtils.isBlank(twoFactorReport.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorReport);
    reports.add(twoFactorReport);
  }


  /**
   * 
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorReport retrieveByUuid(String uuid) {
    for (TwoFactorReport twoFactorReport : TwoFactorServerUtils.nonNull(reports)) {
      if (StringUtils.equals(uuid, twoFactorReport.getUuid())) {
        return twoFactorReport;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveByParentReportUuid(java.lang.String)
   */
  public List<TwoFactorReport> retrieveByParentReportUuid(String parentReportUuid) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveByUserUuid(java.lang.String)
   */
  public List<TwoFactorReport> retrieveByUserUuid(String userUuid) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDao#retrieveAll()
   */
  public List<TwoFactorReport> retrieveAll() {
    return new ArrayList<TwoFactorReport>(reports);
  }

}
