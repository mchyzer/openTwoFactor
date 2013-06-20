/**
 * @author mchyzer
 * $Id: AllTwoFactorTests.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server;

import org.openTwoFactor.server.beans.AllTwoFactorBeansTests;
import org.openTwoFactor.server.daemon.AllDaemonTests;
import org.openTwoFactor.server.encryption.AllEncryptionTests;
import org.openTwoFactor.server.ui.serviceLogic.AllTfServiceLogicTests;
import org.openTwoFactor.server.util.AllTfUtilTests;
import org.openTwoFactor.server.ws.rest.AllTfRestTests;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 */
public class AllTwoFactorTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllTwoFactorTests.suite());
  }
  
  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.openTwoFactor.server");
    //$JUnit-BEGIN$

    suite.addTest(AllDaemonTests.suite());
    suite.addTest(AllEncryptionTests.suite());
    suite.addTest(AllTfRestTests.suite());
    suite.addTest(AllTfServiceLogicTests.suite());
    suite.addTest(AllTfUtilTests.suite());
    suite.addTest(AllTwoFactorBeansTests.suite());

    //$JUnit-END$
    return suite;
  }

}
