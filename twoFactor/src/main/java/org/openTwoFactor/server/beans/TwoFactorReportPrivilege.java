/**
 * @author mchyzer
 * $Id: TwoFactorBrowser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;



/**
 * row for each user who has access to a report
 */
@SuppressWarnings("serial")
public class TwoFactorReportPrivilege extends TwoFactorHibernateBeanBase {

  /**
   * holds for true the list of all privileges
   */
  private static ExpirableCache<Boolean, List<TwoFactorReportPrivilege>> allPrivilegesCache = null;

  /**
   * 
   * @return the cache lazy loaded
   */
  private static ExpirableCache<Boolean, List<TwoFactorReportPrivilege>> allPrivilegesCache() {
    if (allPrivilegesCache == null) {
      allPrivilegesCache = new ExpirableCache<Boolean, List<TwoFactorReportPrivilege>>(2);
    }
    return allPrivilegesCache;
  }

  /**
   * retrieve all privileges, might be from cache
   * @param twoFactorDaoFactory 
   * @return the list of privileges
   */
  public static List<TwoFactorReportPrivilege> retrieveAllPrivileges(TwoFactorDaoFactory twoFactorDaoFactory) {
    List<TwoFactorReportPrivilege> twoFactorReportPrivileges = allPrivilegesCache().get(Boolean.TRUE);
    
    if (twoFactorReportPrivileges == null) {
      synchronized (TwoFactorReportPrivilege.class) {
        if (twoFactorReportPrivileges == null) {
          twoFactorReportPrivileges = twoFactorDaoFactory.getTwoFactorReportPrivilege().retrieveAll();
          allPrivilegesCache().put(Boolean.TRUE, twoFactorReportPrivileges);
        }
      }
    }
    return twoFactorReportPrivileges;
  }
  
  /**
   * retrieve a report privilege by uuid
   * @param twoFactorDaoFactory
   * @param uuid
   * @return the report or null if not found
   */
  public static TwoFactorReportPrivilege retrieveByUuid(final TwoFactorDaoFactory twoFactorDaoFactory, final String uuid) {

    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorReportPrivilege twoFactorReportPrivilege = twoFactorDaoFactory.getTwoFactorReportPrivilege().retrieveByUuid(uuid);
    
    return twoFactorReportPrivilege;
    
  }
  
  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorReportPrivilege twoFactorReportPrivilege = (TwoFactorReportPrivilege)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(twoFactorReportPrivilege, this, DB_VERSION_FIELDS);
    
  }

  /** constant for field name for: userUuid */
  public static final String FIELD_REPORT_USER_UUID = "userUuid";

  /** constant for field name for: reportUuid */
  public static final String FIELD_REPORT_REPORT_UUID = "reportUuid";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_VERSION_NUMBER,
      FIELD_REPORT_USER_UUID,
      FIELD_REPORT_REPORT_UUID
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
   * number of inserts and updates
   */
  static int testDeletes = 0;

  
  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_VERSION_NUMBER,
      FIELD_REPORT_USER_UUID,
      FIELD_REPORT_REPORT_UUID
      ));


  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_DELETED_ON,
            FIELD_LAST_UPDATED,
            FIELD_UUID,
            FIELD_VERSION_NUMBER,
            FIELD_REPORT_USER_UUID,
            FIELD_REPORT_REPORT_UUID
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
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorReportPrivilege.class);

  /**
   * @see TwoFactorHibernateBeanBase
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {
    twoFactorDaoFactory.getTwoFactorReportPrivilege().delete(this);
  }

  /**
   * friendly name is included in the report UI
   */
  private String userUuid;

  /**
   * friendly name is included in the report UI
   * @return friendly name
   */
  public String getUserUuid() {
    return this.userUuid;
  }

  /**
   * friendly name is included in the report UI
   * @param reportNameFriendly1
   */
  public void setUserUuid(String reportNameFriendly1) {
    this.userUuid = reportNameFriendly1;

  }
  
  /**
   * some system key on the report which can be used for queries to populate data and should not change
   */
  private String reportUuid;

  /**
   * twoFactorReport
   */
  private TwoFactorReport twoFactorReport;
  
  /**
   * twoFactorReport
   * assume this has been set already
   * @return report
   */
  public TwoFactorReport getTwoFactorReport() {
    return this.twoFactorReport;
  }
  
  
  /**
   * twoFactorReport
   * @param twoFactorReport1 the twoFactorReport to set
   */
  public void setTwoFactorReport(TwoFactorReport twoFactorReport1) {
    this.twoFactorReport = twoFactorReport1;
  }

  /**
   * two factor user
   */
  private TwoFactorUser twoFactorUser;
  
  /**
   * @return the twoFactorUser
   */
  public TwoFactorUser getTwoFactorUser() {
    return this.twoFactorUser;
  }
  
  /**
   * @param twoFactorUser1 the twoFactorUser to set
   */
  public void setTwoFactorUser(TwoFactorUser twoFactorUser1) {
    this.twoFactorUser = twoFactorUser1;
  }

  /**
   * some system key on the report which can be used for queries to populate data and should not change
   * @return system name
   */
  public String getReportUuid() {
    return this.reportUuid;
  }

  /**
   * some system key on the report which can be used for queries to populate data and should not change
   * @param reportNameSystem1
   */
  public void setReportUuid(String reportNameSystem1) {
    this.reportUuid = reportNameSystem1;
    this.twoFactorReport = null;
  }
  
  
  
}
  
