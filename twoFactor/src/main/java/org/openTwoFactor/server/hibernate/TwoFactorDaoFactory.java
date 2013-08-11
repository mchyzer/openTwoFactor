

package org.openTwoFactor.server.hibernate;
import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.openTwoFactor.server.dao.TwoFactorAuditDao;
import org.openTwoFactor.server.dao.TwoFactorBrowserDao;
import org.openTwoFactor.server.dao.TwoFactorDaemonLogDao;
import org.openTwoFactor.server.dao.TwoFactorIpAddressDao;
import org.openTwoFactor.server.dao.TwoFactorServiceProviderDao;
import org.openTwoFactor.server.dao.TwoFactorUserAgentDao;
import org.openTwoFactor.server.dao.TwoFactorUserAttrDao;
import org.openTwoFactor.server.dao.TwoFactorUserDao;
import org.openTwoFactor.server.dao.TwoFactorUserViewDao;
import org.openTwoFactor.server.hibernate.dao.HibernateDaoFactory;
import org.openTwoFactor.server.hibernate.dao.TransactionDAO;



/** 
 * Factory for returning <code>DAO</code> objects.
 * <p/>
 * @version $Id: TwoFactorDaoFactory.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
public abstract class TwoFactorDaoFactory {

  /**
   * 
   */
  private static TwoFactorDaoFactory gdf;


  /**
   * Return singleton {@link TwoFactorDaoFactory} implementation.
   * <p/>
   * @return factory
   * @since   1.2.0
   */
  public static TwoFactorDaoFactory getFactory() {
    if (gdf == null) {
      gdf = getFactoryHelper( );
    }
    return gdf;
  } 

  /**
   * Return singleton {@link TwoFactorDaoFactory} implementation using the specified
   * configuration.
   * <p/>
   * @return factory
   * @throws  IllegalArgumentException if <i>cfg</i> is null.
   */
  private static TwoFactorDaoFactory getFactoryHelper() 
    throws  IllegalArgumentException
  {
    return new HibernateDaoFactory();
  }

  /**
   * @return user dao
   */
  public abstract TwoFactorUserDao getTwoFactorUser();

  /**
   * @return browser dao
   */
  public abstract TwoFactorBrowserDao getTwoFactorBrowser();

  /**
   * @return user agent dao
   */
  public abstract TwoFactorUserAgentDao getTwoFactorUserAgent();

  /**
   * @return audit dao
   */
  public abstract TwoFactorAuditDao getTwoFactorAudit();

  /**
   * @return ip address
   */
  public abstract TwoFactorIpAddressDao getTwoFactorIpAddress();

  /**
   * @return the daemon log
   */
  public abstract TwoFactorDaemonLogDao getTwoFactorDaemonLog();
  
  /**
   * @return service provider
   */
  public abstract TwoFactorServiceProviderDao getTwoFactorServiceProvider();

  /**
   * 
   */
  public static void internal_resetFactory() {
    gdf = null;
  }

  /**
   * get a hibernate session (note, this is a framework method
   * that should not be called outside of hibernate framework methods
   * @return the session
   */
  public Session getSession() {
    return null;
  }
  
  /**
   * get a hibernate configuration (this is internal for dev team only)
   * @return the configuration
   */
  public Configuration getConfiguration() {
    return null;
  }

  /**
   * return the transaction implementation
   * @return the transaction implementation
   */
  public TransactionDAO getTransaction() {
    return null;
  }

  /**
   * @return user attr dao
   */
  public abstract TwoFactorUserAttrDao getTwoFactorUserAttr();

  /**
   * @return user view dao
   */
  public abstract TwoFactorUserViewDao getTwoFactorUserView();
  
} 

