/**
 * @author mchyzer
 * $Id: TwoFactorDaemonName.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;


/**
 * name of daemon
 */
public enum TwoFactorDaemonName {

  /** delete old audits */
  deleteOldAudits,
  
  /** permanently delete old records */
  permanentlyDeleteOldRecords,
  
  /** delete old daemon logs */
  deleteOldDaemonLogs;
  
}
