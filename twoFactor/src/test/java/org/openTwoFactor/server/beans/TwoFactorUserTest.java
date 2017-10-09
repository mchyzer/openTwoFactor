/**
 * @author mchyzer
 * $Id: TwoFactorUserTest.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditView;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.beans.TwoFactorUserAttr.TwoFactorUserAttrName;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class TwoFactorUserTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TwoFactorUserTest("testPersistence"));
  }
  
  /**
   * @param name
   */
  public TwoFactorUserTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testAudits() {
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    
    if (twoFactorUser != null) {
      twoFactorUser.delete(TwoFactorDaoFactory.getFactory());
    }
    
    twoFactorUser = new TwoFactorUser();
    twoFactorUser.setUuid(TwoFactorServerUtils.uuid());
    twoFactorUser.setLoginid("abc");
    twoFactorUser.setTwoFactorSecret("def");
    
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    int auditInsertsAndUpdates = TwoFactorAudit.testInsertsAndUpdates;
    
    twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    
    //store it again, nothing should change
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(auditInsertsAndUpdates, TwoFactorAudit.testInsertsAndUpdates);
    
    List<TwoFactorAuditView> audits = TwoFactorAudit.retrieveByUser(TwoFactorDaoFactory.getFactory(), twoFactorUser.getUuid(), null);
    @SuppressWarnings("unused")
    int auditsSize = TwoFactorServerUtils.length(audits);
    //assertTrue(auditsSize > 0);
    
    //update an attr
    twoFactorUser.setTwoFactorSecret("efg");
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    //assertEquals(auditInsertsAndUpdates+1, TwoFactorAudit.testInsertsAndUpdates);
  
    //retrieve
    audits = TwoFactorAudit.retrieveByUser(TwoFactorDaoFactory.getFactory(), twoFactorUser.getUuid(), null);
    //assertEquals(audits.size(), auditsSize+1);
    
    auditInsertsAndUpdates = TwoFactorAudit.testInsertsAndUpdates;
    
    
    //insert an attr
    twoFactorUser.setTwoFactorSecretTemp("hij");
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    //assertEquals(auditInsertsAndUpdates+1, TwoFactorAudit.testInsertsAndUpdates);
  
    int auditDeletes = TwoFactorAudit.testDeletes;
    
    audits = TwoFactorAudit.retrieveByUser(TwoFactorDaoFactory.getFactory(), twoFactorUser.getUuid(), null);
    
    for (TwoFactorAuditView audit : audits) {
      
      TwoFactorAudit twoFactorAudit = TwoFactorDaoFactory.getFactory().getTwoFactorAudit().retrieveByUuid(audit.getUuid());
      
      twoFactorAudit.delete(TwoFactorDaoFactory.getFactory());
    }
    assertEquals(auditDeletes+audits.size(), TwoFactorAudit.testDeletes);
  }

  /**
   * 
   */
  public void testPersistence() {
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    
    if (twoFactorUser != null) {
      twoFactorUser.delete(TwoFactorDaoFactory.getFactory());
    }

    twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "123");
    
    if (twoFactorUser != null) {
      twoFactorUser.delete(TwoFactorDaoFactory.getFactory());
    }

    twoFactorUser = new TwoFactorUser();
    twoFactorUser.setUuid(TwoFactorServerUtils.uuid());
    twoFactorUser.setLoginid("abc");
    twoFactorUser.setTwoFactorSecret("def");
    
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    int userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;
    int userAttrInsertsAndUpdates = TwoFactorUserAttr.testInsertsAndUpdates;
    
    twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    
    //store it again, nothing should change
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(userInsertsAndUpdates, TwoFactorUser.testInsertsAndUpdates);
    assertEquals(userAttrInsertsAndUpdates, TwoFactorUserAttr.testInsertsAndUpdates);
    
    //update an attr
    twoFactorUser.setTwoFactorSecret("efg");
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(userInsertsAndUpdates, TwoFactorUser.testInsertsAndUpdates);
    assertEquals(userAttrInsertsAndUpdates+1, TwoFactorUserAttr.testInsertsAndUpdates);
  
    userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;
    userAttrInsertsAndUpdates = TwoFactorUserAttr.testInsertsAndUpdates;
    
    
    //insert an attr
    twoFactorUser.setTwoFactorSecretTemp("hij");
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(userInsertsAndUpdates, TwoFactorUser.testInsertsAndUpdates);
    assertEquals(userAttrInsertsAndUpdates+1, TwoFactorUserAttr.testInsertsAndUpdates);
  
    userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;
    userAttrInsertsAndUpdates = TwoFactorUserAttr.testInsertsAndUpdates;
    int userAttrDeletes = TwoFactorUserAttr.testDeletes;
    
    //delete an attr
    twoFactorUser.attributeDeleteFromDb(TwoFactorUserAttrName.two_factor_secret_temp);
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(userInsertsAndUpdates, TwoFactorUser.testInsertsAndUpdates);
    assertEquals(userAttrInsertsAndUpdates+1, TwoFactorUserAttr.testInsertsAndUpdates);
    assertEquals(userAttrDeletes, TwoFactorUserAttr.testDeletes);

    userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;
    userAttrInsertsAndUpdates = TwoFactorUserAttr.testInsertsAndUpdates;

    //see if its still there
    twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    String secretTemp = twoFactorUser.getTwoFactorSecretTemp();
    assertNull(secretTemp, secretTemp);
    
    twoFactorUser.setTwoFactorSecretTemp("klm");
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    secretTemp = twoFactorUser.getTwoFactorSecretTemp();
    assertEquals("klm", secretTemp);

    assertEquals(userInsertsAndUpdates, TwoFactorUser.testInsertsAndUpdates);
    assertEquals(userAttrInsertsAndUpdates+1, TwoFactorUserAttr.testInsertsAndUpdates);

    userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;
    userAttrInsertsAndUpdates = TwoFactorUserAttr.testInsertsAndUpdates;
    
    //update a field
    twoFactorUser.setLoginid("123");
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    assertEquals(userInsertsAndUpdates+1, TwoFactorUser.testInsertsAndUpdates);
    assertEquals(userAttrInsertsAndUpdates, TwoFactorUserAttr.testInsertsAndUpdates);
  
    userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;
    userAttrInsertsAndUpdates = TwoFactorUserAttr.testInsertsAndUpdates;
    
    userAttrDeletes = TwoFactorUserAttr.testDeletes;
    int userDeletes = TwoFactorUser.testDeletes;
    
    twoFactorUser.delete(TwoFactorDaoFactory.getFactory());

    
    assertEquals(userDeletes + 1, TwoFactorUser.testDeletes);

    userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;

    //try autocreate
    twoFactorUser = TwoFactorUser.retrieveByLoginidOrCreate(TwoFactorDaoFactory.getFactory(), "abc");
    
    assertNotNull(twoFactorUser);
    assertEquals(userInsertsAndUpdates + 1, TwoFactorUser.testInsertsAndUpdates);
    
    //make sure in DB
    twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), "abc");
    assertNotNull(twoFactorUser);

    userInsertsAndUpdates = TwoFactorUser.testInsertsAndUpdates;
    
    //try again, shouldnt create
    twoFactorUser = TwoFactorUser.retrieveByLoginidOrCreate(TwoFactorDaoFactory.getFactory(), "abc");
    
    assertNotNull(twoFactorUser);
    assertEquals(userInsertsAndUpdates, TwoFactorUser.testInsertsAndUpdates);

    twoFactorUser.delete(TwoFactorDaoFactory.getFactory());
    
  }

}
