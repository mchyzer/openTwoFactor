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
    
    //TwoFactorClient.main(new String[]{"--operation=validatePasswordWs", "--twoFactorPass=489384", "--username=mchyzer",
    //   "--format=xml", "--debug=true", "--userBrowserUuid=0bb2368adb294254841d9db7a12a720c" });
    
    TwoFactorClient.main(new String[]{"--operation=validatePasswordWs", "--username=mchyzer", 
        "--format=xml", "--debug=true", "--userBrowserUuid=0bb2368adb294254841d9db7a12a720c" });
    
  }
  
}
