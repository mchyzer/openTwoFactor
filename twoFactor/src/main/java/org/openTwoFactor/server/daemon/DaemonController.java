/**
 * @author mchyzer
 * $Id: DaemonController.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;


/**
 * Controller for daemons
 */
public class DaemonController {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(DaemonController.class);

  /**
  * scheduler
  * @return scheduler
  */
  private static Scheduler scheduler() {
    try {
      Scheduler scheduler = schedulerFactory().getScheduler();
      return scheduler;
    } catch (SchedulerException se) {
      throw new RuntimeException(se);
    }
  }

  /**
  * scheduler factory singleton
  */
  private static SchedulerFactory schedulerFactory = null;

  /**
  * lazy load (and start the scheduler) the scheduler factory
  * @return the scheduler factory
  */
  private static SchedulerFactory schedulerFactory() {
    if (schedulerFactory == null) {
      schedulerFactory = new StdSchedulerFactory();
      try {
        schedulerFactory.getScheduler().start();
      } catch (SchedulerException se) {
        throw new RuntimeException(se);
      }
    }
    return schedulerFactory;
  }

  /** make sure we only schedule jobs once */
  private static boolean scheduledJobs = false;
  
  /**
   * schedule jobs once
   */
  public static void scheduleJobsOnce() {
    if (scheduledJobs) {
      return;
    }
    synchronized (DaemonController.class) {
      if (scheduledJobs) {
        return;
      }
      try {
        //maybe we dont run on this server...
        //# if this is true, then dont run daemons here
        //twoFactorServer.dontRunDaemonsHere = false
        
        //# if we arent restricting daemons in twoFactorServer.dontRunDaemonsHere, then if server
        //# names are listed here, then only run on this server
        //twoFactorServer.runOnlyOnServerNames = fasttest-small-d-01
        
        boolean runDaemons = true;
        
        if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.dontRunDaemonsHere", false)) {
          
          LOG.warn("Daemons dont run here since this is set in twoFactor.server properties file overlay: " +
          		"twoFactorServer.dontRunDaemonsHere, hostname: " + TwoFactorServerUtils.hostname());

          runDaemons = false;
        }

        if (runDaemons) {
          
          String serverNamesString = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.runOnlyOnServerNames");
          
          if (!StringUtils.isBlank(serverNamesString)) {
            
            List<String> serverNamesList = TwoFactorServerUtils.splitTrimToList(serverNamesString, ",");
            runDaemons = false;
            for (String serverName : serverNamesList) {
              
              if (StringUtils.equalsIgnoreCase(serverName, TwoFactorServerUtils.hostname())) {
                
                runDaemons = true;
                LOG.warn("Daemons running since " + serverName + "  is in the list of allowed servernames from twoFactor.server config file: " +
                		"twoFactorServer.runOnlyOnServerNames: " + serverNamesString );
                break;
                
              }
              
            }
            
            if (!runDaemons) {

              LOG.warn("Daemons dont run here since " + TwoFactorServerUtils.hostname() + " is not in the list of allowed servernames " +
              		"from twoFactor.server config file: " +
                  "twoFactorServer.runOnlyOnServerNames: " + serverNamesString );
              
            }
            
          }
          
          
        }
        
        if (runDaemons) {
          scheduleReports();
          scheduleDaemon(TfAuditClearingJob.class);
          scheduleDaemon(TfDeletedClearingJob.class);
        }
        
        scheduledJobs = true;
      } catch (Throwable t) {
        LOG.error("Error scheduling jobs once", t);
      }
    }
  }

  /**
   * schedule email reports from config file
   */
  static void scheduleReports() {
    
    //
    //  # quartz cron for when this job should run
    //  #twoFactorServer.report.<name>.quartzCron = 
    //
    //  # query of the report, can be any number of rows / cols
    //  #twoFactorServer.report.<name>.query =
    //
    //  # if emailing to people in the report, this is the column name of the email address
    //  # will send one email to each person
    //  #twoFactorServer.report.<name>.emailToColumn = COLUMN_NAME
    //  #twoFactorServer.report.<name>.tos =
    //  #twoFactorServer.report.<name>.ccs =
    //  #twoFactorServer.report.<name>.bccs =
    //
    //  #twoFactorServer.report.<name>.emailSubject =
    //  #twoFactorServer.report.<name>.emailBody =
    Map<String, TfReportConfig> tfReportConfigs = TwoFactorServerConfig.retrieveConfig().tfReportConfigs();
   
    for (TfReportConfig tfReportConfig : tfReportConfigs.values()) {

      String jobName = TfReportJob.class.getSimpleName() + "_" + tfReportConfig.getName();
      try {
        Scheduler scheduler = scheduler();
    
        //note, in old versions of quartz, blank group cannot be used
        String quartzJobName = "report_" + jobName;
    
        JobDetail job = JobBuilder.newJob(TfReportJob.class)
                     .withIdentity(quartzJobName, Scheduler.DEFAULT_GROUP)
                     .build();
    
        String cronString = tfReportConfig.getQuartzCron();
        
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger");
        Trigger trigger = TriggerBuilder.newTrigger() 
           .withIdentity(triggerKey)
           .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
           .build();
    
        scheduler.scheduleJob(job, trigger);

      } catch (Throwable t) {
        LOG.error("Error scheduling " + jobName + " daemon", t);
      }

    }
    
  }
  
  /**
   * generic schedule deamon method
   * @param jobClass 
   */
  private static void scheduleDaemon(Class<? extends Job> jobClass) {
    
    try {
      Scheduler scheduler = scheduler();
      String jobName = jobClass.getSimpleName() + "Daemon";
  
      //note, in old versions of quartz, blank group cannot be used
      String quartzJobName = "fullRefresh_" + jobName;
  
      JobDetail job = JobBuilder.newJob(jobClass)
                   .withIdentity(quartzJobName, Scheduler.DEFAULT_GROUP)
                   .build();
  
      String cronString = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer." + jobClass.getSimpleName() + ".quartzCron");
      
      TriggerKey triggerKey = TriggerKey.triggerKey(jobName + "_trigger");
      Trigger trigger = TriggerBuilder.newTrigger() 
         .withIdentity(triggerKey)
         .withSchedule(CronScheduleBuilder.cronSchedule(cronString))
         .build();
  
      scheduler.scheduleJob(job, trigger);

    } catch (Throwable t) {
      LOG.error("Error scheduling " + jobClass.getSimpleName() + " daemon", t);
    }

  }

}
