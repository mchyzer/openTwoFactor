/**
 * @author mchyzer
 * $Id: AllTfRestTests.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.rest;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;


/**
 *
 */
public class AllTfRestTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllTfRestTests.suite());
  }
  
  /**
   * 
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.openTwoFactor.server.ws.rest");
    //$JUnit-BEGIN$
    suite.addTestSuite(TfRestLogicTest.class);
    //$JUnit-END$
    return suite;
  }

}
