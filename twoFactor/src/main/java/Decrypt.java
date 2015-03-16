import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class Decrypt {

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (args.length != 1) {
      throw new RuntimeException("Pass in loginid and current encrypted pass");
    }
    String loginid = args[0];
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), loginid);
    
    System.out.println("/opt/appserv/tomcat/apps/twoFactor/java/bin/java -cp \"classes:lib/*:/opt/appserv/common/tomcat6_32_ds_base/lib/servlet-api.jar\" Encrypt " + loginid + " " + twoFactorUser.getTwoFactorSecretUnencrypted());
    
//    System.out.println("update two_factor_user_attr set attribute_value_string = '" + twoFactorUser.getTwoFactorSecret() + "'"
//        + " where user_uuid = '" + twoFactorUser.getUuid() + "' and attribute_name = 'two_factor_secret';");
    
  }

}
