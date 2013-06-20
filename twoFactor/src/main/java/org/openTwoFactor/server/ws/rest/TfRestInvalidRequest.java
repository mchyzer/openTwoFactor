/*
 * @author mchyzer $Id: TfRestInvalidRequest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.rest;

/**
 * exception when there is not a valid request from client
 * this must be called before any response is written
 */
public class TfRestInvalidRequest extends RuntimeException {

  /**
   * default id
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public TfRestInvalidRequest() {
    //empty constructor
  }

  /**
   * @param message
   */
  public TfRestInvalidRequest(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public TfRestInvalidRequest(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public TfRestInvalidRequest(String message, Throwable cause) {
    super(message, cause);
  }

}
