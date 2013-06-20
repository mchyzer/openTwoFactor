/**
 * @author mchyzer
 * $Id: PublicKeyPoc.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.poc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import javax.crypto.Cipher;


/**
 *
 */
public class PublicKeyPoc {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main (String[] args) throws Exception {
    //
    // check args and get plaintext
    if (args.length !=1) {
      System.err.println("Usage: java PublicExample text");
    }
    byte[] plainText = null;
    if (args.length == 0) {
      plainText = "This is some text".getBytes("UTF8");
    } else {
      plainText = args[0].getBytes("UTF8");
    }
    //
    // generate an RSA key
    System.out.println( "\nStart generating RSA key" );
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(1024);
    KeyPair key = keyGen.generateKeyPair();
    System.out.println( "Finish generating RSA key" );
    System.out.println("Private: " + hex(key.getPrivate().getEncoded()));
    System.out.println("Public: " + hex(key.getPublic().getEncoded()));
    
    
    //
    // get an RSA cipher object and print the provider   
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    System.out.println( "\n" + cipher.getProvider().getInfo() );
    //
    // encrypt the plaintext using the public key
    System.out.println( "\nStart encryption" );
    cipher.init(Cipher.ENCRYPT_MODE, key.getPrivate());
    byte[] cipherText = cipher.doFinal(plainText);
    System.out.println( "Finish encryption: " );
    System.out.println("String: " + new String(cipherText, "UTF8") );
    System.out.println("String(hex): " + hex(cipherText) );
    //
    // decrypt the ciphertext using the private key
    System.out.println( "\nStart decryption" );
    cipher.init(Cipher.DECRYPT_MODE, key.getPublic());
    byte[] newPlainText = cipher.doFinal(cipherText);
    System.out.println( "Finish decryption: " );
    System.out.println( new String(newPlainText, "UTF8") );
  }
  
  /**
   * 
   * @param theBytes
   * @return string
   */
  public static String hex(byte[] theBytes) {
    StringBuilder hexStringBuilder = new StringBuilder();
    for (int i=0;i<theBytes.length;i++) {
      hexStringBuilder.append(Integer.toHexString(0xFF & theBytes[i]));
    }
    String hexString = hexStringBuilder.toString();
    hexStringBuilder = new StringBuilder();
    for (int i=0;i<hexString.length();i++) {
      if (i!=0 && i%2 == 0) {
        hexStringBuilder.append(' ');
      }
      hexStringBuilder.append(hexString.charAt(i));
    }
    return hexStringBuilder.toString();

  }
  
}
