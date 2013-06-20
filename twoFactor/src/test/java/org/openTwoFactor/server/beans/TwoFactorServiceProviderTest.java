/**
 * @author mchyzer
 * $Id: TwoFactorServiceProviderTest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import org.openTwoFactor.server.beans.TwoFactorServiceProvider;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class TwoFactorServiceProviderTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TwoFactorServiceProviderTest("testPersistence"));
  }
  
  /**
   * @param name
   */
  public TwoFactorServiceProviderTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testPersistence() {
    
    TwoFactorServiceProvider twoFactorServiceProvider = TwoFactorServiceProvider.retrieveByServiceProviderId(TwoFactorDaoFactory.getFactory(), "abc");
    
    if (twoFactorServiceProvider != null) {
      twoFactorServiceProvider.delete(TwoFactorDaoFactory.getFactory());
    }

    twoFactorServiceProvider = new TwoFactorServiceProvider();
    twoFactorServiceProvider.setUuid(TwoFactorServerUtils.uuid());
    twoFactorServiceProvider.setServiceProviderId("abc");
    twoFactorServiceProvider.setServiceProviderName("ABC");
    
    twoFactorServiceProvider.store(TwoFactorDaoFactory.getFactory());
    
    int serviceProviderInsertsAndUpdates = TwoFactorServiceProvider.testInsertsAndUpdates;
    int serviceProviderDeletes = TwoFactorServiceProvider.testDeletes;
    
    twoFactorServiceProvider = TwoFactorServiceProvider.retrieveByServiceProviderId(TwoFactorDaoFactory.getFactory(), "abc");
    
    //store it again, nothing should change
    twoFactorServiceProvider.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(serviceProviderInsertsAndUpdates, TwoFactorServiceProvider.testInsertsAndUpdates);
    
    //update an attr
    twoFactorServiceProvider.setServiceProviderName("ABD");
    twoFactorServiceProvider.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(serviceProviderInsertsAndUpdates+1, TwoFactorServiceProvider.testInsertsAndUpdates);
  
    serviceProviderInsertsAndUpdates = TwoFactorServiceProvider.testInsertsAndUpdates;
    
    assertEquals(serviceProviderDeletes, TwoFactorServiceProvider.testDeletes);
    serviceProviderDeletes = TwoFactorServiceProvider.testDeletes;
    
    twoFactorServiceProvider.delete(TwoFactorDaoFactory.getFactory());

    assertEquals(serviceProviderDeletes + 1, TwoFactorServiceProvider.testDeletes);

    serviceProviderInsertsAndUpdates = TwoFactorServiceProvider.testInsertsAndUpdates;

    //try autocreate
    twoFactorServiceProvider = TwoFactorServiceProvider.retrieveByServiceProviderIdOrCreate(TwoFactorDaoFactory.getFactory(), "abc", "ABC");
    
    assertNotNull(twoFactorServiceProvider);
    assertEquals(serviceProviderInsertsAndUpdates + 1, TwoFactorServiceProvider.testInsertsAndUpdates);
    
    //make sure in DB
    twoFactorServiceProvider = TwoFactorServiceProvider.retrieveByServiceProviderId(TwoFactorDaoFactory.getFactory(), "abc");
    assertNotNull(twoFactorServiceProvider);
    
    assertEquals("ABC", twoFactorServiceProvider.getServiceProviderName());
    
    serviceProviderInsertsAndUpdates = TwoFactorServiceProvider.testInsertsAndUpdates;
    
    String uuid = twoFactorServiceProvider.getUuid();
    
    //try again, should update
    twoFactorServiceProvider = TwoFactorServiceProvider.retrieveByServiceProviderIdOrCreate(TwoFactorDaoFactory.getFactory(), "abc", "ABD");

    assertNotNull(twoFactorServiceProvider);
    assertEquals(serviceProviderInsertsAndUpdates + 1, TwoFactorServiceProvider.testInsertsAndUpdates);
    assertEquals("ABD", twoFactorServiceProvider.getServiceProviderName());
    assertEquals(uuid, twoFactorServiceProvider.getUuid());


    serviceProviderInsertsAndUpdates = TwoFactorServiceProvider.testInsertsAndUpdates;
    
    //try again, shouldnt create or change
    twoFactorServiceProvider = TwoFactorServiceProvider.retrieveByServiceProviderIdOrCreate(TwoFactorDaoFactory.getFactory(), "abc", "ABD");
    
    assertNotNull(twoFactorServiceProvider);
    assertEquals(serviceProviderInsertsAndUpdates, TwoFactorServiceProvider.testInsertsAndUpdates);

    twoFactorServiceProvider.delete(TwoFactorDaoFactory.getFactory());

  }

}
