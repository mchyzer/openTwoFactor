/**
 * @author mchyzer
 * $Id: AllTfServiceLogicTests.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 */
public class AllTfServiceLogicTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllTfServiceLogicTests.suite());
  }
  
  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.openTwoFactor.server.ui.serviceLogic");
    //$JUnit-BEGIN$
    suite.addTestSuite(UiMainTest.class);
    //$JUnit-END$
    return suite;
  }

}
