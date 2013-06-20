/**
 * @author mchyzer
 * $Id: EncryptionKey.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.encryption;

import java.io.InputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Crypto;


/**
 * encryption key management gives out different keys based on timestamp
 */
public class EncryptionKey {

  
  /**
   * 
   */
  private static final int SECRET_FILE_SIZE = 5000;

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    
//    for (int i=0;i<10;i++) {
//      System.out.println(encryptKeyForTimestamp(System.currentTimeMillis()));
//      TwoFactorServerUtils.sleep(10);
//    }
    
    //System.out.println();
    
    for (int i=0;i<10;i++) {
      System.out.println(encryptKeyForTimestamp(System.currentTimeMillis()));
      TwoFactorServerUtils.sleep(10);
    }
    
    String[] encryptionKeys = encryptionKeys();
    String[] thingsToEncrypt = new String[SECRET_FILE_SIZE];
    for (int i=0;i<SECRET_FILE_SIZE;i++) {
      thingsToEncrypt[i] = TwoFactorServerUtils.uuid() + TwoFactorServerUtils.uuid() + TwoFactorServerUtils.uuid() + TwoFactorServerUtils.uuid();
    }
    
    new Crypto(encryptionKeys[0]).encrypt(thingsToEncrypt[0]);
    
    long start = System.nanoTime();
    
    for (int i=0;i<10000;i++) {
      String encrypted = new Crypto(encryptionKeys[i%SECRET_FILE_SIZE]).encrypt(thingsToEncrypt[i%999]);
      new Crypto(encryptionKeys[i%SECRET_FILE_SIZE]).decrypt(encrypted);
    }
    
    System.out.println("10000 encryptions/decryptions took: " + ((System.nanoTime() - start)/1000000) + "ms");
    System.out.println("Encypt string size: " + thingsToEncrypt[0].length() + " chars");
    System.out.println("Encypted string size: " 
        + new Crypto(encryptionKeys[0]).encrypt(thingsToEncrypt[0]).length() + " chars");
    System.out.println("Encypted string example: " 
        + new Crypto(encryptionKeys[0]).encrypt(thingsToEncrypt[0]));
    
  }
  
  /** refresh the cache every 5 minutes */
  private static ExpirableCache<Boolean, String[]> encryptionKeysCache = new ExpirableCache<Boolean, String[]>(5);
  
  /**
   * get the SECRET_FILE_SIZE encryption keys of size 32
   * @return the encryption keys
   */
  private static String[] encryptionKeys() {
    String[] encryptionKeys = encryptionKeysCache.get(Boolean.TRUE);
    if (encryptionKeys != null) {
      return encryptionKeys;
    }
    
    synchronized (EncryptionKey.class) {
      encryptionKeys = encryptionKeysCache.get(Boolean.TRUE);
      if (encryptionKeys != null) {
        return encryptionKeys;
      }
      
      //rebuild
      String encryptionFileLocation  = TwoFactorServerConfig.retrieveConfig()
        .propertyValueStringRequired("twoFactorServer.encryptKeyFileLocation");
      
      InputStream inputStream = TwoFactorServerUtils.fileOrClasspathInputstream(encryptionFileLocation, 
          "twoFactorServer.encryptKeyFileLocation");
      
      Properties properties = new Properties();
      try {
        properties.load(inputStream);
        
      } catch (Exception e) {
        throw new RuntimeException("Problem reading file: " + encryptionFileLocation);
      
      } finally {
        TwoFactorServerUtils.closeQuietly(inputStream);
      }
      
      if (properties.size() != SECRET_FILE_SIZE) {
        throw new RuntimeException("Properties file should have 5000 keys: " 
            + encryptionFileLocation + ", " + properties.size());
      }
      
      encryptionKeys = new String[SECRET_FILE_SIZE];
             
      //loop through and set the keys and check the length
      for (int i=0;i<SECRET_FILE_SIZE;i++) {
        String encryptKey = properties.getProperty("encryptKey." + i);
        if (StringUtils.isBlank(encryptKey) || encryptKey.length() != 32) {
          throw new RuntimeException("Encryption key needs to be of size 32! " + encryptionFileLocation + ", " + "encryptKey." + i);
        }
        encryptionKeys[i] = encryptKey;
      }
      encryptionKeysCache.put(Boolean.TRUE, encryptionKeys);
    }
    return encryptionKeys;
  }

  /**
   * will return the timestamp used to encrypt underscore, underscore, and the data
   * @param data
   * @return the encrypted string
   */
  public static String encrypt(String data) {
    
    long timestamp = System.currentTimeMillis();
    
    String encryptKeyForTimestamp = encryptKeyForTimestamp(timestamp);
    
    String encryptedString = new Crypto(encryptKeyForTimestamp).encrypt(data);
    
    return timestamp + "__" + encryptedString;
  }

  /**
   * encrypted pattern is the pattern of the encrypted string
   */
  private static final Pattern encryptedPattern = Pattern.compile("^([0-9]+)__(.*)$");
  
  /**
   * decrypt data based on the key referenced by the timestamp
   * @param timestampAndEncryptedData must have the timestamp underscore underscore and the encrypted string
   * @return the decrypted string
   */
  public static String decrypt(String timestampAndEncryptedData) {
    
    if (StringUtils.isBlank(timestampAndEncryptedData)) {
      return null;
    }
    
    Matcher matcher = encryptedPattern.matcher(timestampAndEncryptedData);
    
    if (!matcher.matches()) {

      //probably shouldnt print out encrypted data, but not sure why it wouldnt match the regex
      throw new RuntimeException("Encrypted data doesnt match regex! " + timestampAndEncryptedData);
    
    }
    
    long timestamp = Long.parseLong(matcher.group(1));

    String encryptedData = matcher.group(2);
    
    String encryptKeyForTimestamp = encryptKeyForTimestamp(timestamp);
    
    String decryptedString = new Crypto(encryptKeyForTimestamp).decrypt(encryptedData);
    
    return decryptedString;
  }
  
  /**
   * get the encrypt key for a timestamp, which includes the timestamp, two underscores and the secret
   * @param timestamp
   * @return the key
   */
  private static String encryptKeyForTimestamp(long timestamp) {
    String[] encryptionKeys = encryptionKeys();

    //day since 1970 mod 5000
    int daySince1970modSize = (int)((timestamp / (1000 * 60 * 60 * 24)) % SECRET_FILE_SIZE);
    
    //millis mod 5000
    int millisModSize = (int)(timestamp % SECRET_FILE_SIZE);
    
    // 32 length uuid in file.  key is: 
    StringBuilder encryptionKey = new StringBuilder();
    encryptionKey.append(encryptionKeys[daySince1970modSize].substring(0,16));
    encryptionKey.append(encryptionKeys[millisModSize].substring(16,32));
    
    return encryptionKey.toString();
  }
  
}
