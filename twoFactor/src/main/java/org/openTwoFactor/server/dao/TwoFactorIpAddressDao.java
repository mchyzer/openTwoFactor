/**
 * @author mchyzer
 * $Id: TwoFactorIpAddressDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorIpAddress;



/**
 * data access object interface for ip address table
 */
public interface TwoFactorIpAddressDao {

  /**
   * retrieve ip addresses that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the browsers
   */
  public List<TwoFactorIpAddress> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);

  /**
   * retrieve ipAddress by ipAddress
   * @param ipAddress
   * @return the ip address
   */
  public TwoFactorIpAddress retrieveByIpAddress(String ipAddress);
  
  /**
   * retrieve ipAddress by uuid
   * @param uuid
   * @return the ipaddress
   */
  public TwoFactorIpAddress retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorIpAddress
   */
  public void store(TwoFactorIpAddress twoFactorIpAddress);

  /**
   * delete ip address from table
   * @param twoFactorIpAddress
   */
  public void delete(TwoFactorIpAddress twoFactorIpAddress);
}
