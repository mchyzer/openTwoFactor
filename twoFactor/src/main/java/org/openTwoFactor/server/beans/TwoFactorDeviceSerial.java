/**
 * @author mchyzer
 * $Id: TwoFactorUser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.encryption.EncryptionKey;
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
 * two factor user
 */
@SuppressWarnings("serial")
public class TwoFactorDeviceSerial extends TwoFactorHibernateBeanBase {

  /**
   * two factor secret hsah
   */
  private String twoFactorSecretHash;
  
  /**
   * two factor secret hsah
   * @return hash
   */
  public String getTwoFactorSecretHash() {
    return this.twoFactorSecretHash;
  }

  /**
   * two factor secret hsah
   * @param twoFactorSecretHash1
   */
  public void setTwoFactorSecretHash(String twoFactorSecretHash1) {
    this.twoFactorSecretHash = twoFactorSecretHash1;
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
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorDeviceSerial dbTwoFactorUser = (TwoFactorDeviceSerial)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(dbTwoFactorUser, this, DB_VERSION_FIELDS);
    
  }
  
  /** constant for field name for: serialNumber */
  public static final String FIELD_SERIAL_NUMBER = "serialNumber";

  /** constant for field name for: twoFactorSecret */
  public static final String FIELD_TWO_FACTOR_SECRET = "twoFactorSecret";

  /** constant for field name for: twoFactorSecretHash */
  public static final String FIELD_TWO_FACTOR_SECRET_HASH = "twoFactorSecretHash";

  /** constant for field name for: userUuid */
  public static final String FIELD_USER_UUID = "userUuid";

