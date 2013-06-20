/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserAgentDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.dao.TwoFactorUserAgentDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorUserAgentDao implements TwoFactorUserAgentDao {

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#delete(org.openTwoFactor.server.beans.TwoFactorUserAgent)
   */
  @Override
  public void delete(final TwoFactorUserAgent twoFactorUserAgent) {
    if (twoFactorUserAgent == null) {
      throw new NullPointerException("twoFactorUserAgent is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorUserAgent);

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#retrieveByUserAgent(java.lang.String)
   */
  @Override
  public TwoFactorUserAgent retrieveByUserAgent(String userAgent) {
    if (TwoFactorServerUtils.isBlank(userAgent)) {
      throw new RuntimeException("Why is userAgent blank? ");
    }
    
    List<TwoFactorUserAgent> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfua from TwoFactorUserAgent as tfua where tfua.userAgent = :theUserAgent")
        .setString("theUserAgent", userAgent).list(TwoFactorUserAgent.class);
    TwoFactorUserAgent twoFactorUserAgent = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorUserAgent;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#store(org.openTwoFactor.server.beans.TwoFactorUserAgent)
   */
  @Override
  public void store(TwoFactorUserAgent twoFactorUserAgent) {
    if (twoFactorUserAgent == null) {
      throw new NullPointerException("twoFactorUserAgent is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorUserAgent);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorUserAgent retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    List<TwoFactorUserAgent> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfua from TwoFactorUserAgent as tfua where tfua.uuid = :theUuid")
        .setString("theUuid", uuid).list(TwoFactorUserAgent.class);
    TwoFactorUserAgent twoFactorUserAgent = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorUserAgent;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorUserAgent> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorUserAgent> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfua from TwoFactorUserAgent as tfua where tfua.deletedOn is not null and tfua.deletedOn < :selectBeforeThisMilli " +
          "and not exists (select tfa from TwoFactorAudit tfa where tfa.userAgentUuid = tfua.uuid )")
        .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
        .options(new TfQueryOptions().paging(1000, 1,false))
        .list(TwoFactorUserAgent.class);
    return theList;
  }

}
