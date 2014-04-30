/**
 * @author mchyzer
 * $Id: TfMemoryUserAttrDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.dao.TwoFactorUserAttrDao;



/**
 * 
 */
public class TfMemoryUserAttrDao implements TwoFactorUserAttrDao {

  /**
   * see if there is a user who picked the other user to opt them out
   * @param userUuidWhoPicked
   * @param userUuidGotPicked
   * @return true if exists
   */
  public boolean userPickedThisUserToOptThemOut(String userUuidWhoPicked, String userUuidGotPicked) {

    for (TwoFactorUserAttr current : userAttrs) {
      if (StringUtils.equals(current.getUserUuid(), userUuidWhoPicked)
          && StringUtils.equals(current.getAttributeValueString(), userUuidGotPicked)
          && (StringUtils.equals(current.getAttributeName(), TwoFactorUserAttr.TwoFactorUserAttrName.colleague_user_uuid0.name() )
              || StringUtils.equals(current.getAttributeName(), TwoFactorUserAttr.TwoFactorUserAttrName.colleague_user_uuid1.name() )
              || StringUtils.equals(current.getAttributeName(), TwoFactorUserAttr.TwoFactorUserAttrName.colleague_user_uuid2.name() )
              || StringUtils.equals(current.getAttributeName(), TwoFactorUserAttr.TwoFactorUserAttrName.colleague_user_uuid3.name() )
              || StringUtils.equals(current.getAttributeName(), TwoFactorUserAttr.TwoFactorUserAttrName.colleague_user_uuid4.name() ))) {
        return true;
      }

    }
    return false;
  }
  
  /**
   * browsers
   */
  public static List<TwoFactorUserAttr> userAttrs = Collections.synchronizedList(new ArrayList<TwoFactorUserAttr>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#delete(org.openTwoFactor.server.beans.TwoFactorUserAttr)
   */
  @Override
  public void delete(TwoFactorUserAttr twoFactorUserAttr) {
    Iterator<TwoFactorUserAttr> iterator = userAttrs.iterator();
    while (iterator.hasNext()) {
      TwoFactorUserAttr current = iterator.next();
      if (current == twoFactorUserAttr || StringUtils.equals(twoFactorUserAttr.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveByUser(java.lang.String)
   */
  @Override
  public Set<TwoFactorUserAttr> retrieveByUser(String userUuid) {
    Set<TwoFactorUserAttr> result = new HashSet<TwoFactorUserAttr>();
    
    for (TwoFactorUserAttr current : userAttrs) {
      if (StringUtils.equals(current.getUserUuid(), userUuid)) {
        result.add(current);
      }
    }
    return result;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveByUserAndAttributeName(java.lang.String, java.lang.String)
   */
  @Override
  public TwoFactorUserAttr retrieveByUserAndAttributeName(String userUuid,
      String attributeName) {
    for (TwoFactorUserAttr current : userAttrs) {
      if (StringUtils.equals(current.getUserUuid(), userUuid) 
          && StringUtils.equals(current.getAttributeName(), attributeName)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveByAttributeName(java.lang.String)
   */
  @Override
  public List<TwoFactorUserAttr> retrieveByAttributeName(
      String attributeName) {
    List<TwoFactorUserAttr> result = new ArrayList<TwoFactorUserAttr>();

    for (TwoFactorUserAttr current : userAttrs) {
      if (StringUtils.equals(current.getAttributeName(), attributeName)) {
        result.add(current);
      }
    }
    return result;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#store(org.openTwoFactor.server.beans.TwoFactorUserAttr)
   */
  @Override
  public void store(TwoFactorUserAttr twoFactorUserAttr) {
    if (StringUtils.isBlank(twoFactorUserAttr.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }
    delete(twoFactorUserAttr);

    userAttrs.add(twoFactorUserAttr);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorUserAttr> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorUserAttr> result = new ArrayList<TwoFactorUserAttr>();
    for (TwoFactorUserAttr current : userAttrs) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        result.add(current);
      }
      if (result.size() == 1000) {
        break;
      }
    }
    return result;

  }

}
