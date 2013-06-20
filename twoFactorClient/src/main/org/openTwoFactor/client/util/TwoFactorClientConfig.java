package org.openTwoFactor.client.util;

import org.openTwoFactor.client.config.TfClientConfigPropertiesCascadeBase;


/**
 * hierarchical config class for twoFactor.client.properties
 * @author mchyzer
 *
 */
public class TwoFactorClientConfig extends TfClientConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private TwoFactorClientConfig() {
    
  }
  
  /**
   * @see org.openTwoFactor.client.config.TfClientConfigPropertiesCascadeBase#getClassInSiblingJar()
   */
  @Override
  protected Class<?> getClassInSiblingJar() {
    return TwoFactorClientConfig.class;
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static TwoFactorClientConfig retrieveConfig() {
    return retrieveConfig(TwoFactorClientConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see TfClientConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "twoFactorClient.config.hierarchy";
  }

  /**
   * @see TfClientConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "twoFactor.client.properties";
  }
  
  /**
   * @see TfClientConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "twoFactor.client.base.properties";
  }

  /**
   * @see TfClientConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "twoFactorClient.config.secondsBetweenUpdateChecks";
  }

  
  
}
