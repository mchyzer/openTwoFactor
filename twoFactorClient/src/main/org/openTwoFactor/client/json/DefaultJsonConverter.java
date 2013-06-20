/**
 * @author Chris
 * $Id: DefaultJsonConverter.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client.json;

import java.io.IOException;
import java.io.Writer;

import org.openTwoFactor.client.contentType.TfClientRestContentType;
import org.openTwoFactor.client.corebeans.TfCheckPasswordResponse;
import org.openTwoFactor.client.util.TwoFactorClientUtils;




/**
 * use grouper's default json library
 */
public class DefaultJsonConverter implements JsonConverter {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    String string = "{\n"
        + "changeUserBrowserUuid : \"abc\",\n"
        + "twoFactorPasswordCorrect : \"abc\",\n"
        + "twoFactorRequired : \"abc\",\n"
        + "twoFactorUserAllowed : \"abc\",\n"
        + "userBrowserUuidIsNew : \"abc\",\n"
        + "userEnrolledInTwoFactor : \"abc\",\n"
        + "whenTrusted : \"abc\",\n"
        + "}";
    
    TfCheckPasswordResponse tfCheckPasswordResponse = 
        TfClientRestContentType.json.parseString(TfCheckPasswordResponse.class, string, new StringBuilder());
    
    System.out.println(tfCheckPasswordResponse.getResponseMessage());
    
  }
  /**
   * @see JsonConverter#convertFromJson(Class, java.lang.String, StringBuilder)
   */
  public <T> T convertFromJson(Class<T> theClass, String json, StringBuilder warnings) {
    return TwoFactorClientUtils.jsonConvertFrom(json, theClass);
  }

  /**
   * @see JsonConverter#convertToJson(java.lang.Object)
   */
  public String convertToJson(Object object) {
    String result = TwoFactorClientUtils.jsonConvertToNoWrap(object);
    return result;
  }

  /**
   * @see JsonConverter#convertToJson(java.lang.Object, java.io.Writer)
   */
  public void convertToJson(Object object, Writer writer) {
    String json = convertToJson(object);
    try {
      writer.write(json);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  
  
}
