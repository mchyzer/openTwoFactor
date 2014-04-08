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
import org.openTwoFactor.server.beans.TwoFactorReportRollup;
import org.openTwoFactor.server.dao.TwoFactorReportRollupDao;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * 
 */
public class TfMemoryReportRollupDao implements TwoFactorReportRollupDao {

  /**
   * users
   */
  public static List<TwoFactorReportRollup> reportRollups = Collections.synchronizedList(new ArrayList<TwoFactorReportRollup>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportRollupDao#delete(TwoFactorReportRollup)
   */
  @Override
  public void delete(TwoFactorReportRollup twoFactorReportRollup) {
    Iterator<TwoFactorReportRollup> iterator = reportRollups.iterator();
    while (iterator.hasNext()) {
      TwoFactorReportRollup current = iterator.next();
      if (current == twoFactorReportRollup || StringUtils.equals(twoFactorReportRollup.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportRollupDao#store(TwoFactorReportRollup)
   */
  @Override
  public void store(TwoFactorReportRollup twoFactorReportRollup) {
    if (StringUtils.isBlank(twoFactorReportRollup.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorReportRollup);
    reportRollups.add(twoFactorReportRollup);
  }

  /**
   * 
   * @see org.openTwoFactor.server.dao.TwoFactorReportRollupDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorReportRollup retrieveByUuid(String uuid) {
    for (TwoFactorReportRollup twoFactorReportRollup : TwoFactorServerUtils.nonNull(reportRollups)) {
      if (StringUtils.equals(uuid, twoFactorReportRollup.getUuid())) {
        return twoFactorReportRollup;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportRollupDao#retrieveAll()
   */
  @Override
  public List<TwoFactorReportRollup> retrieveAll() {
    return new ArrayList<TwoFactorReportRollup>(reportRollups);
  }


}
