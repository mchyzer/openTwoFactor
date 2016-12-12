/**
 * @author mchyzer
 * $Id: TfAuditClearingJob.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.email.TwoFactorEmail;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.dao.HibernateDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;



/**
 * daemon which clears out daemon audit records
 */
public class TfReportJob implements Job {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfReportJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    // e.g. report_TfReportJob_testPersonal
    String jobName = context.getJobDetail().getKey().getName();
    String reportName = jobName.substring(("report_" + TfReportJob.class.getSimpleName() + "_").length(), jobName.length());
    TfReportConfig tfReportConfig = TwoFactorServerConfig.retrieveConfig().tfReportConfigs().get(reportName);
    if (tfReportConfig == null) {
      LOG.error("Cant find report config: " + reportName);
      return;
    }
    
    runReport(HibernateDaoFactory.getFactory(), tfReportConfig);
    
  }

  /**
   * run report
   * @param twoFactorDaoFactory
   * @param tfReportConfig
   */
  @SuppressWarnings("cast")
  public static void runReport(TwoFactorDaoFactory twoFactorDaoFactory, TfReportConfig tfReportConfig) {
    
    TfReportData tfReportData = twoFactorDaoFactory.getTwoFactorReport().retrieveReportByConfig(tfReportConfig);

    if (!TwoFactorServerUtils.isBlank(tfReportConfig.getEmailToColumn())) {
      int emailToColumnIndex = -1;
      for (emailToColumnIndex = 0; emailToColumnIndex < tfReportData.getHeaders().size(); emailToColumnIndex++) {
        if (TwoFactorServerUtils.equalsIgnoreCase(tfReportConfig.getEmailToColumn(), tfReportData.getHeaders().get(emailToColumnIndex))) {
          break;
        }
      }
      if (emailToColumnIndex == tfReportData.getHeaders().size()) {
        throw new RuntimeException("Cant find header: " + tfReportConfig.getEmailToColumn() + " in list of columns: " + tfReportConfig.getQuery());
      }
      
      //get the list of email addresses
      for(int i=0; i<tfReportData.getData().size(); i++) {
        String emailAddress = tfReportData.getData().get(i)[emailToColumnIndex];
        composeReportEmail(tfReportConfig, emailAddress, null);
      }
      
    } else {

      String reportFileName = tfReportConfig.getReportFileName();
      
      File reportFile = null;
      
      String tempFilePath = TwoFactorServerUtils.tempFileDirLocation() + "reports" + File.separator;

      //make sure the dir exists
      TwoFactorServerUtils.assertDirExists(tempFilePath);

      if (!StringUtils.isBlank(reportFileName)) {
        for (int i=0;i<10;i++) {
          String dateTime = TwoFactorServerUtils.datetimeToFileString(new Date());
          // # you can use $date_time$ to subsitute the current date/time
          // # if you have a report file name, it will attach csv with this file name
          String tempReportName = TwoFactorServerUtils.replace(reportFileName, "$date_time$", dateTime);
          File tempReportFile = new File(tempFilePath + tempReportName);
          if (!tempReportFile.exists()) {
            reportFile = tempReportFile;
            break;
          }
          //pause
          TwoFactorServerUtils.sleep(1000);
          
        }
        
        if (reportFile == null) {
          throw new RuntimeException("Cant make reportFile work: " + tempFilePath + ", " + reportFileName);
        }
        
        TwoFactorServerUtils.fileCreateNewFile(reportFile);

        //convert the report to CSV
        //Delimiter used in CSV file
        String NEW_LINE_SEPARATOR = "\n";
        
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;

        //Create the CSVFormat object with "\n" as a record delimiter
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);

        try {

          //initialize FileWriter object
          fileWriter = new FileWriter(reportFile);

          //initialize CSVPrinter object
          csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);

          //Create CSV file header
          csvFilePrinter.printRecord(tfReportData.getHeaders());

          //Write a new student object list to the CSV file
          for (String[] row : tfReportData.getData()) {

            csvFilePrinter.printRecord((Object[])(Object)row);
          }

        } catch (Exception e) {

          throw new RuntimeException("Error in CsvFileWriter !!! " + reportFile.getName(), e);
        
        } finally {

          TwoFactorServerUtils.closeQuietly(fileWriter);
          TwoFactorServerUtils.closeQuietly(csvFilePrinter);
        }
      }
      
      composeReportEmail(tfReportConfig, null, reportFile);
    }

    

    
  }

  /**
   * @param tfReportConfig
   * @param toEmailAddress optional
   * @param attachment optional
   */
  private static void composeReportEmail(TfReportConfig tfReportConfig, String toEmailAddress, File attachment) {
    TwoFactorEmail twoFactorMail = new TwoFactorEmail();
    
    if (!TwoFactorServerUtils.isBlank(tfReportConfig.getBccs())) {
      String[] bccArray = TwoFactorServerUtils.splitTrim(tfReportConfig.getBccs(), ",");
      for (String bcc : bccArray) {
        twoFactorMail.addBcc(bcc);
      }
    }
    
    if (!TwoFactorServerUtils.isBlank(tfReportConfig.getCcs())) {
      String[] ccArray = TwoFactorServerUtils.splitTrim(tfReportConfig.getCcs(), ",");
      for (String cc : ccArray) {
        twoFactorMail.addCc(cc);
      }
    }

    if (!StringUtils.isBlank(toEmailAddress)) {
      twoFactorMail.addTo(toEmailAddress);
    }
    
    if (!TwoFactorServerUtils.isBlank(tfReportConfig.getTos())) {
      String[] toArray = TwoFactorServerUtils.splitTrim(tfReportConfig.getTos(), ",");
      for (String to : toArray) {
        twoFactorMail.addTo(to);
      }
    }

    twoFactorMail.assignBody(tfReportConfig.getEmailBody());
    twoFactorMail.assignSubject(tfReportConfig.getEmailSubject());
    
    if (attachment != null) {
      twoFactorMail.addAttachment(attachment);
    }
    
    twoFactorMail.send();
  }
  
}

