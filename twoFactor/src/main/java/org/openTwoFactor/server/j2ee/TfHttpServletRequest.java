/*
 * @author mchyzer $Id: TfHttpServletRequest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.j2ee;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.collections.IteratorUtils;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.rest.TfRestHttpMethod;
import org.openTwoFactor.server.ws.rest.TfRestInvalidRequest;


/**
 * wrap request so that no nulls are given to axis (since it handles badly)
 */
public class TfHttpServletRequest extends HttpServletRequestWrapper {
  
  /**
   * retrieve from threadlocal
   * @return the request
   */
  public static TfHttpServletRequest retrieve() {
    return (TfHttpServletRequest)TwoFactorFilterJ2ee.retrieveHttpServletRequest();
  }
  
  /**
   * method for this request
   */
  private String method = null;
  
  /**
   * @see HttpServletRequest#getMethod()
   */
  @Override
  public String getMethod() {
    if (this.method == null) {
      //get it from the URL if it is there, lets not mess with method
      String methodString = null; //this.getParameter("method");
      if (TwoFactorServerUtils.isBlank(methodString)) {
        methodString = super.getMethod();
      }
      //lets see if it is a valid method
      TfRestHttpMethod.valueOfIgnoreCase(methodString, true);
      this.method = methodString;
    }
    return this.method;
  }

  /**
   * @return original method from underlying servlet
   * @see HttpServletRequest#getMethod()
   */
  public String getOriginalMethod() {
    return super.getMethod();
  }

  /**
   * valid params that the API knows about
   */
  private static Set<String> validParamNames = TwoFactorServerUtils.toSet(
      //for WS
      "browserUserAgent", "debug", "requireReauth", "requireTwoFactor", 
      "serviceId", "serviceName", "spRequiredFactors", "trustedBrowser", 
      "twoFactorPass", "userBrowserUuid",
      "userIpAddress", "username",
      
      //for UI
      "twoFactorCode", "twoFactorCustomCode", "profileForOptin",
      "userIdOperatingOnName", "userIdOperatingOn", "email0", "colleagueLogin0Name", "colleagueLogin1Name",
      "colleagueLogin2Name", "colleagueLogin3Name", "colleagueLogin4Name", "phone0",
      "phoneVoice0", "phoneText0", 
      "phone1", "phoneVoice1", "phoneText1", "phone2", "phoneVoice2", "phoneText2",
      "tfBackdoorLoginName", "phoneIndex", "phoneType", "name", "id", "checkedApproval",
      
      //for status calls
      "status", "diagnosticType"
      );

  /**
   * construct with underlying request
   * @param theHttpServletRequest
   */
  public TfHttpServletRequest(HttpServletRequest theHttpServletRequest) {
    super(theHttpServletRequest);
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  @Override
  public String getParameter(String name) {
    return this.getParameterMap().get(name);
  }

  /**
   * @param name 
   * @return the boolean
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  public Boolean getParameterBoolean(String name) {
    return TwoFactorServerUtils.booleanObjectValue(this.getParameterMap().get(name));
  }

  /**
   * @param name 
   * @return the parameter long
   * @see javax.servlet.ServletRequestWrapper#getParameter(java.lang.String)
   */
  public Long getParameterLong(String name) {
    return TwoFactorServerUtils.longObjectValue(this.getParameterMap().get(name), true);
  }

  /** param map which doesnt return null */
  private Map<String, String> parameterMap = null;

  /** unused http params */
  private Set<String> unusedParams = null;

  /**
   * return unused params that arent in the list to ignore
   * @return the unused params
   */
  public Set<String> unusedParams() {
    //init stuff
    this.getParameterMap();
    return this.unusedParams;
  }
  
  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterMap()
   */
  @Override
  public Map<String, String> getParameterMap() {

    if (this.parameterMap == null) {
      boolean valuesProblem = false;
      Set<String> valuesProblemName = new LinkedHashSet<String>();
      Map<String, String> newMap = new LinkedHashMap<String, String>();
      Set<String> newUnusedParams = new LinkedHashSet<String>();

      Enumeration enumeration = super.getParameterNames();
      Set<String> paramsToIgnore = new HashSet<String>();
      {
        String paramsToIgnoreString = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.httpParamsToIgnore");
        if (!TwoFactorServerUtils.isBlank(paramsToIgnoreString)) {
          paramsToIgnore.addAll(TwoFactorServerUtils.splitTrimToList(paramsToIgnoreString, ","));
        }
      }
      if (enumeration != null) {
        while(enumeration.hasMoreElements()) {
          String paramName = (String)enumeration.nextElement();
          
          if (!validParamNames.contains(paramName)) {
            if (!paramsToIgnore.contains(paramName)) {
            newUnusedParams.add(paramName);
            }
            continue;
          }
          
          String[] values = super.getParameterValues(paramName);
          String value = null;
          if (values != null && values.length > 0) {
            
            //there is probably something wrong if multiple values detected
            if (values.length > 1) {
              valuesProblem = true;
              valuesProblemName.add(paramName);
            }
            value = values[0];
          }
          newMap.put(paramName, value);
        }
      }
      this.parameterMap = newMap;
      this.unusedParams = newUnusedParams;
      if (valuesProblem) {
        throw new TfRestInvalidRequest(
            "Multiple request parameter values where detected for key: " + TwoFactorServerUtils.toStringForLog(valuesProblemName)
                + ", when only one is expected");
      }
    }
    return this.parameterMap;
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterNames()
   */
  @Override
  public Enumeration getParameterNames() {
    return IteratorUtils.asEnumeration(this.getParameterMap().keySet().iterator());
  }

  /**
   * @see javax.servlet.ServletRequestWrapper#getParameterValues(java.lang.String)
   */
  @Override
  public String[] getParameterValues(String name) {
    if (this.getParameterMap().containsKey(name)) {
      return new String[]{this.getParameterMap().get(name)};
    }
    return null;
  }

}
