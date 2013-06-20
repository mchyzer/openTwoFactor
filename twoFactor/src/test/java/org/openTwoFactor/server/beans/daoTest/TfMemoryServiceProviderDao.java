/**
 * @author mchyzer
 * $Id: TfMemoryServiceProviderDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorServiceProvider;
import org.openTwoFactor.server.dao.TwoFactorServiceProviderDao;



/**
 * 
 */
public class TfMemoryServiceProviderDao implements TwoFactorServiceProviderDao {

  /**
   * browsers
   */
  public static List<TwoFactorServiceProvider> serviceProviders = Collections.synchronizedList(new ArrayList<TwoFactorServiceProvider>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#delete(org.openTwoFactor.server.beans.TwoFactorServiceProvider)
   */
  @Override
  public void delete(TwoFactorServiceProvider twoFactorServiceProvider) {
    Iterator<TwoFactorServiceProvider> iterator = serviceProviders.iterator();
    while (iterator.hasNext()) {
      TwoFactorServiceProvider current = iterator.next();
      if (current == twoFactorServiceProvider || StringUtils.equals(twoFactorServiceProvider.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#retrieveByServiceProviderId(java.lang.String)
   */
  @Override
  public TwoFactorServiceProvider retrieveByServiceProviderId(String serviceProviderId) {
    for (TwoFactorServiceProvider current : serviceProviders) {
      if (StringUtils.equals(current.getServiceProviderId(), serviceProviderId)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#store(org.openTwoFactor.server.beans.TwoFactorServiceProvider)
   */
  @Override
  public void store(TwoFactorServiceProvider twoFactorServiceProvider) {
    if (StringUtils.isBlank(twoFactorServiceProvider.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorServiceProvider);

    serviceProviders.add(twoFactorServiceProvider);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorServiceProvider retrieveByUuid(String uuid) {
    for (TwoFactorServiceProvider current : serviceProviders) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorServiceProvider> retrieveDeletedOlderThanAge(
      long selectBeforeThisMilli) {
    List<TwoFactorServiceProvider> result = new ArrayList<TwoFactorServiceProvider>();
    for (TwoFactorServiceProvider current : serviceProviders) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        if (!TfMemoryAuditDao.auditUsesServiceProvider(current)) {
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
