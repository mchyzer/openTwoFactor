/*
 * @author mchyzer $Id: TfRestHttpMethod.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.rest;

import java.util.List;
import java.util.Map;

import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.corebeans.TwoFactorResponseBeanBase;


/**
 * types of http methods accepted by rest
 */
public enum TfRestHttpMethod {

  /** GET */
  GET {

    /**
     * @see TfRestHttpMethod#service(List, Map, String)
     */
    @Override
    public TwoFactorResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
            
      throw new RuntimeException("No expecting this request");

    }


  },

  /** POST */
  POST {

    /**
     * @see TfRestHttpMethod#service(List, Map, String)
     */
    @Override
    public TwoFactorResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {

      if (urlStrings.size() == 0) {

        throw new RuntimeException("No expecting this request");
      
      }

      String firstResource = TwoFactorServerUtils.popUrlString(urlStrings);
      
      //validate and get the first resource
      TfRestPost tfRestPost = TfRestPost.valueOfIgnoreCase(
          firstResource, true);
  
      return tfRestPost.service(urlStrings, params, body);

    }


  },

  /** PUT */
  PUT {

    /**
     * @see TfRestHttpMethod#service(List, Map, String)
     */
    @Override
    public TwoFactorResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      throw new RuntimeException("No expecting this request");
    }


  },

  /** DELETE */
  DELETE {

    /**
     * @see TfRestHttpMethod#service(List, Map, String)
     */
    @Override
    public TwoFactorResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      throw new RuntimeException("No expecting this request");
    }


  };

  /**
   * handle the incoming request based on HTTP method
   * @param body version of client, e.g. v1_3_000
   * @param urlStrings not including the app name or servlet.  for http://localhost/ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param params is the request body converted to object
   * @return the resultObject
   */
  public abstract TwoFactorResponseBeanBase service(
      List<String> urlStrings, Map<String, String> params, String body);

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws RuntimeException if there is a problem
   */
  public static TfRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws RuntimeException {
    return TwoFactorServerUtils.enumValueOfIgnoreCase(TfRestHttpMethod.class, string, exceptionOnNotFound);
  }
}
