/**
 * @author mchyzer
 * $Id: TwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorUserView;



/**
 * data access object interface for user view
 */
public interface TwoFactorUserViewDao {

  /**
   * retrieve user by loginid
   * @param loginid
   * @return the user
   */
  public TwoFactorUserView retrieveByLoginid(String loginid);

  /**
   * retrieve user by uuid
   * @param uuid
   * @return the user
   */
  public TwoFactorUserView retrieveByUuid(String uuid);

  /**
   * retrieve all opted in users
   * @return the users
   */
  public List<TwoFactorUserView> retrieveAllOptedInUsers();

}
