/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.dao;

import java.util.Map;

import org.openTwoFactor.server.ws.rest.TfRestRequiredUser;


/**
 * required user
 */
public interface TwoFactorRequiredUserDao {

  /**
   * get the map of required users with the key as the loginid
   * @return the map
   */
  public Map<String, TfRestRequiredUser> retrieveRequiredUsers();
  
}
