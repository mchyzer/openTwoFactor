/**
 * @author mchyzer
 * $Id: TwoFactorServiceProviderDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorServiceProvider;



/**
 * data access object interface for service provider table
 */
public interface TwoFactorServiceProviderDao {

  /**
   * retrieve service providers that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the service providers
   */
  public List<TwoFactorServiceProvider> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);

  /**
   * retrieve serviceProvider by serviceProvider
   * @param serviceProvider
   * @return the service provider
   */
  public TwoFactorServiceProvider retrieveByServiceProviderId(String serviceProvider);
  
  /**
   * retrieve serviceProvider by uuid
   * @param uuid
   * @return the service provider
   */
  public TwoFactorServiceProvider retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorServiceProvider
   */
  public void store(TwoFactorServiceProvider twoFactorServiceProvider);

  /**
   * delete ip address from table
   * @param twoFactorServiceProvider
   */
  public void delete(TwoFactorServiceProvider twoFactorServiceProvider);
}
