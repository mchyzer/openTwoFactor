/**
 * @author mchyzer
 * $Id: HibernateTwoFactorIpAddressDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorIpAddress;
import org.openTwoFactor.server.dao.TwoFactorIpAddressDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorIpAddressDao implements TwoFactorIpAddressDao {

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#delete(org.openTwoFactor.server.beans.TwoFactorIpAddress)
   */
  @Override
  public void delete(final TwoFactorIpAddress twoFactorIpAddress) {
    if (twoFactorIpAddress == null) {
      throw new NullPointerException("twoFactorIpAddress is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorIpAddress);

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#retrieveByIpAddress(java.lang.String)
   */
  @Override
  public TwoFactorIpAddress retrieveByIpAddress(String ipAddress) {
    if (TwoFactorServerUtils.isBlank(ipAddress)) {
      throw new RuntimeException("Why is ipAddress blank? ");
    }
    
    List<TwoFactorIpAddress> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfia from TwoFactorIpAddress as tfia where tfia.ipAddress = :theIpAddress")
        .setString("theIpAddress", ipAddress).list(TwoFactorIpAddress.class);
    TwoFactorIpAddress twoFactorIpAddress = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorIpAddress;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#store(org.openTwoFactor.server.beans.TwoFactorIpAddress)
   */
  @Override
  public void store(TwoFactorIpAddress twoFactorIpAddress) {
    if (twoFactorIpAddress == null) {
      throw new NullPointerException("twoFactorIpAddress is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorIpAddress);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorIpAddress retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    List<TwoFactorIpAddress> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfia from TwoFactorIpAddress as tfia where tfia.uuid = :theUuid")
        .setString("theUuid", uuid).list(TwoFactorIpAddress.class);
    TwoFactorIpAddress twoFactorIpAddress = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorIpAddress;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorIpAddressDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorIpAddress> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {

    List<TwoFactorIpAddress> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfia from TwoFactorIpAddress as tfia where tfia.deletedOn is not null and tfia.deletedOn < :selectBeforeThisMilli " +
          "and not exists (select tfa from TwoFactorAudit tfa where tfa.ipAddressUuid = tfia.uuid )")
        .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
        .options(new TfQueryOptions().paging(1000, 1,false))
        .list(TwoFactorIpAddress.class);
      return theList;
    
  }

}
