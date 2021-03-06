/**
 * @author mchyzer
 * $Id: TwoFactorIpAddressTest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import org.openTwoFactor.server.beans.TwoFactorIpAddress;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class TwoFactorIpAddressTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TwoFactorIpAddressTest("testPersistence"));
  }
  
  /**
   * @param name
   */
  public TwoFactorIpAddressTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void atestPersistence() {
    
    TwoFactorIpAddress twoFactorIpAddress = TwoFactorIpAddress.retrieveByIpAddress(TwoFactorDaoFactory.getFactory(), "1.2.3.4");
    
    if (twoFactorIpAddress != null) {
      twoFactorIpAddress.delete(TwoFactorDaoFactory.getFactory());
    }

    twoFactorIpAddress = TwoFactorIpAddress.retrieveByIpAddress(TwoFactorDaoFactory.getFactory(), "1.2.3.5");

    if (twoFactorIpAddress != null) {
      twoFactorIpAddress.delete(TwoFactorDaoFactory.getFactory());
    }

    twoFactorIpAddress = new TwoFactorIpAddress();
    twoFactorIpAddress.setUuid(TwoFactorServerUtils.uuid());
    twoFactorIpAddress.setDomainName("whatever.com");
    twoFactorIpAddress.setIpAddress("1.2.3.4");
    twoFactorIpAddress.setLookedUpDomainName(true);
    
    twoFactorIpAddress.store(TwoFactorDaoFactory.getFactory(), true);
    
    int ipAddressInsertsAndUpdates = TwoFactorIpAddress.testInsertsAndUpdates;
    int ipAddressDeletes = TwoFactorIpAddress.testDeletes;
    
    twoFactorIpAddress = TwoFactorIpAddress.retrieveByIpAddress(TwoFactorDaoFactory.getFactory(), "1.2.3.4");
    
    //store it again, nothing should change
    twoFactorIpAddress.store(TwoFactorDaoFactory.getFactory(), true);
    
    assertEquals(ipAddressInsertsAndUpdates, TwoFactorIpAddress.testInsertsAndUpdates);
    
    //update an attr
    twoFactorIpAddress.setIpAddress("1.2.3.5");
    twoFactorIpAddress.store(TwoFactorDaoFactory.getFactory(), true);
    
    assertEquals(ipAddressInsertsAndUpdates+1, TwoFactorIpAddress.testInsertsAndUpdates);
  
    ipAddressInsertsAndUpdates = TwoFactorIpAddress.testInsertsAndUpdates;
    
    assertEquals(ipAddressDeletes, TwoFactorIpAddress.testDeletes);
    ipAddressDeletes = TwoFactorIpAddress.testDeletes;
    
    twoFactorIpAddress.delete(TwoFactorDaoFactory.getFactory());

    assertEquals(ipAddressDeletes + 1, TwoFactorIpAddress.testDeletes);

    ipAddressInsertsAndUpdates = TwoFactorIpAddress.testInsertsAndUpdates;

    //try autocreate
    twoFactorIpAddress = TwoFactorIpAddress.retrieveByIpAddressOrCreate(TwoFactorDaoFactory.getFactory(), "1.2.3.4");
    
    assertNotNull(twoFactorIpAddress);
    assertEquals(ipAddressInsertsAndUpdates + 1, TwoFactorIpAddress.testInsertsAndUpdates);
    
    //make sure in DB
    twoFactorIpAddress = TwoFactorIpAddress.retrieveByIpAddress(TwoFactorDaoFactory.getFactory(), "1.2.3.4");
    assertNotNull(twoFactorIpAddress);

    ipAddressInsertsAndUpdates = TwoFactorIpAddress.testInsertsAndUpdates;
    
    //try again, shouldnt create
    twoFactorIpAddress = TwoFactorIpAddress.retrieveByIpAddressOrCreate(TwoFactorDaoFactory.getFactory(), "1.2.3.4");
    
    assertNotNull(twoFactorIpAddress);
    assertEquals(ipAddressInsertsAndUpdates, TwoFactorIpAddress.testInsertsAndUpdates);

    twoFactorIpAddress.delete(TwoFactorDaoFactory.getFactory());

  }

}
