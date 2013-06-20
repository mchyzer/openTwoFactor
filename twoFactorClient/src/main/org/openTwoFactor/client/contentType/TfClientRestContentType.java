/*
 * @author mchyzer $Id: TfClientRestContentType.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client.contentType;

import java.io.File;

import org.openTwoFactor.client.corebeans.TwoFactorResponseBeanBase;
import org.openTwoFactor.client.json.DefaultJsonConverter;
import org.openTwoFactor.client.json.JsonConverter;
import org.openTwoFactor.client.util.JsonIndenter;
import org.openTwoFactor.client.util.TwoFactorClientConfig;
import org.openTwoFactor.client.util.TwoFactorClientUtils;
import org.openTwoFactor.client.util.XmlIndenter;
import org.openTwoFactor.clientExt.org.apache.commons.logging.Log;
import org.openTwoFactor.clientExt.org.apache.commons.logging.LogFactory;


/**
 * possible content types by grouper ws rest
 */
public enum TfClientRestContentType {

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
    public <T extends TwoFactorResponseBeanBase> T parseString(Class<T> expectedResultClass, String input, StringBuilder warnings) {
      return TwoFactorClientUtils.xmlConvertFrom(input, expectedResultClass);
    }

    /**
     * write a string representation to result string
     * @param object to write to output
     * @return the string representation
     */
    @Override
    public String writeString(Object object) {
      
      return XML_HEADER + TwoFactorClientUtils.xmlConvertTo(object);
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
    public <T extends TwoFactorResponseBeanBase> T parseString(Class<T> expectedResultClass, String input, StringBuilder warnings) {

      JsonConverter jsonConverter = jsonConverter();
      
      try {
        return jsonConverter.convertFromJson(expectedResultClass, input, warnings);
      } catch (RuntimeException re) {
        LOG.error("Error unparsing string with converter: " + TwoFactorClientUtils.className(jsonConverter) + ", " + input);
        throw new RuntimeException("Problem unparsing string with converter: " + TwoFactorClientUtils.className(jsonConverter)
            + ", " + TwoFactorClientUtils.indent(input, false), re);
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
            + TwoFactorClientUtils.className(jsonConverter) + ", " + TwoFactorClientUtils.className(object));
        throw new RuntimeException("Error converting json object with converter: " + TwoFactorClientUtils.className(jsonConverter)
            + ", " + TwoFactorClientUtils.className(object), re);
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
    String jsonConverterClassName = TwoFactorClientConfig.retrieveConfig().propertyValueString(
        "jsonConverter", DefaultJsonConverter.class.getName());
    Class<? extends JsonConverter> jsonConverterClass = TwoFactorClientUtils.forName(jsonConverterClassName);
    JsonConverter jsonConverter = TwoFactorClientUtils.newInstance(jsonConverterClass);
    return jsonConverter;
  }
  
  /**
   * test out a parse
   * @param args
   */
  public static void main(String[] args) {
    String jsonString = TwoFactorClientUtils.readFileIntoString(new File("c:/temp/problem.json"));
    TfClientRestContentType.json.parseString(TwoFactorResponseBeanBase.class, jsonString, new StringBuilder());
  }
  
  /**
   * parse a string to an object
   * @param expectedResultClass 
   * @param <T> is the class to return
   * @param input
   * @param warnings is where warnings should be written to
   * @return the object
   */
  public abstract <T extends TwoFactorResponseBeanBase> T parseString(Class<T> expectedResultClass, String input, StringBuilder warnings);

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
  private TfClientRestContentType(String theContentType) {
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
  
  /**
   * logger 
   */
  private static final Log LOG = LogFactory.getLog(TfClientRestContentType.class);

  /**
   * do a case-insensitive matching
   * throws TfRestInvalidRequest if problem 
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception on not found
   * @return the enum or null or exception if not found
   */
  public static TfClientRestContentType valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) {
    return TwoFactorClientUtils.enumValueOfIgnoreCase(TfClientRestContentType.class, string, exceptionOnNotFound);
  }

}
