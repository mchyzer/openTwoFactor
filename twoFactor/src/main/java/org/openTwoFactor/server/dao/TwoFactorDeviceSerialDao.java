/**
 * @author mchyzer
 * $Id: TwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.dao;

import java.util.List;

import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;



/**
 * data access object interface for device serial table
 */
public interface TwoFactorDeviceSerialDao {
  
  /**
   * find the device serial by the encrypted secret
   * @param uuid
   * @return device serial row
   */
  public TwoFactorDeviceSerial retrieveBySecretHash(String secretEncrypted);
  
  /**
   * find the device serial by the serial number
   * @param serial
   * @return device serial row
   */
  public TwoFactorDeviceSerial retrieveBySerial(String serial);
  
  /**
   * retrieve serials that are deleted for longer than a certain amount of time
   * @param selectBeforeThisMilli is the millis since 1970 where records should be older
   * @return the users
   */
  public List<TwoFactorDeviceSerial> retrieveDeletedOlderThanAge(long selectBeforeThisMilli);


  /**
   * retrieve user by uuid
   * @param uuid
   * @return the device serial row
   */
  public TwoFactorDeviceSerial retrieveByUuid(String uuid);
  
  /**
   * insert or update to the DB
   * @param twoFactorDeviceSerial
   */
  public void store(TwoFactorDeviceSerial twoFactorDeviceSerial);

  /**
   * delete device serial from table, note, make sure to set the delete date first
   * @param twoFactorDeviceSerial
   */
  public void delete(TwoFactorDeviceSerial twoFactorDeviceSerial);

}
