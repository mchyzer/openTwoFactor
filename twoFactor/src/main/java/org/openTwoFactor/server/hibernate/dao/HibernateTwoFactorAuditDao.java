/**
 * @author mchyzer
 * $Id: HibernateTwoFactorAuditDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;
import java.util.Set;

import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditView;
import org.openTwoFactor.server.dao.TwoFactorAuditDao;
import org.openTwoFactor.server.hibernate.ByHqlStatic;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfHibUtils;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * implementation of audit dao
 */
public class HibernateTwoFactorAuditDao implements TwoFactorAuditDao {

  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#delete(org.openTwoFactor.server.beans.TwoFactorAudit)
   */
  @Override
  public void delete(TwoFactorAudit twoFactorAudit) {
    HibernateSession.byObjectStatic().delete(twoFactorAudit);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveByUser(java.lang.String, org.openTwoFactor.server.hibernate.TfQueryOptions)
   */
  @Override
  public List<TwoFactorAuditView> retrieveByUser(String userUuid, TfQueryOptions tfQueryOptions) {
    if (TwoFactorServerUtils.isBlank(userUuid)) {
      throw new RuntimeException("Why is userUuid blank? ");
    }

    List<TwoFactorAuditView> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfav from TwoFactorAuditView as tfav where tfav.userUuid = :theUserUuid")
        .setString("theUserUuid", userUuid).options(tfQueryOptions)
        .list(TwoFactorAuditView.class);

    return theList;

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#store(org.openTwoFactor.server.beans.TwoFactorAudit)
   */
  @Override
  public void store(TwoFactorAudit twoFactorAudit) {
    if (twoFactorAudit == null) {
      throw new RuntimeException("Why is audit null?");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorAudit);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorAudit retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }

    List<TwoFactorAudit> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfa from TwoFactorAudit as tfa where tfa.uuid = :theUuid")
        .setString("theUuid", uuid)
        .list(TwoFactorAudit.class);

    TwoFactorAudit twoFactorAudit = TwoFactorServerUtils.listPopOne(theList);
    
    return twoFactorAudit;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorAudit> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorAudit> theList = HibernateSession.byHqlStatic().createQuery(
      "select tfa from TwoFactorAudit as tfa where tfa.deletedOn is not null and tfa.deletedOn < :selectBeforeThisMilli")
      .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
      .options(new TfQueryOptions().paging(1000, 1,false))
      .list(TwoFactorAudit.class);
    return theList;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveOlderThanAgeAndActions(java.util.Set, long)
   */
  @Override
  public List<TwoFactorAudit> retrieveOlderThanAgeAndActions(Set<String> actions,
      long selectBeforeThisMilli) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    String queryActionPart = TfHibUtils.convertToInClause(actions, byHqlStatic);
    
    List<TwoFactorAudit> theList = byHqlStatic.createQuery(
      "select tfa from TwoFactorAudit as tfa where tfa.deletedOn is null and tfa.action in (" + queryActionPart + ") and tfa.theTimestamp < :selectBeforeThisMilli")
      .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
      .options(new TfQueryOptions().paging(1000, 1,false))
      .list(TwoFactorAudit.class);
    return theList;
  }

  /**
   * @see TwoFactorAuditDao#retrieveCountOptinOptouts(String)
   */
  @Override
  public int retrieveCountOptinOptouts(String userUuid) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    int count = byHqlStatic.createQuery(
      "select count(tfa.uuid) from TwoFactorAudit as tfa where " +
      " tfa.userUuid = :theUserUuid and tfa.action in ('OPTIN_TWO_FACTOR', 'OPTOUT_TWO_FACTOR') ")
      .setString("theUserUuid", userUuid)
      .setCacheable(true)
      .setCacheRegion(HibernateTwoFactorAuditDao.class.getName() + ".retrieveCountOptinOptouts")
      .uniqueResult(long.class).intValue();
    return count;
  }

}
