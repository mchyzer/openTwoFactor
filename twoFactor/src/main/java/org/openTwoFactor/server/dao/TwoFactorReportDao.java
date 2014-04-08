/**
 * @author mchyzer
 * $Id: TwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorReport;



/**
 * data access object interface for report table.
 */
public interface TwoFactorReportDao {

  /**
   * reports that are implied by a parent report
   * @param parentReportUuid
   * @return devi
   */
  public List<TwoFactorReport> retrieveByParentReportUuid(String parentReportUuid);

  /**
   * find two factor reports allowed by user uuid (note, doesnt take rollups into account)
   * @param userUuid
   * @return reports
   */
  public List<TwoFactorReport> retrieveByUserUuid(String userUuid);
  
  /**
   * retrieve report by uuid
   * @param uuid
   * @return the report row
   */
  public TwoFactorReport retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorReport
   */
  public void store(TwoFactorReport twoFactorReport);

  /**
   * delete report from table, note, make sure to set the delete date first
   * @param twoFactorReport
   */
  public void delete(TwoFactorReport twoFactorReport);

  /**
   * get all reports
   * @return all reports
   */
  public List<TwoFactorReport> retrieveAll();

}
