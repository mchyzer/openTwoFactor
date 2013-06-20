/**
 * @author mchyzer
 * $Id: TwoFactorUserAgentDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorUserAgent;



/**
 * data access object interface for user agent table
 */
public interface TwoFactorUserAgentDao {

  /**
   * retrieve user agents that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the user agents
   */
  public List<TwoFactorUserAgent> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);

  /**
   * retrieve userAgent by userAgent
   * @param userAgent
   * @return the user agent
   */
  public TwoFactorUserAgent retrieveByUserAgent(String userAgent);
  
  /**
   * retrieve userAgent by uuid
   * @param uuid
   * @return the user agent
   */
  public TwoFactorUserAgent retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorUserAgent
   */
  public void store(TwoFactorUserAgent twoFactorUserAgent);

  /**
   * delete user agent from table
   * @param twoFactorUserAgent
   */
  public void delete(TwoFactorUserAgent twoFactorUserAgent);
}
