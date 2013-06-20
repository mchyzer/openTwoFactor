/**
 * @author mchyzer
 * $Id: TwoFactorUntrustBrowserContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;


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
  public int getNumberOfBrowsers() {
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
