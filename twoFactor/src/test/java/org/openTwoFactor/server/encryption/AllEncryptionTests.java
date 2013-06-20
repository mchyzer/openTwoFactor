/**
 * @author mchyzer
 * $Id: AllEncryptionTests.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.encryption;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllEncryptionTests {

  /**
   * 
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for org.openTwoFactor.server.encryption");
    //$JUnit-BEGIN$
    suite.addTestSuite(EncryptionKeyTest.class);
    //$JUnit-END$
    return suite;
  }

}
