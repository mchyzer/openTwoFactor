import org.openTwoFactor.server.TwoFactorLogic;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class Sample {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    String loginid = "10062064";
    
    //TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), loginid);
    
    TwoFactorLogic.printPasswordsForUser(loginid);

  }

}
