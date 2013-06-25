/**
 * @author mchyzer
 * $Id: TwoFactorBrowser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.security.SecureRandom;
import java.util.Collections;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
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
 * row for each browser that a user has
 */
@SuppressWarnings("serial")
public class TwoFactorBrowser extends TwoFactorHibernateBeanBase {

  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorBrowser dbTwoFactorUser = (TwoFactorBrowser)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(dbTwoFactorUser, this, DB_VERSION_FIELDS);
    
  }

  /** constant for field name for: browserTrustedUuid */
  public static final String FIELD_BROWSER_TRUSTED_UUID = "browserTrustedUuid";

  /** constant for field name for: trustedBrowser */
  public static final String FIELD_TRUSTED_BROWSER = "trustedBrowser";

  /** constant for field name for: userUuid */
  public static final String FIELD_USER_UUID = "userUuid";

  /** constant for field name for: whenTrusted */
  public static final String FIELD_WHEN_TRUSTED = "whenTrusted";

  
  
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_BROWSER_TRUSTED_UUID,
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_TRUSTED_BROWSER,
      FIELD_UUID,
      FIELD_VERSION_NUMBER,
      FIELD_WHEN_TRUSTED
      ));

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbVersionReset()
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.assignDbVersion(TwoFactorServerUtils.clone(this, DB_VERSION_FIELDS));

  }

  /**
   * WHEN_TRUSTED          INTEGER
   * timestamp of when the browser was last trusted
   */
  private long whenTrusted;
  
  /**
   * WHEN_TRUSTED          INTEGER
   * timestamp of when the browser was last trusted
   * @return the whenTrusted
   */
  public long getWhenTrusted() {
    return this.whenTrusted;
  }
  
  /**
   * WHEN_TRUSTED          INTEGER
   * timestamp of when the browser was last trusted
   * @param whenTrusted1 the whenTrusted to set
   */
  public void setWhenTrusted(long whenTrusted1) {
    this.whenTrusted = whenTrusted1;
  }

  /**
   * TRUSTED_BROWSER       VARCHAR2(1 BYTE)        NOT NULL,
   * if the browser is trusted (indicated by user)
   */
  private boolean trustedBrowser;

  
  
  
  /**
   * TRUSTED_BROWSER       VARCHAR2(1 BYTE)        NOT NULL,
   * if the browser is trusted (indicated by user)
   * @return the trustedBrowser
   */
  public boolean isTrustedBrowser() {
    return this.trustedBrowser;
  }

  /**
   * based on when this browser was trusted, is it still trusted
   * @return true if trusted and still trusted
   */
  public boolean isTrustedBrowserCalculateDate() {
    if (!this.trustedBrowser) {
      return false;
    }
    
    int daysTrustLasts = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.trustBrowserForDays", 30);
    
    return (System.currentTimeMillis() - this.whenTrusted) < ((long)daysTrustLasts * 24L * 60L * 60L * 1000L);
    
  }
  
  /**
   * TRUSTED_BROWSER       VARCHAR2(1 BYTE)        NOT NULL,
   * if the browser is trusted (indicated by user)
   * @param trustedBrowser1 the trustedBrowser to set
   */
  public void setTrustedBrowser(boolean trustedBrowser1) {
    this.trustedBrowser = trustedBrowser1;
  }

  /**
   * BROWSER_TRUSTED_UUID  VARCHAR2(40 CHAR)       NOT NULL,
   * the cookie value of the, encrypted
   */
  private String browserTrustedUuid;
  
  /**
   * BROWSER_TRUSTED_UUID  VARCHAR2(40 CHAR)       NOT NULL,
   * the cookie value of the, encrypted
   * @return the browserTrustedUuid
   */
  public String getBrowserTrustedUuid() {
    return this.browserTrustedUuid;
  }
  
  /**
   * BROWSER_TRUSTED_UUID  VARCHAR2(40 CHAR)       NOT NULL,
   * the cookie value of the, encrypted
   * @param browserTrustedUuid1 the browserTrustedUuid to set
   */
  public void setBrowserTrustedUuid(String browserTrustedUuid1) {
    this.browserTrustedUuid = browserTrustedUuid1;
  }

  /**
   * cookie value unencrypted
   * @param browserTrustedUuidUnencrypted
   */
  public void setBrowserTrustedUuidUnencrypted(String browserTrustedUuidUnencrypted) {
    if (StringUtils.isBlank(browserTrustedUuidUnencrypted)) {
      this.setBrowserTrustedUuid(browserTrustedUuidUnencrypted); 
      
    } else {
      String browserTrustedUuidEncrypted = encryptBrowserUserUuid(browserTrustedUuidUnencrypted);
      this.setBrowserTrustedUuid(browserTrustedUuidEncrypted);
    }
  }

  /**
   * encrypt the browser user uuid
   * @param browserUserUuid
   * @return the encrypted string
   */
  public static String encryptBrowserUserUuid(String browserUserUuid) {
    
    return TwoFactorServerUtils.encryptSha(browserUserUuid);
    
  }
  
  /**
   * foreign key to the user table
   * USER_UUID             VARCHAR2(40 CHAR),
   */
  private String userUuid;

  /**
   * number of inserts and updates
   */
  static int testDeletes = 0;

  
  /**
   * foreign key to the user table
   * USER_UUID             VARCHAR2(40 CHAR),
   * @return the userUuid
   */
  public String getUserUuid() {
    return this.userUuid;
  }

  
  /**
   * foreign key to the user table
   * USER_UUID             VARCHAR2(40 CHAR),
   * @param userUuid1 the userUuid to set
   */
  public void setUserUuid(String userUuid1) {
    this.userUuid = userUuid1;
  }

  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_BROWSER_TRUSTED_UUID,
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_TRUSTED_BROWSER,
      FIELD_UUID,
      FIELD_VERSION_NUMBER,
      FIELD_WHEN_TRUSTED
      ));


  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_BROWSER_TRUSTED_UUID,
            FIELD_DELETED_ON,
            FIELD_LAST_UPDATED,
            FIELD_TRUSTED_BROWSER,
            FIELD_UUID,
            FIELD_VERSION_NUMBER,
            FIELD_WHEN_TRUSTED
        ));

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
   * get the list of browsers for a user
   * @param twoFactorDaoFactory
   * @param userUuid
   * @param allowDeleted if delete dated records should be included
   * @return the list of two factor browsers
   */
  public static List<TwoFactorBrowser> retrieveByUserUuid(
      TwoFactorDaoFactory twoFactorDaoFactory, String userUuid,
      boolean allowDeleted) {
    if (TwoFactorServerUtils.isBlank(userUuid)) {
      throw new RuntimeException("Why is userUuid blank? ");
    }

    List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser()
      .retrieveByUserUuid(userUuid);
    
    Iterator<TwoFactorBrowser> iterator = twoFactorBrowsers.iterator();
    
    if (!allowDeleted) {
      while (iterator.hasNext()) {
        TwoFactorBrowser current = iterator.next();
        if (current.isDeleted()) {
          iterator.remove();
        }
      }
    }
    
    return twoFactorBrowsers;
  }
  
  /**
   * retrieve a browser by browserTrustedUuid
   * @param twoFactorDaoFactory
   * @param browserTrustedUuidEncrypted
   * @param allowDeleted if deleted records should be returned
   * @return the user or null if not found
   */
  public static TwoFactorBrowser retrieveByBrowserTrustedUuidEncrypted(
      final TwoFactorDaoFactory twoFactorDaoFactory, 
      final String browserTrustedUuidEncrypted,
      boolean allowDeleted) {
    if (TwoFactorServerUtils.isBlank(browserTrustedUuidEncrypted)) {
      throw new RuntimeException("Why is browserTrustedUuid blank? ");
    }
    
    TwoFactorBrowser result = (TwoFactorBrowser)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorBrowser twoFactorBrowser = twoFactorDaoFactory.getTwoFactorBrowser()
          .retrieveByBrowserTrustedUuid(browserTrustedUuidEncrypted);
        
        return twoFactorBrowser;
      }
    });
    
    if (!allowDeleted && result.isDeleted()) {
      return null;
    }
    
    return result;
    
    
  }
  
  /**
   * retrieve a browser by browserTrustedUuid
   * @param twoFactorDaoFactory
   * @param browserTrustedUuidUnencrypted
   * @param allowDeleted 
   * @return the user or null if not found
   */
  public static TwoFactorBrowser retrieveByBrowserTrustedUuid(
      final TwoFactorDaoFactory twoFactorDaoFactory, final String browserTrustedUuidUnencrypted,
      boolean allowDeleted) {

    if (TwoFactorServerUtils.isBlank(browserTrustedUuidUnencrypted)) {
      throw new RuntimeException("Why is browserTrustedUuid blank? ");
    }
    String browserTrustedUuidEncrypted = encryptBrowserUserUuid(browserTrustedUuidUnencrypted);
    
    return retrieveByBrowserTrustedUuidEncrypted(
        twoFactorDaoFactory, browserTrustedUuidEncrypted, allowDeleted);
  }

  
  /**
   * store this object and audit
   * @param twoFactorDaoFactory 
   * @return if changed
   */
  public boolean store(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    if (StringUtils.isBlank(this.browserTrustedUuid)) {
      throw new RuntimeException("browserTrustedUuid is null");
    }
    
    if (this.browserTrustedUuid != null && this.browserTrustedUuid.length() > 100) {
      //generally we dont print these out, but in this case, might as well
      throw new RuntimeException("browserTrustedUuid is too long (100): '" + this.browserTrustedUuid.length() + ": " + this.browserTrustedUuid.substring(0, 10) + "...'");
    }

    
    return (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        TwoFactorBrowser dbVersion = (TwoFactorBrowser)TwoFactorBrowser.this.dbVersion();

        if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, TwoFactorBrowser.this)) {
          twoFactorDaoFactory.getTwoFactorBrowser().store(TwoFactorBrowser.this);
        
          testInsertsAndUpdates++;
          hibernateSession.misc().flush();
          TwoFactorBrowser.this.dbVersionReset();
          return true;
        }
        return false;

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
        
        twoFactorDaoFactory.getTwoFactorBrowser().delete(TwoFactorBrowser.this);
        TwoFactorBrowser.testDeletes++;
        return null;
      }
    });

  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorBrowser.class);

  /**
   * make sure the trusted matches up
   * @param twoFactorDaoFactory 
   * @param newTrusted
   * @return this or the new object if changed
   */
  private TwoFactorBrowser fixTrusted(final TwoFactorDaoFactory twoFactorDaoFactory, final boolean newTrusted) {
    if (this.isTrustedBrowser() == newTrusted) {
      return this;
    }
    final int loops = 5;
    for (int i=0;i<loops;i++) {
      try {
        final int I = i;
        final String browserUuid = this.getBrowserTrustedUuid();
        return (TwoFactorBrowser)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, 
            TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
            
            TwoFactorBrowser twoFactorBrowser = null;
            //just use this object if first loop
            if (I != 0) {
              twoFactorBrowser = TwoFactorBrowser.this;
            } else {
              //not sure why we include deleted here... hmmm
              twoFactorBrowser = retrieveByBrowserTrustedUuidEncrypted(twoFactorDaoFactory, browserUuid, true);
            }
            twoFactorBrowser.setTrustedBrowser(newTrusted);
            if (newTrusted) {
              twoFactorBrowser.setWhenTrusted(System.currentTimeMillis());
            } else {
              twoFactorBrowser.setWhenTrusted(0);
            }
            twoFactorBrowser.store(twoFactorDaoFactory);
            return twoFactorBrowser;
          }
        });
        
      } catch (Exception e) {
        String error = "Error fixing browser: " + this.getUuid() + ", " + newTrusted;
        if (i != loops-1) {
          //generally ignore
          LOG.info(error, e);
          
        } else {
          throw new RuntimeException(error, e);
        }
      }
      //sleep a random amount of time from 250ms to 500ms
      TwoFactorServerUtils.sleep(250 + new SecureRandom().nextInt(250));
    }
    throw new RuntimeException("Should never get here: " + this.getUuid());
  }

  
  /**
   * retrieve a browser by trusted uuid or create.  note, if there is one
   * which exists, and it is deleted, then if allowedDeleted is true, return it.
   * If allow deleted is false, it will be null
   * @param twoFactorDaoFactory
   * @param browserTrustedUuidUnencrypted
   * @param userUuid
   * @param trusted
   * @param allowDeleted 
   * @return the ip address
   */
  public static TwoFactorBrowser retrieveByBrowserTrustedUuidOrCreate(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final String browserTrustedUuidUnencrypted, final String userUuid, boolean trusted,
      boolean allowDeleted) {
    try {
      TwoFactorBrowser twoFactorBrowser = retrieveByBrowserTrustedUuidOrCreateHelper(
          twoFactorDaoFactory, browserTrustedUuidUnencrypted, userUuid, trusted, allowDeleted);
      
      if (twoFactorBrowser != null) {
        twoFactorBrowser = twoFactorBrowser.fixTrusted(twoFactorDaoFactory, trusted);
      }
      
      return twoFactorBrowser;
    } catch (Exception e) {
      LOG.debug("Non-fatal error getting browser: " + browserTrustedUuidUnencrypted.substring(0, 5) + ", " + userUuid, e);
      //hmm, error
    }
    //wait some time, maybe someone else created it
    TwoFactorServerUtils.sleep(250 + new SecureRandom().nextInt(250));
    //try again, throw exception if happens
    return retrieveByBrowserTrustedUuidOrCreateHelper(
        twoFactorDaoFactory, browserTrustedUuidUnencrypted, userUuid, trusted, allowDeleted);
  }

  /**
   * safely delete this object by date in a new transaction, and retry the change
   * @param twoFactorDaoFactory
   * @return the browser
   */
  public TwoFactorBrowser deleteByDate(TwoFactorDaoFactory twoFactorDaoFactory) {
    
    long now = System.currentTimeMillis();
    
    try {
      
      this.deleteByDateHelper(twoFactorDaoFactory, now);
      return this;
      
    } catch (Exception e) {
      LOG.debug("Non-fatal error saving browser: " + this.getUuid(), e);
    }
    
    //sleep a random amount of time from 250ms to 500ms
    TwoFactorServerUtils.sleep(250 + new SecureRandom().nextInt(250));

    //try to retrieve and save again
    TwoFactorBrowser twoFactorBrowser = retrieveByBrowserTrustedUuidEncrypted(
        twoFactorDaoFactory, this.getBrowserTrustedUuid(), true);
    
    twoFactorBrowser.deleteByDateHelper(twoFactorDaoFactory, now);
    return twoFactorBrowser;
    
  }

  /**
   * delete by date if not already in a new tx 
   * @param twoFactorDaoFactory
   * @param deleteDate when the delete date should be
   */
  private void deleteByDateHelper(final TwoFactorDaoFactory twoFactorDaoFactory, final long deleteDate) {

    if (this.getDeletedOn() != null && this.getDeletedOn() == deleteDate) {
      return;
    }
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorBrowser.this.setDeletedOn(deleteDate);
        TwoFactorBrowser.this.store(twoFactorDaoFactory);
        
        return null;
      }
    });
  }
  
  
  /**
   * retrieve a browser record by cookie.  note, if there is one
   * which exists, and it is deleted, then if allowedDeleted is true, return it.
   * If allow deleted is false, it will be null
   * @param twoFactorDaoFactory
   * @param browserTrustedUuidUnencrypted
   * @param userUuid
   * @param trusted
   * @param allowDeleted if allow deleted
   * @return the browser or null if not found
   */
  private static TwoFactorBrowser retrieveByBrowserTrustedUuidOrCreateHelper(
      final TwoFactorDaoFactory twoFactorDaoFactory, 
      final String browserTrustedUuidUnencrypted, final String userUuid, final boolean trusted,
      boolean allowDeleted) {
  
    //retrieve even if deleted
    TwoFactorBrowser twoFactorBrowser = retrieveByBrowserTrustedUuid(
        twoFactorDaoFactory, browserTrustedUuidUnencrypted, true);
    
    if (twoFactorBrowser != null) {
      
      if (!allowDeleted && twoFactorBrowser.isDeleted()) {
        return null;
      }
      twoFactorBrowser = twoFactorBrowser.fixTrusted(twoFactorDaoFactory, trusted);
      
      return twoFactorBrowser;
    }
    
    return (TwoFactorBrowser)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorBrowser localTwoFactorBrowser = new TwoFactorBrowser();
        localTwoFactorBrowser.setBrowserTrustedUuidUnencrypted(browserTrustedUuidUnencrypted);
        localTwoFactorBrowser.setUuid(TwoFactorServerUtils.uuid());
        localTwoFactorBrowser.setUserUuid(userUuid);
        localTwoFactorBrowser.setTrustedBrowser(trusted);
        if (trusted) {
          localTwoFactorBrowser.setWhenTrusted(System.currentTimeMillis());
        }
        localTwoFactorBrowser.store(twoFactorDaoFactory);
        
        return localTwoFactorBrowser;
      }
    });
    
  }

}
  
