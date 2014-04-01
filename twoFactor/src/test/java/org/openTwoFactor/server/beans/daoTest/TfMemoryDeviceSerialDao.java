/**
 * @author mchyzer
 * $Id: TfMemoryUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * 
 */
public class TfMemoryDeviceSerialDao implements TwoFactorDeviceSerialDao {

  /**
   * users
   */
  public static List<TwoFactorDeviceSerial> deviceSerials = Collections.synchronizedList(new ArrayList<TwoFactorDeviceSerial>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao#delete(TwoFactorDeviceSerial)
   */
  @Override
  public void delete(TwoFactorDeviceSerial twoFactorDeviceSerial) {
    Iterator<TwoFactorDeviceSerial> iterator = deviceSerials.iterator();
    while (iterator.hasNext()) {
      TwoFactorDeviceSerial current = iterator.next();
      if (current == twoFactorDeviceSerial || StringUtils.equals(twoFactorDeviceSerial.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao#store(TwoFactorDeviceSerial)
   */
  @Override
  public void store(TwoFactorDeviceSerial twoFactorDeviceSerial) {
    if (StringUtils.isBlank(twoFactorDeviceSerial.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorDeviceSerial);
    deviceSerials.add(twoFactorDeviceSerial);
  }

  /**
   * @see TwoFactorDeviceSerialDao#retrieveBySecretHash(String)
   */
  @Override
  public TwoFactorDeviceSerial retrieveBySecretHash(String secretHash) {
    for (TwoFactorDeviceSerial twoFactorDeviceSerial : TwoFactorServerUtils.nonNull(deviceSerials)) {
      if (StringUtils.equals(secretHash, twoFactorDeviceSerial.getTwoFactorSecretHash())) {
        return twoFactorDeviceSerial;
      }
    }
    return null;
  }

  /**
   * @see TwoFactorDeviceSerialDao#retrieveBySerial(String)
   */
  @Override
  public TwoFactorDeviceSerial retrieveBySerial(String serial) {
    for (TwoFactorDeviceSerial twoFactorDeviceSerial : TwoFactorServerUtils.nonNull(deviceSerials)) {
      if (StringUtils.equals(serial, twoFactorDeviceSerial.getSerialNumber())) {
        return twoFactorDeviceSerial;
      }
    }
    return null;
  }

  @Override
  public List<TwoFactorDeviceSerial> retrieveDeletedOlderThanAge(
      long selectBeforeThisMilli) {

    List<TwoFactorDeviceSerial> result = new ArrayList<TwoFactorDeviceSerial>();
    
    for (TwoFactorDeviceSerial current : TwoFactorServerUtils.nonNull(deviceSerials)) {
      
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        
        result.add(current);
        
        if (result.size() == 1000) {
          break;
        }
      }
    }
    
    return result;
  }

  @Override
  public TwoFactorDeviceSerial retrieveByUuid(String uuid) {
    for (TwoFactorDeviceSerial twoFactorDeviceSerial : TwoFactorServerUtils.nonNull(deviceSerials)) {
      if (StringUtils.equals(uuid, twoFactorDeviceSerial.getUuid())) {
        return twoFactorDeviceSerial;
      }
    }
    return null;
  }

}
