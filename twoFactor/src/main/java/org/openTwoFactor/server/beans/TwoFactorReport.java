/**
 * @author mchyzer
 * $Id: TwoFactorBrowser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * row for each report we are sending out
 */
@SuppressWarnings("serial")
public class TwoFactorReport extends TwoFactorHibernateBeanBase {
  

  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorReport twoFactorReport = (TwoFactorReport)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(twoFactorReport, this, DB_VERSION_FIELDS);
    
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

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorReport.class);

  /**
   * 
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {
    // TODO Auto-generated method stub
    LOG.debug("");
  }

}
  
