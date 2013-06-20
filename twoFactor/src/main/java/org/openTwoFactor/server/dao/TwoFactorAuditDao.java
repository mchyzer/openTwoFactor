/**
 * @author mchyzer
 * $Id: TwoFactorAuditDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;
import java.util.Set;

import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditView;
import org.openTwoFactor.server.hibernate.TfQueryOptions;



/**
 * data access object interface for user table
 */
public interface TwoFactorAuditDao {

  /**
   * retrieve audits by age but not deleted.  Should be in a reasonable batch (e.g. 1000?)
   * @param actions are the actions to retrieve
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the audits
   */
  public List<TwoFactorAudit> retrieveOlderThanAgeAndActions(Set<String> actions, long selectBeforeThisMilli);

  /**
   * retrieve audits that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the daemon log
   */
  public List<TwoFactorAudit> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);

  /**
   * retrieve attributes for a user, cannot be delete dated
   * @param userUuid
   * @param tfQueryOptions
   * @return the user
   */
  public List<TwoFactorAuditView> retrieveByUser(String userUuid, TfQueryOptions tfQueryOptions);

  /**
   * retrieve audit by uuid
   * @param uuid
   * @return the audit
   */
  public TwoFactorAudit retrieveByUuid(String uuid);

  /**
   * insert or update to the DB
   * @param twoFactorAudit
   */
  public void store(TwoFactorAudit twoFactorAudit);

  /**
   * delete audit from table, note, make sure to set the delete date first
   * @param twoFactorAudit
   */
  public void delete(TwoFactorAudit twoFactorAudit);
}
