/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDaoTest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao.hibernate;

import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class HibernateTwoFactorUserDaoTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new HibernateTwoFactorUserDaoTest("testPersistence"));
  }

  /**
   * 
   */
  public HibernateTwoFactorUserDaoTest() {
    super();
    
  }

  /**
   * @param name
   */
  public HibernateTwoFactorUserDaoTest(String name) {
    super(name);
    
  }
  
  /**
   * 
   */
  public void atestPersistence() {

    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    
    if (twoFactorUser != null) {
      twoFactorUser.delete(TwoFactorDaoFactory.getFactory());
    }
    
    twoFactorUser = new TwoFactorUser();
    twoFactorUser.setUuid(TwoFactorServerUtils.uuid());
    twoFactorUser.setLoginid("abc");
    twoFactorUser.setTwoFactorSecret("def");
    
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
      
    twoFactorUser.delete(TwoFactorDaoFactory.getFactory());
  }
}
