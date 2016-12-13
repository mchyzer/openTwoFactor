/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.daemon;

/**
 * config of report from config file
 */
public class TfReportConfig {

  /**
   * 
   */
  public TfReportConfig() {
  }
  
  /**
   * you can use $date_time$ to subsitute the current date/time
   */
  private String reportFileName;
  
  /**
   * you can use $date_time$ to subsitute the current date/time
   * @return the reportFileName
   */
  public String getReportFileName() {
    return this.reportFileName;
  }
  
  /**
   * you can use $date_time$ to subsitute the current date/time
   * @param reportFileName1 the reportFileName to set
   */
  public void setReportFileName(String reportFileName1) {
    this.reportFileName = reportFileName1;
  }

  /**
   * #twoFactorServer.report.<name>.emailBody =
   * name of report in config
   */
  private String name;
  
  /**
   * #twoFactorServer.report.<name>.emailBody =
   * name of report in config
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /**
   * #twoFactorServer.report.<name>.emailBody =
   * name of report in config
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * #twoFactorServer.report.<name>.emailBody =
   */
  private String emailBody;

  /**
   * @return the emailBody
   */
  public String getEmailBody() {
    return this.emailBody;
  }
  
  /**
   * @param emailBody1 the emailBody to set
   */
  public void setEmailBody(String emailBody1) {
    this.emailBody = emailBody1;
  }

  /**
   * #twoFactorServer.report.<name>.emailSubject =
   */
  private String emailSubject;
  
  /**
   * @return the emailSubject
   */
  public String getEmailSubject() {
    return this.emailSubject;
  }
  
  /**
   * @param emailSubject1 the emailSubject to set
   */
  public void setEmailSubject(String emailSubject1) {
    this.emailSubject = emailSubject1;
  }

  /**
   * #twoFactorServer.report.<name>.bccs =
   */
  private String bccs;
  
  /**
   * #twoFactorServer.report.<name>.bccs =
   * @return the bccs
   */
  public String getBccs() {
    return this.bccs;
  }
  
  /**
   * #twoFactorServer.report.<name>.bccs =
   * @param bccs1 the bccs to set
   */
  public void setBccs(String bccs1) {
    this.bccs = bccs1;
  }

  /**
   * #twoFactorServer.report.<name>.ccs =
   */
  private String ccs;
  
  /**
   * #twoFactorServer.report.<name>.ccs =
   * @return the ccs
   */
  public String getCcs() {
    return this.ccs;
  }
  
  /**
   * #twoFactorServer.report.<name>.ccs =
   * @param ccs1 the ccs to set
   */
  public void setCcs(String ccs1) {
    this.ccs = ccs1;
  }

  /**
   * email address this is from
   * # from has a default if not filled in
   * #twoFactorServer.report.<name>.from =
   */
  private String from;
  
  /**
   * email address this is from
   * # from has a default if not filled in
   * #twoFactorServer.report.<name>.from =
   * @return the from
   */
  public String getFrom() {
    return this.from;
  }
  
  /**
   * email address this is from
   * # from has a default if not filled in
   * #twoFactorServer.report.<name>.from =
   * @param from1 the from to set
   */
  public void setFrom(String from1) {
    this.from = from1;
  }

  /**
   * #twoFactorServer.report.<name>.tos =
   */
  private String tos;
  
  
  /**
   * #twoFactorServer.report.<name>.tos =
   * @return the tos
   */
  public String getTos() {
    return this.tos;
  }

  
  /**
   * #twoFactorServer.report.<name>.tos =
   * @param tos1 the tos to set
   */
  public void setTos(String tos1) {
    this.tos = tos1;
  }

  /**
   * # if emailing to people in the report, this is the column name of the email address
   * # will send one email to each person
   * #twoFactorServer.report.<name>.emailToColumn = COLUMN_NAME
   */
  private String emailToColumn;
  
  
  /**
   * # if emailing to people in the report, this is the column name of the email address
   * # will send one email to each person
   * #twoFactorServer.report.<name>.emailToColumn = COLUMN_NAME
   * @return the emailToColumn
   */
  public String getEmailToColumn() {
    return this.emailToColumn;
  }

  
  /**
   * # if emailing to people in the report, this is the column name of the email address
   * # will send one email to each person
   * #twoFactorServer.report.<name>.emailToColumn = COLUMN_NAME
   * @param emailToColumn1 the emailToColumn to set
   */
  public void setEmailToColumn(String emailToColumn1) {
    this.emailToColumn = emailToColumn1;
  }

  /**
   * # quartz cron for when this job should run
   * #twoFactorServer.report.<name>.quartzCron = 
   */
  private String quartzCron;
  
  /**
   * # quartz cron for when this job should run
   * #twoFactorServer.report.<name>.quartzCron = 
   * @return the quartzCron
   */
  public String getQuartzCron() {
    return this.quartzCron;
  }

  /**
   * # quartz cron for when this job should run
   * #twoFactorServer.report.<name>.quartzCron = 
   * @param quartzCron1 the quartzCron to set
   */
  public void setQuartzCron(String quartzCron1) {
    this.quartzCron = quartzCron1;
  }

  /**
   * # query of the report, can be any number of rows / cols
   * #twoFactorServer.report.<name>.query =
   */
  private String query;

  
  /**
   * # query of the report, can be any number of rows / cols
   * #twoFactorServer.report.<name>.query =
   * @return the query
   */
  public String getQuery() {
    return this.query;
  }

  
  /**
   * # query of the report, can be any number of rows / cols
   * #twoFactorServer.report.<name>.query =
   * @param query1 the query to set
   */
  public void setQuery(String query1) {
    this.query = query1;
  }

  
  
}
