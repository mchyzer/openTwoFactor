/**
 * @author mchyzer
 * $Id: TfRestLogicTrafficLog.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.duo;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * logger to log the traffic of duo
 */
public class DuoLog {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(DuoLog.class);
 
  /**
   * username thread local
   */
  private static ThreadLocal<String> usernameThreadLocal = new InheritableThreadLocal<String>();
  
  /**
   * assign username or null
   * @param username
   */
  public static void assignUsername(String username) {
    if (StringUtils.isBlank(username)) {
      usernameThreadLocal.remove();
    } else {
      usernameThreadLocal.set(username);
    }
  }
  
  /**
   * log something to the log file
   * @param message
   */
  public static void duoLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   * @param startTimeNanos nanos when the request started
   */
  public static void duoLog(Map<String, Object> messageMap, Long startTimeNanos) {
    if (LOG.isDebugEnabled()) {
      if (messageMap != null && startTimeNanos != null) {
        messageMap.put("elapsedMillis", (System.nanoTime() - startTimeNanos) / 1000000);
      }
      String username = usernameThreadLocal.get();
      if (messageMap != null && !StringUtils.isBlank(username)) {
        messageMap.put("loggedUsername", username);
      }
      LOG.debug(TwoFactorServerUtils.mapToString(messageMap));
    }
  }

  
}
