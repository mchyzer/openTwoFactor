/**
 * @author mchyzer $Id$
 */
package org.openTwoFactor.server.encryption;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

/**
 * AES/ECB/PKCS5Padding
 */
public class TfSymmetricEncryptAesCbcPkcs5Padding implements TfSymmetricEncryption {

  /**
   * Hex encodes a byte array. <BR>
   * Returns an empty string if the input array is null or empty.
   * 
   * @param input bytes to encode
   * @return string containing hex representation of input byte array
   */
  public static String hexEncode(byte[] input)
  {
    if (input == null || input.length == 0)
    {
      return "";
    }

    int inputLength = input.length;
    StringBuilder output = new StringBuilder(inputLength * 2);

    for (int i = 0; i < inputLength; i++)
    {
      int next = input[i] & 0xff;
      if (next < 0x10)
      {
        output.append("0");
      }

      output.append(Integer.toHexString(next));
    }

    return output.toString();
  }

  /**
   * @see org.openTwoFactor.server.encryption.TfSymmetricEncryption#encrypt(java.lang.String, java.lang.String)
   */
  public String encrypt(String key, String data) {

    //    SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(), "AES");
    //
    //    // build the initialization vector.  This example is all zeros, but it 
    //    // could be any value or generated using a random number generator.
    //    byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    //    IvParameterSpec ivspec = new IvParameterSpec(iv);
    //
    //    // initialize the cipher for encrypt mode
    //    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    //    cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);
    //
    //    // encrypt the message
    //    byte[] encrypted = cipher.doFinal(message.getBytes());
    //    System.out.println("Ciphertext: " + hexEncode(encrypted) + "\n");
    //
    //    // reinitialize the cipher for decryption
    //    cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);
    //
    //    // decrypt the message
    //    byte[] decrypted = cipher.doFinal(encrypted);
    //    System.out.println("Plaintext: " + new String(decrypted) + "\n");
    //key = TwoFactorServerUtils.encryptMd5(key);
    try {

      byte[] keyBytes = retrieveKeyBytes(key);
      
      SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");

      // build the initialization vector.  This example is all zeros, but it 
      // could be any value or generated using a random number generator.
      byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      // initialize the cipher for encrypt mode
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);

      // encrypt the message
      byte[] encrypted = cipher.doFinal(data.getBytes());

      return new String(new Base64().encode(encrypted));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
  
  /**
   * so we dont have to keep validating
   */
  private static Set<String> validBaseKeys = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
  
  /**
   * if the base key is valid
   * @param baseKey
   * @param exceptionOnError 
   * @return if valid
   */
  private static boolean validateBaseKey(String baseKey, boolean exceptionOnError) {
    
    if (validBaseKeys.contains(baseKey)) {
      return true;
    }
    
    if (baseKey == null || baseKey.length() < 16) {
      if (exceptionOnError) {
        throw new RuntimeException("Base key for aes128 must be at least 16 byte (128 bit) value, make it longer: twoFactorServer.aes128keyBase32 : " 
            + baseKey.length());
      }
      return false;
    }
    
    //make sure it has lower, upper, numeric, and non-numeric
    if (!baseKey.matches(".*[a-z].*") || !baseKey.matches(".*[A-Z].*")
        || !baseKey.matches(".*[0-9].*") || !baseKey.matches(".*[^A-Za-z0-9].*")) {
      if (exceptionOnError) {
        throw new RuntimeException("Base key for aes128 must contain at least one upper, lower, numeric, and non alphanumeric: twoFactorServer.aes128keyBase32 : " 
            + baseKey.length());
      }
      return false;
      
    }

    validBaseKeys.add(baseKey);
    
    return true;
  }
  
  /**
   * hash key in case too long
   * get a base key (if key not long enough), overlay key on top
   * @param key
   * @return key bytes
   */
  private byte[] retrieveKeyBytes(String key) {
    
    String baseKey = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired(
        "twoFactorServer.aes128keyBase32");

    validateBaseKey(baseKey, true);
    
    //md5 if too long to use all chars
    byte[] baseKeyBytes = baseKey.getBytes();
    if (baseKeyBytes.length > 16) {
      baseKey = TwoFactorServerUtils.encryptMd5(baseKey);
      baseKeyBytes = baseKey.getBytes();
    }

    //lets hash the secret coming in in case it is the wrong size or whatever
    byte[] passedInKeyBytes = key.getBytes();

    //md5 if too long to use all chars
    if (passedInKeyBytes.length > 16) {
      key = TwoFactorServerUtils.encryptMd5(key);
      passedInKeyBytes = key.getBytes();
    }

    //overlay the passed in on top of the base (if too short this helps)
    System.arraycopy(passedInKeyBytes, 0, baseKeyBytes, 0,
        Math.min(passedInKeyBytes.length, baseKeyBytes.length));

    //we need 16 bytes, if longer (which will be after md5), take the first 16 bytes
    if (baseKeyBytes.length > 16) {
      byte[] newBaseKeyBytes = new byte[16];
      System.arraycopy(baseKeyBytes, 0, newBaseKeyBytes, 0, 16);
      baseKeyBytes = newBaseKeyBytes;
    }
    return baseKeyBytes;
  }

  /**
   * @see org.openTwoFactor.server.encryption.TfSymmetricEncryption#decrypt(java.lang.String, java.lang.String)
   */
  public String decrypt(String key, String encryptedData) {

    try {

      byte[] keyBytes = retrieveKeyBytes(key);

      SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");

      // build the initialization vector.  This example is all zeros, but it 
      // could be any value or generated using a random number generator.
      byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      // reinitialize the cipher for decryption
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

      byte[] encryptedDataBytes = new Base64().decode(encryptedData);

      // decrypt the message
      byte[] decrypted = cipher.doFinal(encryptedDataBytes);

      return new String(decrypted);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
