/**
 * @author mchyzer
 * $Id: TfMemoryIpAddressDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorIpAddress;
import org.openTwoFactor.server.dao.TwoFactorIpAddressDao;



/**
 * 
 */
public class TfMemoryIpAddressDao implements TwoFactorIpAddressDao {

  /**
   * browsers
   */
  public static List<TwoFactorIpAddress> ipAddresses = Collections.synchronizedList(new ArrayList<TwoFactorIpAddress>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#delete(org.openTwoFactor.server.beans.TwoFactorIpAddress)
   */
  @Override
  public void delete(TwoFactorIpAddress twoFactorIpAddress) {
    Iterator<TwoFactorIpAddress> iterator = ipAddresses.iterator();
    while (iterator.hasNext()) {
      TwoFactorIpAddress current = iterator.next();
      if (current == twoFactorIpAddress || StringUtils.equals(twoFactorIpAddress.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#retrieveByIpAddress(java.lang.String)
   */
  @Override
  public TwoFactorIpAddress retrieveByIpAddress(String ipAddress) {
    for (TwoFactorIpAddress current : ipAddresses) {
      if (StringUtils.equals(current.getIpAddress(), ipAddress)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#store(org.openTwoFactor.server.beans.TwoFactorIpAddress)
   */
  @Override
  public void store(TwoFactorIpAddress twoFactorIpAddress) {
    if (StringUtils.isBlank(twoFactorIpAddress.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorIpAddress);

    ipAddresses.add(twoFactorIpAddress);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorIpAddress retrieveByUuid(String uuid) {
    for (TwoFactorIpAddress current : ipAddresses) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorIpAddress> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorIpAddress> result = new ArrayList<TwoFactorIpAddress>();
    for (TwoFactorIpAddress current : ipAddresses) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        if (!TfMemoryAuditDao.auditUsesIpAddress(current)) {
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
