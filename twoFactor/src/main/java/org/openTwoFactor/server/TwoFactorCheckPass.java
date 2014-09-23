/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server;

import org.openTwoFactor.server.util.TwoFactorPassResult;


/**
 * check a token
 */
public interface TwoFactorCheckPass {

  /**
   * check a password.  
   * @param secretString base32
   * @param password e.g. 6 digit number
   * @param sequentialPassIndexAvailable
   * @param lastTotp30TimestampUsed
   * @param lastTotp60TimestampUsed
   * @param tokenIndexAvailable
   * @param phonePass
   * @param duoUserId is duo user id so we dont have to do another lookup
   * @return the result.  Note, if things are updated in result, will get updated in DB
   */
  public TwoFactorPassResult twoFactorCheckPassword(String secretString, String password, 
      Long sequentialPassIndexAvailable, Long lastTotp30TimestampUsed, 
      Long lastTotp60TimestampUsed, Long tokenIndexAvailable, String phonePass, String duoUserId);
}
