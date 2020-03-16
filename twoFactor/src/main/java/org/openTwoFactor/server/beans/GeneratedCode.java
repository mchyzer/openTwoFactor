/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.beans;


/**
 *
 */
public class GeneratedCode {

  /**
   * millis since 1970 when this code was sent
   */
  private long sent;

  /**
   * millis since 1970 when this code was sent
   * @return the sent
   */
  public long getSent() {
    return this.sent;
  }
  
  /**
   * millis since 1970 when this code was sent
   * @param sent1 the sent to set
   */
  public void setSent(long sent1) {
    this.sent = sent1;
  }

  /**
   * encrypted code
   */
  private String code;
  
  /**
   * encrypted code
   * @return the code
   */
  public String getCode() {
    return this.code;
  }
  
  /**
   * encrypted code
   * @param code1 the code to set
   */
  public void setCode(String code1) {
    this.code = code1;
  }

  /**
   * 
   */
  public GeneratedCode() {
  }

}
