/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.encryption;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * encrypt with cbc, 
 */
public class TfSymmetricEncryptAesCbcCombo implements TfSymmetricEncryption {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfSymmetricEncryptAesCbcCombo.class);

  /**
   * @see org.openTwoFactor.server.encryption.TfSymmetricEncryption#encrypt(java.lang.String, java.lang.String)
   */
  public String encrypt(String key, String data) {
    return new TfSymmetricEncryptAesCbcPkcs5Padding().encrypt(key, data);
  }

  /**
   * @see org.openTwoFactor.server.encryption.TfSymmetricEncryption#decrypt(java.lang.String, java.lang.String)
   */
  public String decrypt(String key, String encryptedData) {
    try {
      return new TfSymmetricEncryptAesCbcPkcs5Padding().decrypt(key, encryptedData);
    } catch (Exception e) {
      //ignore, probably a padding issue or something
      LOG.debug("Error decrypting", e);
    }
    return new TfSymmetricLegacyAesEncryption().decrypt(key, encryptedData);
  }

}
