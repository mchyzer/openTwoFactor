/**
 * @author mchyzer
 * $Id: TwoFactorServerConfig.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.TwoFactorAuthorizationInterface;
import org.openTwoFactor.server.TwoFactorCheckPass;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.contact.TwoFactorContactInterface;
import org.openTwoFactor.server.daemon.TfReportConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;


/**
 *
 */
public class TwoFactorServerConfig extends ConfigPropertiesCascadeBase {

  /**
   * set of users to ignore rate limit
   */
  private Set<String> ignoreRateLimitOnUsers = null;

  /**
   * set of users to ignore rate limit
   * @return the set of strings or empty set, never null
   */
  public Set<String> ignoreRateLimitOnUsers() {
    
    if (this.ignoreRateLimitOnUsers == null) {

      synchronized(this) {
        
        if (this.ignoreRateLimitOnUsers == null) {
          
          String ignoreRateLimitOnUsersString = this.propertyValueString("twoFactorServer.ws.ignoreRateLimitOnUsers");
          
          Set<String> tempIgnoreRateLimitOnUsers = new HashSet<String>();
          
          if (!StringUtils.isBlank(ignoreRateLimitOnUsersString)) {
            
            tempIgnoreRateLimitOnUsers = TwoFactorServerUtils.splitTrimToSet(ignoreRateLimitOnUsersString, ",");
            
          }
          this.ignoreRateLimitOnUsers = tempIgnoreRateLimitOnUsers;
        }
        
      }
      
    }
    return this.ignoreRateLimitOnUsers;
    
  }
  
  /**
   * set of strings not null to not audit
   */
  private Set<String> dontAuditActions = null;
  
  /**
   * audit actions that shouldnt be audited
   * @return the set of strings or empty set, never null
   */
  public Set<String> dontAuditActions() {
    
    if (this.dontAuditActions == null) {

      synchronized(this) {
        
        if (this.dontAuditActions == null) {
          
          String dontAuditActionsString = this.propertyValueString("twoFactorServer.dontAuditActions");
          
          Set<String> tempDontAuditActions = new HashSet<String>();
          
          if (!StringUtils.isBlank(dontAuditActionsString)) {
            
            tempDontAuditActions = TwoFactorServerUtils.splitTrimToSet(dontAuditActionsString, ",");
            
          }
          this.dontAuditActions = tempDontAuditActions;
        }
        
      }
      
    }
    return this.dontAuditActions;
    
  }
  
  /**
   * get the contact implementation
   * @return the authz
   */
  public TwoFactorContactInterface twoFactorContact() {
    
    String className = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.contactInterfaceImplementation");
    @SuppressWarnings("unchecked")
    Class<TwoFactorContactInterface> twoFactorContactInterfaceClass = TwoFactorServerUtils.forName(className);
    TwoFactorContactInterface twoFactorContactInterface = TwoFactorServerUtils.newInstance(twoFactorContactInterfaceClass);
    return twoFactorContactInterface;

  }
  
  /**
   * get the check pass implementation
   * @return the check pass
   */
  public TwoFactorCheckPass twoFactorCheckPass() {
    
    String className = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.checkPassInterfaceImplementation");
    @SuppressWarnings("unchecked")
    Class<TwoFactorCheckPass> twoFactorCheckPassClass = TwoFactorServerUtils.forName(className);
    TwoFactorCheckPass twoFactorCheckPass = TwoFactorServerUtils.newInstance(twoFactorCheckPassClass);
    return twoFactorCheckPass;

  }
  
  
  
  
  /**
   * get the authorization implementation
   * @return the authz
   */
  public TwoFactorAuthorizationInterface twoFactorAuthorization() {
    
    String className = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.twoFactorAuthzImplementation");
    @SuppressWarnings("unchecked")
    Class<TwoFactorAuthorizationInterface> twoFactorAuthorizationInterfaceClass = TwoFactorServerUtils.forName(className);
    TwoFactorAuthorizationInterface twoFactorAuthorizationInterface = TwoFactorServerUtils.newInstance(twoFactorAuthorizationInterfaceClass);
    return twoFactorAuthorizationInterface;

  }
  
  /**
   * retrieve the implementation
   * @return the interface
   */
  public TwoFactorLogicInterface twoFactorLogic() {
    
    String className = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.twoFactorLogicImplementation");
    @SuppressWarnings("unchecked")
    Class<TwoFactorLogicInterface> twoFactorLogicInterfaceClass = TwoFactorServerUtils.forName(className);
    TwoFactorLogicInterface twoFactorLogicInterface = TwoFactorServerUtils.newInstance(twoFactorLogicInterfaceClass);
    return twoFactorLogicInterface;
  }

