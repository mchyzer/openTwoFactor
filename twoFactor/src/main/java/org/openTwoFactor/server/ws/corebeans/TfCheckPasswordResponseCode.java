/**
 * @author mchyzer
 * $Id: TfCheckPasswordResponseCode.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.corebeans;


/**
 * 
 */
public enum TfCheckPasswordResponseCode {

  /**
   * if there was an error processing the request
   */
  ERROR,

  /**
   * if the browser is trusted and no pass was sent in
   */
  TRUSTED_BROWSER,

  /**
   * if no pass was sent in, but it is required
   */
  TWO_FACTOR_REQUIRED,

  /**
   * if no pass was sent in, but it is required
   */
  TWO_FACTOR_FORBIDDEN,

  /**
   * if the password is correct
   */
  CORRECT_PASSWORD,

  /**
   * if password sent is invalid
   */
  WRONG_PASSWORD,

  /**
   * if the request is not valid
   */
  INVALID_REQUEST,

  /**
   * if the user is enrolled
   */
  USER_NOT_ENROLLED,
  

  /**
   * if the user is enrolled only for apps which require it 
   */
  USER_ENROLLED_FOR_REQUIRED_APPS;

}
