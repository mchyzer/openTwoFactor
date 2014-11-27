/**
 * @author mchyzer
 * $Id: TwoFactorContactMultiple.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server;

import java.util.List;

import net.sf.json.JSONObject;

import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.util.TwoFactorPassResult;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.failover.FailoverClient;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;
import edu.internet2.middleware.grouperClient.failover.FailoverLogic;
import edu.internet2.middleware.grouperClient.failover.FailoverLogicBean;


/**
 * use the primary cloud service and if error or timeout, use the secondary
 */
public class TwoFactorCheckPassMultiple implements TwoFactorCheckPass {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    JSONObject jsonObject = DuoCommands.retrieveDuoUserBySomeId("mchyzer");
    
    String userId = jsonObject.getString("user_id");
    
    TwoFactorPassResult twoFactorPassResult = new TwoFactorCheckPassMultiple().twoFactorCheckPassword(null, "230300", null, null, null, null, null, userId);
    
    System.out.println(twoFactorPassResult.isPasswordCorrect());
    
  }
  
  /**
   * init the two failover methods
   * @return config
   */
  private synchronized FailoverConfig init() {

    String classNames = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.checkPass.multiple.implementations");

    List<String> classNamesArray = TwoFactorServerUtils.splitTrimToList(classNames, ",");

    FailoverConfig failoverConfig = new FailoverConfig();
    failoverConfig.setConnectionType(TwoFactorCheckPassMultiple.class.getSimpleName());
    failoverConfig.setTimeoutSeconds(TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.checkPass.multiple.timeoutSeconds", 10));
    failoverConfig.setConnectionNames(classNamesArray);
    failoverConfig.setFailoverStrategy(FailoverStrategy.activeStandby);
    failoverConfig.setAffinitySeconds(-1);
    failoverConfig.setMinutesToKeepErrors(-1);
    FailoverClient.initFailoverClient(failoverConfig);
    return failoverConfig;
  }

  /**
   * 
   * @param className
   * @return the implementation of the interface
   */
  @SuppressWarnings("unchecked")
  private TwoFactorCheckPass twoFactorCheckPassInterface(String className) {
    Class<TwoFactorCheckPass> theClass = TwoFactorServerUtils.forName(className);
    TwoFactorCheckPass twoFactorCheckPass = TwoFactorServerUtils.newInstance(theClass);
    return twoFactorCheckPass;
  }

  /**
   * @see org.openTwoFactor.server.TwoFactorCheckPass#twoFactorCheckPassword(java.lang.String, java.lang.String, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.Long, java.lang.String, java.lang.String)
   */
  @Override
  public TwoFactorPassResult twoFactorCheckPassword(final String secretString, final String password,
      final Long sequentialPassIndexAvailable, final Long lastTotp30TimestampUsed,
      final Long lastTotp60TimestampUsed, final Long tokenIndexAvailable, final String phonePass, final String duoUserId) {

    init();
    
    TwoFactorPassResult twoFactorPassResult = null;

    //try totp since clock might be wrong at duo...
    if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean(
        "twoFactorServer.checkPass.multiple.tryDbFirst", true)) {
      twoFactorPassResult = new TwoFactorCheckPassDb().twoFactorCheckPassword(secretString, password, sequentialPassIndexAvailable, lastTotp30TimestampUsed, lastTotp60TimestampUsed, tokenIndexAvailable, phonePass, duoUserId);
      resetHotpOnLocal(twoFactorPassResult);
      if (twoFactorPassResult.isPasswordCorrect()) {
        return twoFactorPassResult;
      }
    }
    
    twoFactorPassResult = FailoverClient.failoverLogic(TwoFactorCheckPassMultiple.class.getSimpleName(), new FailoverLogic<TwoFactorPassResult>() {

      /**
       * 
       * @see edu.internet2.middleware.grouperClient.failover.FailoverLogic#logic(edu.internet2.middleware.grouperClient.failover.FailoverLogicBean)
       */
      @Override
      public TwoFactorPassResult logic(FailoverLogicBean failoverLogicBean) {
        TwoFactorCheckPass twoFactorCheckPass = 
          TwoFactorCheckPassMultiple.this.twoFactorCheckPassInterface(failoverLogicBean.getConnectionName());
        return twoFactorCheckPass.twoFactorCheckPassword(secretString, password, sequentialPassIndexAvailable,
            lastTotp30TimestampUsed, lastTotp60TimestampUsed, tokenIndexAvailable, phonePass, duoUserId);
      }
      
    });
    
    //if DB is used with Duo, then dont use HOTP since Duo needs to increment index
    //note: could be bad since timeout could still update index
    if (TwoFactorCheckPassDb.class.equals(twoFactorPassResult.getTwoFactorCheckPassImplementation())) {
      resetHotpOnLocal(twoFactorPassResult);
    }
    
    return twoFactorPassResult;
    
  }

  /**
   * @param twoFactorPassResult
   */
  private void resetHotpOnLocal(TwoFactorPassResult twoFactorPassResult) {
    boolean useHotpOnDbIfMultiple = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean(
        "twoFactorServer.checkPass.multiple.useHotpOnDbIfMultiple", false);
    if (!useHotpOnDbIfMultiple) {
      if (twoFactorPassResult.getNextHotpIndex() != null || twoFactorPassResult.getNextTokenIndex() != null) {
        twoFactorPassResult.setNextHotpIndex(null);
        twoFactorPassResult.setNextTokenIndex(null);
        twoFactorPassResult.setPasswordCorrect(false);
      }
    }
  }

}
