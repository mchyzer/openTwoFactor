/**
 * @author mchyzer
 * $Id: TwoFactorHelpLoggingInContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.beans;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorUser;


/**
 * help logging in data
 */
public class TwoFactorHelpLoggingInContainer {

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
   * phone numbers for screen: 21* *** **34
   */
  private List<TwoFactorPhoneForScreen> phonesForScreen;
  
  /**
   * phone numbers for screen: 21* *** **34
   * @return the phoneNumbersForScreen
   */
  public List<TwoFactorPhoneForScreen> getPhonesForScreen() {
    return this.phonesForScreen;
  }
  
  /**
   * phone numbers for screen: 21* *** **34
   * @param phonesForScreen1 the phoneNumbersForScreen to set
   */
  public void setPhonesForScreen(List<TwoFactorPhoneForScreen> phonesForScreen1) {
    this.phonesForScreen = phonesForScreen1;
  }

  /**
   * if the user has phone numbers to help unlock account
   */
  private boolean hasPhoneNumbers;
  
  /**
   * if the user has phone numbers to help unlock account
   * @return the hasPhoneNumbers
   */
  public boolean isHasPhoneNumbers() {
    return this.hasPhoneNumbers;
  }
  
  /**
   * if the user has phone numbers to help unlock account
   * @param hasPhoneNumbers1 the hasPhoneNumbers to set
   */
  public void setHasPhoneNumbers(boolean hasPhoneNumbers1) {
    this.hasPhoneNumbers = hasPhoneNumbers1;
  }

  /**
   * which users have identified this user as someone who can opt them out
   */
  private List<TwoFactorUser> colleaguesIdentifiedUser;
  
  /**
   * which users have identified this user as someone who can opt them out
   * @return the colleaguesIdentifiedUser
   */
  public List<TwoFactorUser> getColleaguesIdentifiedUser() {
    return this.colleaguesIdentifiedUser;
  }
  
  /**
   * which users have identified this user as someone who can opt them out
   * @param colleaguesIdentifiedUser1 the colleaguesIdentifiedUser to set
   */
  public void setColleaguesIdentifiedUser(List<TwoFactorUser> colleaguesIdentifiedUser1) {
    this.colleaguesIdentifiedUser = colleaguesIdentifiedUser1;
  }

  /**
   * if there are colleagues authorized by the user
   */
  private boolean hasColleaguesAuthorizedUser;

  /**
   * if there are colleagues not authorized by the user
   */
  private boolean hasColleaguesNotAuthorizedUser;
  
  /**
   * if there are colleagues authorized by the user
   * @return if there are colleagues authorized by the user
   */
  public boolean isHasColleaguesAuthorizedUser() {
    return this.hasColleaguesAuthorizedUser;
  }

  /**
   * if there are colleagues authorized by the user
   * @param hasColleaguesAuthorizedUser1
   */
  public void setHasColleaguesAuthorizedUser(boolean hasColleaguesAuthorizedUser1) {
    this.hasColleaguesAuthorizedUser = hasColleaguesAuthorizedUser1;
  }

  /**
   * if there are colleagues authorized by the user
   * @return if there are colleagues authorized by the user
   */
  public boolean isHasColleaguesNotAuthorizedUser() {
    return this.hasColleaguesNotAuthorizedUser;
  }

  /**
   * if there are colleagues authorized by the user
   * @param hasColleaguesNotAuthorizedUser1
   */
  public void setHasColleaguesNotAuthorizedUser(boolean hasColleaguesNotAuthorizedUser1) {
    this.hasColleaguesNotAuthorizedUser = hasColleaguesNotAuthorizedUser1;
  }

  /**
   * if a colleague has identified this user
   * as someone who can opt them out
   */
  private boolean hasColleaguesIdentifiedUser;
  
  /**
   * if a colleague has identified this user
   * as someone who can opt them out
   * @return the hasColleaguesIdentifiedUser
   */
  public boolean isHasColleaguesIdentifiedUser() {
    return this.hasColleaguesIdentifiedUser;
  }
  
  /**
   * if a colleague has identified this user
   * as someone who can opt them out
   * @param hasColleaguesIdentifiedUser1 the hasColleaguesIdentifiedUser to set
   */
  public void setHasColleaguesIdentifiedUser(boolean hasColleaguesIdentifiedUser1) {
    this.hasColleaguesIdentifiedUser = hasColleaguesIdentifiedUser1;
  }

  /**
   * if the user has invited colleagues
   */
  private boolean invitedColleagues;
  
  /**
   * if the user has invited colleagues
   * @return the invitedColleagues
   */
  public boolean isInvitedColleagues() {
    return this.invitedColleagues;
  }
  
  /**
   * if the user has invited colleagues
   * @param invitedColleagues1 the invitedColleagues to set
   */
  public void setInvitedColleagues(boolean invitedColleagues1) {
    this.invitedColleagues = invitedColleagues1;
  }

  /**
   * if the user has colleague ids
   */
  private boolean hasColleagueLoginids;

  /**
   * if the user has colleague ids
   * @return the hasColleagueLoginids
   */
  public boolean isHasColleagueLoginids() {
    return this.hasColleagueLoginids;
  }

  /**
   * if the user has colleague ids
   * @param hasColleagueLoginids1 the hasColleagueLoginids to set
   */
  public void setHasColleagueLoginids(boolean hasColleagueLoginids1) {
    this.hasColleagueLoginids = hasColleagueLoginids1;
  }

  /**
   * display the colleague loginids like this: b** or wb*****  (if more than 5 chars)
   */
  private List<String> colleagueLoginidsForScreen;
  
  /**
   * display the colleague loginids like this: b** or wb*****  (if more than 5 chars)
   * @return the colleagueLoginidsForScreen
   */
  public List<String> getColleagueLoginidsForScreen() {
    return this.colleagueLoginidsForScreen;
  }

  
  /**
   * display the colleague loginids like this: b** or wb*****  (if more than 5 chars)
   * @param colleagueLoginidsForScreen1 the colleagueLoginidsForScreen to set
   */
  public void setColleagueLoginidsForScreen(List<String> colleagueLoginidsForScreen1) {
    this.colleagueLoginidsForScreen = colleagueLoginidsForScreen1;
  }
  
  
}
