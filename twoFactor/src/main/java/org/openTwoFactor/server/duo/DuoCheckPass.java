/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.duo;

import org.openTwoFactor.server.TwoFactorCheckPass;
import org.openTwoFactor.server.util.TwoFactorPassResult;


/**
 * check a token pass from duo
 */
public class DuoCheckPass implements TwoFactorCheckPass {

  /**
   * @see org.openTwoFactor.server.TwoFactorCheckPass#twoFactorCheckPassword(java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
   */
  public TwoFactorPassResult twoFactorCheckPassword(String secretStringBase32, String password,
      Long sequentialPassIndexAvailable, Long lastTotp30TimestampUsed,
      Long lastTotp60TimestampUsed, Long tokenIndexAvailable, String phonePass, String duoUserId) {
    
    boolean validCode = DuoCommands.verifyDuoCode(duoUserId, password);
    
    TwoFactorPassResult twoFactorPassResult = new TwoFactorPassResult();
    
    twoFactorPassResult.setPasswordCorrect(validCode);

    twoFactorPassResult.setTwoFactorCheckPassImplementation(DuoCheckPass.class);
    
    return twoFactorPassResult;
  }

}
