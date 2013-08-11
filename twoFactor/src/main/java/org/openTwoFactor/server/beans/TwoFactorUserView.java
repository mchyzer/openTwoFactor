/**
 * @author mchyzer
 * $Id: TwoFactorAuditView.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.util.Set;

import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;



/**
 * view on user view records to make them more human readable
 */
@SuppressWarnings("serial")
public class TwoFactorUserView extends TwoFactorHibernateBeanBase {

  /**
   * login id of first friend
   */
  private String colleagueLoginid0;
  
  /**
   * login id of second friend
   */
  private String colleagueLoginid1;

  /**
   * login id of third friend
   */
  private String colleagueLoginid2;

  /**
   * login id of fourth friend
   */
  private String colleagueLoginid3;

  /**
   * login id of fifth friend
   */
  private String colleagueLoginid4;

  /**
   * user uuid of first friend
   */
  private String colleagueUserUuid0;

  /**
   * user uuid of second friend
   */
  private String colleagueUserUuid1;

  /**
   * user uuid of third friend
   */
  private String colleagueUserUuid2;

  /**
   * user uuid of fourth friend
   */
  private String colleagueUserUuid3;

  /**
   * user uuid of fifth friend
   */
  private String colleagueUserUuid4;

  /**
   * millis since 1970 since colleagues were invited to opt out
   */
  private Long dateInvitedColleagues;

  /**
   * millis since 1970 that the phone code was sent
   */
  private Long datePhoneCodeSent;

  /**
   * first email of user
   */
  private String email0;

  /**
   * last totp timestamp of 60 second code used
   */
  private Long lastTotp60TimestampUsed;

  /**
   * last totp timestamp of 30 second code used
   */
  private Long lastTotpTimestampUsed;

  /**
   * loginid of user
   */
  private String loginid;

  /**
   * if the user is currently logged in
   */
  private Boolean optedIn;

  /**
   * first phone number
   */
  private String phone0;
  
  /**
   * second phone number
   */
  private String phone1;
  
  /**
   * third phone number
   */
  private String phone2;
  
  /**
   * encrypted phone code sent to user
   */
  private String phoneCodeEncrypted;
  
  /**
   * if first phone can text
   */
  private Boolean phoneIsText0;
  
  /**
   * if second phone can text
   */
  private Boolean phoneIsText1;
  
  /**
   * if third phone can text
   */
  private Boolean phoneIsText2;
  
  /**
   * if first phone can do voice
   */
  private Boolean phoneIsVoice0;
  
  /**
   * if second phone can do voice
   */
  private Boolean phoneIsVoice1;
  
  /**
   * if third phone can do voice
   */
  private Boolean phoneIsVoice2;
  
  /**
   * last sequential pass index given to user
   */
  private Long sequentialPassGivenToUser;
  
  /**
   * sequential pass index the user is on
   */
  private Long sequentialPassIndex;
  
  /**
   * abbreviated secret of the user
   */
  private String twoFactorSecretAbbr;
  
  /**
   * abbreviated temp secret that the user has not verified
   */
  private String twoFactorSecretTempAbbr;

    
  /**
   * login id of first friend
   * @return loginid
   */
  public String getColleagueLoginid0() {
    return this.colleagueLoginid0;
  }

  /**
   * login id of first friend
   * @param _colleagueLoginid0
   */
  public void setColleagueLoginid0(String _colleagueLoginid0) {
    this.colleagueLoginid0 = _colleagueLoginid0;
  }

  /**
   * login id of second friend
   * @return login id
   */
  public String getColleagueLoginid1() {
    return this.colleagueLoginid1;
  }

  /**
   * login id of second friend
   * @param _colleagueLoginid1
   */
  public void setColleagueLoginid1(String _colleagueLoginid1) {
    this.colleagueLoginid1 = _colleagueLoginid1;
  }

  /**
   * login id of third friend
   * @return login id
   */
  public String getColleagueLoginid2() {
    return this.colleagueLoginid2;
  }

  /**
   * login id of third friend
   * @param _colleagueLoginid2
   */
  public void setColleagueLoginid2(String _colleagueLoginid2) {
    this.colleagueLoginid2 = _colleagueLoginid2;
  }

  /**
   * login id of fourth friend
   * @return login id
   */
  public String getColleagueLoginid3() {
    return this.colleagueLoginid3;
  }

  /**
   * login id of second friend
   * @param _colleagueLoginid3
   */
  public void setColleagueLoginid3(String _colleagueLoginid3) {
    this.colleagueLoginid3 = _colleagueLoginid3;
  }

