/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.exceptions;


/**
 *
 */
public class TfInvalidSecret extends RuntimeException {

  /**
   * 
   */
  public TfInvalidSecret() {
  }

  /**
   * @param message
   */
  public TfInvalidSecret(String message) {
    super(message);

  }

  /**
   * @param cause
   */
  public TfInvalidSecret(Throwable cause) {
    super(cause);

  }

  /**
   * @param message
   * @param cause
   */
  public TfInvalidSecret(String message, Throwable cause) {
    super(message, cause);

  }

}
