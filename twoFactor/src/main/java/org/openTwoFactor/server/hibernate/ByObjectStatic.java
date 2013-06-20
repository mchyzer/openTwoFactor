package org.openTwoFactor.server.hibernate;

import java.io.Serializable;
import java.util.Collection;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.exceptions.TfStaleObjectStateException;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * 
 * for simple object queries, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * TwoFactorTransactionType.READONLY_OR_USE_EXISTING, and 
 * TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class ByObjectStatic extends ByQueryBase {
  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(ByObjectStatic.class);

  /** assign a transaction type, default use the transaction modes
   * TwoFactorTransactionType.READONLY_OR_USE_EXISTING, and 
   * TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING depending on
   * if a transaction is needed */
  private TwoFactorTransactionType transactionType = null;
  
  /**
   * assign if this query is cacheable or not.
   */
  private Boolean cacheable = null;
  
  /**
   * assign if this query is cacheable or not.
   */
  private String entityName = null;
  
  /**
   * assign a different transactionType (e.g. for autonomous transactions)
   * @param theTransactionType
   * @return the same object for chaining
   */
  public ByObjectStatic setTransactionType(TwoFactorTransactionType 
      theTransactionType) {
    this.transactionType = theTransactionType;
    return this;
  }
  
  
  /**
   * assign if this query is cacheable or not.
   * @param cacheable1 the cacheable to set
   * @return this object for chaining
   */
  public ByObjectStatic setCacheable(Boolean cacheable1) {
    this.cacheable = cacheable1;
    return this;
  }

  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByObjectStatic, query: ', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", entityName: ").append(this.entityName);
    result.append(", tx type: ").append(this.transactionType);
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * cache region for cache
   * @param cacheRegion1 the cacheRegion to set
   * @return this object for chaining
   */
  public ByObjectStatic setCacheRegion(String cacheRegion1) {
    this.cacheRegion = cacheRegion1;
    return this;
  }

  /**
   * entity name if the object is mapped to more than one table
   * @param theEntityName the entity name of the object
   * @return this object for chaining
   */
  public ByObjectStatic setEntityName(String theEntityName) {
    this.entityName = theEntityName;
    return this;
  }

  /**
   * <pre>
   * call hibernate method "update" on a list of objects
   * 
   * HibernateSession.byObjectStatic().update(collection);
   * 
   * </pre>
   * @param collection is collection of objects to update in one transaction.  If null or empty just ignore
   * @throws TfDaoException
   */
  public void update(final Collection<?> collection) throws TfDaoException {
    if (collection == null) {
      return;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.update(collection);
              return null;
            }
        
      });
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + TwoFactorServerUtils.classNameCollection(collection) + ", " + this;

      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * call hibernate "update" method on an object
   * @param object to update
   * @throws TfDaoException
   */
  public void update(final Object object) throws TfDaoException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      update((Collection)object);
      return;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, 
                  "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, 
                  "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.update(object);
              
              return null;
            }
        
      });
      
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + TwoFactorServerUtils.className(object) + ", " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  
  /**
   * call hibernate "load" method on an object
   * @param <T> 
   * @param theClass to load
   * @param id 
   * @return the object
   * @throws TfDaoException
   */
  @SuppressWarnings("unchecked")
  public <T> T load(final Class<T> theClass, final Serializable id) throws TfDaoException {
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      T result = (T)HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              T theResult = byObject.load(theClass, id);
              
              return theResult;
            }
        
      });
      return result;
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in update: " + theClass + ", " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "saveOrUpdate" on a list of objects
   * 
   * HibernateSession.byObjectStatic().saveOrUpdate(collection);
   * 
   * </pre>
   * @param collection is collection of objects to saveOrUpdate in one transaction.  If null or empty just ignore
   * @throws TfDaoException
   */
  public void saveOrUpdate(final Collection<?> collection) throws TfDaoException {
    if (collection == null) {
      return;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.saveOrUpdate(collection);
              return null;
            }
        
      });
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in saveOrUpdate: " + TwoFactorServerUtils.classNameCollection(collection) + ", " + this;

      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }
      
      throw e;
    }
    
  }

  /**
   * call hibernate "saveOrUpdate" method on an object
   * @param object to update
   * @throws TfDaoException
   */
  public void saveOrUpdate(final Object object) throws TfDaoException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      saveOrUpdate((Collection)object);
      return;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.saveOrUpdate(object);
              
              return null;
            }
        
      });
      
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in saveOrUpdate: " + TwoFactorServerUtils.className(object) + ", " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "save" on a list of objects
   * 
   * HibernateSession.byObjectStatic().save(collection);
   * 
   * </pre>
   * @param collection is collection of objects to save in one transaction.  If null or empty just ignore
   * @throws TfDaoException
   */
  public void save(final Collection<?> collection) throws TfDaoException {
    if (collection == null) {
      return;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.save(collection);
              return null;
            }
        
      });
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + TwoFactorServerUtils.classNameCollection(collection) + ", " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  

  /**
   * <pre>
   * call hibernate method "save" on an object
   * 
   * HibernateSession.byObjectStatic().save(dao);
   * 
   * </pre>
   * @param object to save
   * @return the id
   * @throws TfDaoException
   */
  public Serializable save(final Object object) throws TfDaoException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      save((Collection)object);
      return null;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      Serializable result = (Serializable)HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              return byObject.save(object);
              
            }
        
      });
      return result;
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + TwoFactorServerUtils.className(object) + ", " + this;

      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }
      
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "save" on a collection of objects in batch
   * 
   * </pre>
   * @param collection of objects
   * @throws TfDaoException
   */
  public void saveBatch(final Collection<?> collection) throws TfDaoException {
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.saveBatch(collection);
              return null;
            }
        
      });

    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in save: " + TwoFactorServerUtils.classNameCollection(collection) + ", " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hibernate method "delete" on a list of objects
   * 
   * HibernateSession.byObjectStatic().delete(collection);
   * 
   * </pre>
   * @param collection is collection of objects to delete in one transaction.  If null or empty just ignore
   * @throws TfDaoException
   */
  public void delete(final Collection<?> collection) throws TfDaoException {
    if (collection == null) {
      return;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.delete(collection);
              return null;
            }
        
      });
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in delete: " + TwoFactorServerUtils.classNameCollection(collection) + ", " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * copy field to, better by a ByObject...
   */
  @Override
  protected void copyFieldsTo(ByQueryBase byQueryBase) {
    super.copyFieldsTo(byQueryBase);
    ((ByObject)byQueryBase).setEntityName(this.entityName);
  }


  /**
   * <pre>
   * call hibernate method "delete" on a list of objects
   * 
   * HibernateSession.byObjectStatic().delete(Rosetta.getDAO(_f));
   * 
   * </pre>
   * @param object is an object (if collection will still work), if null, will probably throw exception
   * @throws TfDaoException
   */
  public void delete(final Object object) throws TfDaoException {
    //dont fail if collection in there
    if (object instanceof Collection) {
      delete((Collection)object);
      return;
    }
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING);
      
      HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheable == null, "Cant set cacheable here");
              TwoFactorServerUtils.assertion(ByObjectStatic.this.cacheRegion == null, "Cant set cacheRegion here");
              
              ByObject byObject = hibernateSession.byObject();
              ByObjectStatic.this.copyFieldsTo(byObject);
              byObject.delete(object);
              return null;
            }
        
      });
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in delete: " + TwoFactorServerUtils.classNameCollection(object) + ", " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }
  
  
  /**
   * constructor
   *
   */
  ByObjectStatic() {}
  
  /**
   * @see ByQueryBase#setIgnoreHooks(boolean)
   */
  @Override
  public ByObjectStatic setIgnoreHooks(boolean theIgnoreHooks) {
    return (ByObjectStatic)super.setIgnoreHooks(theIgnoreHooks);
  }

  
}
