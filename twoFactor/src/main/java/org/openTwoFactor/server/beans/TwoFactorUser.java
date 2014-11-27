/**
 * @author mchyzer
 * $Id: TwoFactorUser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.security.SecureRandom;
import java.util.Collections;
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
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * two factor user
 */
@SuppressWarnings("serial")
public class TwoFactorUser extends TwoFactorHibernateBeanBase {

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
   * description from subject source, or if not found, the loginid
   * 
   * @return the description from subject source, or if not found, the loginid
   */
  public String getDescription() {
    Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(this.subjectSource, this.getLoginid(), true, false, false);
    if (subject != null) {
      return TfSourceUtils.subjectDescription(subject);
    }
    return this.getLoginid();
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
   * 
   */
  private static ExpirableCache<String, Boolean> adminCache = null;

  /**
   * 
   */
  private static Boolean useAdminCache = null;
  
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
      throw new RuntimeException("Why asking for integer when type is: " 
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
      twoFactorUser.setAttributes(twoFactorUserAttrsSet);
      ((TwoFactorUser)twoFactorUser.dbVersion()).setAttributes(TwoFactorServerUtils.cloneValue(twoFactorUserAttrsSet));
    }
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

    return (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        TwoFactorUser dbVersion = (TwoFactorUser)TwoFactorUser.this.dbVersion();

        boolean hadChange = false;
        
        if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, TwoFactorUser.this)) {
          twoFactorDaoFactory1.getTwoFactorUser().store(TwoFactorUser.this);
        
          testInsertsAndUpdates++;
          hadChange = true;
        }

        //sync up the attributes
        Map<String, TwoFactorUserAttr> newAttributes = TwoFactorUser.this.attributeMap();
        Map<String, TwoFactorUserAttr> oldAttributes = dbVersion == null ? new HashMap<String, TwoFactorUserAttr>() : dbVersion.attributeMap();
        
//        //###################### INSERTS
//        {
//          Set<String> fieldsForInsert = new HashSet<String>(newAttributes.keySet());
//          fieldsForInsert.removeAll(oldAttributes.keySet());
//          
//          for (String fieldForInsert : fieldsForInsert) {
//            twoFactorDaoFactory.getTwoFactorUserAttr().store(newAttributes.get(fieldForInsert));
//            newAttributes.remove(fieldForInsert);
//            hadChange = true;
//          }
//        }
        
        //###################### DELETES
        {
          Set<String> fieldsForDelete = new HashSet<String>(oldAttributes.keySet());
          fieldsForDelete.removeAll(newAttributes.keySet());
          
          for (String fieldForDelete : fieldsForDelete) {
            oldAttributes.get(fieldForDelete).delete(twoFactorDaoFactory1);
            oldAttributes.remove(fieldForDelete);
            hadChange = true;
          }
        }
        
        //###################### UPDATES
        {          
          for (String fieldForUpdate : newAttributes.keySet()) {
            TwoFactorUserAttr newFactorUserAttr = newAttributes.get(fieldForUpdate);
//            TwoFactorUserAttr oldFactorUserAttr = oldAttributes.get(fieldForUpdate);
//            if (TwoFactorServerUtils.dbVersionDifferent(oldFactorUserAttr, newFactorUserAttr)) {
//              twoFactorDaoFactory.getTwoFactorUserAttr().store(newFactorUserAttr);
//              hadChange = true;
//            }
            //this will insert or update and keep track of state
            boolean attrChanged = newFactorUserAttr.store(twoFactorDaoFactory1);
            hadChange = hadChange || attrChanged;
          }
        }
        if (hadChange) {
          hibernateSession.misc().flush();
          TwoFactorUser.this.dbVersionReset();
        }
        return hadChange;
      }
    });

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

        for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorServerUtils.nonNull(TwoFactorUser.this.getAttributes())) {
          
          twoFactorUserAttr.delete(twoFactorDaoFactory1);
          
        }
        
        for (TwoFactorBrowser twoFactorBrowser : TwoFactorServerUtils.nonNull(TwoFactorBrowser.retrieveByUserUuid(twoFactorDaoFactory1, TwoFactorUser.this.getUuid(), true))) {
          
          twoFactorBrowser.delete(twoFactorDaoFactory1);
          
        }
        
        twoFactorDaoFactory1.getTwoFactorUser().delete(TwoFactorUser.this);
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
   * @return browser id __ duo tx id for push 
   */
  public String getDuoPushTransactionId() {
    return attributeValueString(TwoFactorUserAttrName.duo_push_transaction_id);
  }

  /**
   * millis since 1970 __ browser id __ duo tx id for push
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

    int daysInvitesLast = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.daysInviteColleaguesLast", 7);

    if (dateInvitedColleagues != null && (System.currentTimeMillis() - dateInvitedColleagues) / (24D * 60 * 60 * 1000) < daysInvitesLast ) {

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
}
  
