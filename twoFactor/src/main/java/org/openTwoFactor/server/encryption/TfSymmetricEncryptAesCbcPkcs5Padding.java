/**
 * @author mchyzer $Id$
 */
package org.openTwoFactor.server.encryption;

import java.util.Collections;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

/**
 * AES/ECB/PKCS5Padding
 */
public class TfSymmetricEncryptAesCbcPkcs5Padding implements TfSymmetricEncryption {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    String encrypted = new TfSymmetricEncryptAesCbcPkcs5Padding().encrypt("abc", "xyz");
    
    System.out.println("Encrypted: " + encrypted);
    
    String clear = new TfSymmetricEncryptAesCbcPkcs5Padding().decrypt("abc", encrypted);
    
    System.out.println("Clear: " + clear);
    
    encrypted = new TfSymmetricEncryptAesCbcPkcs5Padding().encrypt("abc", "xyz");
    
    System.out.println("Encrypted: " + encrypted);
    
    clear = new TfSymmetricEncryptAesCbcPkcs5Padding().decrypt("abc", encrypted);
    
    System.out.println("Clear: " + clear);
    
    System.out.println("Encryption key: " + EncryptionKey.decrypt("1399084459430__In7qzKqwFG2LNBwhCh6iM1TFqZ89yb9UG8ZbSiUg036BK3YkVSS8W52I3rLYKdx9cb4R3wlAH0rEqqBk+N+vtQ=="));
    
  }
  
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

    try {

      byte[] keyBytes = retrieveKeyBytes(key);

      SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");

      // build the initialization vector.  This example is all zeros, but it 
      // could be any value or generated using a random number generator.
      
      byte[] iv = new byte[16]; //Means 128 bit
      new Random().nextBytes(iv);
      
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      // initialize the cipher for encrypt mode
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);

      // encrypt the message
      byte[] encrypted = cipher.doFinal(data.getBytes());

      byte[] result = new byte[iv.length + encrypted.length];
      
      //copy initialization vector to result
      System.arraycopy(iv, 0, result, 0, iv.length);
      System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
      
      return new String(new Base64().encode(result));

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
    
    if (baseKey == null || baseKey.length() < 32) {
      if (exceptionOnError) {
        throw new RuntimeException("Base key for aes128 must be at least 32 char value, make it longer: twoFactorServer.aes128keyBase32 : " 
            + baseKey.length());
      }
      return false;
    }
    
    //make sure it has lower, upper, numeric, and non-numeric
    if (!baseKey.matches(".*[a-z].*[a-z].*[a-z].*") || !baseKey.matches(".*[A-Z].*[A-Z].*[A-Z].*")
        || !baseKey.matches(".*[0-9].*[0-9].*") || !baseKey.matches(".*[^A-Za-z0-9].*[^A-Za-z0-9].*[^A-Za-z0-9].*")) {
      if (exceptionOnError) {
        throw new RuntimeException("Base key for aes128 must contain at least three upper, lower, numeric, and non alphanumeric: twoFactorServer.aes128keyBase32 : " 
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

    //lets hash the secret coming in in case it is the wrong size or whatever
    byte[] passedInKeyBytes = null;
    
    {
      try {
        passedInKeyBytes = Hex.decodeHex(key.toCharArray());
      } catch (Exception e) {
        //ignore this, just use key
      }
      // try base32 if doesnt contain upper and lower (then base64)
      if (passedInKeyBytes == null && !(key.matches(".*[a-z].*") && key.matches(".*[A-Z].*"))) {
        try {
          passedInKeyBytes = new Base32().decode(key);
        } catch (Exception e) {
          //ignore this, just use key
        }
      }
      if (passedInKeyBytes == null) {
        try {
          passedInKeyBytes = new Base64().decode(key);
        } catch (Exception e) {
          //ignore this, just use key
        }
      }
      //cant use another encoding, just use the key as text
      if (passedInKeyBytes == null) {
        passedInKeyBytes = key.getBytes();
      }
    }
    
    byte[] keyBytesTemp = null;

    //if there are more bits in the basekey, then overlay the passed in key on top
    if (baseKeyBytes.length > passedInKeyBytes.length) {
      keyBytesTemp = baseKeyBytes;
      System.arraycopy(passedInKeyBytes, 0, keyBytesTemp, 0,
          passedInKeyBytes.length);
    } else {
      keyBytesTemp = passedInKeyBytes;
    }
    
    //base64 sha-1
    key = TwoFactorServerUtils.encryptSha(new String(keyBytesTemp));
    
    keyBytesTemp = Base64.decodeBase64(key);
    
    if (keyBytesTemp.length < 16) {
      throw new RuntimeException("Why is output less than 16????  should never happen, should be 20");
    }

    //take the first 16 bytes
    byte[] keyBytes = new byte[16];
    System.arraycopy(keyBytesTemp, 0, keyBytes, 0,
        keyBytes.length);
    
    //keyBytes
    return keyBytes;
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
      byte[] iv = new byte[16];
      
      byte[] totalBytes = new Base64().decode(encryptedData);

      byte[] encryptedDataBytes = new byte[totalBytes.length-iv.length];

      System.arraycopy(totalBytes, 0, iv, 0, iv.length);
      System.arraycopy(totalBytes, iv.length, encryptedDataBytes, 0, encryptedDataBytes.length);

      IvParameterSpec ivspec = new IvParameterSpec(iv);

      // reinitialize the cipher for decryption
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

      // decrypt the message
      byte[] decrypted = cipher.doFinal(encryptedDataBytes);

      return new String(decrypted);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
