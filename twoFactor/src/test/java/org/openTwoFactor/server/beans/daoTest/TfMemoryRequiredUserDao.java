/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.HashMap;
import java.util.Map;

import org.openTwoFactor.server.dao.TwoFactorRequiredUserDao;
import org.openTwoFactor.server.ws.rest.TfRestRequiredUser;


/**
 *
 */
public class TfMemoryRequiredUserDao implements TwoFactorRequiredUserDao {

  /**
   * 
   */
  public static Map<String, TfRestRequiredUser> requiredUsers = new HashMap<String, TfRestRequiredUser>();
  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorRequiredUserDao#retrieveRequiredUsers()
   */
  public Map<String, TfRestRequiredUser> retrieveRequiredUsers() {
    return requiredUsers;
  }

}
