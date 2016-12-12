/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.daemon;

import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 *
 */
public class DaemonControllerTest {

  /**
   * 
   */
  public DaemonControllerTest() {
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    DaemonController.scheduleReports();
    TwoFactorServerUtils.sleep(800000L);
  }
  
}
