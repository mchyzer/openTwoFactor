/**
 * @author mchyzer
 * $Id: TwoFactorOneTimePassRow.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;


/**
 * row of one time passes
 */
public class TwoFactorOneTimePassRow {

  /** col1 one time pass */
  private String oneTimePassCol1;

  /** col2 one time pass */
  private String oneTimePassCol2;

  
  /**
   * col1 one time pass
   * @return the oneTimePassCol1
   */
  public String getOneTimePassCol1() {
    return this.oneTimePassCol1;
  }

  
  /**
   * col1 one time pass
   * @param oneTimePassCol1_ the oneTimePassCol1 to set
   */
  public void setOneTimePassCol1(String oneTimePassCol1_) {
    this.oneTimePassCol1 = oneTimePassCol1_;
  }

  
  /**
   * col2 one time pass
   * @return the oneTimePassCol2
   */
  public String getOneTimePassCol2() {
    return this.oneTimePassCol2;
  }

  
  /**
   * col2 one time pass
   * @param oneTimePassCol2_ the oneTimePassCol2 to set
   */
  public void setOneTimePassCol2(String oneTimePassCol2_) {
    this.oneTimePassCol2 = oneTimePassCol2_;
  }
  
  
  
}
