/**
 * @author mchyzer
 * $Id: TfAjaxException.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.exceptions;


/**
 * ajax exception should be passed to browser
 */
public class TfAjaxException extends RuntimeException {

  /**
   * 
   */
  public TfAjaxException() {
    super();
    
  }

  /**
   * @param message
   * @param cause
   */
  public TfAjaxException(String message, Throwable cause) {
    super(message, cause);
    
  }

  /**
   * @param message
   */
  public TfAjaxException(String message) {
    super(message);
    
  }

  /**
   * @param cause
   */
  public TfAjaxException(Throwable cause) {
    super(cause);
    
  }

}
