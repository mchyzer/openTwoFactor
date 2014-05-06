/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.encryption;

import java.security.SecureRandom;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * call command line like:  /opt/appserv/tomcat/apps/twoFactor/java/bin/java -cp "/opt/appserv/tomcat/apps/twoFactorWs/webapps/twoFactorWs/WEB-INF/classes:/opt/appserv/tomcat/apps/twoFactorWs/webapps/twoFactorWs/WEB-INF/lib/*:/opt/appserv/common/tomcat6_32_ds_base/lib/servlet-api.jar" org.openTwoFactor.server.encryption.TfSymmetricUtils
 */
public class TfSymmetricUtils {

  /**
   * 20 special chars
   */
  private static final char[] SPECIAL_CHARS = new char[]{'!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', '=', '+', '[', ']', '{', '}', ';', ':'};
  
  /**
   * Encrypt a sample message using AES in CBC mode with an IV.
   * 
   * @param args not used
   * @throws Exception if the algorithm, key, iv or any other parameter is
   *             invalid.
   */
  public static void main(String[] args) throws Exception {

    if (args.length < 1) {
      usage();
    }

    if (args.length == 3 && StringUtils.equals(args[0], "encryptCbc")) {
      
      String secret = args[1];
      String text = args[2];
      
      String encrypted = new TfSymmetricEncryptAesCbcPkcs5Padding().encrypt(secret, text);
      
      System.out.println(encrypted);
          
      return;
      
    } else if (args.length == 3 && StringUtils.equals(args[0], "decryptCbc")) {
      
      String secret = args[1];
      String encryptedText = args[2];
      
      String decrypted = new TfSymmetricEncryptAesCbcPkcs5Padding().decrypt(secret, encryptedText);
      
      System.out.println(decrypted);
      return;
      
    } else     if (args.length == 2 && StringUtils.equals(args[0], "encryptKeyCbc")) {
      
      String text = args[1];
      
      String encrypted = EncryptionKey.encrypt(text, new TfSymmetricEncryptAesCbcPkcs5Padding());
      
      System.out.println(encrypted);
          
      return;
      
    } else if (args.length == 2 && StringUtils.equals(args[0], "decryptKeyCbc")) {
      
      String encryptedText = args[1];
      
      String decrypted = EncryptionKey.decrypt(encryptedText, new TfSymmetricEncryptAesCbcPkcs5Padding());
      
      System.out.println(decrypted);
      return;
            
    } else if (args.length == 1 && StringUtils.equals(args[0], "generatePass")) {
      
      char[] pass = new char[40];
      for (int i=0;i<pass.length;i++) {
        
        int theInt = new SecureRandom().nextInt(26+26+10+20);
        
        if (theInt < 26) {
          pass[i] = (char)('a' + theInt);
        } else {
          theInt -= 26;
          if (theInt < 26) {
            pass[i] = (char)('A' + theInt);
          } else {
            theInt -= 26;
            if (theInt < 10) {
              pass[i] = (char)('0' + theInt);
            } else {
              theInt -= 10;
              pass[i] = SPECIAL_CHARS[theInt];
            }
          }
        }
      }
      
      System.out.println("Secure pass is: " + new String(pass));
      return;
      
    } else if (args.length == 1 && StringUtils.equals(args[0], "dumpSecretsAsIs")) {
      for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("two_factor_secret")) {
        
        String attributeValue = twoFactorUserAttr.getAttributeValueString();
        
        if (!StringUtils.isBlank(attributeValue)) {

          System.out.println("update two_factor_user_attr set attribute_value_string = '" 
              + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");
          
        }
      }
      for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("phone_code_encrypted")) {
        
        String attributeValue = twoFactorUserAttr.getAttributeValueString();
        
        if (!StringUtils.isBlank(attributeValue)) {

          System.out.println("update two_factor_user_attr set attribute_value_string = '" 
              + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");
          
        }
      }
      for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("two_factor_secret_temp")) {
        
        String attributeValue = twoFactorUserAttr.getAttributeValueString();
        
        if (!StringUtils.isBlank(attributeValue)) {

          System.out.println("update two_factor_user_attr set attribute_value_string = '" 
              + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");
          
        }
      }
      for (TwoFactorDeviceSerial twoFactorDeviceSerial : TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveAll()) {
        
        String twoFactorSecret = twoFactorDeviceSerial.getTwoFactorSecret();
        
        if (!StringUtils.isBlank(twoFactorSecret)) {

          System.out.println("update two_factor_device_serial set two_factor_secret = '" 
              + twoFactorSecret + "' where uuid = '" + twoFactorDeviceSerial.getUuid() + "';");

        }
      }
      System.out.println("commit;");
      return;  
    }
    if (args.length == 1 && StringUtils.equals(args[0], "dumpSecretsPlainCbc")) {
      dumpSecrets(new TfSymmetricEncryptAesCbcPkcs5Padding(), null);
      return;  

    }

    if (args.length == 1 && StringUtils.equals(args[0], "dumpSecretsPlainLegacy")) {
      dumpSecrets(new TfSymmetricLegacyAesEncryption(), null);
      return;  

    }
    if (args.length == 1 && StringUtils.equals(args[0], "dumpSecretsLegacyToCbc")) {
      dumpSecrets(new TfSymmetricLegacyAesEncryption(), new TfSymmetricEncryptAesCbcPkcs5Padding());
      return;  

    }

    usage();

  }

