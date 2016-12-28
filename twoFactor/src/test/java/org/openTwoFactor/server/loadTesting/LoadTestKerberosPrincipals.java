/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.loadTesting;

import junit.framework.TestCase;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.dao.HibernateDaoFactory;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.ui.serviceLogic.UiMain;
import org.openTwoFactor.server.ui.serviceLogic.UiMain.OptinTestSubmitView;
import org.openTwoFactor.server.ui.serviceLogic.UiMain.OptinView;
import org.openTwoFactor.server.util.TfSourceUtils;


/**
 *
 */
public class LoadTestKerberosPrincipals extends TestCase {

  /**
   * 
   */
  public LoadTestKerberosPrincipals() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    loadTestKerberosPrincipals();
  }

  /**
   * 
   */
  public static void loadTestKerberosPrincipals() {


    String secretUnencrypted = "OLTI4H3XEQUBQGH6";
    String ipAddress = "130.91.219.176";

    for (int i=0;i<200;i++) {
      String userId = "u" + StringUtils.leftPad(Integer.toString(i), 3, '0') + "/cosignloadtest";
      try {
        
        String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
    
        TwoFactorUser twoFactorUserLoggedIn = null;
        
        TwoFactorRequestContainer.storeInThreadLocalForTesting(true);
        TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
        
        //############################ test that no loginid is an error
        TwoFactorDaoFactory daoFactory = HibernateDaoFactory.getFactory();
        
        //############################ the rest is for opted in, so opt the user in...
    
        TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
        
        //first we need to optin step 1
        twoFactorRequestContainer.setError(null);

        twoFactorRequestContainer.init(daoFactory, userId);
    
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        twoFactorUser.setPhone0("215 456 7890");
        twoFactorUser.setPhoneIsText0(false);
        twoFactorUser.setPhoneIsVoice0(true);
        twoFactorUser.setPhone1("215 456 7891");
        twoFactorUser.setPhoneIsText1(false);
        twoFactorUser.setPhoneIsVoice1(true);
        twoFactorUser.setPhone2("215 456 7892");
        twoFactorUser.setPhoneIsText2(true);
        twoFactorUser.setPhoneIsVoice2(false);

        twoFactorUser.store(daoFactory);
        
        if (twoFactorUser.isOptedIn()) {
          new UiMain().optoutLogic(daoFactory, twoFactorRequestContainer, userId, ipAddress, userAgent1, TfSourceUtils.mainSource());
        }
        
        OptinView optinView = new UiMain().optinLogic(daoFactory, 
            twoFactorRequestContainer, userId, ipAddress, userAgent1, TfSourceUtils.mainSource());
        
        assertEquals(OptinView.optin, optinView);
        
        twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(secretUnencrypted);
        twoFactorUserLoggedIn.setTwoFactorSecretUnencrypted(secretUnencrypted);
        twoFactorUserLoggedIn.store(daoFactory);
        
        String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
        
        byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
        
        long timeDiv30 = System.currentTimeMillis()/30000;
        
        {
          int pass = twoFactorLogic.totpPassword(key, timeDiv30-3);
            
          twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
          twoFactorUserLoggedIn.store(daoFactory);
          
          String passString = Integer.toString(pass);
          passString = StringUtils.leftPad(passString, 6, '0');
        
          twoFactorRequestContainer.setError(null);
      
          OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(daoFactory, 
              twoFactorRequestContainer, userId, ipAddress, userAgent1, passString, TfSourceUtils.mainSource(), null, false);
        
          assertEquals(optinTestSubmitView.name() 
              + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
              OptinTestSubmitView.optinSuccess, optinTestSubmitView);
      
          assertTrue(twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
      
          assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
          assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
        }
        TwoFactorRequestContainer.storeInThreadLocalForTesting(false);

      } finally {
        TwoFactorRequestContainer.storeInThreadLocalForTesting(false);
      }
      
    }
  }
  
}
