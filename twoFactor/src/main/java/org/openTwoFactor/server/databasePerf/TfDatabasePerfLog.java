package org.openTwoFactor.server.databasePerf;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


public class TfDatabasePerfLog {

  /** logger */
  public static final Log LOG = TwoFactorServerUtils.getLog(TfDatabasePerfLog.class);
 
  
  /**
   * log something to the log file
   * @param message
   */
  public static void dbPerfLog(String message) {
    LOG.debug(message);
  }
  
  /**
   * log something to the log file
   * @param messageMap
   */
  public static void dbPerfLog(Map<String, Object> messageMap) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(TwoFactorServerUtils.mapToString(messageMap));
    }
  }

}
