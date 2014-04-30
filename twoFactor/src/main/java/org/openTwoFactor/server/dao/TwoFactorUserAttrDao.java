/**
 * @author mchyzer
 * $Id: TwoFactorUserAttrDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;
import java.util.Set;

import org.openTwoFactor.server.beans.TwoFactorUserAttr;


/**
 * data access object interface for user attr table
 */
public interface TwoFactorUserAttrDao {

  /**
   * retrieve user attrs that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the user attrs
   */
  public List<TwoFactorUserAttr> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);

  /**
   * retrieve user attr by loginid
   * @param userUuid
   * @param attributeName
   * @return the user
   */
  public TwoFactorUserAttr retrieveByUserAndAttributeName(String userUuid, String attributeName);
  
  /**
   * retrieve user attrs by attribute name
   * @param attributeName
   * @return the user
   */
  public List<TwoFactorUserAttr> retrieveByAttributeName(String attributeName);
  
  /**
   * retrieve attributes for a user, cannot be delete dated
   * @param userUuid
   * @return the user
   */
  public Set<TwoFactorUserAttr> retrieveByUser(String userUuid);
  
  /**
   * insert or update to the DB, including attributes
   * @param twoFactorUserAttr
   */
  public void store(TwoFactorUserAttr twoFactorUserAttr);

  /**
   * delete rows from table, including attributes, note, make sure to set delete date first
   * @param twoFactorUserAttr
   */
  public void delete(TwoFactorUserAttr twoFactorUserAttr);

}
