/**
 * @author mchyzer
 * $Id: TwoFactorClientMain.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client;


/**
 *
 */
public class TwoFactorClientMain {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    TwoFactorClient.main(new String[]{"--operation=validatePasswordWs", "--username=mchyzer",
       "--format=xml" });
    
  }
  
}
