import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class EncryptToken {

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException("Pass in uuid and decrypted pass");
    }
    String uuid = args[0];
    String decryptedPass = args[1];

    TwoFactorDeviceSerial twoFactorDeviceSerial = TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveByUuid(uuid);

    twoFactorDeviceSerial.setTwoFactorSecretUnencrypted(decryptedPass);

    System.out.println("update two_factor_device_serial set two_factor_secret = '" + twoFactorDeviceSerial.getTwoFactorSecret() 
        + "', two_factor_secret_hash = '" + twoFactorDeviceSerial.getTwoFactorSecretHash() + "'" + " where uuid = '" + uuid + "';");

  }

}
