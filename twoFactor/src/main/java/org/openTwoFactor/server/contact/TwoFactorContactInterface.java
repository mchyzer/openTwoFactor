/**
 * @author mchyzer
 * $Id: TwoFactorContactInterface.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.contact;


/**
 * contact an individual
 */
public interface TwoFactorContactInterface {

  /**
   * call a voice number
   * @param phoneNumber
   * @param message
   */
  public void voice(String phoneNumber, String message);


  /**
   * send a text
   * @param phoneNumber
   * @param message
   */
  public void text(String loginid, String phoneNumber, String message);

}
