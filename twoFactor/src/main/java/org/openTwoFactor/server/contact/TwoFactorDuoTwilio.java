/**
 * @author mchyzer
 * $Id: TwoFactorTwilio.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.contact;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * two factor contact interface for twilio, this is generally the primary method
 */
public class TwoFactorDuoTwilio extends TwoFactorTwilio  {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorDuoTwilio.class);

  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#text(java.lang.String, java.lang.String)
   */
  @Override
  public void text(String loginid, String phoneNumber, String text) {
    
    try {
    
      // dont worry about numbers to set
      DuoCommands.duoSendTextBySomeId(loginid, phoneNumber, false, 30);
    } catch (Exception e) {
      LOG.info("Non fatal problem sending text by duo. " + e.getMessage());
      super.text(loginid, phoneNumber, text);
    }
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) throws InterruptedException {
    
    String userText = "Your verification code is: 123456, Your verification code is: 123456, Your verification code is: 123456";
    
    //new TwoFactorTwilio().voice("215 880 9847", userText);
    //new TwoFactorTwilio().voice("514 448 2461", userText);
    
    // voice is ok with +
    // new TwoFactorTwilio().voice("+ 44 7445 860015", userText);   new Thread().sleep(20000);
    // text needs 011
    new TwoFactorDuoTwilio().text("10021368", "2158809847", userText);
    
  }

}
