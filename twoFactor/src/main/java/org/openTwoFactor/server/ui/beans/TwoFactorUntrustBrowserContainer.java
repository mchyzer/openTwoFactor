/**
 * @author mchyzer
 * $Id: TwoFactorUntrustBrowserContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * container for untrusted browsers
 */
public class TwoFactorUntrustBrowserContainer {

  /**
   * number of browsers untrusted
   */
  private int numberOfBrowsers = -1;

  /**
   * number of browsers untrusted
   * @return the numberOfBrowsers
   */
  public int getNumberOfBrowsersWithoutUntrusting() {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    TwoFactorDaoFactory twoFactorDaoFactory = TwoFactorDaoFactory.getFactory();
    List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser()
      .retrieveTrustedByUserUuid(twoFactorUser.getUuid());
    
    return TwoFactorServerUtils.length(twoFactorBrowsers);
  }

  
  /**
   * number of browsers untrusted
   * @return the numberOfBrowsers
   */
  public int getNumberOfBrowsers() {
    
    if (this.numberOfBrowsers == -1) {
      
      TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
        twoFactorUser.setTwoFactorSecretTemp(null);
      
      if (StringUtils.isBlank(twoFactorUser.getTwoFactorSecret())) {
        
        this.numberOfBrowsers = 0;
        
      } else {
        
        TwoFactorDaoFactory twoFactorDaoFactory = TwoFactorDaoFactory.getFactory();
        List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser()
          .retrieveTrustedByUserUuid(twoFactorUser.getUuid());
        
        //untrust browsers since opting in, dont want orphans from last time
        for (TwoFactorBrowser twoFactorBrowser : twoFactorBrowsers) {
          twoFactorBrowser.setWhenTrusted(0);
          twoFactorBrowser.setTrustedBrowser(false);
          twoFactorBrowser.store(twoFactorDaoFactory);
        }

        this.numberOfBrowsers = TwoFactorServerUtils.length(twoFactorBrowsers);

      }

      
    }
    
    return this.numberOfBrowsers;
  }

  
  /**
   * number of browsers untrusted
   * @param numberOfBrowsers1 the numberOfBrowsers to set
   */
  public void setNumberOfBrowsers(int numberOfBrowsers1) {
    this.numberOfBrowsers = numberOfBrowsers1;
  }
  
}
