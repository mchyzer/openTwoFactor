/**
 * @author mchyzer
 * $Id: TwoFactorProfileContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.subject.Subject;



/**
 * profile data
 */
public class TwoFactorProfileContainer {


  /** account name in QR code, e.g. jsmith@institution.edu */
  private String accountName = null;
  
  /**
   * account name in QR code, e.g. jsmith@institution.edu
   * @return account name
   */
  public String getAccountName() {
    
    if (this.accountName == null) {
      TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
      
      TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
      String accountSuffix = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.accountSuffix");
      //# you need an account suffix for qr codes.  e.g. institution.edu
      //twoFactorServer.accountEl = ${subject.getAttributeValue('pennname')}@test.upenn.edu
      String accountEl = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.accountEl");
      if (StringUtils.isBlank(accountSuffix) && StringUtils.isBlank(accountEl)) {
        throw new RuntimeException("You need to set twoFactorServer.accountSuffix or twoFactorServer.accountEl");
      }
      if (!StringUtils.isBlank(accountSuffix) && !StringUtils.isBlank(accountEl)) {
        throw new RuntimeException("You cant set both twoFactorServer.accountSuffix and twoFactorServer.accountEl");
      }
      if (StringUtils.isBlank(twoFactorUserLoggedIn.getLoginid())) {
        throw new RuntimeException("Why is login blank??? " + twoFactorUserLoggedIn.getUuid());
      }
      String theAccountName = null;
      
      //this will either be subjectId@accountSuffix, or a custom EL if you want to get your netId in there...
      
      if (!StringUtils.isBlank(accountSuffix)) {
        if (accountSuffix.startsWith("@")) {
          accountSuffix = TwoFactorServerUtils.prefixOrSuffix(accountSuffix, "@", false);
        }
        theAccountName = twoFactorUserLoggedIn.getLoginid() + "@" + accountSuffix;
      } else {
        //else we are doing el
        Subject subject =  TfSourceUtils.retrieveSubjectByIdOrIdentifier(TfSourceUtils.mainSource(), 
            twoFactorUserLoggedIn.getLoginid(), true, false, true);
  
        Map<String, Object> substituteMap = new HashMap<String, Object>();
        substituteMap.put("subject", subject);
        substituteMap.put("twoFactorRequestContainer", TwoFactorRequestContainer.retrieveFromRequest());
        substituteMap.put("request", TwoFactorFilterJ2ee.retrieveHttpServletRequest());
        
        theAccountName = TwoFactorServerUtils.substituteExpressionLanguage(accountEl, substituteMap, true, true, true);
  
      }
      this.accountName = theAccountName;
    }
    
    //accountName = "ameliaBedelia@upenn.edu";
    return this.accountName;
  }

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

  /**
   * colleague Name 0
   */
  private String colleagueName0;

  /**
   * colleague Name 1
   */
  private String colleagueName1;

  /**
   * colleague Name 2
   */
  private String colleagueName2;

  /**
   * colleague Name 3
   */
  private String colleagueName3;

  /**
   * colleague Name 4
   */
  private String colleagueName4;



  /**
   * colleague Name 0
   * @return colleague Name
   */
  public String getColleagueName0() {
    return this.colleagueName0;
  }


  /**
   * colleague Name
   * @param _colleagueName0
   */
  public void setColleagueName0(String _colleagueName0) {
    this.colleagueName0 = _colleagueName0;
  }


  /**
   * colleague Name 1
   * @return colleague Name
   */
  public String getColleagueName1() {
    return this.colleagueName1;
  }


  /**
   * colleague Name 1
   * @param _colleagueName1
   */
  public void setColleagueName1(String _colleagueName1) {
    this.colleagueName1 = _colleagueName1;
  }


  /**
   * colleague Name 2
   * @return colleague Name 
   */
  public String getColleagueName2() {
    return this.colleagueName2;
  }


  /**
   * colleague Name 2
   * @param _colleagueName2
   */
  public void setColleagueName2(String _colleagueName2) {
    this.colleagueName2 = _colleagueName2;
  }


  /**
   * colleague Name 3
   * @return colleague Name 
   */
  public String getColleagueName3() {
    return this.colleagueName3;
  }


  /**
   * colleague Name 3
   * @param _colleagueName3
   */
  public void setColleagueName3(String _colleagueName3) {
    this.colleagueName3 = _colleagueName3;
  }


  /**
   * colleague Name 4
   * @return colleague Name 
   */
  public String getColleagueName4() {
    return this.colleagueName4;
  }


  /**
   * colleague Name 4
   * @param _colleagueName4
   */
  public void setColleagueName4(String _colleagueName4) {
    this.colleagueName4 = _colleagueName4;
  }
  
}
