/**
 * @author mchyzer
 * $Id: TfMemoryDaemonLogDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorDaemonLog;
import org.openTwoFactor.server.dao.TwoFactorDaemonLogDao;



/**
 *
 */
public class TfMemoryDaemonLogDao implements TwoFactorDaemonLogDao {

  /**
   * daemon logs
   */
  public static List<TwoFactorDaemonLog> daemonLogs = Collections.synchronizedList(new ArrayList<TwoFactorDaemonLog>());
  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#delete(TwoFactorDaemonLog)
   */
  @Override
  public void delete(TwoFactorDaemonLog twoFactorDaemonLog) {
    
    Iterator<TwoFactorDaemonLog> iterator = daemonLogs.iterator();
    while (iterator.hasNext()) {
      TwoFactorDaemonLog current = iterator.next();
      if (current == twoFactorDaemonLog || StringUtils.equals(twoFactorDaemonLog.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
    
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#store(org.openTwoFactor.server.beans.TwoFactorDaemonLog)
   */
  @Override
  public void store(TwoFactorDaemonLog twoFactorDaemonLog) {
    if (StringUtils.isBlank(twoFactorDaemonLog.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorDaemonLog);

    daemonLogs.add(twoFactorDaemonLog);
  }

  /**
   * @see TwoFactorDaemonLogDao#retrieveByUuid(String)
   */
  @Override
  public TwoFactorDaemonLog retrieveByUuid(String uuid) {
    for (TwoFactorDaemonLog current : daemonLogs) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorDaemonLog> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorDaemonLog> result = new ArrayList<TwoFactorDaemonLog>();
    for (TwoFactorDaemonLog current : daemonLogs) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        result.add(current);
      }
      if (result.size() == 1000) {
        break;
      }
    }
    return result;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDaemonLogDao#retrieveOlderThanAge(long)
   */
  @Override
  public List<TwoFactorDaemonLog> retrieveOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorDaemonLog> result = new ArrayList<TwoFactorDaemonLog>();
    for (TwoFactorDaemonLog current : daemonLogs) {
      if (!current.isDeleted() && current.getTheTimestamp() < selectBeforeThisMilli) {
        result.add(current);
      }
      if (result.size() == 1000) {
        break;
      }
    }
    return result;
  }

}
