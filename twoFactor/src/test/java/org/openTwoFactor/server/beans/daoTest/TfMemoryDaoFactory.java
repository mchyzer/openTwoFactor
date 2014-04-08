/**
 * @author mchyzer
 * $Id: TfMemoryDaoFactory.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import org.openTwoFactor.server.dao.TwoFactorAuditDao;
import org.openTwoFactor.server.dao.TwoFactorBrowserDao;
import org.openTwoFactor.server.dao.TwoFactorDaemonLogDao;
import org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao;
import org.openTwoFactor.server.dao.TwoFactorIpAddressDao;
import org.openTwoFactor.server.dao.TwoFactorReportDao;
import org.openTwoFactor.server.dao.TwoFactorReportDataDao;
import org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao;
import org.openTwoFactor.server.dao.TwoFactorReportRollupDao;
import org.openTwoFactor.server.dao.TwoFactorServiceProviderDao;
import org.openTwoFactor.server.dao.TwoFactorUserAgentDao;
import org.openTwoFactor.server.dao.TwoFactorUserAttrDao;
import org.openTwoFactor.server.dao.TwoFactorUserDao;
import org.openTwoFactor.server.dao.TwoFactorUserViewDao;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;


/**
 * dao factor for two factor in memory
 */
public class TfMemoryDaoFactory extends TwoFactorDaoFactory {

  /**
   * factory
   */
  private static TfMemoryDaoFactory factory = new TfMemoryDaoFactory();

  
  /**
   * get the factory
   * @return the factory
   */
  public static TfMemoryDaoFactory getFactory() {
    return factory;
  }
  
  /**
   * clear
   */
  public static void clear() {
    TfMemoryAuditDao.audits.clear();
    TfMemoryDaemonLogDao.daemonLogs.clear();
    TfMemoryBrowserDao.browsers.clear();
    TfMemoryIpAddressDao.ipAddresses.clear();
    TfMemoryServiceProviderDao.serviceProviders.clear();
    TfMemoryUserAgentDao.userAgents.clear();
    TfMemoryUserAttrDao.userAttrs.clear();
    TfMemoryUserDao.users.clear();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorAudit()
   */
  @Override
  public TwoFactorAuditDao getTwoFactorAudit() {
    return new TfMemoryAuditDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorDaemonLog()
   */
  @Override
  public TwoFactorDaemonLogDao getTwoFactorDaemonLog() {
    return new TfMemoryDaemonLogDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorBrowser()
   */
  @Override
  public TwoFactorBrowserDao getTwoFactorBrowser() {
    return new TfMemoryBrowserDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorIpAddress()
   */
  @Override
  public TwoFactorIpAddressDao getTwoFactorIpAddress() {
    return new TfMemoryIpAddressDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorServiceProvider()
   */
  @Override
  public TwoFactorServiceProviderDao getTwoFactorServiceProvider() {
    return new TfMemoryServiceProviderDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorUser()
   */
  @Override
  public TwoFactorUserDao getTwoFactorUser() {
    return new TfMemoryUserDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorUserAgent()
   */
  @Override
  public TwoFactorUserAgentDao getTwoFactorUserAgent() {
    return new TfMemoryUserAgentDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorUserAttr()
   */
  @Override
  public TwoFactorUserAttrDao getTwoFactorUserAttr() {
    return new TfMemoryUserAttrDao();
  }

  /**
   * @see TwoFactorDaoFactory#getTwoFactorUserView()
   */
  @Override
  public TwoFactorUserViewDao getTwoFactorUserView() {
    return new TfMemoryUserViewDao();
  }

  /**
   * @see TwoFactorDaoFactory#getTwoFactorDeviceSerial()
   */
  @Override
  public TwoFactorDeviceSerialDao getTwoFactorDeviceSerial() {
    return new TfMemoryDeviceSerialDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorReport()
   */
  @Override
  public TwoFactorReportDao getTwoFactorReport() {
    return new TfMemoryReportDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorReportPrivilege()
   */
  @Override
  public TwoFactorReportPrivilegeDao getTwoFactorReportPrivilege() {
    return new TfMemoryReportPrivilegeDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorReportRollup()
   */
  @Override
  public TwoFactorReportRollupDao getTwoFactorReportRollup() {
    return new TfMemoryReportRollupDao();
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorDaoFactory#getTwoFactorReportData()
   */
  @Override
  public TwoFactorReportDataDao getTwoFactorReportData() {
    //need to do something for this at some point
    return null;
  }

}
