/**
 * @author mchyzer
 * $Id: GenerateKeys.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.encryption;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;

import org.apache.commons.codec.binary.Base64;


/**
 * generate keys for encryption, 5000 keys into a properties file.
 * Note, there are actually more keys used than this... 5000*5000 keys
 */
public class GenerateKeys {

  /**
   * return a secret which is 32 chars and alphanumeric (upper/lower)
   * @return the secret
   */
  private static String secret32char() {
    byte[] bytes = new byte[24]; 
    
    new SecureRandom().nextBytes(bytes);
    
    String string = null;
    
    try {
      string = new String(new Base64().encode(bytes), "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
    
    for (char theChar : new char[]{'+', '/', '='}) {
      if (string.contains(Character.toString(theChar))) {
        string = string.replace(theChar, (char)(new SecureRandom().nextInt(26) + 'A'));
      }
    }
    
    while (string.length() < 32) {
      string = (char)(new SecureRandom().nextInt(26) + 'A') + string;
    }

    if (string.length() > 32) {
      string = string.substring(0, 32);
    }
    
    if (!string.matches("[a-zA-Z0-9]{32}")) {
      throw new RuntimeException("Why not valid secret??? '" + string + "'");
    }
    return string;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    System.out.println("### Generated with " + GenerateKeys.class.getName() + "\n");
    
    //encryptKey.0 = abc123
    //encryptKey.1 = def456
    for (int i=0;i<5000;i++) {
      System.out.println("encryptKey." + i + " = " + secret32char());
    }
  }

}
