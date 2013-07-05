/**
 * @author mchyzer
 * $Id: TextContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.config.TwoFactorTextConfig;


/**
 * text container in request for user
 */
public class TextContainer {

  /**
   * retrieve the container from the request or create a new one if not there
   * @return the container
   */
  public static TextContainer retrieveFromRequest() {
    
    TextContainer textContainer = 
        (TextContainer)TwoFactorFilterJ2ee
        .retrieveHttpServletRequest().getAttribute("textContainer");
    
    if (textContainer == null) {
      textContainer = new TextContainer();
      TwoFactorFilterJ2ee.retrieveHttpServletRequest().setAttribute(
          "textContainer", textContainer);
    }
    
    return textContainer;
    
  }
  

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TextContainer.class);

  /**
   * text value for key
   * @param key
   * @return text value
   */
  private static String textValue(String key) {
    TwoFactorTextConfig twoFactorTextConfig = TwoFactorTextConfig.retrieveTextConfig();
    String value = twoFactorTextConfig.propertyValueString(key);
    value = massageText(key, value);
    return value;

  }

  /**
   * massage text with substitutions etc
   * @param key
   * @param value
   * @return the text
   */
  public static String massageText(String key, String value) {
    if (StringUtils.isBlank(value)) {
      LOG.error("Cant find text for variable: '" + key + "'");
      return "$$not found: " + key + "$$";
    }
    
    //if there might be a scriptlet
    if (value.contains("${")) {
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
      substituteMap.put("twoFactorRequestContainer", TwoFactorRequestContainer.retrieveFromRequest());
      substituteMap.put("request", TwoFactorFilterJ2ee.retrieveHttpServletRequest());
      substituteMap.put("textContainer", TextContainer.retrieveFromRequest());
      value = TwoFactorServerUtils.substituteExpressionLanguage(value, substituteMap, true, false, false);
    }
    return value;
  }
  
  /**
   * escape single quotes in javascript
   * @param string
   * @return the escaped string
   */
  private static String escapeSingleQuotes(String string) {
    if (string == null) {
      return string;
    }
    return string.replace("'", "\'");
  }
  
  /**
   * escape double quotes in javascript
   * @param string
   * @return the escaped string
   */
  private static String escapeDoubleQuotes(String string) {
    if (string == null) {
      return string;
    }
    return string.replace("\"", "&quot;");
  }
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return textValue((String)key);
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeSingleMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return escapeSingleQuotes(textValue((String)key));
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeXmlMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return TwoFactorServerUtils.xmlEscape(textValue((String)key), true);
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeDoubleMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return escapeDoubleQuotes(textValue((String)key));
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeSingleDoubleMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return escapeSingleQuotes(escapeDoubleQuotes(textValue((String)key)));
    }

  };
  

  /**
   * text map
   * @return the text object
   */
  public Map<String, String> getText() {
    return this.textMap;
  }
  
  /**
   * text map, escape single quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeSingle() {
    return this.textEscapeSingleMap;
  }
  
  
  /**
   * text map, escape xml
   * @return the text object
   */
  public Map<String, String> getTextEscapeXml() {
    return this.textEscapeXmlMap;
  }
  
  /**
   * text map, escape double quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeDouble() {
    return this.textEscapeDoubleMap;
  }
  
  /**
   * text map, escape single and double quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeSingleDouble() {
    return this.textEscapeSingleDoubleMap;
  }
  

  
  
  
  
}
