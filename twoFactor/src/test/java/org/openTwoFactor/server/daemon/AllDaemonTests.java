/**
 * @author mchyzer
 * $Id: AllDaemonTests.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllDaemonTests {

  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.openTwoFactor.server.daemon");
    //$JUnit-BEGIN$
    suite.addTestSuite(TfAuditClearingJobTest.class);
    suite.addTestSuite(TfDeletedClearingJobTest.class);
    //$JUnit-END$
    return suite;
  }

}
