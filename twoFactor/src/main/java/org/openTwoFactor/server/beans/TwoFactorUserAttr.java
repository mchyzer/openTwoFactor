/**
 * @author mchyzer
 * $Id: TwoFactorUserAttr.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * user field for if passes have been given to the user, this is the last index
 */
@SuppressWarnings("serial")
public class TwoFactorUserAttr extends TwoFactorHibernateBeanBase implements Comparable<TwoFactorUserAttr> {


  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;

  /**
   * number of inserts and updates
   */
  static int testDeletes = 0;

  
  /**
   * types of attributes
   */
  public static enum TwoFactorUserAttrType {
    
    /** boolean, T|F string */
    booleanType,
    
    /** string */
    string,
    
    /** integer */
    integer;
  }
  
  /**
   * name of attributes in user object
   */
  public static enum TwoFactorUserAttrName {

    /**
     * encrypted 6 digit one-time use phone code
     */
    phone_code_encrypted(TwoFactorUserAttrType.string),
    
    /**
     * when the phone code was sent
     */
    date_phone_code_sent(TwoFactorUserAttrType.integer),
    
    /**
     * date in millis since 1970 that the user invited colleagues to opt him out
     */
    date_invited_colleagues(TwoFactorUserAttrType.integer),
    
    /**
     * T|F if the user has opted in
     */
    opted_in(TwoFactorUserAttrType.booleanType), 

    /**
     * password given to the user, last index, which would be revoked if user specifies
     */
    sequential_pass_given_to_user(TwoFactorUserAttrType.integer),

    /**
     * password index given to the user, starts at 500001
     */
    sequential_pass_index(TwoFactorUserAttrType.integer), 

    /**
     * two factor secret in the users phone
     */
    two_factor_secret(TwoFactorUserAttrType.string),

    /**
     * temp password that the user will try
     */
    two_factor_secret_temp(TwoFactorUserAttrType.string),

    /**
     * the millis since 1970 div 30k that the last successful totp pass was used
     */
    last_totp_timestamp_used(TwoFactorUserAttrType.integer),

    /**
     * the millis since 1970 div 60k that the last successful totp pass was used
     */
    last_totp60_timestamp_used(TwoFactorUserAttrType.integer),
    
    /**
     * index of the hardware token, if there is one, starts at 0
     */
    token_index(TwoFactorUserAttrType.integer),

    /**
     * phone to opt out 0
     */
    phone0(TwoFactorUserAttrType.string),

    /**
     * phone to opt out 1
     */
    phone1(TwoFactorUserAttrType.string),

    /**
     * phone to opt out 2
     */
    phone2(TwoFactorUserAttrType.string),

    /**
     * if phone 0 text
     */
    phone_is_text0(TwoFactorUserAttrType.booleanType),

    /**
     * if phone 0 voice
     */
    phone_is_voice0(TwoFactorUserAttrType.booleanType),

    /**
     * if phone 1 text
     */
    phone_is_text1(TwoFactorUserAttrType.booleanType),

    /**
     * if phone 1 voice
     */
    phone_is_voice1(TwoFactorUserAttrType.booleanType),

    /**
     * if phone 2 text
     */
    phone_is_text2(TwoFactorUserAttrType.booleanType),

    /**
     * if phone 2 voice
     */
    phone_is_voice2(TwoFactorUserAttrType.booleanType),

    /**
     * userUuid 0 of the colleague who can unlock this user
     */
    colleague_user_uuid0(TwoFactorUserAttrType.string),

    /**
     * userUuid 1 of the colleague who can unlock this user
     */
    colleague_user_uuid1(TwoFactorUserAttrType.string),

    /**
     * userUuid 2 of the colleague who can unlock this user
     */
    colleague_user_uuid2(TwoFactorUserAttrType.string),

    /**
     * userUuid 3 of the colleague who can unlock this user
     */
    colleague_user_uuid3(TwoFactorUserAttrType.string),

    /**
     * userUuid 4 of the colleague who can unlock this user
     */
    colleague_user_uuid4(TwoFactorUserAttrType.string),

    /**
     * email address for notifications
     */
    email0(TwoFactorUserAttrType.string);

