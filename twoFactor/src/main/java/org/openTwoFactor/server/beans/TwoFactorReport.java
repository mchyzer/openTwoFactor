/**
 * @author mchyzer
 * $Id: TwoFactorBrowser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
   * retrieve a report by uuid
   * @param twoFactorDaoFactory
   * @param uuid
   * @return the report or null if not found
   */
  public static TwoFactorReport retrieveByUuid(final TwoFactorDaoFactory twoFactorDaoFactory, final String uuid) {

    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorReport twoFactorReport = twoFactorDaoFactory.getTwoFactorReport().retrieveByUuid(uuid);
    
    return twoFactorReport;
    
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
    
    TwoFactorReport twoFactorReport = (TwoFactorReport)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(twoFactorReport, this, DB_VERSION_FIELDS);
    
  }

  /** constant for field name for: reportNameFriendly */
  public static final String FIELD_REPORT_NAME_FRIENDLY = "reportNameFriendly";

  /** constant for field name for: reportNameSystem */
  public static final String FIELD_REPORT_NAME_SYSTEM = "reportNameSystem";

  /** constant for field name for: reportType */
  public static final String FIELD_REPORT_TYPE = "reportType";

  
  
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_VERSION_NUMBER,
      FIELD_REPORT_NAME_FRIENDLY,
      FIELD_REPORT_NAME_SYSTEM,
      FIELD_REPORT_TYPE
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
   * encrypt the browser user uuid
   * @param browserUserUuid
   * @return the encrypted string
   */
  public static String encryptBrowserUserUuid(String browserUserUuid) {
    
    return TwoFactorServerUtils.encryptSha(browserUserUuid);
    
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
      FIELD_REPORT_NAME_FRIENDLY,
      FIELD_REPORT_NAME_SYSTEM,
      FIELD_REPORT_TYPE
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
            FIELD_REPORT_NAME_FRIENDLY,
            FIELD_REPORT_NAME_SYSTEM,
            FIELD_REPORT_TYPE
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
   * @see TwoFactorHibernateBeanBase
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {
    twoFactorDaoFactory.getTwoFactorReport().delete(this);
  }

  /**
   * must be of the TwoFactorReportType enum, e.g. group or rollup
   */
  private String reportType;

  /**
   * must be of the TwoFactorReportType enum, e.g. group or rollup
   * @return reportType
   */
  public String getReportType() {
    return this.reportType;
  }

  /**
   * must be of the TwoFactorReportType enum, e.g. group or rollup
   * @return reportType
   */
  public TwoFactorReportType getReportTypeEnum() {
    if (StringUtils.isBlank(this.reportType)) {
      return null;
    }
    return TwoFactorReportType.valueOf(this.reportType);
  }

  /**
   * must be of the TwoFactorReportType enum, e.g. group or rollup
   * @param reportType1
   */
  public void setReportType(String reportType1) {
    
    if (!StringUtils.isBlank(reportType1)) {
      //validate
      TwoFactorReportType.valueOf(reportType1);
    } else {
      reportType1 = null;
    }
    
    this.reportType = reportType1;
  }
  
  /**
   * friendly name is included in the report UI
   */
  private String reportNameFriendly;

  /**
   * friendly name is included in the report UI
   * @return friendly name
   */
  public String getReportNameFriendly() {
    return this.reportNameFriendly;
  }

  /**
   * friendly name is included in the report UI
   * @param reportNameFriendly1
   */
  public void setReportNameFriendly(String reportNameFriendly1) {
    this.reportNameFriendly = reportNameFriendly1;
  }
  
  /**
   * some system key on the report which can be used for queries to populate data and should not change
   */
  private String reportNameSystem;

  /**
   * some system key on the report which can be used for queries to populate data and should not change
   * @return system name
   */
  public String getReportNameSystem() {
    return this.reportNameSystem;
  }

  /**
   * some system key on the report which can be used for queries to populate data and should not change
   * @param reportNameSystem1
   */
  public void setReportNameSystem(String reportNameSystem1) {
    this.reportNameSystem = reportNameSystem1;
  }

  /**
   * retrieve reports
   * @param twoFactorDaoFactory
   * @return the list of all reports
   */
  public static List<TwoFactorReport> retrieveAll(TwoFactorDaoFactory twoFactorDaoFactory) {
    List<TwoFactorReport> results = twoFactorDaoFactory.getTwoFactorReport().retrieveAll();
    return results;
  }

  /**
   * cache the privs related to this report
   */
  private List<TwoFactorReportPrivilege> twoFactorReportPrivileges;
  
  /**
   * get all privileges related to this report
   * @param twoFactorDaoFactory
   * @return the privileges
   */
  public List<TwoFactorReportPrivilege> retrievePrivileges(TwoFactorDaoFactory twoFactorDaoFactory) {
    //lazy load this
    if (this.twoFactorReportPrivileges == null) {
      List<TwoFactorReportPrivilege> tempPrivs = new ArrayList<TwoFactorReportPrivilege>();
      for (TwoFactorReportPrivilege twoFactorReportPrivilege : twoFactorDaoFactory.getTwoFactorReportPrivilege().retrieveAll()) {
        if (StringUtils.equals(this.getUuid(), twoFactorReportPrivilege.getReportUuid())) {
          tempPrivs.add(twoFactorReportPrivilege);
        }
      }
      this.twoFactorReportPrivileges = tempPrivs;
    }
    return this.twoFactorReportPrivileges;
  }
  

  /**
   * cache the child rollups related to this report
   */
  private List<TwoFactorReportRollup> twoFactorChildRollups;
  
  /**
   * get all rollups related to this report
   * @param twoFactorDaoFactory
   * @return the rollups
   */
  public List<TwoFactorReportRollup> retrieveChildRollups(TwoFactorDaoFactory twoFactorDaoFactory) {
    //lazy load this
    if (this.twoFactorChildRollups == null) {
      Map<String, TwoFactorReportRollup> allRollups = TwoFactorReportRollup.retrieveAllRollups(twoFactorDaoFactory);
      this.twoFactorChildRollups = TwoFactorReportRollup.retrieveChildRollups(allRollups, this.getUuid());
    }
    return this.twoFactorChildRollups;
  }

  /**
   * cache the parent rollups related to this report
   */
  private List<TwoFactorReportRollup> twoFactorParentRollups;
  
  /**
   * get all parent rollups related to this report
   * @param twoFactorDaoFactory
   * @return the rollups
   */
  public List<TwoFactorReportRollup> retrieveParentRollups(TwoFactorDaoFactory twoFactorDaoFactory) {
    //lazy load this
    if (this.twoFactorParentRollups == null) {
      Map<String, TwoFactorReportRollup> allRollups = TwoFactorReportRollup.retrieveAllRollups(twoFactorDaoFactory);
      this.twoFactorParentRollups = TwoFactorReportRollup.retrieveParentRollups(allRollups, this.getUuid());
    }
    return this.twoFactorParentRollups;
  }
  
}
  
