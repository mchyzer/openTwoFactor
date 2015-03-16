import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.encryption.EncryptionKey;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class Encrypt {

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length != 2) {
      throw new RuntimeException("Pass in loginid and decrypted pass");
    }
    String loginid = args[0];
    String decryptedPass = args[1];
    
    String encryptedPass = EncryptionKey.encrypt(decryptedPass);
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), loginid);
    
    System.out.println("update two_factor_user_attr set attribute_value_string = '" + encryptedPass + "'"
        + " where user_uuid = '" + twoFactorUser.getUuid() + "' and attribute_name = 'two_factor_secret';");

  }

}
