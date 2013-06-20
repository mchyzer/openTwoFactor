/**
 * @author mchyzer
 * $Id: TwoFactorDaemonLog.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * entry for each daemon which runs
 */
@SuppressWarnings("serial")
public class TwoFactorDaemonLog extends TwoFactorHibernateBeanBase {
  
  /** constant for field name for: status */
  public static final String FIELD_ACTION = "status";

  /** constant for field name for: daemonName */
  public static final String FIELD_DAEMON_NAME = "daemonName";

  /** constant for field name for: details */
  public static final String FIELD_DETAILS = "details";

  /** constant for field name for: endedTime */
  public static final String FIELD_ENDED_TIME = "endedTime";

  /** constant for field name for: startedTime */
  public static final String FIELD_STARTED_TIME = "startedTime";

  /** constant for field name for: millis */
  public static final String FIELD_MILLIS = "millis";

  /** constant for field name for: processId */
  public static final String FIELD_PROCESS_ID = "processId";

  /** constant for field name for: recordsProcessed */
  public static final String FIELD_RECORDS_PROCESSED = "recordsProcessed";

  /** constant for field name for: serverName */
  public static final String FIELD_SERVER_NAME = "serverName";

  /** constant for field name for: status */
  public static final String FIELD_STATUS = "status";

