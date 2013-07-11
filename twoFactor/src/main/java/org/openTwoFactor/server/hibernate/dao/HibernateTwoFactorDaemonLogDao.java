/**
 * @author mchyzer
 * $Id: HibernateTwoFactorDaemonLogDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorDaemonLog;
import org.openTwoFactor.server.dao.TwoFactorDaemonLogDao;
import org.openTwoFactor.server.hibernate.ByHqlStatic;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * implementation of daemon log dao
 */
public class HibernateTwoFactorDaemonLogDao implements TwoFactorDaemonLogDao {


  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#delete(org.openTwoFactor.server.beans.TwoFactorDaemonLog)
   */
  @Override
  public void delete(TwoFactorDaemonLog twoFactorDaemonLog) {
    HibernateSession.byObjectStatic().delete(twoFactorDaemonLog);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#store(org.openTwoFactor.server.beans.TwoFactorDaemonLog)
   */
  @Override
  public void store(TwoFactorDaemonLog twoFactorDaemonLog) {
    if (twoFactorDaemonLog == null) {
      throw new RuntimeException("Why is daemon null?");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorDaemonLog);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorDaemonLog retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }

    List<TwoFactorDaemonLog> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfdl from TwoFactorDaemonLog as tfdl where tfdl.uuid = :theUuid")
        .setString("theUuid", uuid)
        .list(TwoFactorDaemonLog.class);

    TwoFactorDaemonLog twoFactorDaemonLog = TwoFactorServerUtils.listPopOne(theList);
    
    return twoFactorDaemonLog;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorDaemonLog> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorDaemonLog> theList = HibernateSession.byHqlStatic().createQuery(
      "select tfdl from TwoFactorDaemonLog as tfdl where tfdl.deletedOn is not null and tfdl.deletedOn < :selectBeforeThisMilli")
      .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
      .options(new TfQueryOptions().paging(1000, 1,false))
      .list(TwoFactorDaemonLog.class);
    return theList;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#retrieveOlderThanAge(long)
   */
  @Override
  public List<TwoFactorDaemonLog> retrieveOlderThanAge(long selectBeforeThisMilli) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    List<TwoFactorDaemonLog> theList = byHqlStatic.createQuery(
      "select tfdl from TwoFactorDaemonLog as tfdl where tfdl.deletedOn is null and tfa.theTimestamp < :selectBeforeThisMilli")
      .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
      .options(new TfQueryOptions().paging(1000, 1,false))
      .list(TwoFactorDaemonLog.class);
    return theList;
  }

  /**
   * @see TwoFactorDaemonLogDao#retrieveMostRecentSuccessTimestamp(String)
   */
  @Override
  public Long retrieveMostRecentSuccessTimestamp(String daemonName) {
    Long result = HibernateSession.byHqlStatic().createQuery(
        "select max(tfdl.theTimestamp) from TwoFactorDaemonLog as tfdl where " +
        " tfdl.status = 'success' and tfdl.daemonName = :theDaemonName ")
        .setString("theDaemonName", daemonName)
        .uniqueResult(Long.class);

    return result;
    
  }

}
