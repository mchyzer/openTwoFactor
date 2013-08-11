/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorUserView;
import org.openTwoFactor.server.dao.TwoFactorUserViewDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorUserViewDao implements TwoFactorUserViewDao {

  /**
   * test the dao
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveByLoginid("10021368").getEmail0());
    System.out.println(TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveByLoginid("10021368").getEmail0());
  }
  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserViewDao#retrieveByLoginid(java.lang.String)
   */
  @Override
  public TwoFactorUserView retrieveByLoginid(final String loginid) {

    if (TwoFactorServerUtils.isBlank(loginid)) {
      throw new RuntimeException("Why is loginid blank? ");
    }
    
    List<TwoFactorUserView> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfuv from TwoFactorUserView as tfuv where tfuv.loginid = :theLoginId")
        .setString("theLoginId", loginid).list(TwoFactorUserView.class);
    TwoFactorUserView twoFactorUserView = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorUserView;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserViewDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorUserView retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    List<TwoFactorUserView> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfuv from TwoFactorUserView as tfuv where tfuv.uuid = :theUuid")
        .setString("theUuid", uuid).list(TwoFactorUserView.class);
    TwoFactorUserView twoFactorUserView = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorUserView;
  }

  /**
   * @see TwoFactorUserViewDao#retrieveAllOptedInUsers()
   */
  @Override
  public List<TwoFactorUserView> retrieveAllOptedInUsers() {

    List<TwoFactorUserView> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfuv from TwoFactorUserView as tfuv where tfuv.optedIn = 'T'")
        .list(TwoFactorUserView.class);
    
    return theList;
    
  }


}
