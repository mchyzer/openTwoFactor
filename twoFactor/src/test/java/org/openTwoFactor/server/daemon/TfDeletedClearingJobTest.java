/**
 * @author mchyzer
 * $Id: TfDeletedClearingJobTest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
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
import org.openTwoFactor.server.daemon.TfDeletedClearingJob;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class TfDeletedClearingJobTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TfDeletedClearingJobTest("testDaemon"));
  }
  
  /**
   * @param name
   */
  public TfDeletedClearingJobTest(String name) {
    super(name);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    
    
    //  # permanently delete deleted records after this many minutes (2880 is 2 days)
    //  twoFactorServer.purgeDeletedRecordsAfterMinutes = 2880
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
        "twoFactorServer.purgeDeletedRecordsAfterMinutes",
        "2880");
    
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
    
    List<TwoFactorAudit> deleted3daysOld = new ArrayList<TwoFactorAudit>();
    List<TwoFactorAudit> deleted1dayOld = new ArrayList<TwoFactorAudit>();
  
    TfAuditClearingJob.testingLoopsThrough = 0;
    TfAuditClearingJob.testingTotalCount = 0;
    
    int recordsSize = 1500;
    for (int i=0;i<recordsSize;i++) {
      
      //optin that are 80 days old
      TwoFactorAudit twoFactorAudit = TwoFactorAudit.createAndStore(
          twoFactorDaoFactory, TwoFactorAuditAction.OPTIN_TWO_FACTOR, "1.2.3.4", "something", null, null, null);
      
      twoFactorAudit.setTheTimestamp(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 80));
      twoFactorAudit.setDeletedOn(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 3));
      twoFactorAudit.store(twoFactorDaoFactory);
      
      deleted3daysOld.add(twoFactorAudit);
      
      twoFactorAudit = TwoFactorAudit.createAndStore(
          twoFactorDaoFactory, TwoFactorAuditAction.OPTIN_TWO_FACTOR, "1.2.3.4", "something", null, null, null);
      
      twoFactorAudit.setTheTimestamp(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 80));
      twoFactorAudit.setDeletedOn(System.currentTimeMillis()-(1000L * 60 * 60 * 24 * 1));
      twoFactorAudit.store(twoFactorDaoFactory);
      
      deleted1dayOld.add(twoFactorAudit);
      
    }
    
    //run the daemon
    new TfDeletedClearingJob().deletedClearingLogic(twoFactorDaoFactory);
    
    //check the lists
    for (TwoFactorAudit twoFactorAudit : deleted3daysOld) {
      twoFactorAudit = twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(twoFactorAudit.getUuid());
      assertNull(twoFactorAudit);
    }
    
    for (TwoFactorAudit twoFactorAudit : deleted1dayOld) {
      twoFactorAudit = twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(twoFactorAudit.getUuid());
      assertNotNull(twoFactorAudit.getUuid() + ": " + new Date(twoFactorAudit.getTheTimestamp()), twoFactorAudit);
    }
    
    assertEquals(recordsSize , TfDeletedClearingJob.testingTotalCount);
    assertEquals((1+ (recordsSize / 1000)), TfDeletedClearingJob.testingLoopsThrough);
    
    
  }

}
