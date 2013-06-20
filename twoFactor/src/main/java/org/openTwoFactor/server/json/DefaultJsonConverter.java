/**
 * @author Chris
 * $Id: DefaultJsonConverter.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.json;

import java.io.IOException;
import java.io.Writer;

import org.openTwoFactor.server.util.TwoFactorServerUtils;




/**
 * use grouper's default json library
 */
public class DefaultJsonConverter implements JsonConverter {

  /**
   * @see JsonConverter#convertFromJson(Class, String, StringBuilder)
   */
  public Object convertFromJson(Class<?> theClass, String json, StringBuilder warnings) {
    Object object = TwoFactorServerUtils.jsonConvertFrom(json, theClass);
    return object;
  }

  /**
   * @see JsonConverter#convertToJson(java.lang.Object)
   */
  public String convertToJson(Object object) {
    String result = TwoFactorServerUtils.jsonConvertToNoWrap(object);
    return result;
  }

  /**
   * @see JsonConverter#convertToJson(java.lang.Object, java.io.Writer)
   */
  @Override
  public void convertToJson(Object object, Writer writer) {
    String json = convertToJson(object);
    try {
      writer.write(json);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  
  
}
