/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.encryption;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 *
 */
public class TfSymmetricUtils {

  /**
   * Encrypt a sample message using AES in CBC mode with an IV.
   * 
   * @param args not used
   * @throws Exception if the algorithm, key, iv or any other parameter is
   *             invalid.
   */
  public static void main(String[] args) throws Exception {

    if (args.length != 1) {
      usage();
    }

    if (args.length == 1 && StringUtils.equals(args[0], "dumpSecretsAsIs")) {
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
    if (args.length == 1 && StringUtils.equals(args[0], "dumpSecretsCombo")) {
      
     for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("two_factor_secret")) {
        
        String attributeValue = twoFactorUserAttr.getAttributeValueString();
        
        if (!StringUtils.isBlank(attributeValue)) {
          
          //get the raw secret
          String secret = EncryptionKey.decrypt(attributeValue, new TfSymmetricEncryptAesCbcCombo());
          
          attributeValue = EncryptionKey.encrypt(secret, new TfSymmetricEncryptAesCbcPkcs5Padding());
          
          System.out.println("update two_factor_user_attr set attribute_value_string = '" 
              + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");

          TwoFactorServerUtils.sleep(20);

        }
      }
      for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("phone_code_encrypted")) {

        String attributeValue = twoFactorUserAttr.getAttributeValueString();

        if (!StringUtils.isBlank(attributeValue)) {

          //get the raw secret
          String secret = EncryptionKey.decrypt(attributeValue, new TfSymmetricEncryptAesCbcCombo());

          attributeValue = EncryptionKey.encrypt(secret, new TfSymmetricEncryptAesCbcPkcs5Padding());

          System.out.println("update two_factor_user_attr set attribute_value_string = '" 
             + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");

          TwoFactorServerUtils.sleep(20);

        }
      }
      for (TwoFactorUserAttr twoFactorUserAttr : TwoFactorDaoFactory.getFactory().getTwoFactorUserAttr().retrieveByAttributeName("two_factor_secret_temp")) {

        String attributeValue = twoFactorUserAttr.getAttributeValueString();
        
        if (!StringUtils.isBlank(attributeValue)) {

          //get the raw secret
          String secret = EncryptionKey.decrypt(attributeValue, new TfSymmetricEncryptAesCbcCombo());
          
          attributeValue = EncryptionKey.encrypt(secret, new TfSymmetricEncryptAesCbcPkcs5Padding());
          
          System.out.println("update two_factor_user_attr set attribute_value_string = '" 
              + attributeValue + "' where uuid = '" + twoFactorUserAttr.getUuid() + "';");

          TwoFactorServerUtils.sleep(20);

        }
      }
      for (TwoFactorDeviceSerial twoFactorDeviceSerial : TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveAll()) {

        String twoFactorSecret = twoFactorDeviceSerial.getTwoFactorSecret();

        if (!StringUtils.isBlank(twoFactorSecret)) {

          //get the raw secret
          String secret = EncryptionKey.decrypt(twoFactorSecret, new TfSymmetricEncryptAesCbcCombo());

          twoFactorSecret = EncryptionKey.encrypt(secret, new TfSymmetricEncryptAesCbcPkcs5Padding());

          System.out.println("update two_factor_device_serial set two_factor_secret = '" 
              + twoFactorSecret + "' where uuid = '" + twoFactorDeviceSerial.getUuid() + "';");

          TwoFactorServerUtils.sleep(20);

        }
      }
      System.out.println("commit;");
      return;  

    }

    usage();

  }

  /**
   * 
   */
  private static void usage() {
    
    System.out.println("Call with 'dumpSecretsCombo' to generate a backup script for secrets as CBC, ");
    System.out.println("   or if the secrets are in legacy format then convert to CBC");
    System.out.println("Call with 'dumpSecretsAsIs' to generate a backup script for secrets");
    System.exit(1);
  }


  
}
