/**
 * @author mchyzer
 * $Id: Loadtest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.loadTesting;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.ui.serviceLogic.UiMain;
import org.openTwoFactor.server.ui.serviceLogic.UiMain.OptinTestSubmitView;



/**
 * load test the system and database
 */
public class Loadtest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    loadData();
  }
  
  /**
   * 
   */
  public static void loadData() {
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";

    TwoFactorUser twoFactorUserLoggedIn = null;
    
    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();

    TwoFactorDaoFactory daoFactory = TwoFactorDaoFactory.getFactory();
    
    for (int i=1000;i<1001;i++) {

      TwoFactorRequestContainer twoFactorRequestContainer = new TwoFactorRequestContainer();

      String loggedInUser = "jsmith" + i;
      new UiMain().optinLogic(daoFactory, 
          twoFactorRequestContainer, loggedInUser, "130.91.219.176", userAgent1, null);
      
      twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
      
      byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
      
      long timeDiv30 = System.currentTimeMillis()/30000;
      
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30);

        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(daoFactory);

        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');

        twoFactorRequestContainer.setError(null);

        OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(daoFactory, 
            twoFactorRequestContainer, loggedInUser, "130.91.219.176", userAgent1, passString);

        if (OptinTestSubmitView.optinSuccess != optinTestSubmitView) {
          throw new RuntimeException(optinTestSubmitView.name() 
            + ", " + twoFactorRequestContainer.getError() + ", " + passString);
        }
    
        if (!StringUtils.isBlank(twoFactorRequestContainer.getError())) {
          throw new RuntimeException(twoFactorRequestContainer.getError());
        }

      }
    }
    
  }
  
}
