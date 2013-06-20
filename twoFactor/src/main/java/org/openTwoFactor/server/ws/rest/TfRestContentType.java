/*
 * @author mchyzer $Id: TfRestContentType.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.rest;

import java.io.File;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.json.DefaultJsonConverter;
import org.openTwoFactor.server.json.JsonConverter;
import org.openTwoFactor.server.util.JsonIndenter;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.util.XmlIndenter;



/**
 * possible content types by grouper ws rest
 */
public enum TfRestContentType {

  /** xml content type
   * http request content type should be set to text/xml
   */
  xml("text/xml") {


    /**
     * parse a string to an object
     * @param input
     * @param warnings is where warnings should be written to
     * @return the object
     */
    @Override
    public Object parseString(Class<?> theClass, String input, StringBuilder warnings) {
      return TwoFactorServerUtils.xmlConvertFrom(input, theClass);
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      
      return XML_HEADER + TwoFactorServerUtils.xmlConvertTo(object);
    }

    @Override
    public String indent(String string) {
      return new XmlIndenter(string).result();
    }
  },
  /** 
   * json content type, uses the pluggable json converter
   * http request content type should be set to text/x-json
   */
  json("text/x-json") {


    /**
     * parse a string to an object
     * @param input
     * @param warnings is where warnings should be written to
     * @return the object
     */
    @Override
    public Object parseString(Class<?> theClass, String input, StringBuilder warnings) {

      JsonConverter jsonConverter = jsonConverter();
      
      try {
        return jsonConverter.convertFromJson(theClass, input, warnings);
      } catch (RuntimeException re) {
        LOG.error("Error unparsing string with converter: " + TwoFactorServerUtils.className(jsonConverter) + ", " + input);
        throw new RuntimeException("Problem unparsing string with converter: " + TwoFactorServerUtils.className(jsonConverter)
            + ", " + TwoFactorServerUtils.indent(input, false), re);
      }
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      JsonConverter jsonConverter = jsonConverter();
      
      try {
        String jsonString = jsonConverter.convertToJson(object);
        return jsonString;
      } catch (RuntimeException re) {
        LOG.error("Error converting json object with converter: " 
            + TwoFactorServerUtils.className(jsonConverter) + ", " + TwoFactorServerUtils.className(object));
        throw new RuntimeException("Error converting json object with converter: " + TwoFactorServerUtils.className(jsonConverter)
            + ", " + TwoFactorServerUtils.className(object), re);
      }
    }

    @Override
    public String indent(String string) {
      return new JsonIndenter(string).result();
    }
  };

  /** xml header */
  public static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
  
  /**
   * indent the content
   * @param string 
   * @return the indented content
   */
  public abstract String indent(String string);
  
  /**
   * instantiate the json convert configured in the grouper-ws.properties file
   * @return the json converter
   */
  @SuppressWarnings("unchecked")
  public static JsonConverter jsonConverter() {
    String jsonConverterClassName = TwoFactorServerConfig.retrieveConfig().propertyValueString(
        "jsonConverter", DefaultJsonConverter.class.getName());
    Class<? extends JsonConverter> jsonConverterClass = TwoFactorServerUtils.forName(jsonConverterClassName);
    JsonConverter jsonConverter = TwoFactorServerUtils.newInstance(jsonConverterClass);
    return jsonConverter;
  }
  
  /**
   * test out a parse
   * @param args
   */
  public static void main(String[] args) {
    String jsonString = TwoFactorServerUtils.readFileIntoString(new File("c:/temp/problem.json"));
    TfRestContentType.json.parseString(Object.class, jsonString, new StringBuilder());
  }
  
  /**
   * parse a string to an object
   * @param theClass 
   * @param input
   * @param warnings is where warnings should be written to
   * @return the object
   */
  public abstract Object parseString(Class<?> theClass, String input, StringBuilder warnings);

  /**
   * write a string representation to result string
   * @param object to write to output
   * @return the string representation
   */
  public abstract String writeString(Object object);

  /**
   * constructor with content type
   * @param theContentType
   */
  private TfRestContentType(String theContentType) {
    this.contentType = theContentType;
  }
  
  /** content type of request */
  private String contentType;
  
  /**
   * content type header for http
   * @return the content type
   */
  public String getContentType() {
    return this.contentType;
  }
  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfRestContentType.class);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   * @throws TfRestInvalidRequest problem
   */
  public static TfRestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws TfRestInvalidRequest {
    return TwoFactorServerUtils.enumValueOfIgnoreCase(TfRestContentType.class, string, exceptionOnNotFound);
  }

  /** content type thread local */
  private static ThreadLocal<TfRestContentType> contentTypeThreadLocal = new ThreadLocal<TfRestContentType>();

  /**
   * 
   * @param wsRestContentType
   */
  public static void assignContentType(TfRestContentType wsRestContentType) {
    contentTypeThreadLocal.set(wsRestContentType);
  }
  
  /**
   * 
   */
  public static void clearContentType() {
    contentTypeThreadLocal.remove();
  }
  
  /**
   * 
   * @return wsRestContentType
   */
  public static TfRestContentType retrieveContentType() {
    return contentTypeThreadLocal.get();
  }
  

}
