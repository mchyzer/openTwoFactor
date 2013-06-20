/**
 * @author mchyzer
 * $Id: HibernateTwoFactorBrowserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.dao.TwoFactorBrowserDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorBrowserDao implements TwoFactorBrowserDao {

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#delete(org.openTwoFactor.server.beans.TwoFactorBrowser)
   */
  @Override
  public void delete(final TwoFactorBrowser twoFactorBrowser) {
    if (twoFactorBrowser == null) {
      throw new NullPointerException("twoFactorBrowser is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorBrowser);

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveByBrowserTrustedUuid(String)
   */
  @Override
  public TwoFactorBrowser retrieveByBrowserTrustedUuid(String browserTrustedUuidEncrypted) {

    if (TwoFactorServerUtils.isBlank(browserTrustedUuidEncrypted)) {
      throw new RuntimeException("Why is browserTrustedUuidEncrypted blank? ");
    }
    
    List<TwoFactorBrowser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfb from TwoFactorBrowser tfb where tfb.browserTrustedUuid = :theBrowserTrustedUuid")
        .setString("theBrowserTrustedUuid", browserTrustedUuidEncrypted).list(TwoFactorBrowser.class);
    TwoFactorBrowser twoFactorBrowser = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorBrowser;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#store(org.openTwoFactor.server.beans.TwoFactorBrowser)
   */
  @Override
  public void store(TwoFactorBrowser twoFactorBrowser) {
    if (twoFactorBrowser == null) {
      throw new NullPointerException("twoFactorBrowser is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorBrowser);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveByUserUuid(java.lang.String)
   */
  @Override
  public List<TwoFactorBrowser> retrieveByUserUuid(String userUuid) {
    if (TwoFactorServerUtils.isBlank(userUuid)) {
      throw new RuntimeException("Why is userUuid blank? ");
    }
    
    List<TwoFactorBrowser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfb from TwoFactorBrowser tfb where tfb.userUuid = :theUserUuid")
        .setString("theUserUuid", userUuid).list(TwoFactorBrowser.class);
    return theList;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorBrowser retrieveByUuid(String browserUuid) {

    if (TwoFactorServerUtils.isBlank(browserUuid)) {
      throw new RuntimeException("Why is browserUuid blank? ");
    }
    
    List<TwoFactorBrowser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfb from TwoFactorBrowser tfb where tfb.uuid = :theUuid")
        .setString("theUuid", browserUuid).list(TwoFactorBrowser.class);
    TwoFactorBrowser twoFactorBrowser = TwoFactorServerUtils.listPopOne(theList);
    return twoFactorBrowser;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveTrustedByUserUuid(java.lang.String)
   */
  @Override
  public List<TwoFactorBrowser> retrieveTrustedByUserUuid(String userUuid) {
    if (TwoFactorServerUtils.isBlank(userUuid)) {
      throw new RuntimeException("Why is userUuid blank? ");
    }
    
    List<TwoFactorBrowser> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfb from TwoFactorBrowser tfb where tfb.userUuid = :theUserUuid and tfb.trustedBrowser = true")
        .setString("theUserUuid", userUuid).list(TwoFactorBrowser.class);
    return theList;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorBrowserDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorBrowser> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorBrowser> theList = HibernateSession.byHqlStatic().createQuery(
      "select tfb from TwoFactorBrowser as tfb where tfb.deletedOn is not null and tfb.deletedOn < :selectBeforeThisMilli " +
        "and not exists (select tfa from TwoFactorAudit tfa where tfa.browserUuid = tfb.uuid )")
      .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
      .options(new TfQueryOptions().paging(1000, 1,false))
      .list(TwoFactorBrowser.class);
    return theList;
    
  }

}
