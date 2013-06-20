/**
 * @author mchyzer
 * $Id: TfAuditClearingJobTest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.daoTest.TfMemoryDaoFactory;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.daemon.TfAuditClearingJob;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class TfAuditClearingJobTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TfAuditClearingJobTest("testDaemon"));
  }
  
  /**
   * @param name
   */
  public TfAuditClearingJobTest(String name) {
    super(name);
  }

  
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    //twoFactorServer.auditRetention.level1.actions = OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR
    //twoFactorServer.auditRetention.level1.retentionDays = -1
    //
    //twoFactorServer.auditRetention.level2.actions = GENERATE_PASSWORDS, INVALIDATE_PASSWORDS, OPTIN_TWO_FACTOR_STEP1, UNTRUST_BROWSERS
    //twoFactorServer.auditRetention.level2.retentionDays = 60
    //
    //twoFactorServer.auditRetention.level3.actions = AUTHN_ERROR, AUTHN_NOT_OPTED_IN, AUTHN_TRUSTED_BROWSER, AUTHN_TWO_FACTOR, AUTHN_TWO_FACTOR_REQUIRED, AUTHN_WRONG_PASSWORD
    //twoFactorServer.auditRetention.level3.retentionDays = 15
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
        "twoFactorServer.auditRetention.level1.actions",
        "OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
        "twoFactorServer.auditRetention.level1.retentionDays",
        "-1");
    
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
        "twoFactorServer.auditRetention.level2.actions",
        "GENERATE_PASSWORDS, INVALIDATE_PASSWORDS, OPTIN_TWO_FACTOR_STEP1, UNTRUST_BROWSERS");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
        "twoFactorServer.auditRetention.level2.retentionDays",
        "60");
    
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
        "twoFactorServer.auditRetention.level3.actions",
        "AUTHN_ERROR, AUTHN_NOT_OPTED_IN, AUTHN_TRUSTED_BROWSER, AUTHN_TWO_FACTOR, AUTHN_TWO_FACTOR_REQUIRED, AUTHN_WRONG_PASSWORD");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
        "twoFactorServer.auditRetention.level3.retentionDays",
        "15");
    
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().clear();
  }

  /**
   * 
   */
  public void testDaemon() {
    TwoFactorDaoFactory twoFactorDaoFactory = TfMemoryDaoFactory.getFactory();
    //lets setup some audits...
    
    List<TwoFactorAudit> optin80daysOld = new ArrayList<TwoFactorAudit>();
    List<TwoFactorAudit> invalidatePasswords80daysOld = new ArrayList<TwoFactorAudit>();
    List<TwoFactorAudit> invalidatePasswords20daysOld = new ArrayList<TwoFactorAudit>();
    List<TwoFactorAudit> authn20daysOld = new ArrayList<TwoFactorAudit>();
    List<TwoFactorAudit> authn10daysOld = new ArrayList<TwoFactorAudit>();

    TfAuditClearingJob.testingLoopsThrough = 0;
    TfAuditClearingJob.testingTotalCount = 0;
    
    int recordsSize = 1500;
    for (int i=0;i<recordsSize;i++) {
      
      //optin that are 80 days old
      TwoFactorAudit twoFactorAudit = TwoFactorAudit.createAndStore(
          twoFactorDaoFactory, TwoFactorAuditAction.OPTIN_TWO_FACTOR, "1.2.3.4", "something", null, null);
      
      twoFactorAudit.setTheTimestamp(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 80));
      //System.out.println("optin 80 days: " + twoFactorAudit.getUuid() + ", " + twoFactorAudit.getAction()
      //    + ", " + new Date(twoFactorAudit.getTheTimestamp()));
      twoFactorAudit.store(twoFactorDaoFactory);
      optin80daysOld.add(twoFactorAudit);
      
      //invalidate passwords that are 80 days old
      twoFactorAudit = TwoFactorAudit.createAndStore(
          twoFactorDaoFactory, TwoFactorAuditAction.INVALIDATE_PASSWORDS, "1.2.3.4", "something", null, null);
      
      twoFactorAudit.setTheTimestamp(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 80));
      //System.out.println("invalidate 80 days: " + twoFactorAudit.getUuid() + ", " + twoFactorAudit.getAction()
      //    + ", " + new Date(twoFactorAudit.getTheTimestamp()));
      twoFactorAudit.store(twoFactorDaoFactory);
      invalidatePasswords80daysOld.add(twoFactorAudit);

      //invalidate passwords that are 20 days old
      twoFactorAudit = TwoFactorAudit.createAndStore(
          twoFactorDaoFactory, TwoFactorAuditAction.INVALIDATE_PASSWORDS, "1.2.3.4", "something", null, null);
      
      twoFactorAudit.setTheTimestamp(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 20));
      //System.out.println("invalidate 20 days: " + twoFactorAudit.getUuid() + ", " + twoFactorAudit.getAction()
      //    + ", " + new Date(twoFactorAudit.getTheTimestamp()));
      twoFactorAudit.store(twoFactorDaoFactory);
      invalidatePasswords20daysOld.add(twoFactorAudit);

      //authn that are 20 days old
      twoFactorAudit = TwoFactorAudit.createAndStore(
          twoFactorDaoFactory, TwoFactorAuditAction.AUTHN_TWO_FACTOR, "1.2.3.4", "something", null, null);
      
      twoFactorAudit.setTheTimestamp(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 20));
      //System.out.println("authn 20 days: " + twoFactorAudit.getUuid() + ", " + twoFactorAudit.getAction()
      //    + ", " + new Date(twoFactorAudit.getTheTimestamp()));
      twoFactorAudit.store(twoFactorDaoFactory);
      authn20daysOld.add(twoFactorAudit);

      //authn that are 10 days old
      twoFactorAudit = TwoFactorAudit.createAndStore(
          twoFactorDaoFactory, TwoFactorAuditAction.AUTHN_TWO_FACTOR, "1.2.3.4", "something", null, null);
      
      twoFactorAudit.setTheTimestamp(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 10));
      //System.out.println("authn 10 days: " + twoFactorAudit.getUuid() + ", " + twoFactorAudit.getAction()
      //    + ", " + new Date(twoFactorAudit.getTheTimestamp()));
      twoFactorAudit.store(twoFactorDaoFactory);
      authn10daysOld.add(twoFactorAudit);

      
    }
    
    //run the daemon
    new TfAuditClearingJob().auditClearingLogic(twoFactorDaoFactory);
    
    //check the lists
    for (TwoFactorAudit twoFactorAudit : optin80daysOld) {
      twoFactorAudit = twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(twoFactorAudit.getUuid());
      assertNull(twoFactorAudit.getDeletedOn());
    }
    
    //System.out.println("invalidatePasswords80daysOld: " + invalidatePasswords80daysOld.size());
    for (TwoFactorAudit twoFactorAudit : invalidatePasswords80daysOld) {
      twoFactorAudit = twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(twoFactorAudit.getUuid());
      assertNotNull(twoFactorAudit.getUuid() + ": " + new Date(twoFactorAudit.getTheTimestamp()), twoFactorAudit.getDeletedOn());
    }
    
    for (TwoFactorAudit twoFactorAudit : invalidatePasswords20daysOld) {
      twoFactorAudit = twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(twoFactorAudit.getUuid());
      assertNull(twoFactorAudit.getUuid() + ": " + new Date(twoFactorAudit.getTheTimestamp()), twoFactorAudit.getDeletedOn());
    }
    for (TwoFactorAudit twoFactorAudit : authn20daysOld) {
      twoFactorAudit = twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(twoFactorAudit.getUuid());
      assertNotNull(twoFactorAudit.getDeletedOn());
    }
    for (TwoFactorAudit twoFactorAudit : authn10daysOld) {
      twoFactorAudit = twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(twoFactorAudit.getUuid());
      assertNull(twoFactorAudit.getDeletedOn());
    }
    assertEquals(recordsSize * 2, TfAuditClearingJob.testingTotalCount);
    assertEquals(3 * (1+ (recordsSize / 1000)), TfAuditClearingJob.testingLoopsThrough);
    
    
  }
}
