/**
 * @author mchyzer
 * $Id: TwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorReportPrivilege;



/**
 * data access object interface for report privilege table.
 */
public interface TwoFactorReportPrivilegeDao {

  /**
   * find all two factor report privileges
   * @return report privileges
   */
  public List<TwoFactorReportPrivilege> retrieveAll();
  
  /**
   * find two factor report privileges allowed by user uuid (note, doesnt take rollups into account)
   * @param userUuid
   * @return report privileges
   */
  public List<TwoFactorReportPrivilege> retrieveByUserUuid(String userUuid);
  
  /**
   * retrieve report privilege by uuid
   * @param uuid
   * @return the report privilege row
   */
  public TwoFactorReportPrivilege retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorReportPrivilege
   */
  public void store(TwoFactorReportPrivilege twoFactorReportPrivilege);

  /**
   * delete report privilege from table, note, make sure to set the delete date first
   * @param twoFactorReportPrivilege
   */
  public void delete(TwoFactorReportPrivilege twoFactorReportPrivilege);

}
