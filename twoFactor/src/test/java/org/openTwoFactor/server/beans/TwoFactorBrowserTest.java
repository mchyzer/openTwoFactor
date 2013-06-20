/**
 * @author mchyzer
 * $Id: TwoFactorBrowserTest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class TwoFactorBrowserTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TwoFactorBrowserTest("testPersistence"));
  }
  
  /**
   * @param name
   */
  public TwoFactorBrowserTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testPersistence() {

    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginidOrCreate(TwoFactorDaoFactory.getFactory(), "abc");
    
    String browserUuid = TwoFactorServerUtils.uuid();
    String browserUuidEncrypted = TwoFactorServerUtils.encryptSha(browserUuid);
    
    TwoFactorBrowser twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuid(TwoFactorDaoFactory.getFactory(), browserUuid, true);
    
    if (twoFactorBrowser != null) {
      twoFactorBrowser.delete(TwoFactorDaoFactory.getFactory());
    }

    twoFactorBrowser = new TwoFactorBrowser();
    twoFactorBrowser.setUuid(TwoFactorServerUtils.uuid());
    twoFactorBrowser.setBrowserTrustedUuidUnencrypted(browserUuid);
    twoFactorBrowser.setTrustedBrowser(true);
    twoFactorBrowser.setWhenTrusted(System.currentTimeMillis());
    twoFactorBrowser.setUserUuid(twoFactorUser.getUuid());
    
    twoFactorBrowser.store(TwoFactorDaoFactory.getFactory());
    
    int browserInsertsAndUpdates = TwoFactorBrowser.testInsertsAndUpdates;
    int browserDeletes = TwoFactorBrowser.testDeletes;
    
    assertEquals(browserUuidEncrypted, twoFactorBrowser.getBrowserTrustedUuid());
    
    twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuid(TwoFactorDaoFactory.getFactory(), browserUuid, true);
    
    //store it again, nothing should change
    twoFactorBrowser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(browserInsertsAndUpdates, TwoFactorBrowser.testInsertsAndUpdates);
    
    //update an attr
    twoFactorBrowser.setTrustedBrowser(false);
    twoFactorBrowser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(browserInsertsAndUpdates+1, TwoFactorBrowser.testInsertsAndUpdates);
  
    browserInsertsAndUpdates = TwoFactorBrowser.testInsertsAndUpdates;
    
    assertEquals(browserDeletes, TwoFactorBrowser.testDeletes);
    browserDeletes = TwoFactorBrowser.testDeletes;
    
    twoFactorBrowser.delete(TwoFactorDaoFactory.getFactory());

    assertEquals(browserDeletes + 1, TwoFactorBrowser.testDeletes);

    browserInsertsAndUpdates = TwoFactorBrowser.testInsertsAndUpdates;

    //try autocreate
    twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuidOrCreate(TwoFactorDaoFactory.getFactory(), browserUuid, twoFactorUser.getUuid(), false, true);
    
    assertNotNull(twoFactorBrowser);
    assertEquals(browserInsertsAndUpdates + 1, TwoFactorBrowser.testInsertsAndUpdates);
    
    //make sure in DB
    twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuid(TwoFactorDaoFactory.getFactory(), browserUuid, true);
    assertNotNull(twoFactorBrowser);

    browserInsertsAndUpdates = TwoFactorBrowser.testInsertsAndUpdates;
    
    //try again, shouldnt create
    twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuidOrCreate(TwoFactorDaoFactory.getFactory(), browserUuid, twoFactorUser.getUuid(), false, true);
    
    assertNotNull(twoFactorBrowser);
    assertEquals(browserInsertsAndUpdates, TwoFactorBrowser.testInsertsAndUpdates);

    twoFactorBrowser.delete(TwoFactorDaoFactory.getFactory());
    
    twoFactorUser.delete(TwoFactorDaoFactory.getFactory());
  }

}
