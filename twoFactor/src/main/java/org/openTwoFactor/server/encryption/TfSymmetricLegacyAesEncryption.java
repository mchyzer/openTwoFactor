/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.encryption;

import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;


/**
 *
 */
public class TfSymmetricLegacyAesEncryption implements TfSymmetricEncryption {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    String key = "key";
    
    String legacyEncrypted = new TfSymmetricLegacyAesEncryption().encrypt(key, "abc");
    System.out.println("Legacy(" + key + ") encrypted: " + legacyEncrypted);
    
    String legacyDecrypted = new TfSymmetricLegacyAesEncryption().decrypt(key, legacyEncrypted);
    System.out.println("Legacy(" + key + ") decrypted: " + legacyDecrypted);
    
    String cbcEncrypted = new TfSymmetricEncryptAesCbcPkcs5Padding().encrypt(key, "abc");
    System.out.println("AesEcbPkcs5Padding(" + key + ") encrypted: " + cbcEncrypted);
    
    String cbcDecrypted = new TfSymmetricEncryptAesCbcPkcs5Padding().decrypt(key, cbcEncrypted);
    System.out.println("AesEcbPkcs5Padding(" + key + ") decrypted: " + cbcDecrypted);
    
    
  }
  
  /**
   * @see org.openTwoFactor.server.encryption.TfSymmetricEncryption#encrypt(java.lang.String, java.lang.String)
   */
  public String encrypt(String key, String data) {
    return new Crypto(key).encrypt(data);
  }

  /**
   * @see org.openTwoFactor.server.encryption.TfSymmetricEncryption#decrypt(java.lang.String, java.lang.String)
   */
  public String decrypt(String key, String encryptedData) {
    return new Crypto(key).decrypt(encryptedData);
  }

}