  /** constant for field name for: whenRegistered */
  public static final String FIELD_WHEN_REGISTERED = "whenRegistered";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_SERIAL_NUMBER,
      FIELD_TWO_FACTOR_SECRET,
      FIELD_TWO_FACTOR_SECRET_HASH,
      FIELD_USER_UUID,
      FIELD_WHEN_REGISTERED,
      FIELD_VERSION_NUMBER));

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbVersionReset()
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.assignDbVersion(TwoFactorServerUtils.clone(this, DB_VERSION_FIELDS));

  }

  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_SERIAL_NUMBER,
      FIELD_TWO_FACTOR_SECRET,
      FIELD_TWO_FACTOR_SECRET_HASH,
      FIELD_USER_UUID,
      FIELD_WHEN_REGISTERED,
      FIELD_VERSION_NUMBER));


  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_SERIAL_NUMBER,
      FIELD_TWO_FACTOR_SECRET,
      FIELD_TWO_FACTOR_SECRET_HASH,
      FIELD_USER_UUID,
      FIELD_WHEN_REGISTERED,
      FIELD_VERSION_NUMBER));

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
   * serial number on the device
   */
  private String serialNumber;
  
  /**
   * encrypted secret for that device
   */
  private String twoFactorSecret;
  
  /**
   * uuid of the user who registered this fob
   */
  private String userUuid;
  
  /**
   * millis since 1970 that this secret was registered
   */
  private long whenRegistered;
  
  /**
   * serial number on the device
   * @return serial number
   */
  public String getSerialNumber() {
    return this.serialNumber;
  }

  /**
   * serial number on the device
   * @param serialNumber1
   */
  public void setSerialNumber(String serialNumber1) {
    this.serialNumber = serialNumber1;
  }

  /**
   * encrypted secret for that device
   * @return two factor secret
   */
  public String getTwoFactorSecret() {
    return this.twoFactorSecret;
  }

  /**
   * encrypted secret for that device
   * @param twoFactorSecretr1
   * 
   */
  public void setTwoFactorSecret(String twoFactorSecret) {
    this.twoFactorSecret = twoFactorSecret;
  }

  /**
   * @param twoFactorSecretUnencrypted
   */
  public void setTwoFactorSecretUnencrypted(String twoFactorSecretUnencrypted) {
    this.setTwoFactorSecret(EncryptionKey.encrypt(twoFactorSecretUnencrypted));
    this.setTwoFactorSecretHash(StringUtils.isBlank(twoFactorSecretUnencrypted) ? null : TwoFactorServerUtils.encryptSha(twoFactorSecretUnencrypted));
  }

  /**
   * two factor secret encrypted
   * @return the secret encrypted
   */
  public String getTwoFactorSecretUnencrypted() {
    return EncryptionKey.decrypt(this.getTwoFactorSecret());
  }

  /**
   * uuid of the user who registered this fob
   * @return user uuid
   */
  public String getUserUuid() {
    return this.userUuid;
  }

  /**
   * uuid of the user who registered this fob
   * @param userUuid1
   */
  public void setUserUuid(String userUuid1) {
    this.userUuid = userUuid1;
  }

  /**
   * millis since 1970 that this secret was registered
   * @return when registered
   */
  public long getWhenRegistered() {
    return this.whenRegistered;
  }

  /**
   * millis since 1970 that this secret was registered
   * @param whenRegistered1
   */
  public void setWhenRegistered(long whenRegistered1) {
    this.whenRegistered = whenRegistered1;
  }

  /**
   * retrieve a device serial record by secret unencrypted
   * @param twoFactorDaoFactory
   * @param secretUnencrypted
   * @return the user or null if not found
   */
  public static TwoFactorDeviceSerial retrieveBySecretUnencrypted(
      final TwoFactorDaoFactory twoFactorDaoFactory, final String secretUnencrypted) {

    if (TwoFactorServerUtils.isBlank(secretUnencrypted)) {
      throw new RuntimeException("Why is secretUnencrypted blank? ");
    }
    
    TwoFactorDeviceSerial result = (TwoFactorDeviceSerial)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorDeviceSerial twoFactorDeviceSerial = twoFactorDaoFactory.getTwoFactorDeviceSerial()
            .retrieveBySecretHash(TwoFactorServerUtils.encryptSha(secretUnencrypted));
        
        return twoFactorDeviceSerial;
        
      }
    });
    
    return result;
    
  }

  /**
   * retrieve by user uuid
   * @param twoFactorDaoFactory
   * @param userUuid
   * @return device serials that match
   */
  public static Set<TwoFactorDeviceSerial> retrieveByUserUuid(
      final TwoFactorDaoFactory twoFactorDaoFactory, final String userUuid) {

    if (TwoFactorServerUtils.isBlank(userUuid)) {
      throw new RuntimeException("Why is userUuid blank? ");
    }
    
    Set<TwoFactorDeviceSerial> result = (Set<TwoFactorDeviceSerial>)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        Set<TwoFactorDeviceSerial> twoFactorDeviceSerials = twoFactorDaoFactory.getTwoFactorDeviceSerial()
            .retrieveByUserUuid(userUuid);
        
        return twoFactorDeviceSerials;
        
      }
    });
    
    return result;
    
  }

  /**
   * retrieve a device serial record by serial number
   * @param twoFactorDaoFactory
   * @param loginid
   * @return the user or null if not found
   */
  public static TwoFactorDeviceSerial retrieveBySerial(final TwoFactorDaoFactory twoFactorDaoFactory, final String theSerial) {

    if (TwoFactorServerUtils.isBlank(theSerial)) {
      throw new RuntimeException("Why is serial blank? ");
    }
    
    TwoFactorDeviceSerial result = (TwoFactorDeviceSerial)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorDeviceSerial twoFactorDeviceSerial = twoFactorDaoFactory.getTwoFactorDeviceSerial().retrieveBySerial(theSerial);
        
        return twoFactorDeviceSerial;
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
  public static TwoFactorDeviceSerial retrieveByUuid(final TwoFactorDaoFactory twoFactorDaoFactory, final String uuid) {

    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorDeviceSerial result = (TwoFactorDeviceSerial)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorDeviceSerial twoFactorDeviceSerial = twoFactorDaoFactory.getTwoFactorDeviceSerial().retrieveByUuid(uuid);
        
        return twoFactorDeviceSerial;
      }

    });
    
    return result;
    
  }
  
  /**
   * store this object and audit
   * @param twoFactorDaoFactory 
   * @return if changed
   */
  public boolean store(final TwoFactorDaoFactory twoFactorDaoFactory) {
    return this.storeHelper(twoFactorDaoFactory);
  }
  
  /**
   * store this object and audit
   * @param twoFactorDaoFactory 
   * @return if changed
   */
  private boolean storeHelper(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    if (StringUtils.isBlank(this.serialNumber)) {
      throw new RuntimeException("serialNumber is null");
    }
    if (StringUtils.isBlank(this.twoFactorSecret)) {
      throw new RuntimeException("twoFactorSecret is null");
    }

    return (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        TwoFactorDeviceSerial dbVersion = (TwoFactorDeviceSerial)TwoFactorDeviceSerial.this.dbVersion();

        boolean hadChange = false;
        
        if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, TwoFactorDeviceSerial.this)) {
          twoFactorDaoFactory.getTwoFactorDeviceSerial().store(TwoFactorDeviceSerial.this);
        
          testInsertsAndUpdates++;
          hadChange = true;
        }

        if (hadChange) {
          hibernateSession.misc().flush();
          TwoFactorDeviceSerial.this.dbVersionReset();
        }
        return hadChange;
      }
    });

  }

  /**
   * delete this record
   * @param twoFactorDaoFactory is the factor to use
   */
  @Override
  public void delete(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorDaoFactory.getTwoFactorDeviceSerial().delete(TwoFactorDeviceSerial.this);
        testDeletes++;
        return null;
      }
    });

  }

//  /** logger */
//  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorDeviceSerial.class);

}
  
