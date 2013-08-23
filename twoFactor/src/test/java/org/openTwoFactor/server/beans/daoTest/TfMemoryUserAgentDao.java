/**
 * @author mchyzer
 * $Id: TfMemoryUserAgentDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.dao.TwoFactorUserAgentDao;



/**
 * 
 */
public class TfMemoryUserAgentDao implements TwoFactorUserAgentDao {

  /**
   * browsers
   */
  public static List<TwoFactorUserAgent> userAgents = Collections.synchronizedList(new ArrayList<TwoFactorUserAgent>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#delete(org.openTwoFactor.server.beans.TwoFactorUserAgent)
   */
  @Override
  public void delete(TwoFactorUserAgent twoFactorUserAgent) {
    Iterator<TwoFactorUserAgent> iterator = userAgents.iterator();
    while (iterator.hasNext()) {
      TwoFactorUserAgent current = iterator.next();
      if (current == twoFactorUserAgent || StringUtils.equals(twoFactorUserAgent.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#retrieveByUserAgent(java.lang.String)
   */
  @Override
  public TwoFactorUserAgent retrieveByUserAgent(String userAgent) {
    for (TwoFactorUserAgent current : userAgents) {
      if (StringUtils.equals(current.getUserAgent(), userAgent)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#store(org.openTwoFactor.server.beans.TwoFactorUserAgent)
   */
  @Override
  public boolean store(TwoFactorUserAgent twoFactorUserAgent, boolean exceptionOnError) {
    if (StringUtils.isBlank(twoFactorUserAgent.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorUserAgent);

    userAgents.add(twoFactorUserAgent);
    return true;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorUserAgent retrieveByUuid(String uuid) {
    for (TwoFactorUserAgent current : userAgents) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorUserAgent> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorUserAgent> result = new ArrayList<TwoFactorUserAgent>();
    for (TwoFactorUserAgent current : userAgents) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        if (!TfMemoryAuditDao.auditUsesUserAgent(current)) {
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
