import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class DecryptTokenSecret {

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      throw new RuntimeException("Pass in token uuid");
    }
    String uuid = args[0];
    
    TwoFactorDeviceSerial twoFactorDeviceSerial = TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveByUuid(uuid);
    
    System.out.println("/opt/appserv/tomcat/apps/twoFactor/java/bin/java -cp \"classes:lib/*:/opt/appserv/common/tomcat6_32_ds_base/lib/servlet-api.jar\" EncryptToken " + uuid + " " + twoFactorDeviceSerial.getTwoFactorSecretUnencrypted());
    
//    System.out.println("update two_factor_user_attr set attribute_value_string = '" + twoFactorUser.getTwoFactorSecret() + "'"
//        + " where user_uuid = '" + twoFactorUser.getUuid() + "' and attribute_name = 'two_factor_secret';");
    
  }

}
