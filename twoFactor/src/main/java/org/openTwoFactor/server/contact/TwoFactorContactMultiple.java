/**
 * @author mchyzer
 * $Id: TwoFactorContactMultiple.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.contact;

import java.util.List;

import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.failover.FailoverClient;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig;
import edu.internet2.middleware.grouperClient.failover.FailoverLogic;
import edu.internet2.middleware.grouperClient.failover.FailoverLogicBean;
import edu.internet2.middleware.grouperClient.failover.FailoverConfig.FailoverStrategy;


/**
 * use the primary cloud service and if error or timeout, use the secondary
 */
public class TwoFactorContactMultiple implements TwoFactorContactInterface {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    String userText = "Your verification code is: 1, 2, 3, 4, 5, 6.  " +
      "Again, your verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      "verification code is: 1, 2, 3, 4, 5, 6.  Again, your " +
      "verification code is: 1, 2, 3, 4, 5, 6.  ";
    
    new TwoFactorContactMultiple().voice("215 880 9847", userText);
    
    //new TwoFactorContactMultiple().text("215 880 9847", "123456");
  }
  
  /**
   * init the two failover methods
   */
  private synchronized void init() {

    String classNames = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.contact.multiple.contactInterfaceImplementations");

    List<String> classNamesArray = TwoFactorServerUtils.splitTrimToList(classNames, ",");

    FailoverConfig failoverConfig = new FailoverConfig();
    failoverConfig.setConnectionType(TwoFactorContactMultiple.class.getSimpleName());
    failoverConfig.setTimeoutSeconds(TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.contact.multiple.timeoutSeconds", 10));
    failoverConfig.setConnectionNames(classNamesArray);
    failoverConfig.setFailoverStrategy(FailoverStrategy.activeStandby);
    FailoverClient.initFailoverClient(failoverConfig);

  }

  /**
   * 
   * @param connectionName
   * @return the implementation of the interface
   */
  @SuppressWarnings("unchecked")
  private TwoFactorContactInterface twoFactorContactInterface(String connectionName) {
    Class<TwoFactorContactInterface> theClass = TwoFactorServerUtils.forName(connectionName);
    TwoFactorContactInterface twoFactorContactInterface = TwoFactorServerUtils.newInstance(theClass);
    return twoFactorContactInterface;
  }
  
  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#text(java.lang.String, java.lang.String)
   */
  @Override
  public synchronized void text(final String phoneNumber, final String message) {
    
    this.init();
    
    FailoverClient.failoverLogic(TwoFactorContactMultiple.class.getSimpleName(), new FailoverLogic<Object>() {

      /**
       * 
       * @see edu.internet2.middleware.grouperClient.failover.FailoverLogic#logic(edu.internet2.middleware.grouperClient.failover.FailoverLogicBean)
       */
      @Override
      public Object logic(FailoverLogicBean failoverLogicBean) {
        TwoFactorContactInterface twoFactorContactInterface = 
          TwoFactorContactMultiple.this.twoFactorContactInterface(failoverLogicBean.getConnectionName());
        twoFactorContactInterface.text(phoneNumber, message);
        return null;
      }
      
    });
    
    
  }

  /**
   * @see org.openTwoFactor.server.contact.TwoFactorContactInterface#voice(java.lang.String, java.lang.String)
   */
  @Override
  public synchronized void voice(final String phoneNumber, final String message) {

    this.init();

    FailoverClient.failoverLogic(TwoFactorContactMultiple.class.getSimpleName(), new FailoverLogic<Object>() {

      /**
       * 
       * @see edu.internet2.middleware.grouperClient.failover.FailoverLogic#logic(edu.internet2.middleware.grouperClient.failover.FailoverLogicBean)
       */
      @Override
      public Object logic(FailoverLogicBean failoverLogicBean) {
        TwoFactorContactInterface twoFactorContactInterface = 
          TwoFactorContactMultiple.this.twoFactorContactInterface(failoverLogicBean.getConnectionName());
        twoFactorContactInterface.voice(phoneNumber, message);
        return null;
      }
      
    });

  }

}
