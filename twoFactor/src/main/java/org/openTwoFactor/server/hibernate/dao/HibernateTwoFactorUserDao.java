/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.dao.TwoFactorUserDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorUserDao implements TwoFactorUserDao {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveCountOfOptedInUsers());
    System.out.println(TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveCountOfOptedOutUsers());
  }
  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#delete(org.openTwoFactor.server.beans.TwoFactorUser)
   */
  @Override
  public void delete(final TwoFactorUser twoFactorUser) {
    if (twoFactorUser == null) {
      throw new NullPointerException("twoFactorUser is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorUser);

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveByLoginid(java.lang.String)
   */
  @Override
  public TwoFactorUser retrieveByLoginid(final String loginid) {

    if (TwoFactorServerUtils.isBlank(loginid)) {
      throw new RuntimeException("Why is loginid blank? ");
    }
    
    List<TwoFactorUser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfu from TwoFactorUser as tfu where tfu.loginid = :theLoginId")
        .setString("theLoginId", loginid).list(TwoFactorUser.class);
    TwoFactorUser twoFactorUser = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorUser;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#store(TwoFactorUser)
   */
  @Override
  public void store(final TwoFactorUser twoFactorUser) {
    if (twoFactorUser == null) {
      throw new NullPointerException("twoFactorUser is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorUser);

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorUser retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    List<TwoFactorUser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfu from TwoFactorUser as tfu where tfu.uuid = :theUuid")
        .setString("theUuid", uuid).list(TwoFactorUser.class);
    TwoFactorUser twoFactorUser = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorUser;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorUser> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorUser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfu from TwoFactorUser as tfu where tfu.deletedOn is not null and tfu.deletedOn < :selectBeforeThisMilli " +
          "and not exists (select tfa from TwoFactorAudit tfa where tfa.userUuid = tfu.uuid ) " +
          "and not exists (select tfua from TwoFactorUserAttr tfua where tfua.attributeValueString = tfu.uuid ) ")
        .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
        .options(new TfQueryOptions().paging(1000, 1,false))
        .list(TwoFactorUser.class);
    return theList;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveUsersWhoPickedThisUserToOptThemOut(java.lang.String)
   */
  @Override
  public List<TwoFactorUser> retrieveUsersWhoPickedThisUserToOptThemOut(String uuid) {
    List<TwoFactorUser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfu from TwoFactorUser as tfu where exists " +
          " (select tfua from TwoFactorUserAttr as tfua where tfua.userUuid = tfu.uuid " +
        		" and tfua.attributeValueString = :theUuid " +
        		" and tfua.attributeName in ('colleague_user_uuid0', 'colleague_user_uuid1'," +
        		" 'colleague_user_uuid2', 'colleague_user_uuid3', 'colleague_user_uuid4')  ) order by tfu.loginid ")
        .setString("theUuid", uuid)
        .list(TwoFactorUser.class);
    return theList;
  }

  /**
   * @see TwoFactorUserDao#retrieveCountOfOptedInUsers()
   */
  @Override
  public int retrieveCountOfOptedInUsers() {
//    int count = HibernateSession.byHqlStatic().createQuery(
//        "select count(tfu.uuid) from TwoFactorUser as tfu where exists " +
//          " (select tfua from TwoFactorUserAttr as tfua where tfua.userUuid = tfu.uuid " +
//            " and tfua.attributeValueString = 'T' " +
//            " and tfua.attributeName = 'opted_in'  ) ")
//        .setCacheable(true)
//        .setCacheRegion(HibernateTwoFactorUserDao.class.getName() + ".retrieveCountOfOptedInUsers")
//        .uniqueResult(long.class).intValue();
    return -1;
  }

  /**
   * @see TwoFactorUserDao#retrieveCountOfOptedOutUsers()
   */
  @Override
  public int retrieveCountOfOptedOutUsers() {
    int count = HibernateSession.byHqlStatic().createQuery(
        "select count(tfu.uuid) from TwoFactorUser as tfu where (not exists  " +
          " (select tfua from TwoFactorUserAttr as tfua where tfua.userUuid = tfu.uuid " +
            " and tfua.attributeValueString = 'T' " +
            " and tfua.attributeName = 'opted_in'  ) ) and " +
            " exists ( select tfa from TwoFactorAudit tfa where tfa.userUuid = tfu.uuid" +
            " and tfa.action = 'OPTIN_TWO_FACTOR' ) ")
        .setCacheable(true)
        .setCacheRegion(HibernateTwoFactorUserDao.class.getName() + ".retrieveCountOfOptedOutUsers")
        .uniqueResult(long.class).intValue();
    return count;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#retrieveUsersWhoHavePrivilegesInReport()
   */
  public List<TwoFactorUser> retrieveUsersWhoHavePrivilegesInReport() {
    List<TwoFactorUser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfu from TwoFactorUser as tfu where " +
          " exists (select tfrp from TwoFactorReportPrivilege tfrp where tfrp.userUuid = tfu.uuid ) ")
        .list(TwoFactorUser.class);
    return theList;
  }

}
