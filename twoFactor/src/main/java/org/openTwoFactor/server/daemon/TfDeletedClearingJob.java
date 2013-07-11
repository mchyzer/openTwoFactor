/**
 * @author mchyzer
 * $Id: TfDeletedClearingJob.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorDaemonLog;
import org.openTwoFactor.server.beans.TwoFactorDaemonLogStatus;
import org.openTwoFactor.server.beans.TwoFactorDaemonName;
import org.openTwoFactor.server.beans.TwoFactorIpAddress;
import org.openTwoFactor.server.beans.TwoFactorServiceProvider;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;



/**
 * daemon which clears out deleted records
 */
public class TfDeletedClearingJob implements Job {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfDeletedClearingJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    deletedClearingLogic(TwoFactorDaoFactory.getFactory());
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
  public void deletedClearingLogic(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    boolean hasError = false;

    long start = System.nanoTime();
    Date startDate = new Date();
    int elapsedTime = -1;
    
    Map<String, Object> debugLog = new LinkedHashMap<String, Object>();

    int count = 0;
    int[] errorCount = new int[]{0};
    
    Throwable throwable = null;
    try {
      
      int deleteRecordsAfterMinutes = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.purgeDeletedRecordsAfterMinutes", 2880);
      long deleteBeforeMilli = System.currentTimeMillis() - (deleteRecordsAfterMinutes * 60L * 1000);
      
      if (deleteRecordsAfterMinutes < 0) {
        throw new RuntimeException("Why is twoFactorServer.purgeDeletedRecordsAfterMinutes less than 0???");
      }

      debugLog.put("delete before", new Date(deleteBeforeMilli));

      //note: get these in the order they appear in foreign keys...

      for (int i=0;i<10000;i++) {
        List<TwoFactorAudit> twoFactorAudits = twoFactorDaoFactory.getTwoFactorAudit()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorAudits, debugLog, "twoFactorAudits", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }

      for (int i=0;i<10000;i++) {
        List<TwoFactorDaemonLog> twoFactorDaemonLogs = twoFactorDaoFactory.getTwoFactorDaemonLog()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorDaemonLogs, debugLog, "twoFactorDaemonLogs", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }

      for (int i=0;i<10000;i++) {
        List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorBrowsers, debugLog, "twoFactorBrowsers", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }

      for (int i=0;i<10000;i++) {
        List<TwoFactorIpAddress> twoFactorIpAddresses = twoFactorDaoFactory.getTwoFactorIpAddress()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorIpAddresses, debugLog, "twoFactorIpAddresses", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }

      for (int i=0;i<10000;i++) {
        List<TwoFactorServiceProvider> twoFactorServiceProviders = twoFactorDaoFactory.getTwoFactorServiceProvider()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorServiceProviders, debugLog, "twoFactorServiceProviders", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }

      for (int i=0;i<10000;i++) {
        List<TwoFactorUserAgent> twoFactorUserAgents = twoFactorDaoFactory.getTwoFactorUserAgent()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorUserAgents, debugLog, "twoFactorUserAgents", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }

      for (int i=0;i<10000;i++) {
        List<TwoFactorUserAttr> twoFactorUserAttrs = twoFactorDaoFactory.getTwoFactorUserAttr()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorUserAttrs, debugLog, "twoFactorUserAttrs", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }

      for (int i=0;i<10000;i++) {
        List<TwoFactorUser> twoFactorUsers = twoFactorDaoFactory.getTwoFactorUser()
          .retrieveDeletedOlderThanAge(deleteBeforeMilli);
        int records = deleteRecords(twoFactorDaoFactory, twoFactorUsers, debugLog, "twoFactorUsers", i, errorCount);
        count += records;
        if (records == 0) {
          break;
        }
      }


    } catch (Throwable t) {
      hasError = true;
      debugLog.put("exception", TwoFactorServerUtils.getFullStackTrace(t));
      LOG.error(TwoFactorServerUtils.mapToString(debugLog));
      LOG.error("Error in daemon", t);
      throwable = t;
    } finally {
      if (errorCount[0] > 0) {
        debugLog.put("errorCount", errorCount);
      }
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
      twoFactorDaemonLog.setDaemonName(TwoFactorDaemonName.permanentlyDeleteOldRecords.name());
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
      throw new RuntimeException("Error in dameon!", throwable);
    }
  }

  /**
   * delete some records
   * @param debugLabel                     
   * @param twoFactorDaoFactory 
   * @param beans
   * @param debugLog 
   * @param passIndex 
   * @param errorCount 
   * @return the number of records deleted
   */
  public static int deleteRecords(TwoFactorDaoFactory twoFactorDaoFactory,
      Collection<? extends TwoFactorHibernateBeanBase> beans, Map<String, Object> debugLog, 
      String debugLabel, int passIndex, int[] errorCount) {

    if (TwoFactorServerUtils.length(beans) > 0) {
      for (TwoFactorHibernateBeanBase bean : beans) {
        
        try {
          bean.delete(twoFactorDaoFactory);
        } catch (Exception e) {
          LOG.error("Error in record: " + debugLabel + " id: " + bean.getUuid(), e);
          if (++errorCount[0] > 50) {
            throw new RuntimeException("Error count: " + errorCount[0]);
          }
        }
      }
      testingLoopsThrough++;
    }
    
    debugLog.put("deleting " + debugLabel + " index " + passIndex + " records", TwoFactorServerUtils.length(beans));

    return TwoFactorServerUtils.length(beans);

  }
}

