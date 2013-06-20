/**
 * @author mchyzer
 * $Id: TfMemoryBrowserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.dao.TwoFactorBrowserDao;



/**
 *
 */
public class TfMemoryBrowserDao implements TwoFactorBrowserDao {

  /**
   * browsers
   */
  public static List<TwoFactorBrowser> browsers = Collections.synchronizedList(new ArrayList<TwoFactorBrowser>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#delete(org.openTwoFactor.server.beans.TwoFactorBrowser)
   */
  @Override
  public void delete(TwoFactorBrowser twoFactorBrowser) {
    Iterator<TwoFactorBrowser> iterator = browsers.iterator();
    while (iterator.hasNext()) {
      TwoFactorBrowser current = iterator.next();
      if (current == twoFactorBrowser || StringUtils.equals(twoFactorBrowser.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveByBrowserTrustedUuid(java.lang.String)
   */
  @Override
  public TwoFactorBrowser retrieveByBrowserTrustedUuid(String browserTrustedUuidEncrypted) {
    
    for (TwoFactorBrowser current : browsers) {
      if (StringUtils.equals(current.getBrowserTrustedUuid(), browserTrustedUuidEncrypted)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveByUserUuid(java.lang.String)
   */
  @Override
  public List<TwoFactorBrowser> retrieveByUserUuid(String userUuid) {
    List<TwoFactorBrowser> result = new ArrayList<TwoFactorBrowser>();
    
    for (TwoFactorBrowser current : browsers) {
      if (StringUtils.equals(current.getUserUuid(), userUuid)) {
        result.add(current);
      }
    }
    return result;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#store(org.openTwoFactor.server.beans.TwoFactorBrowser)
   */
  @Override
  public void store(TwoFactorBrowser twoFactorBrowser) {
    if (StringUtils.isBlank(twoFactorBrowser.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorBrowser);

    browsers.add(twoFactorBrowser);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorBrowser retrieveByUuid(String uuid) {
    for (TwoFactorBrowser current : browsers) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveTrustedByUserUuid(java.lang.String)
   */
  @Override
  public List<TwoFactorBrowser> retrieveTrustedByUserUuid(String userUuid) {
    List<TwoFactorBrowser> result = new ArrayList<TwoFactorBrowser>();
    
    for (TwoFactorBrowser current : browsers) {
      if (StringUtils.equals(current.getUserUuid(), userUuid) && current.isTrustedBrowser()) {
        result.add(current);
      }
    }
    return result;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorBrowser> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorBrowser> result = new ArrayList<TwoFactorBrowser>();
    for (TwoFactorBrowser current : browsers) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        if (!TfMemoryAuditDao.auditUsesBrowser(current)) {
          result.add(current);
        }
      }
      if (result.size() == 1000) {
        break;
      }
    }
    return result;
  }

}
