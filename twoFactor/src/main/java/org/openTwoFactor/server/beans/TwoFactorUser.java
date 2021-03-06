/**
 * @author mchyzer
 * $Id: TwoFactorUser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.TwoFactorAuthorizationInterface;
import org.openTwoFactor.server.beans.TwoFactorUserAttr.TwoFactorUserAttrName;
import org.openTwoFactor.server.beans.TwoFactorUserAttr.TwoFactorUserAttrType;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.encryption.EncryptionKey;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.ui.serviceLogic.UiMain;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.rest.TfRestLogic;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * two factor user
 */
@SuppressWarnings("serial")
public class TwoFactorUser extends TwoFactorHibernateBeanBase implements Comparable<TwoFactorUser> {

  /**
   * 
   * @return phones
   */
  public String getPhonesCommaSeparated() {
    StringBuilder result = new StringBuilder();
    
    if (!StringUtils.isBlank(this.getPhone0())) {
      result.append(this.getPhone0());
    }
    if (!StringUtils.isBlank(this.getPhone1())) {
      if (!StringUtils.isBlank(result.toString())) {
        result.append(", ");
      }
      result.append(this.getPhone1());
    }
    if (!StringUtils.isBlank(this.getPhone2())) {
      if (!StringUtils.isBlank(result.toString())) {
        result.append(", ");
      }
      result.append(this.getPhone2());
    }
    return result.toString();
  }
  
  /**
   * if has fob attached
   * @return true if has fob
   */
  public boolean isHasFob() {
    return !StringUtils.isBlank(this.getFobSerial());
  }
  
  /**
   * get the fob serial number
   * @return the serial number
   */
  public String getFobSerial() {
    Set<TwoFactorDeviceSerial> twoFactorDeviceSerials = TwoFactorDeviceSerial.retrieveByUserUuid(TwoFactorDaoFactory.getFactory(), this.getUuid());
    if (TwoFactorServerUtils.length(twoFactorDeviceSerials) > 0) {
      for (TwoFactorDeviceSerial twoFactorDeviceSerial : twoFactorDeviceSerials) {
        if (this.isOptedIn() && StringUtils.equals(this.getTwoFactorSecretUnencrypted(), twoFactorDeviceSerial.getTwoFactorSecretUnencrypted())) {
          return twoFactorDeviceSerial.getSerialNumber();
        }
      }
    }
    return null;
  }
  
