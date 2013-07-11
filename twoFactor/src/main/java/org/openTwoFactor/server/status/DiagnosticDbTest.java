
/**
 * 
 */
package org.openTwoFactor.server.status;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openTwoFactor.server.cache.TwoFactorCache;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;



/**
 * see if the server can connect to the DB (cache results)
 * @author mchyzer
 *
 */
public class DiagnosticDbTest extends DiagnosticTask {

  
  /**
   * cache the results
   */
  private static TwoFactorCache<String, Boolean> dbCache = new TwoFactorCache<String, Boolean>(DiagnosticDbTest.class.getName() + ".dbDiagnostic", 100, false, 120, 120, false);
  

  /**
   * @see DiagnosticTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    if (dbCache.containsKey("twoFactor")) {

      this.appendSuccessTextLine("Retrieved object from cache");

    } else {
      
      //doesnt matter what this returns
      TwoFactorDaoFactory.getFactory().getTwoFactorServiceProvider().retrieveByServiceProviderId("abc123xyz789");
  
    
      this.appendSuccessTextLine("Retrieved object from database");
      dbCache.put("twoFactor", Boolean.TRUE);
      
    }
    return true;
    
  }

  /**
   * @see DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "dbTest_twoFactor";
  }

  /**
   * @see DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Database test";
  }

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DiagnosticDbTest;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().toHashCode();
  }

}
