/**
 * @author mchyzer
 * $Id: TfMemoryUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUserView;
import org.openTwoFactor.server.dao.TwoFactorUserViewDao;

/**
 * 
 */
public class TfMemoryUserViewDao implements TwoFactorUserViewDao {

  /**
   * users
   */
  public static List<TwoFactorUserView> userViews = Collections.synchronizedList(new ArrayList<TwoFactorUserView>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserViewDao#retrieveByLoginid(java.lang.String)
   */
  @Override
  public TwoFactorUserView retrieveByLoginid(String loginid) {
    for (TwoFactorUserView current : userViews) {
      if (StringUtils.equals(current.getLoginid(), loginid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserViewDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorUserView retrieveByUuid(String uuid) {
    for (TwoFactorUserView current : userViews) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see TwoFactorUserViewDao#retrieveAllOptedInUsers()
   */
  @Override
  public List<TwoFactorUserView> retrieveAllOptedInUsers() {
    return userViews;
  }
}