  /**
   * people this user chose
   * @return the users who are colleagues of this user
   */
  public Set<TwoFactorUser> getColleagues() {
    Set<TwoFactorUser> colleagues = new TreeSet();
    if (!StringUtils.isBlank(this.getColleagueUserUuid0())) {
      
      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), this.getColleagueUserUuid0());
      twoFactorUser.setSubjectSource(this.subjectSource);
      colleagues.add(twoFactorUser);
    }
    if (!StringUtils.isBlank(this.getColleagueUserUuid1())) {
      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), this.getColleagueUserUuid1());
      twoFactorUser.setSubjectSource(this.subjectSource);
      colleagues.add(twoFactorUser);
    }
    if (!StringUtils.isBlank(this.getColleagueUserUuid2())) {
      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), this.getColleagueUserUuid2());
      twoFactorUser.setSubjectSource(this.subjectSource);
      colleagues.add(twoFactorUser);
    }
    if (!StringUtils.isBlank(this.getColleagueUserUuid3())) {
      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), this.getColleagueUserUuid3());
      twoFactorUser.setSubjectSource(this.subjectSource);
      colleagues.add(twoFactorUser);
    }
    if (!StringUtils.isBlank(this.getColleagueUserUuid4())) {
      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), this.getColleagueUserUuid4());
      twoFactorUser.setSubjectSource(this.subjectSource);
      colleagues.add(twoFactorUser);
    }
    return colleagues;
  }
  
  /**
   * get number of colleagues
   * @return number of colleagues
   */
  public int getNumberOfColleagues() {
    return TwoFactorServerUtils.length(this.getColleagues());
  }

  /**
   * get number of users who picked this user to opt them out
   * @return number of users who picked this user to opt them out
   */
  public int getNumberOfUsersWhoPickedThisUserToOptThemOut() {
    return TwoFactorServerUtils.length(this.getUsersWhoPickedThisUserToOptThemOut());
  }

  /**
   * users who picked this user to opt them out
   */
  private Set<TwoFactorUser> usersWhoPickedThisUserToOptThemOut;
  
  /**
   * users who picked this user to opt them out
   * @return colleagues who initiated the friendship
   */
  public Set<TwoFactorUser> getUsersWhoPickedThisUserToOptThemOut() {
    if (this.usersWhoPickedThisUserToOptThemOut == null) {
      Set<TwoFactorUser> theUsersWhoPickedThisUserToOptThemOut = new TreeSet<TwoFactorUser>();
      
      List<TwoFactorUser> retrieveUsersWhoPickedThisUserToOptThemOutList = this.twoFactorDaoFactory().getTwoFactorUser().retrieveUsersWhoPickedThisUserToOptThemOut(this.getUuid());
      
      for (TwoFactorUser twoFactorUser : retrieveUsersWhoPickedThisUserToOptThemOutList) {
        twoFactorUser.setSubjectSource(this.subjectSource);
      }

      theUsersWhoPickedThisUserToOptThemOut.addAll(retrieveUsersWhoPickedThisUserToOptThemOutList);
      
      this.usersWhoPickedThisUserToOptThemOut = theUsersWhoPickedThisUserToOptThemOut;
    }
    return this.usersWhoPickedThisUserToOptThemOut;
  }
  
  /**
   * get number of phones
   * @return number of phones
   */
  public int getNumberOfPhones() {
    int phoneCount = 0;
    if (!StringUtils.isBlank(this.getPhone0())) {
      phoneCount++;
    }
    if (!StringUtils.isBlank(this.getPhone1())) {
      phoneCount++;
    }
    if (!StringUtils.isBlank(this.getPhone2())) {
      phoneCount++;
    }
    
    return phoneCount;
    
  }
  
  /** subject source if need to lookup name / description */
  private Source subjectSource = null;

  /**
   * subject source if need to lookup name
   * @param subjectSource1
   */
  public void setSubjectSource(Source subjectSource1) {
    this.subjectSource = subjectSource1;
  }
  
  /**
   * name from subject source, or if not found, the loginid
   * 
   * @return the name from subject source, or if not found, the loginid
   */
  public String getName() {
    Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(this.subjectSource, this.getLoginid(), true, false, false);
    if (subject != null) {
      if (!StringUtils.isBlank(subject.getName())) {
        return subject.getName();
      }
    }
    return this.getLoginid();
  }
  
  /**
   * netId from subject source, or if not found, the loginid
   * 
   * @return the name from subject source, or if not found, the loginid
   */
  public String getNetId() {
    Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(this.subjectSource, this.getLoginid(), true, false, false);
    if (subject != null) {
      String netIdAttribute = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.subject.netIdAttribute");
      if (!StringUtils.isBlank(netIdAttribute)) {
        String netId = subject.getAttributeValue(netIdAttribute);
        if (!StringUtils.isBlank(netId)) {
          return netId;
        }
      }
    }
    return this.getLoginid();
  }
  

  /**
   * name from subject source, or if not found, the loginid
   * 
   * @return the name from subject source, or if not found, the loginid
   */
  public String getNameAdmin() {
    Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(this.subjectSource, this.getLoginid(), true, false, true);
    if (subject != null) {
      return TfSourceUtils.subjectName(subject, this.getLoginid());
    }
    return this.getLoginid();
  }
  
  /**
   * cache this for sorting
   */
  private String description = null;
  
  /**
   * description from subject source, or if not found, the loginid
   * 
   * @return the description from subject source, or if not found, the loginid
   */
  public String getDescription() {
    
    if (this.description == null) {
      
      Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(this.subjectSource, this.getLoginid(), true, false, false);
      if (subject != null) {
        this.description = TfSourceUtils.subjectDescription(subject);
      } else {
        this.description = this.getLoginid();
      }
    }
    return this.description;
  }
  
  /**
   * description from subject source, or if not found, the loginid
   * 
   * @return the description from subject source, or if not found, the loginid
   */
  public String getDescriptionAdmin() {
    Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(this.subjectSource, this.getLoginid(), true, false, true);
    if (subject != null) {
      return TfSourceUtils.subjectDescription(subject);
    }
    return this.getLoginid();
  }
  
  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * number of deletes
   */
  static int testDeletes = 0;
  
  /**
   * get the count of trusted browsers
   * @return the trusted browser count from the database
   */
  public int getTrustedBrowserCount() {
    return trustedBrowserCountHelper(TwoFactorDaoFactory.getFactory());
  }
  
  /**
   * get the count of trusted browsers
   * @param theTwoFactorDaoFactory two factor dao factory
   * @return the trusted browser count from the database
   */
  public int trustedBrowserCountHelper(TwoFactorDaoFactory theTwoFactorDaoFactory) {
    List<TwoFactorBrowser> twoFactorBrowsers = theTwoFactorDaoFactory.getTwoFactorBrowser().retrieveTrustedByUserUuid(this.getUuid());
    return TwoFactorServerUtils.length(twoFactorBrowsers);
  }
  
  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorUser dbTwoFactorUser = (TwoFactorUser)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(dbTwoFactorUser, this, DB_VERSION_FIELDS);
    
  }

  /** constant for field name for: loginid */
  public static final String FIELD_LOGINID = "loginid";

  /** constant for field name for: attributes */
  public static final String FIELD_ATTRIBUTES = "attributes";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_LOGINID,
      FIELD_VERSION_NUMBER));

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbVersionReset()
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.assignDbVersion(TwoFactorServerUtils.clone(this, DB_VERSION_FIELDS));
    ((TwoFactorUser)this.dbVersion()).setAttributes(TwoFactorServerUtils.cloneValue(this.attributes));

  }

  /**
   * null if not initted, object if so, attributes for this object
   */
  private Set<TwoFactorUserAttr> attributes = null;
  
  /**
   * null if not initted, object if so, attributes for this object
   * @return the attributes
   */
  public Set<TwoFactorUserAttr> getAttributes() {
    return this.attributes;
  }
  
  /**
   * null if not initted, object if so, attributes for this object
   * @param attributes1 the attributes to set
   */
  public void setAttributes(Set<TwoFactorUserAttr> attributes1) {
    if (attributes1 == null || attributes1.size() == 0 || attributes1 instanceof TreeSet) {
      this.attributes = attributes1;
      return;
    }
    this.attributes = new TreeSet<TwoFactorUserAttr>(attributes1);
  }

  /** loginid from apache plugin for the user */
  private String loginid;

  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_LOGINID,
      FIELD_VERSION_NUMBER));


  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_LOGINID,
      FIELD_VERSION_NUMBER));

  /**
   * encrypted secret for two factor in the workflow before the user has confirmed it
   * might be null if the user is not in the optin or change secret workflow
   * @return two factor secret temp
   */
  public String getTwoFactorSecretTemp() {
    return attributeValueString(TwoFactorUserAttrName.two_factor_secret_temp);
  }

  /**
   * encrypted secret for two factor in the workflow before the user has confirmed it
   * might be null if the user is not in the optin or change secret workflow
   * @param twoFactorSecretTemp1
   */
  public void setTwoFactorSecretTemp(String twoFactorSecretTemp1) {
    this.attribute(TwoFactorUserAttrName.two_factor_secret_temp, true).setAttributeValueString(twoFactorSecretTemp1);
  }

  /**
   * loginid from apache plugin for the user
   * @return the loginid
   */
  public String getLoginid() {
    return this.loginid;
  }
  
  /**
   * loginid from apache plugin for the user
   * @param loginid1 the loginid to set
   */
  public void setLoginid(String loginid1) {
    this.loginid = loginid1;
  }
  
  /**
   * if the user has opted in to two factor
   * @return the optedIn
   */
  public boolean isOptedIn() {
    return TwoFactorServerUtils.booleanValue(this.attributeValueBoolean(TwoFactorUserAttrName.opted_in), false);
  }

  /**
   * if the user is in push in two step
   * @return if enrolled in push
   */
  public boolean isEnrolledInPush() {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(this);
    
    return twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush();
    
    
  }

  /**
   * 
   */
  private static ExpirableCache<String, Boolean> adminCache = null;

  /**
   * 
   */
  private static Boolean useAdminCache = null;
  
  /**
   * 
   */
  private static ExpirableCache<String, Boolean> admin24Cache = null;

  /**
   * 
   */
  private static Boolean useAdmin24Cache = null;
  
  /**
   * 
   * @return if admin
   */
  public boolean isAdmin() {
    
    if (StringUtils.isBlank(this.loginid)) {
      throw new RuntimeException("Why is loginid blank???");
    }
    
    if (useAdminCache == null) {
      synchronized (TwoFactorUser.class) {
        if (useAdminCache == null) {
          int cacheForMinutes = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.adminCacheMinutes", 2);
          if (cacheForMinutes == 0) {
            
            TwoFactorUser.useAdminCache = false;
            
          } else {
            
            adminCache = new ExpirableCache<String, Boolean>(cacheForMinutes);
            TwoFactorUser.useAdminCache = true;
            
          }
          
        }
      }
    }
    
    //see if in cache
    if (TwoFactorUser.useAdminCache) {
      Boolean isAdmin = adminCache.get(this.getLoginid());
      if (isAdmin != null) {
        return isAdmin;
      }
    }

    TwoFactorAuthorizationInterface twoFactorAuthorizationInterface = TwoFactorServerConfig.retrieveConfig().twoFactorAuthorization();
    Set<String> userIds = twoFactorAuthorizationInterface.adminUserIds();
    
    Source theSource = this.subjectSource;
    if (theSource == null) {
      theSource = TfSourceUtils.mainSource();
    }
    
    boolean isAdmin = TfSourceUtils.subjectIdOrNetIdInSet(theSource, this.loginid, userIds);
    
    if (!isAdmin) {
      //see if there is a different loginId
      String userId = TwoFactorFilterJ2ee.retrieveUserIdFromRequestOriginalNotActAs(false);
      isAdmin = !StringUtils.isBlank(userId) && TfSourceUtils.subjectIdOrNetIdInSet(theSource, userId, userIds);
    }
    
    if (TwoFactorUser.useAdminCache) {
      adminCache.put(this.loginid, isAdmin);
    }
    
    return isAdmin;
  }
  
  /**
   * dao factory for object for subsequent queries
   */
  private TwoFactorDaoFactory twoFactorDaoFactory;
  
  /**
   * @return the twoFactorDaoFactory
   */
  public TwoFactorDaoFactory getTwoFactorDaoFactory() {
    return this.twoFactorDaoFactory;
  }
  
  /**
   * @param twoFactorDaoFactory1 the twoFactorDaoFactory to set
   */
  public void setTwoFactorDaoFactory(TwoFactorDaoFactory twoFactorDaoFactory1) {
    this.twoFactorDaoFactory = twoFactorDaoFactory1;
  }

  /**
   * get the factory or 
   * @return the factory
   */
  private TwoFactorDaoFactory twoFactorDaoFactory() {
    if (this.twoFactorDaoFactory == null) {
      this.twoFactorDaoFactory = TwoFactorDaoFactory.getFactory();
    }
    return this.twoFactorDaoFactory;
  }
  
  /**
   * see if the user has report privileges
   * @return if the user has report privileges
   */
  public boolean isHasReportPrivilege() {
    
    //there arent that many privs, so just loop through them
    for (TwoFactorReportPrivilege twoFactorReportPrivilege : TwoFactorReportPrivilege.retrieveAllPrivileges(this.twoFactorDaoFactory())) {
      if (StringUtils.equals(this.getUuid(), twoFactorReportPrivilege.getUserUuid())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * if null, not computed.  else false or true
   */
  private Boolean requiredToOptin;
  
  /**
   * is required to optin.  will refresh cache if needed
   * @return true if required to optin
   */
  public boolean isRequiredToOptin() {
    
    if (this.requiredToOptin == null) {

      this.requiredToOptin = false;
      
      //if configured to do this
      if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean(
          "twoFactorServer.ws.restrictUsersRequiredToBeOptedInWhoArentOptedIn", false)) {
        
        //lets clear this cache
        TfRestLogic.refreshUsersNotOptedInButRequiredIfNeeded(this.twoFactorDaoFactory, false, false);
        
        //is the user in the list?
        if (TfRestLogic.usersNotOptedInButRequired().get(this.getLoginid()) != null) {
          this.requiredToOptin = true;
        }
        
      }
    }    
    return this.requiredToOptin;
  }
  
  /**
   * 
   * @return true if require bday on optin
   */
  public boolean isRequireBirthdayOnOptin() {
    
    // if not configured to do this, then dont
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.promptForBirthdayOnOptin", true)) {
      return false;
    }

    //if cant find bday so dont worry about it, oh well
    return this.getBirthDate() != null;
  }
  
  /**
   * last four if we have it
   */
  private String lastFour = null;

  /**
   * if the last four was searched yet
   */
  private boolean lastFourSearched = false;
  
  /**
   * 
   */
  private Date birthDate = null;
  
  /**
   * if we have looked for bday
   */
  private boolean birthDateSearched = false;
  
  /**
   * 
   * @return the date
   */
  public Date getBirthDate() {
    
    if (!this.birthDateSearched) {
      
      // get the subject, should be found but ok if not i guess
      Source theSource = this.subjectSource;
      
      if (theSource == null) {
        theSource = TfSourceUtils.mainSource();
      }
      
      Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(theSource, this.getLoginid(), false, false, true);
      if (subject != null) {
        
        //'YYYY-MM-DD'
        String birthDateString = subject.getAttributeValue("birthDate");
        
        //if we dont know the bday then dont prompt for it
        if (!StringUtils.isBlank(birthDateString)) {
          
          SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

          try {

            this.birthDate = formatter.parse(birthDateString);

          } catch (ParseException pe) {

            throw new RuntimeException("Cant parse: '" + birthDateString + "'", pe);
            
          }
        }
      }
      
      this.birthDateSearched = true;
      
    }
    
    return this.birthDate;
    
  }
  
  /**
   * 
   * @return the last four
   */
  public String getLastFour() {
    
    if (!this.lastFourSearched) {
      
      // get the subject, should be found but ok if not i guess
      Source theSource = this.subjectSource;
      
      if (theSource == null) {
        theSource = TfSourceUtils.mainSource();
      }
      
      Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(theSource, this.getLoginid(), false, false, true);
      if (subject != null) {
        
        this.lastFour = subject.getAttributeValue("lastFour");
        
      }
      
      this.lastFourSearched = true;
      
    }
    
    return this.lastFour;
    
  }

  /**
   * 
   * @return the date submitted from the page
   */
  public Date getSubmittedBirthDate() {
    
    return null;
  }
  
  /**
   * cache the birth month.  -1 means dont know
   * @return 1-12 or -1
   */
  public int getBirthMonth() {
    
    //init
    Date theBirthDate = this.getBirthDate();
    
    if (theBirthDate == null) {
      return -1;
    }
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(theBirthDate.getTime());
    
    //this returns 0 indexed, so add 1
    return calendar.get(Calendar.MONTH) + 1;
    
  }
  
  /**
   * cache the birth year.  -1 means dont know
   * @return 1900-2025 or -1
   */
  public int getBirthYear() {
    
    //init
    Date theBirthDate = this.getBirthDate();
    
    if (theBirthDate == null) {
      return -1;
    }
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(theBirthDate.getTime());
    
    //this returns 0 indexed, so add 1
    return calendar.get(Calendar.YEAR);
    
  }
  
  /**
   * cache the birth day.  -1 means dont know
   * @return 1-31 or -1
   */
  public int getBirthDay() {
    
    //init
    Date theBirthDate = this.getBirthDate();
    
    if (theBirthDate == null) {
      return -1;
    }
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(theBirthDate.getTime());
    
    return calendar.get(Calendar.DAY_OF_MONTH);
    
  }
  
  /**
   * if the user has opted in to two factor
   * @param optedIn1 the optedIn to set
   */
  public void setOptedIn(boolean optedIn1) {
    this.attribute(TwoFactorUserAttrName.opted_in, true).setAttributeValueBoolean(optedIn1);
  }
  
  /**
   * if passes have been given to the user, this is the last index that has been given
   * @return the seqPassIndexGivenToUser
   */
  public Long getSeqPassIndexGivenToUser() {
    return attributeValueInteger(TwoFactorUserAttrName.sequential_pass_given_to_user);
  }

  /**
   * if opt in for apps that require it, not for other apps
   * @return if opt in only if required
   */
  public Boolean getOptInOnlyIfRequired() {
    return attributeValueBoolean(TwoFactorUserAttrName.opt_in_only_if_required);
  }
  
  /**
   * if opt in for apps that require it, not for other apps
   * @return if opt in only if required
   */
  public boolean isOptInOnlyIfRequiredBoolean() {
    return TwoFactorServerUtils.booleanValue(this.getOptInOnlyIfRequired(), false);
  }
  
  /**
   * if opt in for apps that require it, not for other apps
   * @param theOptInOnlyIfRequired if should opt in only if required
   */
  public void setOptInOnlyIfRequired(Boolean theOptInOnlyIfRequired) {
    this.attribute(TwoFactorUserAttrName.opt_in_only_if_required, true).setAttributeValueBoolean(theOptInOnlyIfRequired);
  }

  /**
   * if opted in by phone (not phone or fob)
   * @return if phone opt in
   */
  public Boolean getPhoneOptIn() {
    return attributeValueBoolean(TwoFactorUserAttrName.phone_opt_in);
  }
  
  /**
   * if opted in by phone (not phone or fob)
   * @return if phone opt in
   */
  public boolean getPhoneOptInBoolean() {
    return TwoFactorServerUtils.booleanValue(this.getPhoneOptIn(), false);
  }
  
  /**
   * if opted in by phone (not phone or fob)
   * @param thePhoneOptIn if phone opt in
   */
  public void setPhoneOptIn(Boolean thePhoneOptIn) {
    this.attribute(TwoFactorUserAttrName.phone_opt_in, true).setAttributeValueBoolean(thePhoneOptIn);
  }

  /**
   * duo phone id of the device which is autocall on login
   * @return if phone auto call text
   */
  public String getPhoneAutoDuoPhoneId() {
    return attributeValueString(TwoFactorUserAttrName.phone_auto_duo_phone_id);
  }

  /**
   * duo phone id of the device which is autocall on login
   * @param phoneAutoDuoPhoneId
   */
  public void setPhoneAutoDuoPhoneId(String phoneAutoDuoPhoneId) {
    this.attribute(TwoFactorUserAttrName.phone_auto_duo_phone_id, true).setAttributeValueString(phoneAutoDuoPhoneId);
  }

  /**
   * if the web should autocall or autotext the user, this is 0v (first phone voice), 0t (first phone text), 1v (second phone voice), etc
   * @return if phone auto call text
   */
  public String getPhoneAutoCalltext() {
    return attributeValueString(TwoFactorUserAttrName.phone_auto_calltext);
  }
  
  /**
   * 
   * @return if phone auto call text
   */
  public boolean isPhoneAutoCallText() {
    return !StringUtils.isBlank(this.getPhoneAutoCallTextNumber());
  }
  
  /**
   * get the phone number of auto call text
   * @return the phone number
   */
  public String getPhoneAutoCallTextNumber() {
    
    if (StringUtils.equals("0t", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone0()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsText0(), false)) {
        return this.getPhone0();
      }
    } else if (StringUtils.equals("0v", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone0()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsVoice0(), false)) {
        return this.getPhone0();
      }
    } else if (StringUtils.equals("1t", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone1()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsText1(), false)) {
        return this.getPhone1();
      }
    } else if (StringUtils.equals("1v", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone1()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsVoice1(), false)) {
        return this.getPhone1();
      }
    } else if (StringUtils.equals("2t", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone2()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsText2(), false)) {
        return this.getPhone2();
      }
    } else if (StringUtils.equals("2v", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone2()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsVoice2(), false)) {
        return this.getPhone2();
      }
    }
    return null;
  }
  
  /**
   * get the phone number of auto call text
   * @return the phone number
   */
  public String getPhoneAutoCallTextType() {
    
    if (StringUtils.equals("0t", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone0()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsText0(), false)) {
        return "text";
      }
    } else if (StringUtils.equals("0v", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone0()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsVoice0(), false)) {
        return "voice";
      }
    } else if (StringUtils.equals("1t", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone1()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsText1(), false)) {
        return "text";
      }
    } else if (StringUtils.equals("1v", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone1()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsVoice1(), false)) {
        return "voice";
      }
    } else if (StringUtils.equals("2t", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone2()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsText2(), false)) {
        return "text";
      }
    } else if (StringUtils.equals("2v", this.getPhoneAutoCalltext())) {
      if (!StringUtils.isBlank(this.getPhone2()) && TwoFactorServerUtils.booleanValue(this.getPhoneIsVoice2(), false)) {
        return "voice";
      }
    }
    return null;
  }
  
  
  /**
   * if the web should autocall or autotext the user, this is 0v (first phone voice), 0t (first phone text), 1v (second phone voice), etc
   * @param thePhoneAutoCalltext if phone auto call text
   */
  public void setPhoneAutoCalltext(String thePhoneAutoCalltext) {
    this.attribute(TwoFactorUserAttrName.phone_auto_calltext, true).setAttributeValueString(thePhoneAutoCalltext);
  }

  /**
   *  millis since 1970 of last email sent to not opted in user who is required
   * @return if phone auto call text
   */
  public Long getLastEmailNotOptedInUser() {
    return attributeValueInteger(TwoFactorUserAttrName.last_email_not_opted_in_user);
  }
  
  /**
   *  millis since 1970 of last email sent to not opted in user who is required
   * @param theLastEmailNotOptedInUser if phone auto call text
   */
  public void setLastEmailNotOptedInUser(Long theLastEmailNotOptedInUser) {
    this.attribute(TwoFactorUserAttrName.last_email_not_opted_in_user, true).setAttributeValueInteger(theLastEmailNotOptedInUser);
  }

  /**
   * <pre>
   * if the user has had wrong birthday attempts
   * the json has the month (0 indexed) and day in the key, will delete keys older than a month
   * {
   * "0001": 2,
   * "0002": 3,
   * "0003": 0
   * ...
   * }
   * 
   * </pre>
   * @return call texts in month
   */
  public String getWrongBdayAttemptsInMonth() {
    return attributeValueString(TwoFactorUserAttrName.wrong_bday_attempts_in_month);
  }
  
  /**
   * <pre>
   * if the user has had wrong birthday attempts
   * the json has the month (0 indexed) and day in the key, will delete keys older than a month
   * {
   * "0001": 2,
   * "0002": 3,
   * "0003": 0
   * ...
   * }
   * 
   * </pre>
   * @param theWrongBdayAttemptsInMonth call texts in month
   */
  public void setWrongBdayAttemptsInMonth(String theWrongBdayAttemptsInMonth) {
    this.attribute(TwoFactorUserAttrName.wrong_bday_attempts_in_month, true).setAttributeValueString(theWrongBdayAttemptsInMonth);
  }


  
  /**
   * <pre>
   * if the web should autocall or autotext the user, this is 1v (first phone voice), 1t (first phone text), 2v (second phone voice), etc
   * the json has the month (0 indexed) and day in the key, will delete keys older than a month
   * {
   * "0001": 2,
   * "0002": 3,
   * "0003": 0
   * ...
   * }
   * 
   * </pre>
   * @return call texts in month
   */
  public String getPhoneAutoCalltextsInMonth() {
    return attributeValueString(TwoFactorUserAttrName.phone_auto_calltexts_in_month);
  }
  
  /**
   * <pre>
   * if the web should autocall or autotext the user, this is 1v (first phone voice), 1t (first phone text), 2v (second phone voice), etc
   * the json has the month (0 indexed) and day in the key, will delete keys older than a month
   * {
   * "0001": 2,
   * "0002": 3,
   * "0003": 0
   * ...
   * }
   * 
   * </pre>
   * @param thePhoneAutoCalltextsInMonth call texts in month
   */
  public void setPhoneAutoCalltextsInMonth(String thePhoneAutoCalltextsInMonth) {
    this.attribute(TwoFactorUserAttrName.phone_auto_calltexts_in_month, true).setAttributeValueString(thePhoneAutoCalltextsInMonth);
  }

  
  /**
   * 
   * @param twoFactorUserAttrName
   * @return the two factor user attribute value as string
   */
  private String attributeValueString(TwoFactorUserAttrName twoFactorUserAttrName) {
    TwoFactorUserAttr twoFactorUserAttr = attribute(twoFactorUserAttrName, false);
    if (twoFactorUserAttr == null) {
      return null;
    }
    if (twoFactorUserAttr.getAttributeNameEnum().getTheType() != TwoFactorUserAttrType.string) {
      throw new RuntimeException("Why asking for string when type is: " 
          + twoFactorUserAttr.getAttributeNameEnum().getTheType());
    }
    return twoFactorUserAttr.getAttributeValueString();
  }
  
  /**
   * 
   * @param twoFactorUserAttrName
   * @return the two factor user attribute value as integer
   */
  private Long attributeValueInteger(TwoFactorUserAttrName twoFactorUserAttrName) {
    TwoFactorUserAttr twoFactorUserAttr = attribute(twoFactorUserAttrName, false);
    if (twoFactorUserAttr == null) {
      return null;
    }
    if (twoFactorUserAttr.getAttributeNameEnum().getTheType() != TwoFactorUserAttrType.integer) {
      throw new RuntimeException("Why asking for integer when type is: " 
          + twoFactorUserAttr.getAttributeNameEnum().getTheType());
    }
    return twoFactorUserAttr.getAttributeValueInteger();
  }
  
  /**
   * delete an attribute from the database, note, this just deletes from local cache, still need
   * to store or delete this object
   * @param attributeName
   */
  public void attributeDeleteFromDb(TwoFactorUserAttrName attributeName) {
    if (this.attributes == null) {
      return;
    }
    Iterator<TwoFactorUserAttr> iterator = this.attributes.iterator();
    while (iterator.hasNext()) {
      
      TwoFactorUserAttr twoFactorUserAttr = iterator.next();
      if (attributeName.equals(twoFactorUserAttr.getAttributeNameEnum())) {
        iterator.remove();
      }
      
    }
  }
  
  /**
   * 
   * @param twoFactorUserAttrName
   * @return the two factor user attribute value as boolean
   */
  private Boolean attributeValueBoolean(TwoFactorUserAttrName twoFactorUserAttrName) {
    TwoFactorUserAttr twoFactorUserAttr = attribute(twoFactorUserAttrName, false);
    if (twoFactorUserAttr == null) {
      return null;
    }
    if (twoFactorUserAttr.getAttributeNameEnum().getTheType() != TwoFactorUserAttrType.booleanType) {
      throw new RuntimeException("Why asking for boolean when type is: " 
          + twoFactorUserAttr.getAttributeNameEnum().getTheType());
    }
    return twoFactorUserAttr.getAttributeValueBoolean();
  }
  
  /**
   * get a map of attributes
   * @return the map, never null
   */
  public Map<String, TwoFactorUserAttr> attributeMap() {
    Map<String, TwoFactorUserAttr> attributeMap = new TreeMap<String, TwoFactorUserAttr>();
    
    if (this.attributes != null) {
      for (TwoFactorUserAttr twoFactorUserAttr : this.attributes) {
        
        if (attributeMap.containsKey(twoFactorUserAttr.getAttributeName())) {
          throw new RuntimeException("Why are there two attributes: " + twoFactorUserAttr.getAttributeName());
        }
        
        attributeMap.put(twoFactorUserAttr.getAttributeName(), twoFactorUserAttr);
        
      }
    }
    
    return attributeMap;
  }
  
  /**
   * 
   * @param twoFactorUserAttrName
   * @param createIfNotExist 
   * @return the two factor user attribute name
   */
  private TwoFactorUserAttr attribute(TwoFactorUserAttrName twoFactorUserAttrName, boolean createIfNotExist) {
    if (twoFactorUserAttrName == null) {
      throw new NullPointerException("Why is twoFactorUserAttrName null????");
    }
    
    if (this.attributes == null) {
      if (createIfNotExist) {
        this.attributes = new TreeSet<TwoFactorUserAttr>();
      } else {
        return null;
      }
    }
    
    //loop through, find one and only one attribute
    TwoFactorUserAttr twoFactorUserAttr = null;
    for (TwoFactorUserAttr currentTwoFactorUserAttr : this.attributes) {
      if (currentTwoFactorUserAttr.getAttributeNameEnum() == twoFactorUserAttrName) {
        if (twoFactorUserAttr != null) {
          throw new RuntimeException("Why are there more than one user attribute with the same name??? " 
              + this.getUuid() + twoFactorUserAttrName);
        }
        twoFactorUserAttr = currentTwoFactorUserAttr;
      }
    }
    
    //if not there, see if we need to create
    if (twoFactorUserAttr == null && createIfNotExist) {
      twoFactorUserAttr = new TwoFactorUserAttr();
      if (TwoFactorServerUtils.isBlank(this.getUuid())) {
        throw new RuntimeException("Why is uuid blank????");
      }
      twoFactorUserAttr.setUuid(TwoFactorServerUtils.uuid());
      twoFactorUserAttr.setUserUuid(this.getUuid());
      twoFactorUserAttr.setAttributeNameEnum(twoFactorUserAttrName);
      this.attributes.add(twoFactorUserAttr);
    }
    
    return twoFactorUserAttr;
  }
  
  /**
   * if passes have been given to the user, this is the last index that has been given
   * @param seqPassIndexGivenToUser1 the seqPassIndexGivenToUser to set
   */
  public void setSeqPassIndexGivenToUser(Long seqPassIndexGivenToUser1) {
    
    this.attribute(TwoFactorUserAttrName.sequential_pass_given_to_user, true)
      .setAttributeValueInteger(seqPassIndexGivenToUser1);
  }

  /**
   * the index that the user is on
   * @return the sequentialPassIndex
   */
  public Long getSequentialPassIndex() {
    return this.attribute(TwoFactorUserAttrName.sequential_pass_index, true).getAttributeValueInteger();
  }

  /**
   * the index that the user is on
   * @param sequentialPassIndex1 the sequentialPassIndex to set
   */
  public void setSequentialPassIndex(Long sequentialPassIndex1) {
    this.attribute(TwoFactorUserAttrName.sequential_pass_index, true)
      .setAttributeValueInteger(sequentialPassIndex1);
  }

  /**
   * the index that the user is on for token
   * @return the tokenIndex
   */
  public Long getTokenIndex() {
    return this.attribute(TwoFactorUserAttrName.token_index, true).getAttributeValueInteger();
  }

  /**
   * the index that the user is on for token
   * @param tokenIndex1 the tokenIndex to set
   */
  public void setTokenIndex(Long tokenIndex1) {
    this.attribute(TwoFactorUserAttrName.token_index, true)
      .setAttributeValueInteger(tokenIndex1);
  }

  /**
   * the millis since 1970 div 30k that the last successful totp pass was used
   * @return the LastTotpTimestampUsed
   */
  public Long getLastTotpTimestampUsed() {
    return this.attribute(TwoFactorUserAttrName.last_totp_timestamp_used, true).getAttributeValueInteger();
  }

  /**
   * the millis since 1970 div 30k that the last successful totp pass was used
   * @param lastTotpTimestampUsed the lastTotpTimestampUsed to set
   */
  public void setLastTotpTimestampUsed(Long lastTotpTimestampUsed) {
    this.attribute(TwoFactorUserAttrName.last_totp_timestamp_used, true)
      .setAttributeValueInteger(lastTotpTimestampUsed);
  }

  /**
   * encrypted secret for two factor.  This could be null if the user has not opted in,
   * it can change when they change it
   * @return the twoFactorSecret
   */
  public String getTwoFactorSecret() {
    return this.attributeValueString(TwoFactorUserAttrName.two_factor_secret);
  }
  
  /**
   * two factor secret unencrypted
   * @return the secret unencrypted
   */
  public String getTwoFactorSecretUnencrypted() {
    return EncryptionKey.decrypt(this.getTwoFactorSecret());
  }

  /**
   * 
   * @param twoFactorSecretUnencrypted
   */
  public void setTwoFactorSecretUnencrypted(String twoFactorSecretUnencrypted) {
    
    this.setTwoFactorSecret(EncryptionKey.encrypt(twoFactorSecretUnencrypted));
    
  }

  /**
   * two factor secret formatted
   * @return the secret formatted
   */
  public String getTwoFactorSecretTempUnencrypted() {
    
    return EncryptionKey.decrypt(this.getTwoFactorSecretTemp());
    
  }

  /**
   * 
   * @param twoFactorSecretTempUnencrypted
   */
  public void setTwoFactorSecretTempUnencrypted(String twoFactorSecretTempUnencrypted) {
        
    this.setTwoFactorSecretTemp(EncryptionKey.encrypt(twoFactorSecretTempUnencrypted));
    
  }

  /**
   * two factor secret formatted
   * @return the secret formatted
   */
  public String getTwoFactorSecretTempUnencryptedFormatted() {
    String twoFactorSecretTemp = this.getTwoFactorSecretTemp();
    if (twoFactorSecretTemp == null) {
      return twoFactorSecretTemp;
    }
    String theTwoFactorSecret = this.getTwoFactorSecretTempUnencrypted();
    
    
    return formatSecret(theTwoFactorSecret);
  }

  /**
   * @param theTwoFactorSecret
   * @return the string
   */
  private static String formatSecret(String theTwoFactorSecret) {
    //strip whitespace
    if (theTwoFactorSecret == null) {
      return null;
    }
    {
      StringBuilder newString = new StringBuilder();
      for (int i=0;i<theTwoFactorSecret.length(); i++) {
        
        char theChar = theTwoFactorSecret.charAt(i);
        
        if (Character.isWhitespace(theChar)) {
          continue;
        }
        newString.append(theChar);
        
      }
      theTwoFactorSecret = newString.toString();
    }

    //add in whitespace
    {
      StringBuilder newString = new StringBuilder();
      for (int i=0;i<theTwoFactorSecret.length();i++) {
        if (i!=0 && i%4 == 0) {
          newString.append(' ');
        }
        newString.append(theTwoFactorSecret.charAt(i));
      }
      return newString.toString();
    }
  }

  /**
   * two factor secret formatted
   * @return the secret formatted
   */
  public String getTwoFactorSecretTempUnencryptedHexFormatted() {
    String twoFactorSecretTemp = this.getTwoFactorSecretTemp();
    if (twoFactorSecretTemp == null) {
      return twoFactorSecretTemp;
    }
    String theTwoFactorSecret = this.getTwoFactorSecretTempUnencrypted();
    return TwoFactorServerUtils.base32toHexFormatted(theTwoFactorSecret);
  }
  
  /**
   * encrypted secret for two factor.  This could be null if the user has not opted in,
   * it can change when they change it
   * @param twoFactorSecret1 the twoFactorSecret to set
   */
  public void setTwoFactorSecret(String twoFactorSecret1) {
    this.attribute(TwoFactorUserAttrName.two_factor_secret, true).setAttributeValueString(twoFactorSecret1);
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    return TwoFactorServerUtils.clone(this, CLONE_FIELDS);
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    return DB_NEEDS_UPDATE_FIELDS;
  }

  /**
   * retrieve a user by login id
   * @param twoFactorDaoFactory
   * @param loginid
   * @return the user or null if not found
   */
  public static TwoFactorUser retrieveByLoginid(final TwoFactorDaoFactory twoFactorDaoFactory, final String loginid) {

    if (TwoFactorServerUtils.isBlank(loginid)) {
      throw new RuntimeException("Why is loginid blank? ");
    }
    
    TwoFactorUser result = (TwoFactorUser)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorUser twoFactorUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByLoginid(loginid);
        
        if (twoFactorUser == null) {
          return twoFactorUser;
        }

        retrieveUserHelper(twoFactorDaoFactory, twoFactorUser.getUuid(), twoFactorUser); 
        
        return twoFactorUser;
      }
    });
    
    return result;
    
  }

  /**
   * retrieve a user by uuid
   * @param twoFactorDaoFactory
   * @param uuid
   * @return the user or null if not found
   */
  public static TwoFactorUser retrieveByUuid(final TwoFactorDaoFactory twoFactorDaoFactory, final String uuid) {

    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorUser result = (TwoFactorUser)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorUser twoFactorUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(uuid);
        
        if (twoFactorUser == null) {
          return twoFactorUser;
        }
        
        retrieveUserHelper(twoFactorDaoFactory, uuid, twoFactorUser); 
        
        return twoFactorUser;
      }

    });
    
    return result;
    
  }

  /**
   * @param twoFactorDaoFactory
   * @param uuid
   * @param twoFactorUser
   */
  private static void retrieveUserHelper(final TwoFactorDaoFactory twoFactorDaoFactory,
      final String uuid, TwoFactorUser twoFactorUser) {
    //also get the attributes...
    Set<TwoFactorUserAttr> twoFactorUserAttrsSet = twoFactorDaoFactory
      .getTwoFactorUserAttr().retrieveByUser(uuid);
    
    
    if (twoFactorUserAttrsSet != null) {
      
      Iterator<TwoFactorUserAttr> iterator = twoFactorUserAttrsSet.iterator();
      
      //if we have two versions of open two factor pointing to the same db, then 
      //dont throw errors if attribute names dont match
      while (iterator.hasNext()) {
        TwoFactorUserAttr twoFactorUserAttr = iterator.next();
        if (StringUtils.isBlank(twoFactorUserAttr.getAttributeName())) {
          iterator.remove();
        }
      }
      
      //make a copy for dbVersion, keep the deletes
      Set<TwoFactorUserAttr> dbVersionAttributes = TwoFactorServerUtils.cloneValue(twoFactorUserAttrsSet);
      ((TwoFactorUser)twoFactorUser.dbVersion()).setAttributes(dbVersionAttributes);
      
      //go through again and remove deleted
      iterator = twoFactorUserAttrsSet.iterator();
      
      //if we have two versions of open two factor pointing to the same db, then 
      //dont throw errors if attribute names dont match
      while (iterator.hasNext()) {
        TwoFactorUserAttr twoFactorUserAttr = iterator.next();
        if (twoFactorUserAttr.getDeletedOn() != null && twoFactorUserAttr.getDeletedOn() < System.currentTimeMillis()) {
          iterator.remove();
        }
      }
      //this one doesnt include deleted items
      twoFactorUser.setAttributes(twoFactorUserAttrsSet);

    }
    twoFactorUser.setTwoFactorDaoFactory(twoFactorDaoFactory);
  }

  
  /**
   * store this object and audit
   * @param twoFactorDaoFactory1 
   * @return if changed
   */
  public boolean store(final TwoFactorDaoFactory twoFactorDaoFactory1) {
    return this.storeHelper(twoFactorDaoFactory1);
  }
  
  /**
   * store this object and audit
   * @param twoFactorDaoFactory1 
   * @return if changed
   */
  private boolean storeHelper(final TwoFactorDaoFactory twoFactorDaoFactory1) {
    
    if (StringUtils.isBlank(this.loginid)) {
      throw new RuntimeException("loginid is null");
    }

    //note, actual length if 90, with extra space for special chars
    if (this.loginid != null && this.loginid.length() > 90) {
      throw new RuntimeException("loginid is too long (90): '" + this.loginid + "'");
    }

    final TwoFactorUser dbVersion = (TwoFactorUser)TwoFactorUser.this.dbVersion();

    //lets do this in a new tx
    boolean hadChange = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        boolean hadChangeLocal = false;
        
        if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, TwoFactorUser.this)) {
          if (!HibernateSession.isReadonlyMode()) {
            twoFactorDaoFactory1.getTwoFactorUser().store(TwoFactorUser.this);
          }
          testInsertsAndUpdates++;
          hadChangeLocal = true;
        }
        
        return hadChangeLocal;
      }
    });

    //sync up the attributes
    Map<String, TwoFactorUserAttr> newAttributes = TwoFactorUser.this.attributeMap();
    Map<String, TwoFactorUserAttr> oldAttributes = dbVersion == null ? new HashMap<String, TwoFactorUserAttr>() : dbVersion.attributeMap();

    //###################### DELETES
    {
      Set<String> fieldsForDelete = new HashSet<String>(oldAttributes.keySet());
      fieldsForDelete.removeAll(newAttributes.keySet());
      
      for (String fieldForDelete : fieldsForDelete) {
        final TwoFactorUserAttr twoFactorUserAttr = oldAttributes.get(fieldForDelete);
        if (twoFactorUserAttr.getDeletedOn() == null || twoFactorUserAttr.getDeletedOn() > System.currentTimeMillis()) {
          twoFactorUserAttr.setDeletedOn(System.currentTimeMillis());
          //delete(twoFactorDaoFactory1);
          oldAttributes.remove(fieldForDelete);
          boolean attrChanged = storeAttribute(twoFactorDaoFactory1, twoFactorUserAttr);
          hadChange = hadChange || attrChanged;
        }
      }
    }


    
    //###################### UPDATES
    {          
      for (String fieldForUpdate : newAttributes.keySet()) {
        TwoFactorUserAttr newFactorUserAttr = newAttributes.get(fieldForUpdate);

        //this will insert or update and keep track of state
        newFactorUserAttr.setDeletedOn(null);
        if (newFactorUserAttr.getVersionNumber() == null || newFactorUserAttr.getVersionNumber() == -1) {
          TwoFactorUserAttr oldAttribute = oldAttributes.get(newFactorUserAttr.getAttributeName());
          
          //maybe an insert is deleted?
          if (oldAttribute != null) {
            if (oldAttribute.getVersionNumber() != null && oldAttribute.getVersionNumber() != -1) {
              newFactorUserAttr.setUuid(oldAttribute.getUuid());
              newFactorUserAttr.setVersionNumber(oldAttribute.getVersionNumber());
            }
          }
        }
        boolean attrChanged = storeAttribute(twoFactorDaoFactory1, newFactorUserAttr);
        hadChange = hadChange || attrChanged;
      }
    }
    if (hadChange) {
      TwoFactorUser.this.dbVersionReset();
    }
    return hadChange;
  }

  /**
   * try to store, if it doesnt work, try again...
   * @param twoFactorDaoFactory1
   * @param twoFactorUserAttr
   * @return if changed
   */
  private boolean storeAttribute(final TwoFactorDaoFactory twoFactorDaoFactory1,
      final TwoFactorUserAttr twoFactorUserAttr) {

    try {
      return storeAttributeHelper(twoFactorDaoFactory1, twoFactorUserAttr);
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("error with attribute: userUuid: " + twoFactorUserAttr.getUserUuid() + ", attrName: " + twoFactorUserAttr.getAttributeName(), e);
      }
      
      TwoFactorUserAttr dbTwoFactorUserAttr = twoFactorDaoFactory1.getTwoFactorUserAttr()
          .retrieveByUserAndAttributeName(twoFactorUserAttr.getUserUuid(), twoFactorUserAttr.getAttributeName());
      
      dbTwoFactorUserAttr.setAttributeValueInteger(twoFactorUserAttr.getAttributeValueInteger());
      dbTwoFactorUserAttr.setAttributeValueString(twoFactorUserAttr.getAttributeValueString());
      dbTwoFactorUserAttr.setDeletedOn(twoFactorUserAttr.getDeletedOn());
      dbTwoFactorUserAttr.setEncryptionTimestamp(twoFactorUserAttr.getEncryptionTimestamp());
      return storeAttributeHelper(twoFactorDaoFactory1, dbTwoFactorUserAttr);
    }
  }

  /**
   * @param twoFactorDaoFactory1
   * @param twoFactorUserAttr
   * @return if changed
   */
  private boolean storeAttributeHelper(final TwoFactorDaoFactory twoFactorDaoFactory1,
      final TwoFactorUserAttr twoFactorUserAttr) {
    
    boolean attrChanged = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        return twoFactorUserAttr.store(twoFactorDaoFactory1);
      }
    });
    return attrChanged;
  }

  /**
   * delete this record
   * @param twoFactorDaoFactory1 is the factor to use
   */
  @Override
  public void delete(final TwoFactorDaoFactory twoFactorDaoFactory1) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        if (!HibernateSession.isReadonlyMode()) {
          
          for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorServerUtils.nonNull(twoFactorDaoFactory1
              .getTwoFactorUserAttr().retrieveByUser(TwoFactorUser.this.getUuid()))) {
            
            twoFactorUserAttr.delete(twoFactorDaoFactory1);
            
          }
          
          for (TwoFactorBrowser twoFactorBrowser : TwoFactorServerUtils.nonNull(TwoFactorBrowser.retrieveByUserUuid(twoFactorDaoFactory1, TwoFactorUser.this.getUuid(), true))) {
            
            twoFactorBrowser.delete(twoFactorDaoFactory1);
            
          }
          
          twoFactorDaoFactory1.getTwoFactorUser().delete(TwoFactorUser.this);
        }
        testDeletes++;
        return null;
      }
    });

  }

  /**
   * retrieve a user record by loginid or create, retry if problem
   * @param twoFactorDaoFactory
   * @param loginid
   * @return the user
   */
  public static TwoFactorUser retrieveByLoginidOrCreate(final TwoFactorDaoFactory twoFactorDaoFactory, final String loginid) {
    try {
      return retrieveByLoginidOrCreateHelper(twoFactorDaoFactory, loginid);
    } catch (Exception e) {
      LOG.debug("Non-fatal error getting user: " + loginid, e);
      //hmm, error
    }
    //wait some time, maybe someone else created it
    TwoFactorServerUtils.sleep(250 + new SecureRandom().nextInt(250));
    //try again, throw exception if happens
    return retrieveByLoginidOrCreateHelper(twoFactorDaoFactory, loginid);
  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorUser.class);

  /**
   * retrieve an ip address record by ip address
   * @param twoFactorDaoFactory
   * @param loginid
   * @return the user or null if not found
   */
  private static TwoFactorUser retrieveByLoginidOrCreateHelper(final TwoFactorDaoFactory twoFactorDaoFactory, final String loginid) {
  
    TwoFactorUser twoFactorUser = retrieveByLoginid(twoFactorDaoFactory, loginid);
    
    if (twoFactorUser != null) {
      return twoFactorUser;
    }
    
    return (TwoFactorUser)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, 
        TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorUser localTwoFactorUser = new TwoFactorUser();
        localTwoFactorUser.setLoginid(loginid);
        localTwoFactorUser.setUuid(TwoFactorServerUtils.uuid());
        localTwoFactorUser.store(twoFactorDaoFactory);
        
        return localTwoFactorUser;
      }
    });
  }

  /**
   * phone to opt out 0
   * @return phone0
   */
  public String getPhone0() {
    return attributeValueString(TwoFactorUserAttrName.phone0);
  }

  /**
   * phone to opt out 0
   * @param phone0
   */
  public void setPhone0(String phone0) {
    this.attribute(TwoFactorUserAttrName.phone0, true).setAttributeValueString(phone0);
  }


  /**
   * duo user id
   * @return duo user id
   */
  public String getDuoUserId() {
    return attributeValueString(TwoFactorUserAttrName.duo_user_id);
  }

  /**
   * duo user id
   * @param duoUserId
   */
  public void setDuoUserId(String duoUserId) {
    this.attribute(TwoFactorUserAttrName.duo_user_id, true).setAttributeValueString(duoUserId);
  }

  /**
   * millis since 1970 __ browser id __ duo tx id for push
   * the 4th entry separated by two underscores is if the push has already successfully been used and when
   * @return browser id __ duo tx id for push 
   */
  public String getDuoPushTransactionId() {
    return attributeValueString(TwoFactorUserAttrName.duo_push_transaction_id);
  }

  /**
   * duo push phone id
   * @param duoPushPhoneId
   */
  public void setDuoPushPhoneId(String duoPushPhoneId) {
    this.attribute(TwoFactorUserAttrName.duo_push_phone_id, true).setAttributeValueString(duoPushPhoneId);
  }

  /**
   * duo push phone id
   * @return duo push phone id
   */
  public String getDuoPushPhoneId() {
    return attributeValueString(TwoFactorUserAttrName.duo_push_phone_id);
  }

  /**
   * millis since 1970 __ browser id __ duo tx id for push
   * the 4th entry separated by two underscores is if the push has already successfully been used and when
   * @param duoPushTransactionId
   */
  public void setDuoPushTransactionId(String duoPushTransactionId) {
    this.attribute(TwoFactorUserAttrName.duo_push_transaction_id, true).setAttributeValueString(duoPushTransactionId);
  }

  /**
   * if phone 0 is text
   * @return phone0
   */
  public Boolean getPhoneIsText0() {
    return attributeValueBoolean(TwoFactorUserAttrName.phone_is_text0);
  }

  /**
   * if phone 0 is text
   * @param phoneIsText0
   */
  public void setPhoneIsText0(Boolean phoneIsText0) {
    this.attribute(TwoFactorUserAttrName.phone_is_text0, true).setAttributeValueBoolean(phoneIsText0);
  }

  /**
   * duo push by default
   * @return push by default
   */
  public Boolean getDuoPushByDefault() {
    return attributeValueBoolean(TwoFactorUserAttrName.duo_push_by_default);
  }

  /**
   * boolean primitive if push by default
   * @return true if push by default, false if not
   */
  public boolean isDuoPushByDefaultBoolean() {
    return TwoFactorServerUtils.booleanValue(this.getDuoPushByDefault(), false);
  }
  
  /**
   * 
   * @return if enrolled in duo push
   */
  public boolean isDuoPush() {
    return !StringUtils.isBlank(this.getDuoPushPhoneId());
  }
  
  /**
   * if push by default
   * @param duoPushByDefault
   */
  public void setDuoPushByDefault(Boolean duoPushByDefault) {
    this.attribute(TwoFactorUserAttrName.duo_push_by_default, true).setAttributeValueBoolean(duoPushByDefault);
  }

  /**
   * if phone 0 is voice
   * @return phone0
   */
  public Boolean getPhoneIsVoice0() {
    return attributeValueBoolean(TwoFactorUserAttrName.phone_is_voice0);
  }

  /**
   * if phone 0 is voice
   * @param phoneIsVoice0
   */
  public void setPhoneIsVoice0(Boolean phoneIsVoice0) {
    this.attribute(TwoFactorUserAttrName.phone_is_voice0, true).setAttributeValueBoolean(phoneIsVoice0);
  }

  /**
   * if phone 1 is text
   * @return phone1
   */
  public Boolean getPhoneIsText1() {
    return attributeValueBoolean(TwoFactorUserAttrName.phone_is_text1);
  }

  /**
   * if phone 1 is text
   * @param phoneIsText1
   */
  public void setPhoneIsText1(Boolean phoneIsText1) {
    this.attribute(TwoFactorUserAttrName.phone_is_text1, true).setAttributeValueBoolean(phoneIsText1);
  }

  /**
   * if phone 1 is voice
   * @return phone1
   */
  public Boolean getPhoneIsVoice1() {
    return attributeValueBoolean(TwoFactorUserAttrName.phone_is_voice1);
  }

  /**
   * if phone 1 is voice
   * @param phoneIsVoice1
   */
  public void setPhoneIsVoice1(Boolean phoneIsVoice1) {
    this.attribute(TwoFactorUserAttrName.phone_is_voice1, true).setAttributeValueBoolean(phoneIsVoice1);
  }


  /**
   * if phone 2 is text
   * @return phone2
   */
  public Boolean getPhoneIsText2() {
    return attributeValueBoolean(TwoFactorUserAttrName.phone_is_text2);
  }

  /**
   * if phone 2 is text
   * @param phoneIsText2
   */
  public void setPhoneIsText2(Boolean phoneIsText2) {
    this.attribute(TwoFactorUserAttrName.phone_is_text2, true).setAttributeValueBoolean(phoneIsText2);
  }


  /**
   * if phone 2 is voice
   * @return phone0
   */
  public Boolean getPhoneIsVoice2() {
    return attributeValueBoolean(TwoFactorUserAttrName.phone_is_voice2);
  }

  /**
   * if phone 2 is voice
   * @param phoneIsVoice2
   */
  public void setPhoneIsVoice2(Boolean phoneIsVoice2) {
    this.attribute(TwoFactorUserAttrName.phone_is_voice2, true).setAttributeValueBoolean(phoneIsVoice2);
  }
  
  /**
   * date the user invited colleagues
   * @return date user invited colleagues
   */
  public Long getDateInvitedColleagues() {
    return attributeValueInteger(TwoFactorUserAttrName.date_invited_colleagues);
  }

  /**
   * date the user invited colleagues
   * @param dateInvitedColleagues
   */
  public void setDateInvitedColleagues(Long dateInvitedColleagues) {
    this.attribute(TwoFactorUserAttrName.date_invited_colleagues, true).setAttributeValueInteger(dateInvitedColleagues);
  }

  /**
   * date the user sent a phone code to their phone
   * @return date user sent a phone code to their phone
   */
  public Long getDatePhoneCodeSent() {
    return attributeValueInteger(TwoFactorUserAttrName.date_phone_code_sent);
  }

  /**
   * date the user sent a phone code to their phone
   * @param datePhoneCodeSent
   */
  public void setDatePhoneCodeSent(Long datePhoneCodeSent) {
    this.attribute(TwoFactorUserAttrName.date_phone_code_sent, true).setAttributeValueInteger(datePhoneCodeSent);
  }
  
  /**
   * date the user sent a auto phone code to their phone
   * @return date user sent a phone code to their phone
   */
  public Long getDateAutoPhoneCodeSent() {
    return attributeValueInteger(TwoFactorUserAttrName.date_auto_phone_code_sent);
  }

  /**
   * date the user sent a auto phone code to their phone
   * @param dateAutoPhoneCodeSent
   */
  public void setDateAutoPhoneCodeSent(Long dateAutoPhoneCodeSent) {
    this.attribute(TwoFactorUserAttrName.date_auto_phone_code_sent, true).setAttributeValueInteger(dateAutoPhoneCodeSent);
  }
  
  /**
   * encrypted 6 digit code sent to users phone
   * @return phone code encrypted
   */
  public String getPhoneCodeEncrypted() {
    return attributeValueString(TwoFactorUserAttrName.phone_code_encrypted);
  }

  /**
   * encrypted 6 digit code sent to users phone
   * @param phoneCodeEncrypted
   */
  public void setPhoneCodeEncrypted(String phoneCodeEncrypted) {
    this.attribute(TwoFactorUserAttrName.phone_code_encrypted, true).setAttributeValueString(phoneCodeEncrypted);
  }

  
  /**
   * unencrypted 6 digit code sent to users phone
   * @return phone code encrypted
   */
  public String getPhoneCodeUnencrypted() {
    return EncryptionKey.decrypt(getPhoneCodeEncrypted());
  }

  /**
   * unencrypted 6 digit code sent to users phone
   * @param phoneCodeUnencrypted
   */
  public void setPhoneCodeUnencrypted(String phoneCodeUnencrypted) {
    setPhoneCodeEncrypted(EncryptionKey.encrypt(phoneCodeUnencrypted));
  }

  /**
   * the phone code if it exists and is not expired
   * @return the phone code if it exists and is not expired
   */
  public String getPhoneCodeUnencryptedIfNotExpired() {
    int phoneCodeLastsMinutes = TwoFactorServerConfig.retrieveConfig()
        .propertyValueInt("twoFactorServer.phoneCodeLastsMinutes", 10);
    String phoneCodeUnencrypted = this.getPhoneCodeUnencrypted();
    if (!StringUtils.isBlank(phoneCodeUnencrypted)) {
      Long datePhoneCodeSent = this.getDatePhoneCodeSent();
      if (datePhoneCodeSent != null 
          && ((System.currentTimeMillis() - datePhoneCodeSent) / (1000L * 60)) < phoneCodeLastsMinutes) {
        return phoneCodeUnencrypted;
      }
    }
    return null;
  }
  
  /**
   * phone to opt out 1
   * @return phone1
   */
  public String getPhone1() {
    return attributeValueString(TwoFactorUserAttrName.phone1);
  }

  /**
   * phone to opt out 1: format; yes_voice__no_text__123-456-7890
   * @param phone1
   */
  public void setPhone1(String phone1) {
    this.attribute(TwoFactorUserAttrName.phone1, true).setAttributeValueString(phone1);
  }

  /**
   * phone to opt out 2: format; yes_voice__no_text__123-456-7890
   * @return phone2
   */
  public String getPhone2() {
    return attributeValueString(TwoFactorUserAttrName.phone2);
  }

  /**
   * phone to opt out 2: format; yes_voice__no_text__123-456-7890
   * @param phone2
   */
  public void setPhone2(String phone2) {
    this.attribute(TwoFactorUserAttrName.phone2, true).setAttributeValueString(phone2);
  }

  /**
   * userUuid 0 of the colleague who can unlock this user
   * @return uuid 0
   */
  public String getColleagueUserUuid0() {
    return attributeValueString(TwoFactorUserAttrName.colleague_user_uuid0);
  }

  /**
   * user 0 of the colleague who can unlock this user
   * @param twoFactorDaoFactory1
   * @return user 0
   */
  public TwoFactorUser colleagueUser0(TwoFactorDaoFactory twoFactorDaoFactory1) {
    String userUuid = attributeValueString(TwoFactorUserAttrName.colleague_user_uuid0);
    if (StringUtils.isBlank(userUuid)) {
      return null;
    }
    return twoFactorDaoFactory1.getTwoFactorUser().retrieveByUuid(userUuid);
  }

  /**
   * user 1 of the colleague who can unlock this user
   * @param twoFactorDaoFactory1
   * @return user 1
   */
  public TwoFactorUser colleagueUser1(TwoFactorDaoFactory twoFactorDaoFactory1) {
    String userUuid = attributeValueString(TwoFactorUserAttrName.colleague_user_uuid1);
    if (StringUtils.isBlank(userUuid)) {
      return null;
    }
    return twoFactorDaoFactory1.getTwoFactorUser().retrieveByUuid(userUuid);
  }

  /**
   * user 2 of the colleague who can unlock this user
   * @param twoFactorDaoFactory1
   * @return user 2
   */
  public TwoFactorUser colleagueUser2(TwoFactorDaoFactory twoFactorDaoFactory1) {
    String userUuid = attributeValueString(TwoFactorUserAttrName.colleague_user_uuid2);
    if (StringUtils.isBlank(userUuid)) {
      return null;
    }
    return twoFactorDaoFactory1.getTwoFactorUser().retrieveByUuid(userUuid);
  }

  /**
   * user 3 of the colleague who can unlock this user
   * @param twoFactorDaoFactory1
   * @return user 3
   */
  public TwoFactorUser colleagueUser3(TwoFactorDaoFactory twoFactorDaoFactory1) {
    String userUuid = attributeValueString(TwoFactorUserAttrName.colleague_user_uuid3);
    if (StringUtils.isBlank(userUuid)) {
      return null;
    }
    return twoFactorDaoFactory1.getTwoFactorUser().retrieveByUuid(userUuid);
  }

  /**
   * user 4 of the colleague who can unlock this user
   * @param twoFactorDaoFactory1
   * @return user 4
   */
  public TwoFactorUser colleagueUser4(TwoFactorDaoFactory twoFactorDaoFactory1) {
    String userUuid = attributeValueString(TwoFactorUserAttrName.colleague_user_uuid4);
    if (StringUtils.isBlank(userUuid)) {
      return null;
    }
    return twoFactorDaoFactory1.getTwoFactorUser().retrieveByUuid(userUuid);
  }

  /**
   * userUuid 0 of the colleague who can unlock this user
   * @param colleagueUserUuid0
   */
  public void setColleagueUserUuid0(String colleagueUserUuid0) {
    this.attribute(TwoFactorUserAttrName.colleague_user_uuid0, true).setAttributeValueString(colleagueUserUuid0);
  }

  /**
   * userUuid 1 of the colleague who can unlock this user
   * @return uuid 1
   */
  public String getColleagueUserUuid1() {
    return attributeValueString(TwoFactorUserAttrName.colleague_user_uuid1);
  }

  /**
   * userUuid 1 of the colleague who can unlock this user
   * @param colleagueUserUuid1
   */
  public void setColleagueUserUuid1(String colleagueUserUuid1) {
    this.attribute(TwoFactorUserAttrName.colleague_user_uuid1, true).setAttributeValueString(colleagueUserUuid1);
  }

  /**
   * userUuid 2 of the colleague who can unlock this user
   * @return uuid 2
   */
  public String getColleagueUserUuid2() {
    return attributeValueString(TwoFactorUserAttrName.colleague_user_uuid2);
  }

  /**
   * userUuid 2 of the colleague who can unlock this user
   * @param colleagueUserUuid2
   */
  public void setColleagueUserUuid2(String colleagueUserUuid2) {
    this.attribute(TwoFactorUserAttrName.colleague_user_uuid2, true).setAttributeValueString(colleagueUserUuid2);
  }

  /**
   * userUuid 3 of the colleague who can unlock this user
   * @return uuid 3
   */
  public String getColleagueUserUuid3() {
    return attributeValueString(TwoFactorUserAttrName.colleague_user_uuid3);
  }

  /**
   * userUuid 3 of the colleague who can unlock this user
   * @param colleagueUserUuid3
   */
  public void setColleagueUserUuid3(String colleagueUserUuid3) {
    this.attribute(TwoFactorUserAttrName.colleague_user_uuid3, true).setAttributeValueString(colleagueUserUuid3);
  }

  /**
   * userUuid 4 of the colleague who can unlock this user
   * @return uuid 4
   */
  public String getColleagueUserUuid4() {
    return attributeValueString(TwoFactorUserAttrName.colleague_user_uuid4);
  }

  /**
   * userUuid 4 of the colleague who can unlock this user
   * @param colleagueUserUuid4
   */
  public void setColleagueUserUuid4(String colleagueUserUuid4) {
    this.attribute(TwoFactorUserAttrName.colleague_user_uuid4, true).setAttributeValueString(colleagueUserUuid4);
  }

  /**
   * email address for notifications
   * @return email address
   */
  public String getEmail0() {
    return attributeValueString(TwoFactorUserAttrName.email0);
  }

  /**
   * 
   * @return birth day
   */
  public String getBirthDayUuid() {
    return attributeValueString(TwoFactorUserAttrName.birth_day_uuid);
  }

  /**
   * birthday uuid is a session param that indicates the user has inputted the correct birthday
   * @param birthDayUuid
   */
  public void setBirthDayUuid(String birthDayUuid) {
    this.attribute(TwoFactorUserAttrName.birth_day_uuid, true).setAttributeValueString(birthDayUuid);
  }

  /**
   * email address for notifications
   * @param email0
   */
  public void setEmail0(String email0) {
    this.attribute(TwoFactorUserAttrName.email0, true).setAttributeValueString(email0);
  }

  /**
   * the millis since 1970 div 60k that the last successful totp pass was used
   * @return the LastTotpTimestampUsed
   */
  public Long getLastTotp60TimestampUsed() {
    return this.attribute(TwoFactorUserAttrName.last_totp60_timestamp_used, true).getAttributeValueInteger();
  }

  /**
   * the millis since 1970 div 60k that the last successful totp pass was used
   * @param lastTotp60TimestampUsed the lastTotpTimestampUsed to set
   */
  public void setLastTotp60TimestampUsed(Long lastTotp60TimestampUsed) {
    this.attribute(TwoFactorUserAttrName.last_totp60_timestamp_used, true)
      .setAttributeValueInteger(lastTotp60TimestampUsed);
  }

  /**
   * if this user has invited colleagues to opt them out within the requisite amount of time
   * @return if allowed to be opted out by colleagues
   */
  public boolean isInvitedColleaguesWithinAllottedTime() {
    
    Long dateInvitedColleagues = this.getDateInvitedColleagues();

    int hoursInvitesLast = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.hoursInviteColleaguesLast", 2);

    if (dateInvitedColleagues != null && (System.currentTimeMillis() - dateInvitedColleagues) / (60 * 60 * 1000) < hoursInvitesLast ) {

      return true;
    }
    return false;

  }

  /**
   * two factor secret formatted
   * @return the secret formatted
   */
  public String getTwoFactorSecretUnencryptedFormatted() {
    String twoFactorSecret = this.getTwoFactorSecret();
    if (twoFactorSecret == null) {
      return twoFactorSecret;
    }
    String theTwoFactorSecret = this.getTwoFactorSecretUnencrypted();
    
    
    //strip whitespace
    return formatSecret(theTwoFactorSecret);
  }

  /**
   * two factor secret formatted
   * @return the secret formatted
   */
  public String getTwoFactorSecretUnencryptedHexFormatted() {
    String twoFactorSecret = this.getTwoFactorSecret();
    if (twoFactorSecret == null) {
      return twoFactorSecret;
    }
    String theTwoFactorSecret = this.getTwoFactorSecretUnencrypted();
    return TwoFactorServerUtils.base32toHexFormatted(theTwoFactorSecret);
  }

  /**
   * two factor secret hex
   * @return the secret formatted
   */
  public String getTwoFactorSecretUnencryptedHex() {
    String twoFactorSecret = this.getTwoFactorSecret();
    if (twoFactorSecret == null) {
      return twoFactorSecret;
    }
    String theTwoFactorSecret = this.getTwoFactorSecretUnencrypted();
    return TwoFactorServerUtils.base32toHex(theTwoFactorSecret);
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(TwoFactorUser o) {
    if (o == null) {
      return 1;
    }
    String thisDescription = this.getDescription();
    String otherDescription = o.getDescription();
    if (StringUtils.equals(thisDescription, otherDescription)) {
      return 0;
    }
    if (thisDescription == null) {
      return -1;
    }
    if (otherDescription == null) {
      return 1;
    }
    return thisDescription.compareTo(otherDescription);
  }

  /**
   * json of encrypted 6 digit one-time use codes, 10 of them.  use from here instead of generating more.  mark as "sent" when sent to user
   * @return phone code encrypted
   */
  public String getBypassCodes() {
    return attributeValueString(TwoFactorUserAttrName.bypass_codes);
  }

  /**
   * json of encrypted 6 digit one-time use codes, 10 of them.  use from here instead of generating more.  mark as "sent" when sent to user
   * @param bypassCodes
   */
  public void setBypassCodes(String bypassCodes) {
    this.attribute(TwoFactorUserAttrName.bypass_codes, true).setAttributeValueString(bypassCodes);
  }

  /**
   * get a bypasscode
   * @return the bypasscode
   */
  public String retrieveBypassCode() {
    String bypassCodes = this.getBypassCodes();
    if (StringUtils.isBlank(bypassCodes)) {
//      bypassCodes = 
    }
    
    return null;
  }
  
  public String generateNewBypassCodes() {

    boolean useDuoForPasscode = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("tfServer.useDuoForPasscode", true);

    List<String> passcodes = null;
    
    //maybe going from duo
    //opt in to duo
    if (useDuoForPasscode && UiMain.duoRegisterUsers() && !StringUtils.isBlank(this.getDuoUserId())) {

//      passcodes = DuoCommands.duoBypassCodesByUserId(this.getDuoUserId());
      
    } else {

      passcodes = new ArrayList<String>();
      
      for (int i=0;i<10;i++) {
        
        //store code and when sent
        String secretCode = Integer.toString(new SecureRandom().nextInt(1000000));
        
        //make this since 9 since thats what duo is
        secretCode = StringUtils.leftPad(secretCode, 9, '0');

        passcodes.add(secretCode);
      }
      
    }
    
//    twoFactorUser.setPhoneCodeUnencrypted(secretCode);
    return null;
  }

  /**
   * 
   * @return if admin
   */
  public boolean isAdmin24() {
    
    if (StringUtils.isBlank(this.loginid)) {
      throw new RuntimeException("Why is loginid blank???");
    }
    
    if (useAdmin24Cache == null) {
      synchronized (TwoFactorUser.class) {
        if (useAdmin24Cache == null) {
          int cacheForMinutes = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.admin24CacheMinutes", 2);
          if (cacheForMinutes == 0) {
            
            TwoFactorUser.useAdmin24Cache = false;
            
          } else {
            
            admin24Cache = new ExpirableCache<String, Boolean>(cacheForMinutes);
            TwoFactorUser.useAdmin24Cache = true;
            
          }
          
        }
      }
    }
    
    //see if in cache
    if (TwoFactorUser.useAdmin24Cache) {
      Boolean isAdmin24 = admin24Cache.get(this.getLoginid());
      if (isAdmin24 != null) {
        return isAdmin24;
      }
    }
  
    TwoFactorAuthorizationInterface twoFactorAuthorizationInterface = TwoFactorServerConfig.retrieveConfig().twoFactorAuthorization();
    Set<String> userIds = twoFactorAuthorizationInterface.admin24UserIds();
    
    Source theSource = this.subjectSource;
    if (theSource == null) {
      theSource = TfSourceUtils.mainSource();
    }
    
    boolean isAdmin24 = TfSourceUtils.subjectIdOrNetIdInSet(theSource, this.loginid, userIds);
    
    if (!isAdmin24) {
      //see if there is a different loginId
      String userId = TwoFactorFilterJ2ee.retrieveUserIdFromRequestOriginalNotActAs(false);
      isAdmin24 = !StringUtils.isBlank(userId) && TfSourceUtils.subjectIdOrNetIdInSet(theSource, userId, userIds);
    }
    
    if (TwoFactorUser.useAdmin24Cache) {
      admin24Cache.put(this.loginid, isAdmin24);
    }
    
    return isAdmin24;
  }
  
}
  
