/**
 * @author mchyzer
 * $Id: TwoFactorProfileContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;



/**
 * profile data
 */
public class TwoFactorProfileContainer {

  /**
   * friend object for sending email
   */
  private TwoFactorUser twoFactorUserFriend;
  
  
  
  /**
   * friend object for sending email
   * @return the friend user object
   */
  public TwoFactorUser getTwoFactorUserFriend() {
    return this.twoFactorUserFriend;
  }


  /**
   * friend object for sending email
   * @param twoFactorUserFriend1
   */
  public void setTwoFactorUserFriend(TwoFactorUser twoFactorUserFriend1) {
    this.twoFactorUserFriend = twoFactorUserFriend1;
  }

  /**
   * this is the friend, phone, etc label for what has the problem
   */
  private String errorFieldLabel;
  
  /**
   * this is the friend, phone, etc label for what has the problem
   * @return the errorFieldLabel
   */
  public String getErrorFieldLabel() {
    return this.errorFieldLabel;
  }

  
  /**
   * this is the friend, phone, etc label for what has the problem
   * @param errorFieldLabel1 the errorFieldLabel to set
   */
  public void setErrorFieldLabel(String errorFieldLabel1) {
    this.errorFieldLabel = errorFieldLabel1;
  }

  /**
   * if this is the profile screen for optin reasons
   */
  private boolean profileForOptin;
  
  /**
   * if this is the profile screen for optin reasons
   * @return the profileForOptin
   */
  public boolean isProfileForOptin() {
    return this.profileForOptin;
  }

  /**
   * if this is the profile screen for optin reasons
   * @param profileForOptin1 the profileForOptin to set
   */
  public void setProfileForOptin(boolean profileForOptin1) {
    this.profileForOptin = profileForOptin1;
  }

  /**
   * if has phone 0
   * @return if
   */
  public boolean isHasPhone0() {
    return !StringUtils.isBlank(this.phone0);
  }

  /**
   * if has phone 1
   * @return if
   */
  public boolean isHasPhone1() {
    return !StringUtils.isBlank(this.phone1);
  }

  /**
   * if has phone 2
   * @return if
   */
  public boolean isHasPhone2() {
    return !StringUtils.isBlank(this.phone2);
  }

  /**
   * the user being operated on
   */
  private TwoFactorUser twoFactorUserOperatingOn;
  
  /**
   * the user being operated on
   * @return the twoFactorUserOperatingOn
   */
  public TwoFactorUser getTwoFactorUserOperatingOn() {
    return this.twoFactorUserOperatingOn;
  }
  
  /**
   * the user being operated on
   * @param twoFactorUserOperatingOn1 the twoFactorUserOperatingOn to set
   */
  public void setTwoFactorUserOperatingOn(TwoFactorUser twoFactorUserOperatingOn1) {
    this.twoFactorUserOperatingOn = twoFactorUserOperatingOn1;
  }

  /**
   * email from/to screen
   */
  private String email0;
  
  /**
   * email from/to screen
   * @return the email0
   */
  public String getEmail0() {
    return this.email0;
  }

  
  /**
   * email from/to screen
   * @param email0_ the email0 to set
   */
  public void setEmail0(String email0_) {
    this.email0 = email0_;
  }

  /**
   * colleague description 0
   */
  private String colleagueDescription0;

  /**
   * colleague description 1
   */
  private String colleagueDescription1;

  /**
   * colleague description 2
   */
  private String colleagueDescription2;

  /**
   * colleague description 3
   */
  private String colleagueDescription3;

  /**
   * colleague description 4
   */
  private String colleagueDescription4;

  /**
   * colleague description 0
   * @return colleague description 0
   */
  public String getColleagueDescription0() {
    return this.colleagueDescription0;
  }

  /**
   * colleague description 0
   * @param colleagueDescription0_
   */
  public void setColleagueDescription0(String colleagueDescription0_) {
    this.colleagueDescription0 = colleagueDescription0_;
  }

  /**
   * colleague description 1
   * @return desc
   */
  public String getColleagueDescription1() {
    return this.colleagueDescription1;
  }

  /**
   * colleague description 1
   * @param colleagueDescription1_
   */
  public void setColleagueDescription1(String colleagueDescription1_) {
    this.colleagueDescription1 = colleagueDescription1_;
  }

  /**
   * colleague description 2
   * @return desc
   */
  public String getColleagueDescription2() {
    return this.colleagueDescription2;
  }

  /**
   * colleague description 2
   * @param colleagueDescription2_
   */
  public void setColleagueDescription2(String colleagueDescription2_) {
    this.colleagueDescription2 = colleagueDescription2_;
  }

  /**
   * colleague description 3
   * @return desc
   */
  public String getColleagueDescription3() {
    return this.colleagueDescription3;
  }

  /**
   * colleague desc 3
   * @param colleagueDescription3_
   */
  public void setColleagueDescription3(String colleagueDescription3_) {
    this.colleagueDescription3 = colleagueDescription3_;
  }

  /**
   * colleague desc 4
   * @return desc
   */
  public String getColleagueDescription4() {
    return this.colleagueDescription4;
  }

  /**
   * colleague desc 4
   * @param colleagueDescription4_
   */
  public void setColleagueDescription4(String colleagueDescription4_) {
    this.colleagueDescription4 = colleagueDescription4_;
  }

  /**
   * colleague login id
   */
  private String colleagueLogin0;

  /**
   * colleague login id
   */
  private String colleagueLogin1;

  /**
   * colleague login id
   */
  private String colleagueLogin2;

  /**
   * colleague login id
   */
  private String colleagueLogin3;

