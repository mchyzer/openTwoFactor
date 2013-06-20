/**
 * @author mchyzer
 * $Id: AllTfUtilTests.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 */
public class AllTfUtilTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllTfUtilTests.suite());
  }
  
  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.openTwoFactor.server.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(TwoFactorServerUtilsTest.class);
    //$JUnit-END$
    return suite;
  }

}