  /**
   * cache the tf server authz
   */
  private Map<String, String> tfServerAuthz = null;
  
  /**
   * tf server authz
   * @return the map
   */
  public Map<String, String> tfServerAuthz() {
    
    //ws.authz.tfServer.local.principal
    if (this.tfServerAuthz == null) {
      
      synchronized (this) {
        
        if (this.tfServerAuthz == null) {
          
          Map<String, String> map = new HashMap<String, String>();
          
          Pattern pattern = Pattern.compile("^ws\\.authz\\.tfServer\\.(.*)\\.principal$");
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String tfServerName = matcher.group(1);
              
              String principal = this.propertyValueString(key);
              String networks = this.propertyValueString("ws.authz.tfServer." + tfServerName + ".networks");
              
              map.put(principal, networks);
              
            }
          }
          this.tfServerAuthz = Collections.unmodifiableMap(map);
        }
      }
    }
    
    return this.tfServerAuthz;
    
  }
  
  /**
   * bean for bundles
   */
  public static class TextBundleBean {
    
    /** country e.g. us */
    private String country;
    
    /** language e.g. en */
    private String language;
    
    /** filename in the package tfText that is before the .base.properties, and .properties */
    private String fileNamePrefix;

    
    /**
     * country e.g. us
     * @return the country
     */
    public String getCountry() {
      return this.country;
    }

    
    /**
     * country e.g. us
     * @param country1 the country to set
     */
    public void setCountry(String country1) {
      this.country = country1;
    }

    
    /**
     * language e.g. en
     * @return the language
     */
    public String getLanguage() {
      return this.language;
    }

    
    /**
     * language e.g. en
     * @param language1 the language to set
     */
    public void setLanguage(String language1) {
      this.language = language1;
    }

    
    /**
     * filename in the package tfText that is before the .base.properties, and .properties
     * @return the fileNamePrefix
     */
    public String getFileNamePrefix() {
      return this.fileNamePrefix;
    }

    
    /**
     * filename in the package tfText that is before the .base.properties, and .properties
     * @param fileNamePrefix1 the fileNamePrefix to set
     */
    public void setFileNamePrefix(String fileNamePrefix1) {
      this.fileNamePrefix = fileNamePrefix1;
    }
    
    
    
  }
  
  /**
   * default bundle
   */
  private TextBundleBean textBundleDefault = null;
  
  
  
  
  /**
   * default bundle
   * @return the textBundleDefault
   */
  public TextBundleBean textBundleDefault() {
    if (this.textBundleDefault == null) {
      this.textBundleFromCountry();
    }
    return this.textBundleDefault;
  }

  /**
   * country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromCountry  = null;
  
  /**
   * language_country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguageAndCountry  = null;
  
  /**
   * language to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguage  = null;
  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorServerConfig.class);

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguage() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguage = this.textBundleFromLanguage;
    if (theTextBundleFromLanguage == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguage = this.textBundleFromLanguage;
    }
    if (theTextBundleFromLanguage == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguage;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguageAndCountry() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    if (theTextBundleFromLanguageAndCountry == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    }
    if (theTextBundleFromLanguageAndCountry == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguageAndCountry;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromCountry() {
    if (this.textBundleFromCountry == null) {
      
      synchronized (this) {
        
        if (this.textBundleFromCountry == null) {
          
          Map<String, TextBundleBean> tempBundleFromCountry = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguage = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguageAndCountry = new HashMap<String, TextBundleBean>();
          
          Pattern pattern = Pattern.compile("^twoFactorServer\\.text\\.bundle\\.(.*)\\.fileNamePrefix$");
          
          boolean foundDefault = false;
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String bundleKey = matcher.group(1);

              String fileNamePrefix = this.propertyValueString(key);
              String language = StringUtils.defaultString(this.propertyValueString("twoFactorServer.text.bundle." + bundleKey + ".language")).toLowerCase();
              String country = StringUtils.defaultString(this.propertyValueString("twoFactorServer.text.bundle." + bundleKey + ".country")).toLowerCase();
              
              TextBundleBean textBundleBean = new TextBundleBean();
              
              textBundleBean.setCountry(country);
              textBundleBean.setLanguage(language);
              textBundleBean.setFileNamePrefix(fileNamePrefix);

              if (StringUtils.equals(bundleKey, propertyValueStringRequired("twoFactorServer.text.defaultBundleIndex"))) {
                foundDefault = true;
                this.textBundleDefault = textBundleBean;
              }
              
              //first in wins
              if (!tempBundleFromCountry.containsKey(country)) {
                tempBundleFromCountry.put(country, textBundleBean);
              }
              if (!tempBundleFromLanguage.containsKey(language)) {
                tempBundleFromLanguage.put(language, textBundleBean);
              }
              String languageAndCountry = language + "_" + country;
              if (tempBundleFromLanguageAndCountry.containsKey(languageAndCountry)) {
                LOG.error("Language and country already defined! " + languageAndCountry);
              }
              tempBundleFromLanguageAndCountry.put(languageAndCountry, textBundleBean);
            }
          }
          
          if (!foundDefault) {
            throw new RuntimeException("Cant find default bundle index: '" 
                + propertyValueStringRequired("twoFactorServer.text.defaultBundleIndex") + "', should have a key: twoFactorServer.text.bundle."
                + propertyValueStringRequired("twoFactorServer.text.defaultBundleIndex") + ".fileNamePrefix");
          }
          
          this.textBundleFromCountry = Collections.unmodifiableMap(tempBundleFromCountry);
          this.textBundleFromLanguage = Collections.unmodifiableMap(tempBundleFromLanguage);
          this.textBundleFromLanguageAndCountry = Collections.unmodifiableMap(tempBundleFromLanguageAndCountry);
          
        }
      }
    }
    return this.textBundleFromCountry;
  }
  
  /**
   * audit levels
   */
  private Map<String, Integer> tfAuditLevels = null;
  
  /**
   * use the factory
   */
  private TwoFactorServerConfig() {
  }

  /**
   * 
   * @return the map of comma separated actions to number of days to keep (or -1)
   */
  public Map<String, Integer> tfAuditLevels() {
    //twoFactorServer.TfAuditClearingJob.level1.actions = OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR
    //twoFactorServer.TfAuditClearingJob.level1.retentionDays = -1
    //
    //twoFactorServer.TfAuditClearingJob.level2.actions = GENERATE_PASSWORDS, INVALIDATE_PASSWORDS, OPTIN_TWO_FACTOR_STEP1, UNTRUST_BROWSERS
    //twoFactorServer.TfAuditClearingJob.level2.retentionDays = 60
    //
    //twoFactorServer.TfAuditClearingJob.level3.actions = AUTHN_ERROR, AUTHN_NOT_OPTED_IN, AUTHN_TRUSTED_BROWSER, AUTHN_TWO_FACTOR, AUTHN_TWO_FACTOR_REQUIRED, AUTHN_WRONG_PASSWORD
    //twoFactorServer.TfAuditClearingJob.level3.retentionDays = 15
    
    if (this.tfAuditLevels == null) {
      
      synchronized (this) {
        
        if (this.tfAuditLevels == null) {
          
          Map<String, Integer> tempAuditLevels = new HashMap<String, Integer>();
          
          Pattern pattern = Pattern.compile("^twoFactorServer\\.TfAuditClearingJob\\.(.*)\\.actions$");
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String levelNameKey = matcher.group(1);
              
              String actions = this.propertyValueString(key);
              String retentionDaysString = this.propertyValueString("twoFactorServer.TfAuditClearingJob." + levelNameKey + ".retentionDays");
              int retentionDays = Integer.parseInt(retentionDaysString);
              tempAuditLevels.put(actions, retentionDays);
              
            }
          }
          this.tfAuditLevels = Collections.unmodifiableMap(tempAuditLevels);

        }
      }
    }
    
    return this.tfAuditLevels;
    
    
  }

  /**
   * report configs, name to config
   */
  private Map<String, TfReportConfig> tfReportConfigs = null;
  
  /**
   * 
   * @return the map, name to config
   */
  public Map<String, TfReportConfig> tfReportConfigs() {
    
    if (this.tfReportConfigs == null) {
      
      synchronized (this) {
        
        if (this.tfReportConfigs == null) {
          
          Map<String, TfReportConfig> tempTfReportConfigs = new HashMap<String, TfReportConfig>();
          
          //  # quartz cron for when this job should run
          //  #twoFactorServer.report.<name>.quartzCron = 
          Pattern pattern = Pattern.compile("^twoFactorServer\\.report\\.(.*)\\.quartzCron$");
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String reportName = matcher.group(1);
              
              TfReportConfig tfReportConfig = new TfReportConfig();
              tfReportConfig.setName(reportName);
              if(StringUtils.isBlank(reportName)) {
                LOG.error("Report name cant be blank!!!");
                continue;
              }

              {
                String quartzCron = this.propertyValueString(key);
                if(StringUtils.isBlank(quartzCron)) {
                  LOG.error("Report quartzCron cant be blank for report " + reportName + "!!!");
                  continue;
                }
                tfReportConfig.setQuartzCron(quartzCron);
              }
              
              //
              //  # query of the report, can be any number of rows / cols
              //  #twoFactorServer.report.<name>.query =
              {
                String query = this.propertyValueString("twoFactorServer.report." + reportName + ".query");
                if(StringUtils.isBlank(query)) {
                  LOG.error("Report query cant be blank for report " + reportName + "!!!");
                  continue;
                }
                tfReportConfig.setQuery(query);
              }
              
              //
              //  # if emailing to people in the report, this is the column name of the email address
              //  # will send one email to each person
              //  #twoFactorServer.report.<name>.emailToColumn = COLUMN_NAME
              {
                String emailToColumn = this.propertyValueString("twoFactorServer.report." + reportName + ".emailToColumn");
                if(!StringUtils.isBlank(emailToColumn)) {
                  tfReportConfig.setEmailToColumn(emailToColumn);
                }
              }
              
              
              //  #twoFactorServer.report.<name>.tos =
              {
                String tos = this.propertyValueString("twoFactorServer.report." + reportName + ".tos");
                if(!StringUtils.isBlank(tos)) {
                  tfReportConfig.setTos(tos);
                }
              }

              //  #twoFactorServer.report.<name>.ccs =
              {
                String ccs = this.propertyValueString("twoFactorServer.report." + reportName + ".ccs");
                if(!StringUtils.isBlank(ccs)) {
                  tfReportConfig.setCcs(ccs);
                }
              }

              //  #twoFactorServer.report.<name>.bccs =
              {
                String bccs = this.propertyValueString("twoFactorServer.report." + reportName + ".bccs");
                if(!StringUtils.isBlank(bccs)) {
                  tfReportConfig.setBccs(bccs);
                }
              }

              //
              //  #twoFactorServer.report.<name>.emailSubject =
              {
                String emailToSubject = this.propertyValueString("twoFactorServer.report." + reportName + ".emailSubject");
                if(StringUtils.isBlank(emailToSubject)) {
                  LOG.error("Report emailSubject cant be blank for report " + reportName + "!!!");
                  continue;
                }
                tfReportConfig.setEmailSubject(emailToSubject);
              }

              //  #twoFactorServer.report.<name>.emailBody =
              {
                String emailBody = this.propertyValueString("twoFactorServer.report." + reportName + ".emailBody");
                if(StringUtils.isBlank(emailBody)) {
                  LOG.error("Report query cant be blank for report " + reportName + "!!!");
                  continue;
                }
                
                emailBody = TwoFactorServerUtils.replace(emailBody, "\\n", "\n");
                
                tfReportConfig.setEmailBody(emailBody);
              }

              //  # you can use $date_time$ to subsitute the current date/time
              {
                String reportFileName = this.propertyValueString("twoFactorServer.report." + reportName + ".reportFileName");
                if(!StringUtils.isBlank(reportFileName)) {
                  tfReportConfig.setReportFileName(reportFileName);
                }
              }
              
              //  # from has a default if not filled in
              //  #twoFactorServer.report.<name>.from =

              {
                String from = this.propertyValueString("twoFactorServer.report." + reportName + ".from");
                if(!StringUtils.isBlank(from)) {
                  tfReportConfig.setFrom(from);
                }
              }
              
              tempTfReportConfigs.put(reportName, tfReportConfig);
              
            }
          }
          this.tfReportConfigs = Collections.unmodifiableMap(tempTfReportConfigs);

        }
      }
    }
    
    return this.tfReportConfigs;
    
    
  }
  

  
  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static TwoFactorServerConfig retrieveConfig() {
    return retrieveConfig(TwoFactorServerConfig.class);
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
    return "twoFactorServer.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "twoFactor.server.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "twoFactor.server.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "twoFactorServer.config.secondsBetweenUpdateChecks";
  }

}
