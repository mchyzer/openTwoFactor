/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.dao.TwoFactorRequiredUserDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.ws.rest.TfRestRequiredUser;


/**
 *
 */
public class HibernateTwoFactorRequiredUserDao implements TwoFactorRequiredUserDao {

  /**
   * 
   */
  public HibernateTwoFactorRequiredUserDao() {
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorRequiredUserDao#retrieveRequiredUsers()
   */
  public Map<String, TfRestRequiredUser> retrieveRequiredUsers() {

    Map<String, TfRestRequiredUser> result = new HashMap<String, TfRestRequiredUser>();
    
    String query = "select loginid, name, email from ( " 
        + TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.ws.restrictUsersQuery") + ")";

    List<Object[]> results = HibernateSession.bySqlStatic().listSelect(Object[].class, query, null);
            
    for (Object[] row : results) {
      TfRestRequiredUser tfRestRequiredUser = new TfRestRequiredUser();
      tfRestRequiredUser.setLoginid((String)row[0]);
      tfRestRequiredUser.setName((String)row[1]);
      tfRestRequiredUser.setEmail((String)row[2]);
      result.put(tfRestRequiredUser.getLoginid(), tfRestRequiredUser);
    }
    return result;
  }

}
