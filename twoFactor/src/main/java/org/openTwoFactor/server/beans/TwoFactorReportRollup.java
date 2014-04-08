/**
 * @author mchyzer
 * $Id: TwoFactorBrowser.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * row for each report that is hierarchically related to another report
 */
@SuppressWarnings("serial")
public class TwoFactorReportRollup extends TwoFactorHibernateBeanBase {

  /**
   * retrieve a report rollup by uuid
   * @param twoFactorDaoFactory
   * @param uuid
   * @return the report or null if not found
   */
  public static TwoFactorReportRollup retrieveByUuid(final TwoFactorDaoFactory twoFactorDaoFactory, final String uuid) {

    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorReportRollup twoFactorReportRollup = twoFactorDaoFactory.getTwoFactorReportRollup().retrieveByUuid(uuid);
    
    return twoFactorReportRollup;
    
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.getUuid()).toHashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null || (!(obj instanceof TwoFactorReportRollup))) {
      return false;
    }
    return new EqualsBuilder().append(this.getUuid(), ((TwoFactorReportRollup)obj).getUuid()).isEquals();
  }

  /**
   * 
   * @param allRollups
   * @param parentReportUuid
   * @return the rollups
   */
  public static List<TwoFactorReportRollup> retrieveChildRollups(Map<String, TwoFactorReportRollup> allRollups, String parentReportUuid) {
    
    return retrieveChildRollupsHelper(allRollups, parentReportUuid, 100);
    
  }  

  /**
   * 
   * @param allRollups
   * @param childReportUuid
   * @return the rollups
   */
  public static List<TwoFactorReportRollup> retrieveParentRollups(Map<String, TwoFactorReportRollup> allRollups, String childReportUuid) {
    
    return retrieveParentRollupsHelper(allRollups, childReportUuid, 100);
    
  }  

  /**
   * get all rollups
   * @param twoFactorDaoFactory
   * @return map of uuid to rollup
   */
  public static Map<String, TwoFactorReportRollup> retrieveAllRollups(TwoFactorDaoFactory twoFactorDaoFactory) {
    Map<String, TwoFactorReportRollup> results = new LinkedHashMap<String, TwoFactorReportRollup>();
    List<TwoFactorReportRollup> rollups = twoFactorDaoFactory.getTwoFactorReportRollup().retrieveAll();
    for (TwoFactorReportRollup twoFactorReportRollup : rollups) {
      results.put(twoFactorReportRollup.getUuid(), twoFactorReportRollup);
    }
    return results;
  }
  
  /**
   * get child rollups safely with a timetolive
   * @param allRollups
   * @param parentReportUuid
   * @param timeToLive
   * @return the rollups
   */
  private static List<TwoFactorReportRollup> retrieveChildRollupsHelper(Map<String, TwoFactorReportRollup> allRollups, 
      String parentReportUuid, int timeToLive) {
    
    if (--timeToLive < 0) {
      throw new RuntimeException("Circular reference? " + parentReportUuid);
    }
    
    List<TwoFactorReportRollup> results = new ArrayList<TwoFactorReportRollup>();
    
    for (TwoFactorReportRollup twoFactorReportRollup : allRollups.values()) {
      if (StringUtils.equals(parentReportUuid, twoFactorReportRollup.getParentReportUuid())) {
        if (!results.contains(twoFactorReportRollup)) {
          results.add(twoFactorReportRollup);
          results.addAll(retrieveChildRollupsHelper(allRollups, twoFactorReportRollup.getChildReportUuid(), timeToLive));
        }
      }
    }
    return results;
  }  
  
  
  /**
   * get parent rollups safely with a timetolive
   * @param allRollups
   * @param childReportUuid
   * @param timeToLive
   * @return the rollups
   */
  private static List<TwoFactorReportRollup> retrieveParentRollupsHelper(Map<String, TwoFactorReportRollup> allRollups, 
      String childReportUuid, int timeToLive) {
    
    if (--timeToLive < 0) {
      throw new RuntimeException("Circular reference? " + childReportUuid);
    }
    
    List<TwoFactorReportRollup> results = new ArrayList<TwoFactorReportRollup>();
    
    for (TwoFactorReportRollup twoFactorReportRollup : allRollups.values()) {
      if (StringUtils.equals(childReportUuid, twoFactorReportRollup.getChildReportUuid())) {
        if (!results.contains(twoFactorReportRollup)) {
          results.add(twoFactorReportRollup);
          results.addAll(retrieveParentRollupsHelper(allRollups, twoFactorReportRollup.getParentReportUuid(), timeToLive));
        }
      }
    }
    return results;
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
    
    TwoFactorReportRollup twoFactorReportRollup = (TwoFactorReportRollup)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(twoFactorReportRollup, this, DB_VERSION_FIELDS);
    
  }

  /** constant for field name for: parentReportUuid */
  public static final String FIELD_PARENT_REPORT_USER_UUID = "parentReportUuid";

  /** constant for field name for: childReportUuid */
  public static final String FIELD_REPORT_CHILD_REPORT_UUID = "childReportUuid";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_VERSION_NUMBER,
      FIELD_REPORT_CHILD_REPORT_UUID,
      FIELD_PARENT_REPORT_USER_UUID
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
      FIELD_REPORT_CHILD_REPORT_UUID,
      FIELD_PARENT_REPORT_USER_UUID
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
            FIELD_REPORT_CHILD_REPORT_UUID,
            FIELD_PARENT_REPORT_USER_UUID
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
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorReportRollup.class);

  /**
   * @see TwoFactorHibernateBeanBase
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {
    twoFactorDaoFactory.getTwoFactorReportRollup().delete(this);
  }

  /**
   * parent report
   */
  private TwoFactorReport parentReport;
  
  /**
   * parent report
   * @return the parentReport
   */
  public TwoFactorReport getParentReport() {
    return this.parentReport;
  }

  /**
   * parent report
   * @param parentReport1 the parentReport to set
   */
  public void setParentReport(TwoFactorReport parentReport1) {
    this.parentReport = parentReport1;
  }


  /**
   * friendly name is included in the report UI
   */
  private String childReportUuid;

  /**
   * child report
   */
  private TwoFactorReport childReport;
  
  /**
   * child report
   * @return the childReport
   */
  public TwoFactorReport getChildReport() {
    return this.childReport;
  }
  
  /**
   * child report
   * @param childReport1 the childReport to set
   */
  public void setChildReport(TwoFactorReport childReport1) {
    this.childReport = childReport1;
  }

  /**
   * friendly name is included in the report UI
   * @return friendly name
   */
  public String getChildReportUuid() {
    return this.childReportUuid;
  }

  /**
   * friendly name is included in the report UI
   * @param reportNameFriendly1
   */
  public void setChildReportUuid(String reportNameFriendly1) {
    this.childReportUuid = reportNameFriendly1;
  }
  
  /**
   * some system key on the report which can be used for queries to populate data and should not change
   */
  private String parentReportUuid;

  /**
   * some system key on the report which can be used for queries to populate data and should not change
   * @return system name
   */
  public String getParentReportUuid() {
    return this.parentReportUuid;
  }

  /**
   * some system key on the report which can be used for queries to populate data and should not change
   * @param reportNameSystem1
   */
  public void setParentReportUuid(String reportNameSystem1) {
    this.parentReportUuid = reportNameSystem1;
  }
  
  
  
}
  
