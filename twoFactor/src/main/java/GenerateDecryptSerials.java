import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class GenerateDecryptSerials {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    for (TwoFactorDeviceSerial twoFactorDeviceSerial : TwoFactorServerUtils.nonNull(
        TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveAll()) ) {
      
      try {
        
        twoFactorDeviceSerial.getTwoFactorSecretUnencrypted();
        
      } catch (Exception e) {

        System.out.println("java -cp \"classes:lib/*:/opt/appserv/common/tomcat6_32_ds_base/lib/servlet-api.jar\" DecryptTokenSecret " + twoFactorDeviceSerial.getUuid());
        //e.printStackTrace();

      }
      
    }
    
  }

}
