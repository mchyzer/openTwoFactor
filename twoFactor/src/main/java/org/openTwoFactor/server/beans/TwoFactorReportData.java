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
 * row for each report we are sending out
 */
@SuppressWarnings("serial")
public class TwoFactorReportData extends TwoFactorHibernateBeanBase {
  
  /**
   * holds for true the list of report name systems
   */
  private static ExpirableCache<Boolean, List<String>> reportNameSystemCache = null;

  /**
   * 
   * @return the cache lazy loaded
   */
  public static ExpirableCache<Boolean, List<String>> reportNameSystemCache() {
    if (reportNameSystemCache == null) {
      reportNameSystemCache = new ExpirableCache<Boolean, List<String>>(5);
    }
    return reportNameSystemCache;
  }
  
  /**
   * retrieve the list of report name systems
   * @param twoFactorDaoFactory
   * @return list
   */
  public static List<String> retrieveReportNameSystems(TwoFactorDaoFactory twoFactorDaoFactory) {
    
    List<String> reportNames = reportNameSystemCache().get(Boolean.TRUE);
    
    if (reportNames == null) {
      reportNames = twoFactorDaoFactory.getTwoFactorReportData().retrieveReportNameSystems();
      reportNameSystemCache().put(Boolean.TRUE, reportNames);
    }
    
    return reportNames;
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
    
    throw new RuntimeException("Not implemented, do not call this");
    
  }

  /** constant for field name for: loginid */
  public static final String FIELD_LOGINID = "loginid";

  /** constant for field name for: reportNameSystem */
  public static final String FIELD_REPORT_NAME_SYSTEM = "reportNameSystem";

  
  
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_VERSION_NUMBER,
      FIELD_REPORT_NAME_SYSTEM,
      FIELD_LOGINID
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
      FIELD_REPORT_NAME_SYSTEM,
      FIELD_LOGINID
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
            FIELD_REPORT_NAME_SYSTEM,
            FIELD_LOGINID
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
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorReportData.class);

  /**
   * friendly name is included in the report UI
   */
  private String loginid;

  /**
   * friendly name is included in the report UI
   * @return friendly name
   */
  public String getLoginid() {
    return this.loginid;
  }

  /**
   * friendly name is included in the report UI
   * @param reportNameFriendly1
   */
  public void setLoginid(String reportNameFriendly1) {
    this.loginid = reportNameFriendly1;
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
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#delete(org.openTwoFactor.server.hibernate.TwoFactorDaoFactory)
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {
    throw new RuntimeException("Not implemented");
  }
  
}
  