    /**
     * construct with type
     * @param twoFactorUserAttributeType
     */
    private TwoFactorUserAttrName(TwoFactorUserAttrType twoFactorUserAttributeType) {
      this.theType = twoFactorUserAttributeType;
    }
    
    /**
     * type of attribute
     */
    private TwoFactorUserAttrType theType;

    
    /**
     * type of attribute
     * @return the theType
     */
    public TwoFactorUserAttrType getTheType() {
      return this.theType;
    }

    /**
     * take a string and convert to enum
     * @param string
     * @return the enum
     */
    public static TwoFactorUserAttrName valueOfIgnoreCase(String string) {
      return TwoFactorServerUtils.enumValueOfIgnoreCase(TwoFactorUserAttrName.class, string, false, true);
    }
        
  }
  
  /**
   * timestamp used for encrypting the data if applicable
   */
  private String encryptionTimestamp;
  
  /**
   * timestamp used for encrypting the data if applicable
   * @return the encryptionTimestamp
   */
  public String getEncryptionTimestamp() {
    return this.encryptionTimestamp;
  }
  
  /**
   * timestamp used for encrypting the data if applicable
   * @param encryptionTimestamp1 the encryptionTimestamp to set
   */
  public void setEncryptionTimestamp(String encryptionTimestamp1) {
    this.encryptionTimestamp = encryptionTimestamp1;
  }

  /**
   * if this is an integer attribute, this is the value
   */
  private Long attributeValueInteger;
  
  
  /**
   * if this is an integer attribute, this is the value
   * @return the attributeValueInteger
   */
  public Long getAttributeValueInteger() {
    return this.attributeValueInteger;
  }

  
  /**
   * if this is an integer attribute, this is the value
   * @param attributeValueInteger1 the attributeValueInteger to set
   */
  public void setAttributeValueInteger(Long attributeValueInteger1) {
    this.attributeValueInteger = attributeValueInteger1;
  }

  /**
   * if this is an integer attribute, this is the value
   * @return the attributeValueInteger
   */
  public Boolean getAttributeValueBoolean() {
    return TwoFactorServerUtils.booleanObjectValue(this.attributeValueString);
  }

  
  /**
   * if this is an integer attribute, this is the value
   * @param attributeValueBoolean1 the attributeValueInteger to set
   */
  public void setAttributeValueBoolean(Boolean attributeValueBoolean1) {
    this.attributeValueString = attributeValueBoolean1 == null ? 
        null : (attributeValueBoolean1 ? "T" : "F") ;
  }



  /**
   * if this is a string or boolean attribute, this is the value
   */
  private String attributeValueString;
  
  /**
   * if this is a string or boolean attribute, this is the value
   * @return the attributeValueString
   */
  public String getAttributeValueString() {
    return this.attributeValueString;
  }
  
  /**
   * if this is a string or boolean attribute, this is the value
   * @param attributeValueString1 the attributeValueString to set
   */
  public void setAttributeValueString(String attributeValueString1) {
    this.attributeValueString = attributeValueString1;
  }



  /**
   * primary key, and also foreign key to two_factor_user
   */
  private String userUuid;
  
  
  /**
   * primary key, and also foreign key to two_factor_user
   * @return the userUuid
   */
  public String getUserUuid() {
    return this.userUuid;
  }

  
  /**
   * primary key, and also foreign key to two_factor_user
   * @param userUuid1 the userUuid to set
   */
  public void setUserUuid(String userUuid1) {
    this.userUuid = userUuid1;
  }



  /**
   * name of the attribute for this user: loginid, opted_in, 
   * sequential_pass_given_to_user, sequential_pass_index, 
   * two_factor_secret, two_factor_secret_temp
   */
  private TwoFactorUserAttrName attributeName;

  /** constant for field name for: attributeName */
  public static final String FIELD_ATTRIBUTE_NAME = "attributeName";

  /** constant for field name for: attributeValueInteger */
  public static final String FIELD_ATTRIBUTE_VALUE_INTEGER = "attributeValueInteger";

  /** constant for field name for: attributeValueString */
  public static final String FIELD_ATTRIBUTE_VALUE_STRING = "attributeValueString";

  /** constant for field name for: encryptionTimestamp */
  public static final String FIELD_ENCRYPTION_TIMESTAMP = "encryptionTimestamp";

