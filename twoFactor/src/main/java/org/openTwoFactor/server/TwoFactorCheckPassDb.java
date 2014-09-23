/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server;

import org.openTwoFactor.server.encryption.TwoFactorOath;
import org.openTwoFactor.server.util.TwoFactorPassResult;


/**
 * check token in DB
 */
public class TwoFactorCheckPassDb implements TwoFactorCheckPass {

  /**
   * @see org.openTwoFactor.server.TwoFactorCheckPass#twoFactorCheckPassword(java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
   */
  public TwoFactorPassResult twoFactorCheckPassword(String secretString, String password,
      Long sequentialPassIndexAvailable, Long lastTotp30TimestampUsed,
      Long lastTotp60TimestampUsed, Long tokenIndexAvailable, String phonePass, String duoUserId) {
    TwoFactorPassResult twoFactorPassResult = TwoFactorOath.twoFactorCheckPassword(
        secretString, password, sequentialPassIndexAvailable, 
        lastTotp30TimestampUsed, lastTotp60TimestampUsed, 
        tokenIndexAvailable, phonePass);
    
    twoFactorPassResult.setTwoFactorCheckPassImplementation(TwoFactorCheckPassDb.class);
    return twoFactorPassResult;
  }

}
