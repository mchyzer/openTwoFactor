/**
 * @author mchyzer
 * $Id: TfAuditClearingJob.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
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
 * daemon which clears out daemon records
 */
public class TfDeleteOldDaemonLogsJob implements Job {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    TwoFactorDaoFactory twoFactorDaoFactory = TwoFactorDaoFactory.getFactory();
    new TfDeleteOldDaemonLogsJob().deleteOldDaemonLogsLogic(twoFactorDaoFactory);

  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfDeleteOldDaemonLogsJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    deleteOldDaemonLogsLogic(TwoFactorDaoFactory.getFactory());
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
  public void deleteOldDaemonLogsLogic(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    boolean hasError = false;

    long start = System.nanoTime();
    Date startDate = new Date();
    int elapsedTime = -1;
    
    Map<String, Object> debugLog = new LinkedHashMap<String, Object>();

    int count = 0;
    
    int daysToDelete = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactoDaemonDeleteOldDaemonRecordsDaysToKeep", 30);
    
    long millisLastWeek = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * daysToDelete);
    
    int errorCount = 0;
    Throwable throwable = null;
    try {

      int pass = 0;
      
      while (true) {
        
        List<TwoFactorDaemonLog> daemonLogs = twoFactorDaoFactory.getTwoFactorDaemonLog().retrieveOlderThanAge(millisLastWeek);
        
        int innerCount = TwoFactorServerUtils.length(daemonLogs);
        count += innerCount;
        
        debugLog.put("recordCount_" + pass, innerCount);
        
        if (innerCount == 0) {
          break;
        }
        
        for (TwoFactorDaemonLog twoFactorDaemonLog : TwoFactorServerUtils.nonNull(daemonLogs)) {
          try {
            
            twoFactorDaemonLog.setDeletedOn(System.currentTimeMillis());
            twoFactorDaemonLog.store(twoFactorDaoFactory);
          } catch (Exception e) {
            LOG.error("Error with record: " + twoFactorDaemonLog.getUuid(), e);
            hasError = true;
            debugLog.put("Error with record: " + twoFactorDaemonLog.getUuid(), TwoFactorServerUtils.getFullStackTrace(e));
            if (errorCount++ > 20) {
              throw new RuntimeException("Error count is too high: " + errorCount, e);
            }
            throwable = e;
          }
        }
        
      }
      
      debugLog.put("recordCount", count);

      if (pass++ > 1000) {
        throw new RuntimeException("Why more than 1000 passes????");
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
      twoFactorDaemonLog.setDaemonName(TwoFactorDaemonName.deleteOldDaemonLogs.name());
      twoFactorDaemonLog.setDetails(TwoFactorServerUtils.mapToString(debugLog));
      //2012-06-05 17:09:19
      twoFactorDaemonLog.setStartedTimeDate(startDate);
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
      throw new RuntimeException("Error in daemon!", throwable);
    }
  }

  
}

