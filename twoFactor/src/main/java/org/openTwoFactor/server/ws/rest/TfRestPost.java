package org.openTwoFactor.server.ws.rest;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.corebeans.TwoFactorResponseBeanBase;



/**
 * enum for post requests
 * @author mchyzer
 *
 */
public enum TfRestPost {

  /** checkPassword post requests */
  validatePassword {

    /**
     * handle the incoming request based on GET HTTP method and groups resource
     * @param urlStrings not including the app name or servlet.  
     * for http://localhost/twoFactor/validatePassword.json
     * @return the result object
     */
    @Override
    public TwoFactorResponseBeanBase service(List<String> urlStrings,
        Map<String, String> params, String body) {
      
      if (TwoFactorServerUtils.length(urlStrings) > 0) {
        throw new TfRestInvalidRequest("Not expecting more url strings: " + TwoFactorServerUtils.toStringForLog(urlStrings));
      }
      
      //make sure sensitive info is not in URL
      if (!StringUtils.isBlank(TwoFactorFilterJ2ee.retrieveHttpServletRequest().getQueryString())) {
        throw new TfRestInvalidRequest("Not expecting query string in request, pass params in the POST body");
      }
      
      return TfRestLogic.checkPassword(TwoFactorDaoFactory.getFactory(), params);
      
    }

  };

  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true if exception should be thrown on not found
   * @return the enum or null or exception if not found
   * @throws TfRestInvalidRequest if there is a problem
   */
  public static TfRestPost valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) throws TfRestInvalidRequest {
    return TwoFactorServerUtils.enumValueOfIgnoreCase(TfRestPost.class, 
        string, exceptionOnNotFound);
  }

  /**
   * handle the incoming request based on HTTP method
   * @param urlStrings not including the app name or servlet.  for http://localhost/grouper-ws/servicesRest/groups/a:b
   * the urlStrings would be size two: {"group", "a:b"}
   * @param params 
   * @param body is the request body converted to object
   * @return the result object
   */
  public abstract TwoFactorResponseBeanBase service(
      List<String> urlStrings,
      Map<String, String> params, String body);

}
