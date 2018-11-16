/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;
import java.util.Set;

import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorDeviceSerialDao implements TwoFactorDeviceSerialDao {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
  }


  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorDeviceSerial retrieveByUuid(String uuid) {
    if (TwoFactorServerUtils.isBlank(uuid)) {
      throw new RuntimeException("Why is uuid blank? ");
    }
    
    TwoFactorDeviceSerial twoFactorDeviceSerial = HibernateSession.byHqlStatic().createQuery(
        "select tfds from TwoFactorDeviceSerial as tfds where tfds.uuid = :theUuid")
        .setString("theUuid", uuid).uniqueResult(TwoFactorDeviceSerial.class);
    return twoFactorDeviceSerial;
  }

  /**
   * @see TwoFactorDeviceSerialDao#retrieveBySecretHash(String)
   */
  @Override
  public TwoFactorDeviceSerial retrieveBySecretHash(String secretHash) {
    if (TwoFactorServerUtils.isBlank(secretHash)) {
      throw new RuntimeException("Why is secret blank? ");
    }
    
    TwoFactorDeviceSerial twoFactorDeviceSerial = HibernateSession.byHqlStatic().createQuery(
        "select tfds from TwoFactorDeviceSerial as tfds where tfds.twoFactorSecretHash = :theTwoFactorSecretHash")
        .setString("theTwoFactorSecretHash", secretHash).uniqueResult(TwoFactorDeviceSerial.class);
    return twoFactorDeviceSerial;
  }

  /**
   * @see TwoFactorDeviceSerialDao#retrieveBySerial(String)
   */
  @Override
  public TwoFactorDeviceSerial retrieveBySerial(String serial) {
    if (TwoFactorServerUtils.isBlank(serial)) {
      throw new RuntimeException("Why is serial blank? ");
    }
    
    TwoFactorDeviceSerial twoFactorDeviceSerial = HibernateSession.byHqlStatic().createQuery(
        "select tfds from TwoFactorDeviceSerial as tfds where tfds.serialNumber = :theSerialNumber")
        .setString("theSerialNumber", serial).uniqueResult(TwoFactorDeviceSerial.class);
    return twoFactorDeviceSerial;
  }

  /**
   * @see TwoFactorDeviceSerialDao#store(TwoFactorDeviceSerial)
   */
  @Override
  public void store(TwoFactorDeviceSerial twoFactorDeviceSerial) {
    if (twoFactorDeviceSerial == null) {
      throw new NullPointerException("twoFactorDeviceSerial is null");
    }
    HibernateSession.byObjectStatic().saveOrUpdate(twoFactorDeviceSerial);
    
  }

  /**
   * @see TwoFactorDeviceSerialDao#delete(TwoFactorDeviceSerial)
   */
  @Override
  public void delete(TwoFactorDeviceSerial twoFactorDeviceSerial) {
    if (twoFactorDeviceSerial == null) {
      throw new NullPointerException("twoFactorDeviceSerial is null");
    }
    HibernateSession.byObjectStatic().delete(twoFactorDeviceSerial);
    
  }


  /**
   * @see TwoFactorDeviceSerialDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorDeviceSerial> retrieveDeletedOlderThanAge(
      long selectBeforeThisMilli) {
    List<TwoFactorDeviceSerial> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfds from TwoFactorDeviceSerial as tfds where tfds.deletedOn is not null and tfds.deletedOn < :selectBeforeThisMilli ")
        .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
        .options(new TfQueryOptions().paging(1000, 1,false))
        .list(TwoFactorDeviceSerial.class);
    return theList;
  }


  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao#retrieveAll()
   */
  public List<TwoFactorDeviceSerial> retrieveAll() {
    List<TwoFactorDeviceSerial> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfds from TwoFactorDeviceSerial as tfds order by tfds.uuid")
        .list(TwoFactorDeviceSerial.class);
    return theList;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorDeviceSerialDao#retrieveByUserUuid(java.lang.String)
   */
  public Set<TwoFactorDeviceSerial> retrieveByUserUuid(String userUuid) {
    Set<TwoFactorDeviceSerial> theSet = HibernateSession.byHqlStatic().createQuery(
        "select tfds from TwoFactorDeviceSerial as tfds where tfds.userUuid = :userUuid ")
        .setString("userUuid", userUuid)
        .listSet(TwoFactorDeviceSerial.class);
    return theSet;
  }

}