  /**
   * @param decryptEncryption 
   * @param encryptEncryption 
   * 
   */
  private static void dumpSecrets(TfSymmetricEncryption decryptEncryption,
       TfSymmetricEncryption encryptEncryption) {
    for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("two_factor_secret")) {
        
        String attributeValue = twoFactorUserAttr.getAttributeValueString();
        
        if (!StringUtils.isBlank(attributeValue)) {
          
          //get the raw secret
          String secret = EncryptionKey.decrypt(attributeValue, decryptEncryption);

          if (encryptEncryption == null) {

            System.out.println("Uuid: " + twoFactorUserAttr.getUuid() + ", secret: " + secret);
            
          } else {
            
            attributeValue = EncryptionKey.encrypt(secret, encryptEncryption);
            
            {
              String decrypted = EncryptionKey.decrypt(attributeValue, encryptEncryption);
              //System.out.println(secret + ", " + decrypted + ", " + attributeValue);
              if (!StringUtils.equals(secret, decrypted)) {
                System.out.println("PROBLEM with attribute: " + twoFactorUserAttr.getUuid() + ", '" + secret + "', decryptedSecret: '" + decrypted + "'");
              }
            }
            
            System.out.println("update two_factor_user_attr set attribute_value_string = '" 
                + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");

          }
          
          TwoFactorServerUtils.sleep(20);

        }
      }
      for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("phone_code_encrypted")) {

        String attributeValue = twoFactorUserAttr.getAttributeValueString();

        if (!StringUtils.isBlank(attributeValue)) {

          //get the raw secret
          String secret = EncryptionKey.decrypt(attributeValue, decryptEncryption);

          if (encryptEncryption == null) {

            System.out.println("Uuid: " + twoFactorUserAttr.getUuid() + ", secret: " + secret);
            
          } else {
            
            attributeValue = EncryptionKey.encrypt(secret, encryptEncryption);

            {
              String decrypted = EncryptionKey.decrypt(attributeValue, encryptEncryption);
              //System.out.println(decrypted + ", " + attributeValue);
              if (!StringUtils.equals(secret, decrypted)) {
                System.out.println("PROBLEM with attribute: " + twoFactorUserAttr.getUuid() + ", '" + secret + "', decryptedSecret: '" + decrypted + "'");
              }
            }
            

            System.out.println("update two_factor_user_attr set attribute_value_string = '" 
               + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");
          }

          TwoFactorServerUtils.sleep(20);

        }
      }
      for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("two_factor_secret_temp")) {

        String attributeValue = twoFactorUserAttr.getAttributeValueString();
        
        if (!StringUtils.isBlank(attributeValue)) {

          //get the raw secret
          String secret = EncryptionKey.decrypt(attributeValue, decryptEncryption);
          
          if (encryptEncryption == null) {

            System.out.println("Uuid: " + twoFactorUserAttr.getUuid() + ", secret: " + secret);
            
          } else {
            
            attributeValue = EncryptionKey.encrypt(secret, encryptEncryption);

            {
              String decrypted = EncryptionKey.decrypt(attributeValue, encryptEncryption);
              //System.out.println(secret + ", " + decrypted + ", " + attributeValue);
              if (!StringUtils.equals(secret, decrypted)) {
                System.out.println("PROBLEM with attribute: " + twoFactorUserAttr.getUuid() + ", '" + secret + "', decryptedSecret: '" + decrypted + "'");
              }
            }

            System.out.println("update two_factor_user_attr set attribute_value_string = '" 
                + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");

          }
          
          TwoFactorServerUtils.sleep(20);

        }
      }
      for (TwoFactorDeviceSerial twoFactorDeviceSerial : TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveAll()) {

        String twoFactorSecret = twoFactorDeviceSerial.getTwoFactorSecret();

        if (!StringUtils.isBlank(twoFactorSecret)) {

          //get the raw secret
          String secret = EncryptionKey.decrypt(twoFactorSecret, decryptEncryption);

          if (encryptEncryption == null) {

            System.out.println("Uuid: " + twoFactorDeviceSerial.getUuid() + ", secret: " + secret);
            
          } else {
            
            twoFactorSecret = EncryptionKey.encrypt(secret, encryptEncryption);

            {
              String decrypted = EncryptionKey.decrypt(twoFactorSecret, encryptEncryption);
              //System.out.println(secret + ", " + decrypted + ", " + twoFactorSecret);
              if (!StringUtils.equals(secret, decrypted)) {
                System.out.println("PROBLEM with serial: " + twoFactorDeviceSerial.getUuid() + ", '" + secret + "', decryptedSecret: '" + decrypted + "'");
              }
            }
            

            System.out.println("update two_factor_device_serial set two_factor_secret = '" 
                + twoFactorSecret + "' where uuid = '" + twoFactorDeviceSerial.getUuid() + "';");
          }
          
          TwoFactorServerUtils.sleep(20);

        }
      }
      System.out.println("commit;");
  }

  /**
   * 
   */
  private static void usage() {

    System.out.println("Call with 'generatePass' to generate a secure password for config file");
    System.out.println("Call with 'dumpSecretsAsIs' to generate a backup script for secrets");
    System.out.println("Call with 'dumpSecretsLegacyToCbc' to generate an update script to convert from legacy to CBC");
    System.out.println("Call with 'dumpSecretsPlainLegacy' to show secrets from legacy encryption");
    System.out.println("Call with 'dumpSecretsPlainCbc' to show secrets from cbc encryption");
    System.out.println("Call with 'encryptCbc secret text' encrypt something with cbc");
    System.out.println("Call with 'decryptCbc secret encryptedText' decrypt something with cbc");
    System.out.println("Call with 'encryptKeyCbc text' decrypt something with cbc encrypt key");
    System.out.println("Call with 'decryptKeyCbc encryptedText' decrypt something with cbc encrypt key");
    System.exit(1);
  }
  
}
