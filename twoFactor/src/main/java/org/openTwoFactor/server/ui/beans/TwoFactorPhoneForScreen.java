/**
 * @author mchyzer
 * $Id: TwoFactorPhoneForScreen.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;


/**
 * phone bean for screen
 */
public class TwoFactorPhoneForScreen {

  /**
   * if this bean has a phone number
   */
  private boolean hasPhone;
  
  /**
   * if this bean has a phone number
   * @return the hasPhone
   */
  public boolean isHasPhone() {
    return this.hasPhone;
  }
  
  /**
   * if this bean has a phone number
   * @param hasPhone1 the hasPhone to set
   */
  public void setHasPhone(boolean hasPhone1) {
    this.hasPhone = hasPhone1;
  }

  /**
   * phone number for screen, e.g. 21* *** **45
   */
  private String phoneForScreen;
  
  /**
   * if this phone number can be used for text
   */
  private boolean text;

  /**
   * if this phone number can be used for voice
   */
  private boolean voice;
  
  /**
   * phone number for screen, e.g. 21* *** **45
   * @return the phoneForScreen
   */
  public String getPhoneForScreen() {
    return this.phoneForScreen;
  }

  
  /**
   * phone number for screen, e.g. 21* *** **45
   * @param phoneForScreen1 the phoneForScreen to set
   */
  public void setPhoneForScreen(String phoneForScreen1) {
    this.phoneForScreen = phoneForScreen1;
  }
  
  /**
   * if this phone number can be used for text
   * @return the text
   */
  public boolean isText() {
    return this.text;
  }
  
  /**
   * if this phone number can be used for text
   * @param text1 the text to set
   */
  public void setText(boolean text1) {
    this.text = text1;
  }
  
  /**
   * if this phone number can be used for voice
   * @return the voice
   */
  public boolean isVoice() {
    return this.voice;
  }
  
  /**
   * if this phone number can be used for voice
   * @param voice1 the voice to set
   */
  public void setVoice(boolean voice1) {
    this.voice = voice1;
  }
}
