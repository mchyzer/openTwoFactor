/*
 * @author mchyzer $Id: TfClientRestHttpMethod.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client.ws;

import org.openTwoFactor.client.util.TwoFactorClientUtils;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.HttpMethodBase;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.methods.DeleteMethod;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.methods.GetMethod;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.methods.PostMethod;
import org.openTwoFactor.clientExt.org.apache.commons.httpclient.methods.PutMethod;


/**
 * types of http methods accepted by grouper rest
 */
public enum TfClientRestHttpMethod {

  /** GET */
  GET {

    /**
     * @see TfClientRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new GetMethod(url);
    }


  },

  /** POST */
  POST {

    /**
     * @see TfClientRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new PostMethod(url);
    }

  },

  /** PUT */
  PUT {

    /**
     * @see TfClientRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new PutMethod(url);
    }

  },

  /** DELETE */
  DELETE {

    /**
     * @see TfClientRestHttpMethod#httpMethod(String)
     * make a get method
     */
    @Override
    public HttpMethodBase httpMethod(String url) {
      return new DeleteMethod(url);
    }
  };

  /**
   * make the method for httpClient
   * @param url
   * @return the method
   */
  public abstract HttpMethodBase httpMethod(String url);
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @param exceptionOnNotFound true to throw exception if method not found
   * @return the enum or null or exception if not found
   * @throws GrouperRestInvalidRequest if there is a problem
   */
  public static TfClientRestHttpMethod valueOfIgnoreCase(String string,
      boolean exceptionOnNotFound) {
    return TwoFactorClientUtils.enumValueOfIgnoreCase(TfClientRestHttpMethod.class, string, exceptionOnNotFound);
  }
}