  /** constant for field name for: userUuid */
  public static final String FIELD_USER_UUID = "userUuid";

  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_ATTRIBUTE_NAME,
      FIELD_ATTRIBUTE_VALUE_INTEGER,
      FIELD_ATTRIBUTE_VALUE_STRING,
      FIELD_DELETED_ON,
      FIELD_ENCRYPTION_TIMESTAMP,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_USER_UUID,
      FIELD_VERSION_NUMBER));

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_ATTRIBUTE_NAME,
      FIELD_ATTRIBUTE_VALUE_INTEGER,
      FIELD_ATTRIBUTE_VALUE_STRING,
      FIELD_DELETED_ON,
      FIELD_ENCRYPTION_TIMESTAMP,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_USER_UUID,
      FIELD_VERSION_NUMBER));
  
  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(this.getAttributeName() + " - ");
    if (this.getAttributeName() != null) {
      switch(this.getAttributeNameEnum().getTheType()) {
        case booleanType:
          result.append(this.getAttributeValueBoolean());
          break;
        case integer:
          result.append(this.getAttributeValueInteger());
          break;
        case string:
          result.append(this.getAttributeValueString());
          break;
      }
    }
    return result.toString();
  }

  /**
   * name of the attribute for this user: loginid, opted_in, 
   * sequential_pass_given_to_user, sequential_pass_index, 
   * two_factor_secret, two_factor_secret_temp
   * 
   * @return the attributeName
   */
  public String getAttributeName() {
    return this.attributeName == null ? null : this.attributeName.name();
  }
  
  /**
   * name of the attribute for this user: loginid, opted_in, 
   * sequential_pass_given_to_user, sequential_pass_index, 
   * two_factor_secret, two_factor_secret_temp
   * @param attributeName1 the attributeName to set
   */
  public void setAttributeName(String attributeName1) {
    this.attributeName = TwoFactorUserAttrName.valueOfIgnoreCase(attributeName1);
  }

  /**
   * attribute name
   * @param twoFactorUserAttrName
   */
  public void setAttributeNameEnum(TwoFactorUserAttrName twoFactorUserAttrName) {
    this.attributeName = twoFactorUserAttrName;
  }

  /**
   * attribute name
   * @return the attribute name
   */
  public TwoFactorUserAttrName getAttributeNameEnum() {
    return this.attributeName;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    return TwoFactorServerUtils.clone(this, CLONE_FIELDS);
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbVersionReset()
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.assignDbVersion(TwoFactorServerUtils.clone(this, DB_VERSION_FIELDS));
  
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    return false;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    return DB_VERSION_FIELDS;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(TwoFactorUserAttr o) {
    if (o == null) {
      return 1;
    }
    
    TwoFactorUserAttrName localAttributeName = this.attributeName;
    TwoFactorUserAttrName otherAttributeName = o.getAttributeNameEnum();
    return localAttributeName.compareTo(otherAttributeName);
  }

  /**
   * delete an audit record
   * @param twoFactorDaoFactory
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {

    twoFactorDaoFactory.getTwoFactorUserAttr().delete(this);
    testDeletes++;

  }
  
  /**
   * store this object and audit
   * @param twoFactorDaoFactory 
   * @return if changed
   */
  public boolean store(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    if (StringUtils.isBlank(this.getAttributeName())) {
      throw new RuntimeException("attributeName is null");
    }
    
    if (this.getAttributeName() != null && this.getAttributeName().length() > 30) {
      throw new RuntimeException("attributeName is too long (30): '" + this.getAttributeName() + "'");
    }

    if (this.getAttributeValueString() != null && this.getAttributeValueString().length() > 90) {
      throw new RuntimeException("attributeValue is too long(90): '" + this.getAttributeValueString() + "'");
    }
    
    return (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        TwoFactorUserAttr dbVersion = (TwoFactorUserAttr)TwoFactorUserAttr.this.dbVersion();

        if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, TwoFactorUserAttr.this)) {
          twoFactorDaoFactory.getTwoFactorUserAttr().store(TwoFactorUserAttr.this);
          hibernateSession.misc().flush();
          TwoFactorUserAttr.this.dbVersionReset();

          testInsertsAndUpdates++;

          return true;
        }
        return false;
      }
    });

  }

}