  /**
   * login id of fifth friend
   * @return colleague id
   */
  public String getColleagueLoginid4() {
    return this.colleagueLoginid4;
  }

  
  public void setColleagueLoginid4(String colleagueLoginid4) {
    this.colleagueLoginid4 = colleagueLoginid4;
  }

  
  public String getColleagueUserUuid0() {
    return colleagueUserUuid0;
  }

  
  public void setColleagueUserUuid0(String colleagueUserUuid0) {
    this.colleagueUserUuid0 = colleagueUserUuid0;
  }

  
  public String getColleagueUserUuid1() {
    return colleagueUserUuid1;
  }

  
  public void setColleagueUserUuid1(String colleagueUserUuid1) {
    this.colleagueUserUuid1 = colleagueUserUuid1;
  }

  
  public String getColleagueUserUuid2() {
    return colleagueUserUuid2;
  }

  
  public void setColleagueUserUuid2(String colleagueUserUuid2) {
    this.colleagueUserUuid2 = colleagueUserUuid2;
  }

  
  public String getColleagueUserUuid3() {
    return colleagueUserUuid3;
  }

  
  public void setColleagueUserUuid3(String colleagueUserUuid3) {
    this.colleagueUserUuid3 = colleagueUserUuid3;
  }

  
  public String getColleagueUserUuid4() {
    return colleagueUserUuid4;
  }

  
  public void setColleagueUserUuid4(String colleagueUserUuid4) {
    this.colleagueUserUuid4 = colleagueUserUuid4;
  }

  
  public Long getDateInvitedColleagues() {
    return dateInvitedColleagues;
  }

  
  public void setDateInvitedColleagues(Long dateInvitedColleagues) {
    this.dateInvitedColleagues = dateInvitedColleagues;
  }

  
  public Long getDatePhoneCodeSent() {
    return datePhoneCodeSent;
  }

  
  public void setDatePhoneCodeSent(Long datePhoneCodeSent) {
    this.datePhoneCodeSent = datePhoneCodeSent;
  }

  
  public String getEmail0() {
    return email0;
  }

  
  public void setEmail0(String email0) {
    this.email0 = email0;
  }

  
  public Long getLastTotp60TimestampUsed() {
    return lastTotp60TimestampUsed;
  }

  
  public void setLastTotp60TimestampUsed(Long lastTotp60TimestampUsed) {
    this.lastTotp60TimestampUsed = lastTotp60TimestampUsed;
  }

  
  public Long getLastTotpTimestampUsed() {
    return lastTotpTimestampUsed;
  }

  
  public void setLastTotpTimestampUsed(Long lastTotpTimestampUsed) {
    this.lastTotpTimestampUsed = lastTotpTimestampUsed;
  }

  
  public String getLoginid() {
    return loginid;
  }

  
  public void setLoginid(String loginid) {
    this.loginid = loginid;
  }

  
  public Boolean getOptedIn() {
    return optedIn;
  }

  
  public void setOptedIn(Boolean optedIn) {
    this.optedIn = optedIn;
  }

  
  public String getPhone0() {
    return phone0;
  }

  
  public void setPhone0(String phone0) {
    this.phone0 = phone0;
  }

  
  public String getPhone1() {
    return phone1;
  }

  
  public void setPhone1(String phone1) {
    this.phone1 = phone1;
  }

  
  public String getPhone2() {
    return phone2;
  }

  
  public void setPhone2(String phone2) {
    this.phone2 = phone2;
  }

  
  public String getPhoneCodeEncrypted() {
    return phoneCodeEncrypted;
  }

  
  public void setPhoneCodeEncrypted(String phoneCodeEncrypted) {
    this.phoneCodeEncrypted = phoneCodeEncrypted;
  }

  
  public Boolean getPhoneIsText0() {
    return phoneIsText0;
  }

  
  public void setPhoneIsText0(Boolean phoneIsText0) {
    this.phoneIsText0 = phoneIsText0;
  }

  
  public Boolean getPhoneIsText1() {
    return phoneIsText1;
  }

  
  public void setPhoneIsText1(Boolean phoneIsText1) {
    this.phoneIsText1 = phoneIsText1;
  }

  
  public Boolean getPhoneIsText2() {
    return phoneIsText2;
  }

  
  public void setPhoneIsText2(Boolean phoneIsText2) {
    this.phoneIsText2 = phoneIsText2;
  }

  
  public Boolean getPhoneIsVoice0() {
    return phoneIsVoice0;
  }

  
  public void setPhoneIsVoice0(Boolean phoneIsVoice0) {
    this.phoneIsVoice0 = phoneIsVoice0;
  }

  
  public Boolean getPhoneIsVoice1() {
    return phoneIsVoice1;
  }

  
  public void setPhoneIsVoice1(Boolean phoneIsVoice1) {
    this.phoneIsVoice1 = phoneIsVoice1;
  }

  
  public Boolean getPhoneIsVoice2() {
    return phoneIsVoice2;
  }

  
  public void setPhoneIsVoice2(Boolean phoneIsVoice2) {
    this.phoneIsVoice2 = phoneIsVoice2;
  }

  
  public Long getSequentialPassGivenToUser() {
    return sequentialPassGivenToUser;
  }

  
  public void setSequentialPassGivenToUser(Long sequentialPassGivenToUser) {
    this.sequentialPassGivenToUser = sequentialPassGivenToUser;
  }

  
  public Long getSequentialPassIndex() {
    return sequentialPassIndex;
  }

  
  public void setSequentialPassIndex(Long sequentialPassIndex) {
    this.sequentialPassIndex = sequentialPassIndex;
  }

  
  public String getTwoFactorSecretAbbr() {
    return twoFactorSecretAbbr;
  }

  
  public void setTwoFactorSecretAbbr(String twoFactorSecretAbbr) {
    this.twoFactorSecretAbbr = twoFactorSecretAbbr;
  }

  
  public String getTwoFactorSecretTempAbbr() {
    return twoFactorSecretTempAbbr;
  }

  
  public void setTwoFactorSecretTempAbbr(String twoFactorSecretTempAbbr) {
    this.twoFactorSecretTempAbbr = twoFactorSecretTempAbbr;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#delete(org.openTwoFactor.server.hibernate.TwoFactorDaoFactory)
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {
    throw new RuntimeException("Cant delete a view!!!!");
  }
  
}
