/**
 * @author mchyzer
 * $Id: TwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.Collection;
import java.util.List;



/**
 * data access object interface for report data view.
 */
public interface TwoFactorReportDataDao {

  /**
   * return the total count of users by report system names
   * @param reportSystemNames
   * @return the number of users
   */
  public int retrieveTotalCountByReportSystemNames(Collection<String> reportSystemNames);

  /**
   * return the count of opted in users by report system names
   * @param reportSystemNames
   * @return the number of opted in users
   */
  public int retrieveOptedInCountByReportSystemNames(Collection<String> reportSystemNames);

  /**
   * return a page (e.g. size 200?) of loginids who are not signed up for 2-step verficiation
   * @param reportSystemNames
   * @return the list of users
   */
  public List<String> retrieveUsersNotOptedInPageByReportSystemNames(Collection<String> reportSystemNames);

  /**
   * retrieve all the system names
   * @return system names
   */
  public List<String> retrieveReportNameSystems();

  
}
