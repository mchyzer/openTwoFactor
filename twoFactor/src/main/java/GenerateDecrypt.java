import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserView;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class GenerateDecrypt {

  /**
   * @param args
   */
  public static void main(String[] args) {
    for (TwoFactorUserView twoFactorUserView : TwoFactorServerUtils.nonNull(
        TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveAllOptedInUsers())) {
      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), twoFactorUserView.getLoginid());
      try {
        twoFactorUser.getTwoFactorSecretUnencrypted();
      } catch (Exception e) {
        System.out.println("java -cp \"classes:lib/*:/opt/appserv/common/tomcat6_32_ds_base/lib/servlet-api.jar\" Decrypt " + twoFactorUser.getLoginid());
        System.out.println("\n");
        e.printStackTrace();
      }
    }

  }

}
