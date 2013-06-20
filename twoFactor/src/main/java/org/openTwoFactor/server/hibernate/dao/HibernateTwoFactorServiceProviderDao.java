/**
 * @author mchyzer
 * $Id: HibernateTwoFactorServiceProviderDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorServiceProvider;
import org.openTwoFactor.server.dao.TwoFactorServiceProviderDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorServiceProviderDao implements TwoFactorServiceProviderDao {

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#delete(org.openTwoFactor.server.beans.TwoFactorServiceProvider)
   */
  @Override
  public void delete(final TwoFactorServiceProvider twoFactorServiceProvider) {
    if (twoFactorServiceProvider == null) {
      throw new NullPointerException("twoFactorServiceProvider is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorServiceProvider);

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#retrieveByServiceProviderId(java.lang.String)
   */
  @Override
  public TwoFactorServiceProvider retrieveByServiceProviderId(String serviceProviderId) {
    if (TwoFactorServerUtils.isBlank(serviceProviderId)) {
      throw new RuntimeException("Why is serviceProvider blank? ");
    }
    
    List<TwoFactorServiceProvider> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfsp from TwoFactorServiceProvider as tfsp where tfsp.serviceProviderId = :theServiceProviderId")
        .setString("theServiceProviderId", serviceProviderId).list(TwoFactorServiceProvider.class);
    TwoFactorServiceProvider twoFactorServiceProvider = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorServiceProvider;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#store(org.openTwoFactor.server.beans.TwoFactorServiceProvider)
   */
  @Override
  public void store(TwoFactorServiceProvider twoFactorServiceProvider) {
    if (twoFactorServiceProvider == null) {
      throw new NullPointerException("twoFactorServiceProvider is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorServiceProvider);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorServiceProvider retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    List<TwoFactorServiceProvider> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfsp from TwoFactorServiceProvider as tfsp where tfsp.uuid = :theUuid")
        .setString("theUuid", uuid).list(TwoFactorServiceProvider.class);
    TwoFactorServiceProvider twoFactorServiceProvider = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorServiceProvider;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorServiceProviderDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorServiceProvider> retrieveDeletedOlderThanAge(
      long selectBeforeThisMilli) {
    List<TwoFactorServiceProvider> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfsp from TwoFactorServiceProvider as tfsp where tfsp.deletedOn is not null and tfsp.deletedOn < :selectBeforeThisMilli " +
          "and not exists (select tfa from TwoFactorAudit tfa where tfa.serviceProviderUuid = tfsp.uuid )")
        .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
        .options(new TfQueryOptions().paging(1000, 1,false))
        .list(TwoFactorServiceProvider.class);
    return theList;
  }

}
