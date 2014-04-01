
package org.openTwoFactor.server.hibernate;
import java.io.File;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditView;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorDaemonLog;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.beans.TwoFactorIpAddress;
import org.openTwoFactor.server.beans.TwoFactorServiceProvider;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.beans.TwoFactorUserView;
import org.openTwoFactor.server.config.TwoFactorHibernateConfig;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;


/**
 * Base Hibernate DAO interface.
 * @version $Id: TwoFactorDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
public abstract class TwoFactorDao {

  /**
   * 
   */
  private static Configuration  CFG;

  /**
   * 
   */
  private static SessionFactory FACTORY;

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorDao.class);

  /**
   * keep track of if hibernate is initted yet, allow resets... (e.g. for testing)
   */
  public static boolean hibernateInitted = false;
  
  /**
   * init hibernate if not initted
   */
  public static void initHibernateIfNotInitted() {
    if (hibernateInitted) {
      return;
    }
    
    synchronized(TwoFactorDao.class) {
      if (hibernateInitted) {
        return;
      }

      try {
        // Find the custom configuration file
        Properties  p   = TwoFactorHibernateConfig.retrieveConfig().properties();
        
        //unencrypt pass
        if (p.containsKey("hibernate.connection.password")) {
          String newPass = Morph.decryptIfFile(p.getProperty("hibernate.connection.password"));
          p.setProperty("hibernate.connection.password", newPass);
        }
        
        String connectionUrl = StringUtils.defaultString(TwoFactorServerUtils.propertiesValue(p,"hibernate.connection.url"));

        {
          String dialect = StringUtils.defaultString(TwoFactorServerUtils.propertiesValue(p,"hibernate.dialect"));
          dialect = TwoFactorServerUtils.convertUrlToHibernateDialectIfNeeded(connectionUrl, dialect);
          p.setProperty("hibernate.dialect", dialect);
        }
        
        {
          String driver = StringUtils.defaultString(TwoFactorServerUtils.propertiesValue(p,"hibernate.connection.driver_class"));
          driver = TwoFactorServerUtils.convertUrlToDriverClassIfNeeded(connectionUrl, driver);
          p.setProperty("hibernate.connection.driver_class", driver);
        }      
        
        // And now load all configuration information
        CFG = new Configuration().addProperties(p);
        addClass(CFG, TwoFactorAudit.class);
        addClass(CFG, TwoFactorAuditView.class);
        addClass(CFG, TwoFactorBrowser.class);
        addClass(CFG, TwoFactorDaemonLog.class);
        addClass(CFG, TwoFactorIpAddress.class);
        addClass(CFG, TwoFactorServiceProvider.class);
        addClass(CFG, TwoFactorDeviceSerial.class);
        addClass(CFG, TwoFactorUser.class);
        addClass(CFG, TwoFactorUserAgent.class);
        addClass(CFG, TwoFactorUserAttr.class);
        addClass(CFG, TwoFactorUserView.class);
        CFG.setInterceptor(new TfSessionInterceptor());
     
        // And finally create our session factory
        //trying to avoid warning of using the same dir
        String tmpDir = TwoFactorServerUtils.tmpDir();
        try {
          String newTmpdir = StringUtils.trimToEmpty(tmpDir);
          if (!newTmpdir.endsWith("\\") && !newTmpdir.endsWith("/")) {
            newTmpdir += File.separator;
          }
          newTmpdir += "twoFactor_ehcache_auto_" + TwoFactorServerUtils.uniqueId();
          System.setProperty(TwoFactorServerUtils.JAVA_IO_TMPDIR, newTmpdir);
          
          //now it should be using a unique directory
          FACTORY = CFG.buildSessionFactory();
        } finally {
          
          //put tmpdir back
          if (tmpDir == null) {
            System.clearProperty(TwoFactorServerUtils.JAVA_IO_TMPDIR);
          } else {
            System.setProperty(TwoFactorServerUtils.JAVA_IO_TMPDIR, tmpDir);
          }
        }

      } catch (Throwable t) {
        String msg = "unable to initialize hibernate: " + t.getMessage();
        LOG.fatal(msg, t);
        throw new RuntimeException(msg, t);
      }
      //this might not be completely accurate
      hibernateInitted = true;

    
    }
    

  }
  
  /**
   * 
   * @param _CFG
   * @param mappedClass
   */
  private static void addClass(Configuration _CFG, Class<?> mappedClass) {
    addClass(_CFG, mappedClass, null);
  }

  /**
   * 
   * @param _CFG
   * @param mappedClass
   * @param entityNameXmlFileNameOverride send in an entity name if the entity name and xml file are different than
   * the class file.
   */
  private static void addClass(Configuration _CFG, Class<?> mappedClass, String entityNameXmlFileNameOverride) {
    String resourceName = resourceNameFromClassName(mappedClass, entityNameXmlFileNameOverride);
    String xml = TwoFactorServerUtils.readResourceIntoString(resourceName, false);
    
    if (xml.contains("<version")) {
      
      //if versioned, then make sure the setting in class is there
      String optimisiticLockVersion = "optimistic-lock=\"version\"";
      
      if (!StringUtils.contains(xml, optimisiticLockVersion)) {
        throw new RuntimeException("If there is a versioned class, it must contain " +
        		"the class level attribute: optimistic-lock=\"version\": " + mappedClass.getName() + ", " + resourceName);
      }
      
      //if versioned, then see if we are disabling
      boolean optimisiticLocking = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean(
          "dao.optimisticLocking", true);
      
      if (!optimisiticLocking) {
        xml = StringUtils.replace(xml, optimisiticLockVersion, "optimistic-lock=\"none\"");
      }
    }
    _CFG.addXML(xml);

  }

  /**
   * class is e.g. edu.school.dto.Attribute,
   * must return e.g. edu.school.internal.dao.hib3.Hib3AttributeDAO
   * @param theClass
   * @param entityNameXmlFileNameOverride pass in an override if the entity name and xml file are different than
   * the class file
   * @return the string of resource
   */
  public static String resourceNameFromClassName(Class theClass, String entityNameXmlFileNameOverride) {
    String daoClass = theClass.getName();
    if (!StringUtils.isBlank(entityNameXmlFileNameOverride)) {
      daoClass = StringUtils.replace(daoClass, theClass.getSimpleName(), entityNameXmlFileNameOverride);
    }
    //replace with hbm
    String result = StringUtils.replace(daoClass, ".", "/") + ".hbm.xml";
    
    return result;
  }
  
  /**
   * @return the configuration
   * @throws HibernateException
   */
  public static Configuration getConfiguration()
    throws  HibernateException {
    return CFG;
  }

  /**
   * DONT CALL THIS METHOD, IT IS FOR INTERNAL FRAMEWORK USE
   * ONLY.  Use the HibernateSession callback to get a hibernate Session
   * object
   * @return the session
   * @throws HibernateException
   */
	public static Session session()
    throws  HibernateException {
	  //just in case
	  initHibernateIfNotInitted();
		return FACTORY.openSession();
	} 

	/**
	 * evict a persistent class
	 * @param persistentClass
	 */
	public static void evict(Class persistentClass) {
	  FACTORY.getCache().evictEntityRegion(persistentClass);
	}
	
  /**
   * evict a persistent class
   * @param entityName
   */
  public static void evictEntity(String entityName) {
    FACTORY.getCache().evictEntityRegion(entityName);
  }
  
  /**
   * evict a persistent class
   * @param cacheRegion
   */
  public static void evictQueries(String cacheRegion) {
    FACTORY.getCache().evictQueryRegion(cacheRegion);
  }
  
} 