  /**
   * colleague login id
   */
  private String colleagueLogin4; 

  
  /**
   * colleague login id
   * @return the colleagueLogin0
   */
  public String getColleagueLogin0() {
    return this.colleagueLogin0;
  }

  
  /**
   * colleague login id
   * @param colleagueLogin0_ the colleagueLogin0 to set
   */
  public void setColleagueLogin0(String colleagueLogin0_) {
    this.colleagueLogin0 = colleagueLogin0_;
  }

  
  /**
   * colleague login id
   * @return the colleagueLogin1
   */
  public String getColleagueLogin1() {
    return this.colleagueLogin1;
  }

  
  /**
   * colleague login id
   * @param colleagueLogin1_ the colleagueLogin1 to set
   */
  public void setColleagueLogin1(String colleagueLogin1_) {
    this.colleagueLogin1 = colleagueLogin1_;
  }

  
  /**
   * colleague login id
   * @return the colleagueLogin2
   */
  public String getColleagueLogin2() {
    return this.colleagueLogin2;
  }

  
  /**
   * colleague login id
   * @param colleagueLogin2_ the colleagueLogin2 to set
   */
  public void setColleagueLogin2(String colleagueLogin2_) {
    this.colleagueLogin2 = colleagueLogin2_;
  }

  
  /**
   * colleague login id
   * @return the colleagueLogin3
   */
  public String getColleagueLogin3() {
    return this.colleagueLogin3;
  }

  
  /**
   * colleague login id
   * @param colleagueLogin3_ the colleagueLogin3 to set
   */
  public void setColleagueLogin3(String colleagueLogin3_) {
    this.colleagueLogin3 = colleagueLogin3_;
  }

  
  /**
   * colleague login id
   * @return the colleagueLogin4
   */
  public String getColleagueLogin4() {
    return this.colleagueLogin4;
  }

  
  /**
   * colleague login id
   * @param colleagueLogin4_ the colleagueLogin4 to set
   */
  public void setColleagueLogin4(String colleagueLogin4_) {
    this.colleagueLogin4 = colleagueLogin4_;
  }

  /**
   * phone from screen
   */
  private String phone0;
  
  /**
   * phone voice checkbox
   */
  private String phoneVoice0;

  /**
   * phone text checkbox
   */
  private String phoneText0;
  
  
  /**
   * phone from screen
   * @return the phone0
   */
  public String getPhone0() {
    return this.phone0;
  }

  
  /**
   * phone from screen
   * @param phone0_ the phone0 to set
   */
  public void setPhone0(String phone0_) {
    this.phone0 = phone0_;
  }

  
  /**
   * phone voice checkbox
   * @return the phoneVoice0
   */
  public String getPhoneVoice0() {
    return this.phoneVoice0;
  }

  
  /**
   * phone voice checkbox
   * @param phoneVoice0_ the phoneVoice0 to set
   */
  public void setPhoneVoice0(String phoneVoice0_) {
    this.phoneVoice0 = phoneVoice0_;
  }

  
  /**
   * phone text checkbox
   * @return the phoneText0
   */
  public String getPhoneText0() {
    return this.phoneText0;
  }

  
  /**
   * phone text checkbox
   * @param phoneText0_ the phoneText0 to set
   */
  public void setPhoneText0(String phoneText0_) {
    this.phoneText0 = phoneText0_;
  }

  
  /**
   * phone from screen
   * @return the phone1
   */
  public String getPhone1() {
    return this.phone1;
  }

  
  /**
   * phone from screen
   * @param phone1_ the phone1 to set
   */
  public void setPhone1(String phone1_) {
    this.phone1 = phone1_;
  }

  
  /**
   * phone voice checkbox
   * @return the phoneVoice1
   */
  public String getPhoneVoice1() {
    return this.phoneVoice1;
  }

  
  /**
   * phone voice checkbox
   * @param phoneVoice1_ the phoneVoice1 to set
   */
  public void setPhoneVoice1(String phoneVoice1_) {
    this.phoneVoice1 = phoneVoice1_;
  }

  
  /**
   * phone text checkbox
   * @return the phoneText1
   */
  public String getPhoneText1() {
    return this.phoneText1;
  }

  
  /**
   * phone text checkbox
   * @param phoneText1_ the phoneText1 to set
   */
  public void setPhoneText1(String phoneText1_) {
    this.phoneText1 = phoneText1_;
  }

  
  /**
   * phone from screen
   * @return the phone2
   */
  public String getPhone2() {
    return this.phone2;
  }

  
  /**
   * phone from screen
   * @param phone2_ the phone2 to set
   */
  public void setPhone2(String phone2_) {
    this.phone2 = phone2_;
  }

  
  /**
   * phone voice checkbox
   * @return the phoneVoice2
   */
  public String getPhoneVoice2() {
    return this.phoneVoice2;
  }

  
  /**
   * phone voice checkbox
   * @param phoneVoice2_ the phoneVoice2 to set
   */
  public void setPhoneVoice2(String phoneVoice2_) {
    this.phoneVoice2 = phoneVoice2_;
  }

  
  /**
   * phone text checkbox
   * @return the phoneText2
   */
  public String getPhoneText2() {
    return this.phoneText2;
  }

  
  /**
   * phone text checkbox
   * @param phoneText2_ the phoneText2 to set
   */
  public void setPhoneText2(String phoneText2_) {
    this.phoneText2 = phoneText2_;
  }

  /**
   * phone from screen
   */
  private String phone1;

  /**
   * phone voice checkbox
   */
  private String phoneVoice1;

  /**
   * phone text checkbox
   */
  private String phoneText1;

  /**
   * phone from screen
   */
  private String phone2;

  /**
   * phone voice checkbox
   */
  private String phoneVoice2;

  /**
   * phone text checkbox
   */
  private String phoneText2;
  
}
