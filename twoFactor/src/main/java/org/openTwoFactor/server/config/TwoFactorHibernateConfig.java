/**
 * @author mchyzer
 * $Id: TwoFactorHibernateConfig.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.config;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;


/**
 *
 */
public class TwoFactorHibernateConfig extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private TwoFactorHibernateConfig() {
    
  }
  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static TwoFactorHibernateConfig retrieveConfig() {
    return retrieveConfig(TwoFactorHibernateConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "twoFactorHibernate.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "twoFactor.hibernate.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "twoFactor.hibernate.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "twoFactorHibernate.config.secondsBetweenUpdateChecks";
  }

}
