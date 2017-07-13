/**
 * @author mchyzer
 * $Id: TwoFactorProfileContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;


/**
 * optin data
 */
public class TwoFactorOptinContainer {

  /**
   * birth day submitted by screen
   */
  private int birthDaySubmitted = -1;
  
  /**
   * birth month submitted by screen
   */
  private int birthMonthSubmitted = -1;
  
  /**
   * birth year submitted by screen
   */
  private int birthYearSubmitted = -1;

  /**
   * if the optin wizard is in progress
   */
  private boolean optinWizardInProgress = false;
  
  /**
   * if the optin wizard is in progress
   * @return the optinWizardInProgress
   */
  public boolean isOptinWizardInProgress() {
    return this.optinWizardInProgress;
  }
  
  /**
   * if the optin wizard is in progress
   * @param optinWizardInProgress1 the optinWizardInProgress to set
   */
  public void setOptinWizardInProgress(boolean optinWizardInProgress1) {
    this.optinWizardInProgress = optinWizardInProgress1;
  }


  /**
   * birth day submitted by screen
   * @return the birthDaySubmitted
   */
  public int getBirthDaySubmitted() {
    return this.birthDaySubmitted;
  }

  
  /**
   * birth day submitted by screen
   * @param birthDaySubmitted1 the birthDaySubmitted to set
   */
  public void setBirthDaySubmitted(int birthDaySubmitted1) {
    this.birthDaySubmitted = birthDaySubmitted1;
  }

  
  /**
   * birth month submitted by screen
   * @return the birthMonthSubmitted
   */
  public int getBirthMonthSubmitted() {
    return this.birthMonthSubmitted;
  }

  
  /**
   * birth month submitted by screen
   * @param birthMonthSubmitted1 the birthMonthSubmitted to set
   */
  public void setBirthMonthSubmitted(int birthMonthSubmitted1) {
    this.birthMonthSubmitted = birthMonthSubmitted1;
  }

  
  /**
   * birth year submitted by screen
   * @return the birthYearSubmitted
   */
  public int getBirthYearSubmitted() {
    return this.birthYearSubmitted;
  }

  
  /**
   * birth year submitted by screen
   * @param birthYearSubmitted1 the birthYearSubmitted to set
   */
  public void setBirthYearSubmitted(int birthYearSubmitted1) {
    this.birthYearSubmitted = birthYearSubmitted1;
  }
  
  
  
}
