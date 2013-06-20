/**
 * @author mchyzer
 * $Id: TfDaemonLog.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * logger to log the traffic of 
 */
public class TfDaemonLog {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfDaemonLog.class);
 
  
  /**
   * log something to the log file
   * @param message
   */
  public static void daemonLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   */
  public static void daemonLog(Map<String, Object> messageMap) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(TwoFactorServerUtils.mapToString(messageMap));
    }
  }

  
}
