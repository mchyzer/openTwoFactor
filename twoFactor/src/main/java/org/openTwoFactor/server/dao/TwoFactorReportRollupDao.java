/**
 * @author mchyzer
 * $Id: TwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorReportRollup;



/**
 * data access object interface for report rollup table.
 */
public interface TwoFactorReportRollupDao {

  /**
   * find all two factor report rollups
   * @return report privileges
   */
  public List<TwoFactorReportRollup> retrieveAll();
  
  /**
   * retrieve report rollup by uuid
   * @param uuid
   * @return the report rollup row
   */
  public TwoFactorReportRollup retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorReportRollup
   */
  public void store(TwoFactorReportRollup twoFactorReportRollup);

  /**
   * delete report rollup from table
   * @param twoFactorReportRollup
   */
  public void delete(TwoFactorReportRollup twoFactorReportRollup);

}
