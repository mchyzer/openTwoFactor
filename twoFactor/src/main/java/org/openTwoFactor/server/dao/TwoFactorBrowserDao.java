/**
 * @author mchyzer
 * $Id: TwoFactorBrowserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorBrowser;



/**
 * data access object interface for browser table
 */
public interface TwoFactorBrowserDao {

  /**
   * retrieve browser trusted uuid
   * @param browserTrustedUuidEncrypted
   * @return the user
   */
  public TwoFactorBrowser retrieveByBrowserTrustedUuid(String browserTrustedUuidEncrypted);
  
  /**
   * retrieve browser by uuid
   * @param uuid
   * @return the user
   */
  public TwoFactorBrowser retrieveByUuid(String uuid);
  
  /**
   * retrieve browsers by uuid
   * @param userUuid
   * @return the list of browsers
   */
  public List<TwoFactorBrowser> retrieveByUserUuid(String userUuid);
  
  /**
   * retrieve trusted browsers by uuid
   * @param userUuid
   * @return the list of browsers
   */
  public List<TwoFactorBrowser> retrieveTrustedByUserUuid(String userUuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorBrowser
   */
  public void store(TwoFactorBrowser twoFactorBrowser);

  /**
   * delete browser from table
   * @param twoFactorBrowser
   */
  public void delete(TwoFactorBrowser twoFactorBrowser);
  
  /**
   * retrieve browsers that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the browsers
   */
  public List<TwoFactorBrowser> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);

}
