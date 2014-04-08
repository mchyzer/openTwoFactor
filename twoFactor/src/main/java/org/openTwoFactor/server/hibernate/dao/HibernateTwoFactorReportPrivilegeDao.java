/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorReportPrivilege;
import org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorReportPrivilegeDao implements TwoFactorReportPrivilegeDao {

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorReportPrivilege retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorReportPrivilege twoFactorReportPrivilege = HibernateSession.byHqlStatic().createQuery(
        "select tfrp from TwoFactorReportPrivilege as tfrp where tfrp.uuid = :theUuid")
        .setString("theUuid", uuid).uniqueResult(TwoFactorReportPrivilege.class);
    return twoFactorReportPrivilege;
  }

  /**
   * @see TwoFactorReportPrivilegeDao#store(TwoFactorReportPrivilege)
   */
  @Override
  public void store(TwoFactorReportPrivilege twoFactorReportPrivilege) {
    if (twoFactorReportPrivilege == null) {
      throw new NullPointerException("twoFactorReportPrivilege is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorReportPrivilege);
    
  }

  /**
   * @see TwoFactorReportPrivilegeDao#delete(TwoFactorReportPrivilege)
   */
  @Override
  public void delete(TwoFactorReportPrivilege twoFactorReportPrivilege) {
    if (twoFactorReportPrivilege == null) {
      throw new NullPointerException("twoFactorReportPrivilege is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorReportPrivilege);
    
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#retrieveAll()
   */
  public List<TwoFactorReportPrivilege> retrieveAll() {
    List<TwoFactorReportPrivilege> twoFactorReportPrivileges = HibernateSession.byHqlStatic().createQuery(
        "select tfrp from TwoFactorReportPrivilege as tfrp")
        .list(TwoFactorReportPrivilege.class);
    return twoFactorReportPrivileges;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#retrieveByUserUuid(java.lang.String)
   */
  public List<TwoFactorReportPrivilege> retrieveByUserUuid(String userUuid) {
    List<TwoFactorReportPrivilege> twoFactorReportPrivileges = HibernateSession.byHqlStatic().createQuery(
        "select tfrp from TwoFactorReportPrivilege as tfrp where tfrp.userUuid = :theUserUuid ")
        .setString("theUserUuid", userUuid)
        .list(TwoFactorReportPrivilege.class);
    return twoFactorReportPrivileges;
  }

}
