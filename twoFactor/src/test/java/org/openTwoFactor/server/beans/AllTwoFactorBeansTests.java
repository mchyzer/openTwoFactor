/**
 * @author mchyzer
 * $Id: AllTwoFactorBeansTests.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 */
public class AllTwoFactorBeansTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllTwoFactorBeansTests.suite());
  }
  
  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.openTwoFactor.server.beans");
    //$JUnit-BEGIN$
    suite.addTestSuite(TwoFactorUserTest.class);
    suite.addTestSuite(TwoFactorBrowserTest.class);
    //suite.addTestSuite(TwoFactorUserAgentTest.class);
    suite.addTestSuite(TwoFactorServiceProviderTest.class);
    //suite.addTestSuite(TwoFactorIpAddressTest.class);
    //$JUnit-END$
    return suite;
  }

}
