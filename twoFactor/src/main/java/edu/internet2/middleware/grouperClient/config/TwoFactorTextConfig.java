/**
 * @author mchyzer
 * $Id: TwoFactorTextConfig.java,v 1.2 2013/06/20 06:02:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.config;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.config.TwoFactorServerConfig.TextBundleBean;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * save the text bundle for the session.  Determine it based on 
 * the language and locale of the browser
 */
public class TwoFactorTextConfig extends ConfigPropertiesCascadeBase {

  /**
   * the name of the file on classpath, e.g. tfText/twoFactor.text.en.us.properties
   */
  private String mainConfigClasspath;
  
  /**
   * e.g. en_us
   */
  private String languageCountry;
  
  /**
   * the name of the file on classpath, e.g. tfText/twoFactor.text.en.us.base.properties
   */
  private String mainExampleConfigClasspath;

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorTextConfig.class);
  
  /**
   * constructor with the correct files
   * @param theMainConfigClasspath 
   * @param theMainExampleConfigClasspath 
   * @param theLanguageCountry e.g. en_us
   */
  private TwoFactorTextConfig(String theMainConfigClasspath, String theMainExampleConfigClasspath,  String theLanguageCountry) {
    this.mainConfigClasspath = theMainConfigClasspath;
    this.mainExampleConfigClasspath = theMainExampleConfigClasspath;
    this.languageCountry = theLanguageCountry;
  }
  
  /**
   * 
   */
  public TwoFactorTextConfig() {
    
  }
  
  /**
   * text config for this user's locale
   * @return the config for this user's locale
   */
  public static TwoFactorTextConfig retrieveTextConfig() {
    
    HttpServletRequest httpServletRequest = TwoFactorFilterJ2ee.retrieveHttpServletRequest();
    
    //cache this in the request
    TwoFactorTextConfig twoFactorTextConfig = (TwoFactorTextConfig)httpServletRequest.getAttribute("twoFactorTextConfig");
    
    if (twoFactorTextConfig == null) {
      
      synchronized(httpServletRequest) {
        
        if (twoFactorTextConfig == null) {
          
          Locale locale = httpServletRequest.getLocale();
          
          twoFactorTextConfig = retrieveText(locale);
          
          httpServletRequest.setAttribute("twoFactorTextConfig", twoFactorTextConfig);
        
        }        
      }
      
    }
    
    return twoFactorTextConfig;
    
  }
  
  /**
   * text config for a certain locale
   * @param locale
   * @return the config for this user's locale
   */
  public static TwoFactorTextConfig retrieveText(Locale locale) {

    TextBundleBean textBundleBean = retrieveTextBundleBean(locale);
    
    TwoFactorTextConfig twoFactorTextConfig = new TwoFactorTextConfig(textBundleBean.getFileNamePrefix() + ".properties",
          textBundleBean.getFileNamePrefix() + ".base.properties", textBundleBean.getLanguage() + "_" + textBundleBean.getCountry());
    twoFactorTextConfig = (TwoFactorTextConfig)twoFactorTextConfig.retrieveFromConfigFileOrCache();

    return twoFactorTextConfig;
    
  }
  
  /**
   * text config for a certain locale
   * @param locale
   * @return the config for this user's locale
   */
  private static TextBundleBean retrieveTextBundleBean(Locale locale) {
    
    if (locale == null) {
      //return the default
      return TwoFactorServerConfig.retrieveConfig().textBundleDefault();

    }
    
    String language = StringUtils.defaultString(locale.getLanguage()).toLowerCase();
    String country = StringUtils.defaultString(locale.getCountry()).toLowerCase();
    
    //see if there is a match by language and country
    TextBundleBean textBundleBean = TwoFactorServerConfig.retrieveConfig()
      .textBundleFromLanguageAndCountry().get(language + "_" + country);
    
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //see if there is a match by language
    textBundleBean = TwoFactorServerConfig.retrieveConfig()
      .textBundleFromLanguage().get(language);
  
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //see if there is a match by country
    textBundleBean = TwoFactorServerConfig.retrieveConfig()
      .textBundleFromCountry().get(country);
  
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //do the default
    return TwoFactorServerConfig.retrieveConfig().textBundleDefault();

  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getHierarchyConfigKey()
   */
  @Override
  protected String getHierarchyConfigKey() {
    //"twoFactorServer.config.hierarchy"
    return "text.config.hierarchy";
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getMainConfigClasspath()
   */
  @Override
  protected String getMainConfigClasspath() {
    //"twoFactor.server.properties"
    return this.mainConfigClasspath;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getMainExampleConfigClasspath()
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    //"twoFactor.server.base.properties"
    return this.mainExampleConfigClasspath;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey()
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    //"twoFactorServer.config.secondsBetweenUpdateChecks"
    return "text.config.secondsBetweenUpdateChecks";
  }
  
  /**
   * config file cache
   */
  private static Map<String, ConfigPropertiesCascadeBase> configFileCache = null;

  /**
   * pattern to find where the variables are in the textm, e.g. $$something$$
   */
  private static Pattern substitutePattern = Pattern.compile("\\$\\$([^\\s\\$]+?)\\$\\$");
  
  
  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#retrieveFromConfigFiles()
   */
  @Override
  protected ConfigPropertiesCascadeBase retrieveFromConfigFiles() {
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = super.retrieveFromConfigFiles();

    Properties properties = (Properties)TwoFactorServerUtils.fieldValue(configPropertiesCascadeBase, "properties");
    
    Set<String> propertyNamesToCheck = new LinkedHashSet<String>();
    
    for (Object propertyName : properties.keySet()) {
      propertyNamesToCheck.add((String)propertyName);
    }

    Set<String> nextPropertyNamesToCheck = new LinkedHashSet<String>();
    
    //lets resolve variables
    for (int i=0;i<20;i++) {
      
      boolean foundVariable = false;
      
      for (Object propertyNameObject : properties.keySet()) {
        
        String propertyName = (String)propertyNameObject;
        
        String value = properties.getProperty(propertyName);
        String newValue = substituteVariables(properties, value);
        
        //next run, dont do the ones that dont change...
        if (!StringUtils.equals(value, newValue)) {
          nextPropertyNamesToCheck.add(value);
          foundVariable = true;
          properties.put(propertyName, newValue);
        }
      }
      
      if (!foundVariable) {
        break;
      }

      //keep track of ones to check
      propertyNamesToCheck = nextPropertyNamesToCheck;
      nextPropertyNamesToCheck = new LinkedHashSet<String>();
      
    }
    return configPropertiesCascadeBase;
  }

  /**
   * 
   * @param properties to get data from
   * @param value 
   * @return the subsituted string
   */
  protected String substituteVariables(Properties properties, String value) {

    Matcher matcher = substitutePattern.matcher(value);
    
    StringBuilder result = new StringBuilder();
    
    int index = 0;
    
    //loop through and find each script
    while(matcher.find()) {
      result.append(value.substring(index, matcher.start()));
      
      //here is the script inside the dollars
      String variable = matcher.group(1);
      
      index = matcher.end();

      String variableText = properties.getProperty(variable);
      
      if (StringUtils.isBlank(variableText)) {
        LOG.error("Cant find text for variable: '" + variable + "'");
        variableText = "$$not found: " + variable + "$$";
      }
      
      result.append(variableText);
    }
    
    result.append(value.substring(index, value.length()));
    return result.toString();
    
  }

  /**
   * see if there is one in cache, if so, use it, if not, get from config files
   * @return the config from file or cache
   */
  @Override
  protected ConfigPropertiesCascadeBase retrieveFromConfigFileOrCache() {
    
    if (configFileCache == null) {
      configFileCache = 
        new HashMap<String, ConfigPropertiesCascadeBase>();
    }
    ConfigPropertiesCascadeBase configObject = configFileCache.get(this.languageCountry);
    
    if (configObject == null) {
      
      logDebug("Config file has not be created yet, will create now: " + this.getMainConfigClasspath());
      
      configObject = retrieveFromConfigFiles();
      configFileCache.put(this.languageCountry, configObject);
      
    } else {
      
      //see if that much time has passed
      if (configObject.needToCheckIfFilesNeedReloading()) {
        
        synchronized (configObject) {
          
          configObject = configFileCache.get(this.languageCountry);
          
          //check again in case another thread did it
          if (configObject.needToCheckIfFilesNeedReloading()) {
            
            if (configObject.filesNeedReloadingBasedOnContents()) {
              configObject = retrieveFromConfigFiles();
              configFileCache.put(this.languageCountry, configObject);
            }
          }
        }
      }
    }
    
    return configObject;
  }
}
