/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.poc;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import com.duosecurity.client.Http;


/**
 *
 */
public class DuoPoc {

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {

    Http request = new Http("POST",
        "api-ecae067e.duosecurity.com",
        "/admin/v1/tokens");
    request.addParam("type", "t6"); //totp 6 digit
    request.addParam("serial", "123462");
    request.addParam("secret", "782467af4a3ba35XXXXX");  //hex!
    
    request.signRequest("XXXXXXXXXXXXX",
      "XXXXXXXXXXXXXXXXX");
    
    
    String result = request.executeRequestRaw();
    
    // {"code": 40002, "message": "Invalid request parameters", "message_detail": "secret", "stat": "FAIL"}
    
    // jsonObject.get("message")
    
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
    
    System.out.println(jsonObject.get("message"));
  }
  
}
