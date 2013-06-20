/**
 * @author mchyzer
 * $Id: TwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorUser;



/**
 * data access object interface for user table
 */
public interface TwoFactorUserDao {
  
  /**
   * find the users who selected the param uuid to be able to opt them out
   * @param uuid
   * @return the list of users
   */
  public List<TwoFactorUser> retrieveUsersWhoPickedThisUserToOptThemOut(String uuid);
  
  /**
   * retrieve users that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the users
   */
  public List<TwoFactorUser> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);


  /**
   * retrieve user by loginid, include attributes
   * @param loginid
   * @return the user
   */
  public TwoFactorUser retrieveByLoginid(String loginid);
  
  /**
   * retrieve user by uuid
   * @param uuid
   * @return the user
   */
  public TwoFactorUser retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB, include attributes
   * @param twoFactorUser
   */
  public void store(TwoFactorUser twoFactorUser);

  /**
   * delete user from table, note, make sure to set the delete date first, include attributes
   * @param twoFactorUser
   */
  public void delete(TwoFactorUser twoFactorUser);
}
