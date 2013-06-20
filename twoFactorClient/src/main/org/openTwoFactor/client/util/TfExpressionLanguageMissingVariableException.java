package org.openTwoFactor.client.util;


/**
 * @author mchyzer This class is thrown there is a missing variable in EL
 * @version $Id: NotConcurrentRevisionException.java,v 1.1 2004/05/02 05:14:59
 *          mchyzer Exp $
 */
@SuppressWarnings("serial")
public class TfExpressionLanguageMissingVariableException extends RuntimeException {

  /**
   *  
   */
  public TfExpressionLanguageMissingVariableException() {
    super();
  }

  /**
   * @param s
   */
  public TfExpressionLanguageMissingVariableException(String s) {
    super(s);
  }

  /**
   * @param message
   * @param cause
   */
  public TfExpressionLanguageMissingVariableException(String message, Throwable cause) {
    super(message, cause);
    
  }

  /**
   * @param cause
   */
  public TfExpressionLanguageMissingVariableException(Throwable cause) {
    super(cause);
    
  }
}