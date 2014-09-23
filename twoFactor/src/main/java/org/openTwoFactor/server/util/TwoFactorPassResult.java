/**
 * 
 */
package org.openTwoFactor.server.util;

import org.openTwoFactor.server.TwoFactorCheckPass;


/**
 * bean that holds if pass is correct, and which index is the next hotp index if it changed
 * @author mchyzer
 *
 */
public class TwoFactorPassResult {

  /**
   * message to log
   */
  private Class<? extends TwoFactorCheckPass> twoFactorCheckPassImplementation;
  
  /**
   * message to log
   * @return the logMessage
   */
  public Class<? extends TwoFactorCheckPass> getTwoFactorCheckPassImplementation() {
    return this.twoFactorCheckPassImplementation;
  }
  
  /**
   * message to log
   * @param twoFactorCheckPassImplementation1 the logMessage to set
   */
  public void setTwoFactorCheckPassImplementation(Class<? extends TwoFactorCheckPass> twoFactorCheckPassImplementation1) {
    this.twoFactorCheckPassImplementation = twoFactorCheckPassImplementation1;
  }

  /**
   * if this is a phone pass
   */
  private boolean phonePass = false;
  
  /**
   * if this is a phone pass
   * @return the phonePass
   */
  public boolean isPhonePass() {
    return this.phonePass;
  }
  
  /**
   * if this is a phone pass
   * @param phonePass1 the phonePass to set
   */
  public void setPhonePass(boolean phonePass1) {
    this.phonePass = phonePass1;
  }

  /**
   * if this is blank, then it was not a printed pass
   * update the next hotp index to be able to be used
   */
  private Long nextHotpIndex;
  
  /**
   * if this is blank, then it was not a token pass
   * update the next token index to be able to be used
   */
  private Long nextTokenIndex;
  
  /**
   * if this is blank, then it was not a token pass
   * update the next token index to be able to be used
   * @return the nextTokenIndex
   */
  public Long getNextTokenIndex() {
    return this.nextTokenIndex;
  }
  
  /**
   * if this is blank, then it was not a token pass
   * update the next token index to be able to be used
   * @param nextTokenIndex1 the nextTokenIndex to set
   */
  public void setNextTokenIndex(Long nextTokenIndex1) {
    this.nextTokenIndex = nextTokenIndex1;
  }

  /**
   * if this is blank, then it wasnt a totp 30 password
   * update the last time based time period millis div 30k that was successfully used
   */
  private Long lastTotp30TimestampUsed;
  
  /**
   * if this is blank, then it wasnt a totp 60 password
   * update the last time based time period millis div 60k that was successfully used
   */
  private Long lastTotp60TimestampUsed;
  
  
  /**
   * if this is blank, then it was a sequential password
   * update the last time based time period millis div 30k that was successfully used
   * @return the lastTotpTimestampUsed
   */
  public Long getLastTotp30TimestampUsed() {
    return this.lastTotp30TimestampUsed;
  }

  
  /**
   * if this is blank, then it was a sequential password
   * update the last time based time period millis div 30k that was successfully used
   * @param lastTotpPasswordUsed1 the lastTotpTimestampUsed to set
   */
  public void setLastTotp30TimestampUsed(Long lastTotpPasswordUsed1) {
    this.lastTotp30TimestampUsed = lastTotpPasswordUsed1;
  }

  /**
   * if this is blank, then it wasnt a totp60 password
   * update the last time based time period millis div 60k that was successfully used
   * @return the lastTotpTimestampUsed
   */
  public Long getLastTotp60TimestampUsed() {
    return this.lastTotp60TimestampUsed;
  }

  
  /**
   * if this is blank, then it wasnt a totp 60 password
   * update the last time based time period millis div 30k that was successfully used
   * @param lastTotpPasswordUsed1 the lastTotpTimestampUsed to set
   */
  public void setLastTotp60TimestampUsed(Long lastTotpPasswordUsed1) {
    this.lastTotp60TimestampUsed = lastTotpPasswordUsed1;
  }

  /**
   * whether the password is ok or not
   */
  private boolean passwordCorrect = false;

  /**
   * if this is blank, then it was a time based password
   * @return index
   */
  public Long getNextHotpIndex() {
    return this.nextHotpIndex;
  }

  /**
   * if this is blank, then it was a time based password
   * @param nextHotpIndex1
   */
  public void setNextHotpIndex(Long nextHotpIndex1) {
    this.nextHotpIndex = nextHotpIndex1;
  }

  /**
   * whether the password is ok or not
   * @return if correct
   */
  public boolean isPasswordCorrect() {
    return this.passwordCorrect;
  }

  /**
   * whether the password is ok or not
   * @param passwordCorrect1
   */
  public void setPasswordCorrect(boolean passwordCorrect1) {
    this.passwordCorrect = passwordCorrect1;
  }
  

}
