/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserAgentDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.dao.TwoFactorUserAgentDao;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorUserAgentDao implements TwoFactorUserAgentDao {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(HibernateTwoFactorUserAgentDao.class);

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
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#store(org.openTwoFactor.server.beans.TwoFactorUserAgent, boolean)
   */
  @Override
  public boolean store(final TwoFactorUserAgent twoFactorUserAgent, final boolean exceptionOnError) {
    
    if (twoFactorUserAgent == null) {
      throw new NullPointerException("twoFactorUserAgent is null");
    }

    //do this in a new transaction
    boolean success = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        try {
          hibernateHandlerBean.getHibernateSession().byObject().saveOrUpdate(twoFactorUserAgent);
        } catch (RuntimeException re) {
          if (exceptionOnError) {
            throw re;
          }
          if (LOG.isDebugEnabled()) {
            LOG.debug("error with user agent: " + twoFactorUserAgent.getUserAgent(), re);
          }
          return false;
        }
        return true;
      }
    });
    
    return success;
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

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAgentDao#retrieveAll()
   */
  public List<TwoFactorUserAgent> retrieveAll() {
    List<TwoFactorUserAgent> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfua from TwoFactorUserAgent as tfua where tfua.deletedOn is null")
        .list(TwoFactorUserAgent.class);
    return theList;
  }

}
