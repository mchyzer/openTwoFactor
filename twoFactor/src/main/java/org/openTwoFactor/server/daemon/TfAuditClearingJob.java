/**
 * @author mchyzer
 * $Id: TfAuditClearingJob.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorDaemonLog;
import org.openTwoFactor.server.beans.TwoFactorDaemonLogStatus;
import org.openTwoFactor.server.beans.TwoFactorDaemonName;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;



/**
 * daemon which clears out daemon audit records
 */
public class TfAuditClearingJob implements Job {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfAuditClearingJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    auditClearingLogic(TwoFactorDaoFactory.getFactory());
  }

  /**
   * loops through the audit clearing logic
   */
  static long testingLoopsThrough = 0;
  
  /**
   * total count of last run
   */
  static long testingTotalCount = 0;
  
  /**
   * clear out audit logs
   * @param twoFactorDaoFactory 
   */
  public void auditClearingLogic(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    boolean hasError = false;

    long start = System.nanoTime();
    int elapsedTime = -1;
    
    Map<String, Object> debugLog = new LinkedHashMap<String, Object>();

    int count = 0;
    
    int errorCount = 0;
    Throwable throwable = null;
    try {
      
      Map<String, Integer> auditLevelsToRetentionDays = TwoFactorServerConfig.retrieveConfig().tfAuditLevels();
      
      if (TwoFactorServerUtils.length(auditLevelsToRetentionDays) == 0) {
        throw new RuntimeException("Why are there no audit levels configured in the two factor config file?");
      }

      debugLog.put("found configs", TwoFactorServerUtils.length(auditLevelsToRetentionDays));

      Set<TwoFactorAuditAction> actionsProcessed = new HashSet<TwoFactorAuditAction>();

      int actionIndex = 0;

      //loop through the configs
      for (String actionsString : auditLevelsToRetentionDays.keySet()) {
        
        int innerCount = 0;
        
        //split by comma
        Set<String> actionSet = TwoFactorServerUtils.splitTrimToSet(actionsString, ",");
        
        //for each one see if it exists and keep track of what we have processed
        for (String action : actionSet) {
          
          try {
            actionsProcessed.add(TwoFactorAuditAction.valueOfIgnoreCase(action));
          } catch (Exception e) {
            debugLog.put(action + " action not found: ", true);
            LOG.error("Error decoding action from two factor config file: " + action);
          }
        }
        
        //get the millis before which to delete
        int i=0;
        Integer retentionDays = auditLevelsToRetentionDays.get(actionsString);
        if (retentionDays == null || retentionDays <= 0) {
          
          debugLog.put("actions not deleting logs: " + actionsString, true);

          continue;
        }
        
        long selectBeforeThisMillis = System.currentTimeMillis() - (1000L * 60 * 60 * 24 * retentionDays);

        debugLog.put("Index: " + actionIndex, actionsString);

        //only a subset of records are returned for each batch (since it can cause performance
        //problems in the database if not
        while(true) {
          
          testingLoopsThrough++;
          
          List<TwoFactorAudit> audits = twoFactorDaoFactory.getTwoFactorAudit().retrieveOlderThanAgeAndActions(actionSet, selectBeforeThisMillis);
          
          debugLog.put("Records for index " + actionIndex + " in pass", TwoFactorServerUtils.length(audits));
          
          count += TwoFactorServerUtils.length(audits);
          innerCount += TwoFactorServerUtils.length(audits);
          
          if (TwoFactorServerUtils.length(audits) == 0) {
            break;
          }
          
          for (TwoFactorAudit twoFactorAudit : TwoFactorServerUtils.nonNull(audits)) {
            try {
              //System.out.println("setting deleted on for: " + twoFactorAudit.getUuid() + ", " + twoFactorAudit.getAction()
              //    + ", " + new Date(twoFactorAudit.getTheTimestamp()));
              
              twoFactorAudit.setDeletedOn(System.currentTimeMillis());
              twoFactorAudit.store(twoFactorDaoFactory);
            } catch (Exception e) {
              LOG.error("Error with record: " + twoFactorAudit.getUuid(), e);
              hasError = true;
              debugLog.put("Error with record: " + twoFactorAudit.getUuid(), TwoFactorServerUtils.getFullStackTrace(e));
              if (errorCount++ > 20) {
                throw new RuntimeException("Error count is too high: " + errorCount, e);
              }
              throwable = e;
            }
          }
          
          //why have we done more than 10000????
          if (i++ > 10000) {
            throw new RuntimeException("Why did we hit 10000 iterations????");
          }
        }
        
        debugLog.put("records for index: " + actionIndex  + ", before " + new Date(selectBeforeThisMillis), innerCount);
        actionIndex++;
      }
    } catch (Throwable t) {
      hasError = true;
      debugLog.put("exception", TwoFactorServerUtils.getFullStackTrace(t));
      LOG.error(TwoFactorServerUtils.mapToString(debugLog));
      LOG.error("Error in daemon", t);
      throwable = t;
    } finally {
      elapsedTime = (int)((System.nanoTime() - start) / 1000000);
      debugLog.put("tookMillis", elapsedTime);
      debugLog.put("total records", count);
      TfDaemonLog.daemonLog(debugLog);
      testingTotalCount = count;
    }
    
    try {
      //lets store to DB
      TwoFactorDaemonLog twoFactorDaemonLog = new TwoFactorDaemonLog();
      twoFactorDaemonLog.setUuid(TwoFactorServerUtils.uuid());
      twoFactorDaemonLog.setDaemonName(TwoFactorDaemonName.deleteOldAudits.name());
      twoFactorDaemonLog.setDetails(TwoFactorServerUtils.mapToString(debugLog));
      //2012-06-05 17:09:19
      twoFactorDaemonLog.setStartedTimeDate(new Date(start/1000000));
      twoFactorDaemonLog.setEndedTimeDate(new Date());
      twoFactorDaemonLog.setMillis(new Long(elapsedTime));
      twoFactorDaemonLog.setRecordsProcessed(new Long(count));
      twoFactorDaemonLog.setServerName(TwoFactorServerUtils.hostname());
      twoFactorDaemonLog.setStatus(hasError ? TwoFactorDaemonLogStatus.error.toString() : TwoFactorDaemonLogStatus.success.toString());
      twoFactorDaemonLog.store(twoFactorDaoFactory);
    } catch (Throwable t) {
      LOG.error("Error storing log", t);
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t);
    }
    if (hasError) {
      throw new RuntimeException("Error in dameon!", throwable);
    }
  }

  
}