  /** constant for field name for: theTimestamp */
  public static final String FIELD_THE_TIMESTAMP = "theTimestamp";


  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
      FIELD_ACTION,
      FIELD_DAEMON_NAME,
      FIELD_DELETED_ON,
      FIELD_DETAILS,
      FIELD_ENDED_TIME,
      FIELD_LAST_UPDATED,
      FIELD_MILLIS,
      FIELD_PROCESS_ID,
      FIELD_RECORDS_PROCESSED,
      FIELD_SERVER_NAME,
      FIELD_STARTED_TIME,
      FIELD_THE_TIMESTAMP,
      FIELD_VERSION_NUMBER,
      FIELD_UUID
      ));

  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_ACTION,
            FIELD_DAEMON_NAME,
            FIELD_DELETED_ON,
            FIELD_DETAILS,
            FIELD_ENDED_TIME,
            FIELD_LAST_UPDATED,
            FIELD_MILLIS,
            FIELD_PROCESS_ID,
            FIELD_RECORDS_PROCESSED,
            FIELD_SERVER_NAME,
            FIELD_STARTED_TIME,
            FIELD_THE_TIMESTAMP,
            FIELD_VERSION_NUMBER,
            FIELD_UUID
        ));
  
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_ACTION,
            FIELD_DAEMON_NAME,
            FIELD_DELETED_ON,
            FIELD_DETAILS,
            FIELD_ENDED_TIME,
            FIELD_LAST_UPDATED,
            FIELD_MILLIS,
            FIELD_PROCESS_ID,
            FIELD_RECORDS_PROCESSED,
            FIELD_SERVER_NAME,
            FIELD_STARTED_TIME,
            FIELD_THE_TIMESTAMP,
            FIELD_VERSION_NUMBER,
            FIELD_UUID
            ));

  /**
   * default some fields
   */
  public TwoFactorDaemonLog() {
    this.processId = ManagementFactory.getRuntimeMXBean().getName();
    this.serverName = TwoFactorServerUtils.hostname();
  }
  
  /**
   * status of daemon: TwoFactorDaemonLogStatus e.g. success, error
   */
  private String status;
  
  /**
   * status of daemon: TwoFactorDaemonLogStatus e.g. success, error
   * @return the status
   */
  public String getStatus() {
    return this.status;
  }

  /**
   * status of daemon: TwoFactorDaemonLogStatus e.g. success, error
   * @param status1 the status to set
   */
  public void setStatus(String status1) {
    this.status = status1;
  }

  /**
   * timestamp that this record was entered
   */
  private Long theTimestamp;
  
  /**
   * timestamp that this record was entered
   * @return the theTimestamp
   */
  public Long getTheTimestamp() {
    return this.theTimestamp;
  }
  
  /**
   * timestamp that this record was entered
   * @param theTimestamp1 the theTimestamp to set
   */
  public void setTheTimestamp(Long theTimestamp1) {
    this.theTimestamp = theTimestamp1;
  }

  /**
   * details of the dameon run
   */
  private String details;
  
  /**
   * details of the dameon run
   * @return the details
   */
  public String getDetails() {
    return this.details;
  }
  
  /**
   * details of the dameon run
   * @param details1 the details to set
   */
  public void setDetails(String details1) {
    this.details = details1;
  }

  /**
   * friendly string about when this daemon ended, e.g. 2012-06-05 17:09:19
   */
  private String endedTime;
  
  /**
   * friendly string about when this daemon ended, e.g. 2012-06-05 17:09:19
   * @return the endedTime
   */
  public String getEndedTime() {
    return this.endedTime;
  }
  
  /**
   * friendly string about when this daemon ended, e.g. 2012-06-05 17:09:19
   * @param endedTime1 the endedTime to set
   */
  public void setEndedTime(String endedTime1) {
    this.endedTime = endedTime1;
  }

  /**
   * friendly string about when this daemon started, e.g. 2012-06-05 17:09:19
   */
  private String startedTime;
  
  /**
   * friendly string about when this daemon started, e.g. 2012-06-05 17:09:19
   * @return the startedTime
   */
  public String getStartedTime() {
    return this.startedTime;
  }

  /**
   * 
   * @param date
   * @return the date string
   */
  private static String convertDate(Date date) {
    if (date == null) {
      return null;
    }
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return dateFormat.format(date);
  }
  
  /**
   * set time date
   * @param startedTimeDate
   */
  public void setStartedTimeDate(Date startedTimeDate) {
    this.startedTime = convertDate(startedTimeDate);
  }
  
  /**
   * set time date
   * @param endedTimeDate
   */
  public void setEndedTimeDate(Date endedTimeDate) {
    this.endedTime = convertDate(endedTimeDate);
  }
  
  /**
   * friendly string about when this daemon started, e.g. 2012-06-05 17:09:19
   * @param startedTime1 the startedTime to set
   */
  public void setStartedTime(String startedTime1) {
    this.startedTime = startedTime1;
  }

  /**
   * number of millis that this daemon ran in
   */
  private Long millis;
  
  /**
   * number of millis that this daemon ran in
   * @return the millis
   */
  public Long getMillis() {
    return this.millis;
  }
  
  /**
   * @param millis1 the millis to set
   */
  public void setMillis(Long millis1) {
    this.millis = millis1;
  }

  /**
   * number of records processed by the daemon (if applicable)
   */
  private Long recordsProcessed;
  
  /**
   * number of records processed by the daemon (if applicable)
   * @return the recordsProcessed
   */
  public Long getRecordsProcessed() {
    return this.recordsProcessed;
  }
  
  /**
   * number of records processed by the daemon (if applicable)
   * @param recordsProcessed1 the recordsProcessed to set
   */
  public void setRecordsProcessed(Long recordsProcessed1) {
    this.recordsProcessed = recordsProcessed1;
  }

  /**
   * name of the server that the daemon ran on
   */
  private String serverName;
  
  /**
   * processId of the process running the daemon
   */
  private String processId;
  
  /**
   * processId of the process running the daemon
   * @return process id
   */
  public String getProcessId() {
    return this.processId;
  }

  /**
   * processId of the process running the daemon
   * @param processId1
   */
  public void setProcessId(String processId1) {
    this.processId = processId1;
  }

  /**
   * name of the server that the daemon ran on
   * @return the serverName
   */
  public String getServerName() {
    return this.serverName;
  }
  
  /**
   * name of the server that the daemon ran on
   * @param serverName1 the serverName to set
   */
  public void setServerName(String serverName1) {
    this.serverName = serverName1;
  }

  /**
   * name of the daemon this record is about
   */
  private String daemonName;

  /**
   * number of deletes
   */
  static int testDeletes = 0;

  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * name of the daemon this record is about
   * @return the daemonName
   */
  public String getDaemonName() {
    return this.daemonName;
  }
  
  /**
   * name of the daemon this record is about
   * @param daemonName1 the daemonName to set
   */
  public void setDaemonName(String daemonName1) {
    this.daemonName = daemonName1;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    return TwoFactorServerUtils.clone(this, CLONE_FIELDS);
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    TwoFactorAudit dbTwoFactorAudit = (TwoFactorAudit)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(dbTwoFactorAudit, this, DB_VERSION_FIELDS);
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    return DB_NEEDS_UPDATE_FIELDS;
  }

  /**
   * 
   * @param twoFactorDaoFactory
   */
  public void store(TwoFactorDaoFactory twoFactorDaoFactory) {
  
    if (StringUtils.isBlank(this.serverName)) {
      throw new RuntimeException("serverName is null");
    }
    
    this.serverName = StringUtils.abbreviate(this.serverName, 45);
    
    if (StringUtils.isBlank(this.daemonName)) {
      throw new RuntimeException("daemonName is null");
    }
    
    this.daemonName = StringUtils.abbreviate(this.daemonName, 35);
    
    if (this.theTimestamp == null) {
      this.theTimestamp = System.currentTimeMillis();
    }
    
    if (this.details != null) {
      this.details = StringUtils.abbreviate(this.details, 3800);
      
    }
    
    twoFactorDaoFactory.getTwoFactorDaemonLog().store(this);
    
    testInsertsAndUpdates++;
  
  }

  /**
   * delete this audit
   * @param twoFactorDaoFactory
   */
  @Override
  public void delete(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    twoFactorDaoFactory.getTwoFactorDaemonLog().delete(this);
    testDeletes++;
  
  }
  
}
