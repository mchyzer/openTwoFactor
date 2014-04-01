/**
 * @author mchyzer
 * $Id: HibernateDaoFactory.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.openTwoFactor.server.dao.TwoFactorAuditDao;
import org.openTwoFactor.server.dao.TwoFactorBrowserDao;
import org.openTwoFactor.server.dao.TwoFactorDaemonLogDao;
import org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao;
import org.openTwoFactor.server.dao.TwoFactorIpAddressDao;
import org.openTwoFactor.server.dao.TwoFactorServiceProviderDao;
import org.openTwoFactor.server.dao.TwoFactorUserAgentDao;
import org.openTwoFactor.server.dao.TwoFactorUserAttrDao;
import org.openTwoFactor.server.dao.TwoFactorUserDao;
import org.openTwoFactor.server.dao.TwoFactorUserViewDao;
import org.openTwoFactor.server.hibernate.TwoFactorDao;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;



/**
 *
 */
public class HibernateDaoFactory extends TwoFactorDaoFactory {

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getConfiguration()
   */
  @Override
  public Configuration getConfiguration() {
    return TwoFactorDao.getConfiguration();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getSession()
   */
  @Override
  public Session getSession() {
    return TwoFactorDao.session();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTransaction()
   */
  @Override
  public TransactionDAO getTransaction() {
    return new Hib3TransactionDAO();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorUser()
   */
  @Override
  public TwoFactorUserDao getTwoFactorUser() {
    return new HibernateTwoFactorUserDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorIpAddress()
   */
  @Override
  public TwoFactorIpAddressDao getTwoFactorIpAddress() {
    return new HibernateTwoFactorIpAddressDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorAudit()
   */
  @Override
  public TwoFactorAuditDao getTwoFactorAudit() {
    return new HibernateTwoFactorAuditDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorUserAttr()
   */
  @Override
  public TwoFactorUserAttrDao getTwoFactorUserAttr() {
    return new HibernateTwoFactorUserAttrDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorBrowser()
   */
  @Override
  public TwoFactorBrowserDao getTwoFactorBrowser() {
    return new HibernateTwoFactorBrowserDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorServiceProvider()
   */
  @Override
  public TwoFactorServiceProviderDao getTwoFactorServiceProvider() {
    return new HibernateTwoFactorServiceProviderDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorUserAgent()
   */
  @Override
  public TwoFactorUserAgentDao getTwoFactorUserAgent() {
    return new HibernateTwoFactorUserAgentDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorDaemonLog()
   */
  @Override
  public TwoFactorDaemonLogDao getTwoFactorDaemonLog() {
    return new HibernateTwoFactorDaemonLogDao();
  }

  /**
   * @see TwoFactorDaoFactory#getTwoFactorUserView()
   */
  @Override
  public TwoFactorUserViewDao getTwoFactorUserView() {
    return new HibernateTwoFactorUserViewDao();
  }

  /**
   * @see TwoFactorDaoFactory#getTwoFactorDeviceSerial()
   */
  @Override
  public TwoFactorDeviceSerialDao getTwoFactorDeviceSerial() {
    return new HibernateTwoFactorDeviceSerialDao();
  }

}
