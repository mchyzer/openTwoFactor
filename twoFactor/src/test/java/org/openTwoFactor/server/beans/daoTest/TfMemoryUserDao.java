/**
 * @author mchyzer
 * $Id: TfMemoryUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.dao.TwoFactorUserDao;



/**
 * 
 */
public class TfMemoryUserDao implements TwoFactorUserDao {

  /**
   * users
   */
  public static List<TwoFactorUser> users = Collections.synchronizedList(new ArrayList<TwoFactorUser>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#delete(org.openTwoFactor.server.beans.TwoFactorUser)
   */
  @Override
  public void delete(TwoFactorUser twoFactorUser) {
    Iterator<TwoFactorUser> iterator = users.iterator();
    while (iterator.hasNext()) {
      TwoFactorUser current = iterator.next();
      if (current == twoFactorUser || StringUtils.equals(twoFactorUser.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveByLoginid(java.lang.String)
   */
  @Override
  public TwoFactorUser retrieveByLoginid(String loginid) {
    for (TwoFactorUser current : users) {
      if (StringUtils.equals(current.getLoginid(), loginid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#store(org.openTwoFactor.server.beans.TwoFactorUser)
   */
  @Override
  public void store(TwoFactorUser twoFactorUser) {
    if (StringUtils.isBlank(twoFactorUser.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorUser);
    users.add(twoFactorUser);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorUser retrieveByUuid(String uuid) {
    for (TwoFactorUser current : users) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorUser> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorUser> result = new ArrayList<TwoFactorUser>();
    for (TwoFactorUser current : users) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        if (!TfMemoryAuditDao.auditUsesUser(current)) {
          result.add(current);
        }
      }
      if (result.size() == 1000) {
        break;
      }
    }
    return result;

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveUsersWhoPickedThisUserToOptThemOut(java.lang.String)
   */
  @Override
  public List<TwoFactorUser> retrieveUsersWhoPickedThisUserToOptThemOut(String uuid) {
    if (StringUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank?");
    }
    List<TwoFactorUser> result = new ArrayList<TwoFactorUser>();
    for (TwoFactorUser current : users) {
      if (new TfMemoryUserAttrDao().userPickedThisUserToOptThemOut(current.getUuid(), uuid)) {
        result.add(current);
      }
    }
    return result;
  }

  /**
   * @see TwoFactorUserDao#retrieveCountOfOptedInUsers()
   */
  @Override
  public int retrieveCountOfOptedInUsers() {
    int count = 0;
    for (TwoFactorUser current : users) {
      if (current.isOptedIn()) {
        count++;
      }
    }
    return count;
  }

}
