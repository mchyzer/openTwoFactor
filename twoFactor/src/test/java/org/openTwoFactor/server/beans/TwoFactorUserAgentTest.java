/**
 * @author mchyzer
 * $Id: TwoFactorUserAgentTest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class TwoFactorUserAgentTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TwoFactorUserAgentTest("testPersistence"));
  }
  
  /**
   * @param name
   */
  public TwoFactorUserAgentTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void atestPersistence() {
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
    String userAgent2 = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:10.0.11) Gecko/20100101 Firefox/10.0.11";
    
    TwoFactorUserAgent twoFactorUserAgent = TwoFactorUserAgent.retrieveByUserAgent(TwoFactorDaoFactory.getFactory(), userAgent1);
    
    if (twoFactorUserAgent != null) {
      twoFactorUserAgent.delete(TwoFactorDaoFactory.getFactory());
    }
    
    twoFactorUserAgent = TwoFactorUserAgent.retrieveByUserAgent(TwoFactorDaoFactory.getFactory(), userAgent2);
    
    if (twoFactorUserAgent != null) {
      twoFactorUserAgent.delete(TwoFactorDaoFactory.getFactory());
    }
    
    twoFactorUserAgent = TwoFactorUserAgent.retrieveByUserAgent(TwoFactorDaoFactory.getFactory(), userAgent1);

    if (twoFactorUserAgent != null) {
      twoFactorUserAgent.delete(TwoFactorDaoFactory.getFactory());
    }

    twoFactorUserAgent = new TwoFactorUserAgent();
    twoFactorUserAgent.setUuid(TwoFactorServerUtils.uuid());
    twoFactorUserAgent.setUserAgent(userAgent1);
    twoFactorUserAgent.calculateBrowserFields();

    twoFactorUserAgent.store(TwoFactorDaoFactory.getFactory(), true);
    
    int userAgentInsertsAndUpdates = TwoFactorUserAgent.testInsertsAndUpdates;
    int userAgentDeletes = TwoFactorUserAgent.testDeletes;
    
    twoFactorUserAgent = TwoFactorUserAgent.retrieveByUserAgent(TwoFactorDaoFactory.getFactory(), userAgent1);
    
    //store it again, nothing should change
    twoFactorUserAgent.store(TwoFactorDaoFactory.getFactory(), true);
    
    assertEquals(userAgentInsertsAndUpdates, TwoFactorUserAgent.testInsertsAndUpdates);
    
    //update an attr
    twoFactorUserAgent.setUserAgent(userAgent2);
    twoFactorUserAgent.store(TwoFactorDaoFactory.getFactory(), true);
    
    assertEquals(userAgentInsertsAndUpdates+1, TwoFactorUserAgent.testInsertsAndUpdates);
  
    userAgentInsertsAndUpdates = TwoFactorUserAgent.testInsertsAndUpdates;
    
    assertEquals(userAgentDeletes, TwoFactorUserAgent.testDeletes);
    userAgentDeletes = TwoFactorUserAgent.testDeletes;
    
    twoFactorUserAgent.delete(TwoFactorDaoFactory.getFactory());

    assertEquals(userAgentDeletes + 1, TwoFactorUserAgent.testDeletes);

    userAgentInsertsAndUpdates = TwoFactorUserAgent.testInsertsAndUpdates;

    //try autocreate
    twoFactorUserAgent = TwoFactorUserAgent.retrieveByUserAgentOrCreate(TwoFactorDaoFactory.getFactory(), userAgent1);
    
    assertNotNull(twoFactorUserAgent);
    assertEquals(userAgentInsertsAndUpdates + 1, TwoFactorUserAgent.testInsertsAndUpdates);
    
    //make sure in DB
    twoFactorUserAgent = TwoFactorUserAgent.retrieveByUserAgent(TwoFactorDaoFactory.getFactory(), userAgent1);
    assertNotNull(twoFactorUserAgent);

    userAgentInsertsAndUpdates = TwoFactorUserAgent.testInsertsAndUpdates;
    
    //try again, shouldnt create
    twoFactorUserAgent = TwoFactorUserAgent.retrieveByUserAgentOrCreate(TwoFactorDaoFactory.getFactory(), userAgent1);
    
    assertNotNull(twoFactorUserAgent);
    assertEquals(userAgentInsertsAndUpdates, TwoFactorUserAgent.testInsertsAndUpdates);

    twoFactorUserAgent.delete(TwoFactorDaoFactory.getFactory());

  }

}
