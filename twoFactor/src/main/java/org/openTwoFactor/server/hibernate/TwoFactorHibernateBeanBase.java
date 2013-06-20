
package org.openTwoFactor.server.hibernate;
import java.io.Serializable;
import java.util.Set;

import org.hibernate.CallbackException;
import org.hibernate.Session;
import org.hibernate.classic.Lifecycle;
import org.openTwoFactor.server.util.TwoFactorCloneable;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/** 
 * Base Hib API class.
 * <p/>
 * @version $Id: TwoFactorHibernateBeanBase.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
@SuppressWarnings("serial")
public abstract class TwoFactorHibernateBeanBase implements TwoFactorCloneable, Serializable, HibTfLifecycle, Lifecycle {

  /**
   * delete this record from the database
   * @param twoFactorDaoFactory
   */
  public abstract void delete(final TwoFactorDaoFactory twoFactorDaoFactory);

  /**
   * if the db needs update
   * @return if the db record needs update
   */
  public abstract boolean dbNeedsUpdate();
  
  /**
   * 
   * @param cloneTo
   */
  protected void cloneHelper(TwoFactorHibernateBeanBase cloneTo) {
    cloneTo.setLastUpdated(this.lastUpdated);
    cloneTo.setUuid(this.uuid);
    cloneTo.setVersionNumber(this.versionNumber);
    cloneTo.setDeletedOn(this.deletedOn);
  }
  
  /**
   * 
   */
  public static final long INITIAL_VERSION_NUMBER = -1L;


  /** column */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";

  
  /** save the state when retrieving from DB */
  private Object dbVersion = null;

  /**
   * assign db version
   * @param theDbVersion the db version to assign
   */
  protected void assignDbVersion(Object theDbVersion) {
    this.dbVersion = theDbVersion;
  }
  
  /** field name for db version */
  public static final String FIELD_DB_VERSION = "dbVersion";
  
  /** constant for field name for: deletedOn */
  public static final String FIELD_DELETED_ON = "deletedOn";

  /** constant for field name for: lastUpdated */
  public static final String FIELD_LAST_UPDATED = "lastUpdated";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /** constant name of field (and javabean property) for versionNumber */
  public static final String FIELD_VERSION_NUMBER = "versionNumber";

  /**
   * call this method to get the field value (e.g. from dbVersionDifferentFields).
   * some objects have different interpretations (e.g. Group will process attribute__whatever)
   * @param fieldName
   * @return the value
   */
  public Object fieldValue(String fieldName) {
    //dont consider field value since we are already in fieldValue
    return TwoFactorServerUtils.fieldValue(null, this, fieldName, true, true, false);
  }
  
  /**
   * db version fields
   * @return the number of fields
   */
  public abstract Set<String> dbNeedsUpdateFields();
  
  /**
   * version of this object in the database
   * @return the db version
   */
  public Object dbVersion() {
    return this.dbVersion;
  }

  /**
   * see if the state of this object has changed compared to the DB state (last known)
   * @return true if changed, false if not
   */
  public boolean dbVersionIsDifferent() {
    Set<String> differentFields = dbVersionDifferentFields();
    return differentFields.size() > 0;
  }

  /**
   * see which fields have changed compared to the DB state (last known)
   * note that attributes will print out: attribute__attributeName
   * @return a set of attributes changed, or empty set if none
   */
  public Set<String> dbVersionDifferentFields() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * take a snapshot of the data since this is what is in the db
   */
  public void dbVersionReset() {
  }

  /**
   * set to null (e.g. on delete)
   */
  public void dbVersionClear() {
    this.dbVersion = null;
  }

  /**
   * @see org.hibernate.classic.Lifecycle#onDelete(org.hibernate.Session)
   */
  public boolean onDelete(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onLoad(org.hibernate.Session, java.io.Serializable)
   */
  public void onLoad(Session s, Serializable id) {
    this.dbVersionReset();
  }


  /**
   * @see HibTfLifecycle#onPostDelete(HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
  }


  /**
   * @see HibTfLifecycle#onPostSave(HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    this.dbVersionReset();
  }


  /**
   * @see HibTfLifecycle#onPostUpdate(HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {
    this.dbVersionReset();
  }


  /**
   * @see HibTfLifecycle#onPreDelete(HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    
  }


  /**
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    this.lastUpdated = System.currentTimeMillis();
    if (TwoFactorServerUtils.equals(INITIAL_VERSION_NUMBER, this.versionNumber)
        && TwoFactorServerUtils.isBlank(this.uuid)) {
      this.uuid = TwoFactorServerUtils.uuid();
    }
  }


  /**
   * @see HibTfLifecycle#onPreUpdate(HibernateSession)
   */
  public void onPreUpdate(HibernateSession hibernateSession) {
    this.lastUpdated = System.currentTimeMillis();
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onSave(org.hibernate.Session)
   */
  public boolean onSave(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  }


  /**
   * @see org.hibernate.classic.Lifecycle#onUpdate(org.hibernate.Session)
   */
  public boolean onUpdate(Session s) throws CallbackException {
    return Lifecycle.NO_VETO;
  } 

  /**
   * deep clone the fields in this object
   */
  @Override
  public abstract TwoFactorHibernateBeanBase clone();

  /**
   * millis since 1970 when this record was last inserted or updated
   */
  private long lastUpdated;
  
  /**
   * if this row needs to be deleted, set a delete date for a week, then actually delete it
   */
  private Long deletedOn;
  
  /**
   * if this row needs to be deleted, set a delete date for a week, then actually delete it
   * @return the deletedOn
   */
  public Long getDeletedOn() {
    return this.deletedOn;
  }
  
  /**
   * see if this record is deleted
   * @return if deleted
   */
  public boolean isDeleted() {
    return this.deletedOn != null && this.deletedOn <= System.currentTimeMillis();
  }
  
  /**
   * if this row needs to be deleted, set a delete date for a week, then actually delete it
   * @param deletedOn1 the deletedOn to set
   */
  public void setDeletedOn(Long deletedOn1) {
    this.deletedOn = deletedOn1;
  }

  /**
   * uuid is the primary key for the object
   */
  private String uuid;
  
  /**
   * millis since 1970 when this record was last inserted or updated
   * @return the lastUpdated
   */
  public long getLastUpdated() {
    return this.lastUpdated;
  }

  
  /**
   * millis since 1970 when this record was last inserted or updated
   * @param lastUpdated1 the lastUpdated to set
   */
  public void setLastUpdated(long lastUpdated1) {
    this.lastUpdated = lastUpdated1;
  }

  
  /**
   * uuid is the primary key for the object
   * @return the uuid
   */
  public String getUuid() {
    return this.uuid;
  }

  
  /**
   * uuid is the primary key for the object
   * @param uuid1 the uuid to set
   */
  public void setUuid(String uuid1) {
    this.uuid = uuid1;
  }

  /**
   * hibernate increments with each insert/update (-1 means insert, 0+ means update, null means 0)
   */
  private Long versionNumber = INITIAL_VERSION_NUMBER;
  
  /**
   * hibernate increments with each insert/update (-1 means insert, 0+ means update, null means 0)
   * @return the hibernateVersion
   */
  public Long getVersionNumber() {
    return this.versionNumber;
  }

  /**
   * hibernate increments with each insert/update (-1 means insert, 0+ means update, null means 0)
   * @param hibernateVersionNumber1 the hibernateVersion to set
   */
  public void setVersionNumber(Long hibernateVersionNumber1) {
    
    //no nulls, set to 0
    hibernateVersionNumber1 = hibernateVersionNumber1 == null ? 0 : hibernateVersionNumber1;
    
    this.versionNumber = hibernateVersionNumber1;
    
  }

} 

