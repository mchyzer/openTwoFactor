/**
 * @author mchyzer
 * $Id: TwoFactorDaemonLogDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorDaemonLog;


/**
 * data access object interface for daemon log
 */
public interface TwoFactorDaemonLogDao {


  /**
   * retrieve daemon log by uuid
   * @param uuid
   * @return the daemon log
   */
  public TwoFactorDaemonLog retrieveByUuid(String uuid);

  /**
   * retrieve daemon logs by age but not deleted
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the daemon log
   */
  public List<TwoFactorDaemonLog> retrieveOlderThanAge(long selectBeforeThisMilli);

  /**
   * retrieve the timestamp of the most recent success
   * @param daemonName is the daemon name checking
   * @return the timestamp of the daemon
   */
  public Long retrieveMostRecentSuccessTimestamp(String daemonName);

  /**
   * retrieve daemon log that is deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the daemon log
   */
  public List<TwoFactorDaemonLog> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);

  /**
   * insert or update to the DB
   * @param twoFactorDaemonLog
   */
  public void store(TwoFactorDaemonLog twoFactorDaemonLog);

  /**
   * delete daemon log from table, note, make sure to set the delete date first
   * @param twoFactorDaemonLog
   */
  public void delete(TwoFactorDaemonLog twoFactorDaemonLog);
}
