package org.openTwoFactor.server.hibernate;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.exceptions.TfStaleObjectStateException;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * 
 * for simple HQL, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * TwoFactorTransactionType.READONLY_OR_USE_EXISTING, and 
 * TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class ByHqlStatic implements HqlQuery {
  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(ByHqlStatic.class);

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
   * assign a different transactionType (e.g. for autonomous transactions)
   * @param theTransactionType
   * @return the same object for chaining
   */
  public ByHqlStatic setTransactionType(TwoFactorTransactionType 
      theTransactionType) {
    this.transactionType = theTransactionType;
    return this;
  }
  
  
  /**
   * assign if this query is cacheable or not.
   * @param cacheable1 the cacheable to set
   * @return this object for chaining
   */
  public ByHqlStatic setCacheable(Boolean cacheable1) {
    this.cacheable = cacheable1;
    return this;
  }

  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByHqlStatic, query: '");
    result.append(this.query).append("', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", tx type: ").append(this.transactionType);
    if (this.queryOptions != null) {
      result.append(", options: ").append(this.queryOptions.toString());
    }
    result.append(", tx type: ").append(this.transactionType);
    //dont use bindVarParams() method so it doesnt lazy load
    if (this.bindVarNameParams != null) {
      int index = 0;
      int size = this.bindVarNameParams().size();
      for (HibernateParam hibernateParam : this.bindVarNameParams()) {
        result.append("Bind var[").append(index++).append("]: '");
        result.append(hibernateParam);
        if (index!=size-1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * map of params to attach to the query.
   * access this with the bindVarNameParams method
   */
  private List<HibernateParam> bindVarNameParams = null;

  /**
   * query to execute
   */
  private String query = null;

  /**
   * if we are sorting, paging, resultSize, etc 
   */
  private TfQueryOptions queryOptions = null;

  /**
   * set the query to run
   * @param theHqlQuery
   * @return this object for chaining
   */
  public ByHqlStatic createQuery(String theHqlQuery) {
    this.query = theHqlQuery;
    return this;
  }
  
  /**
   * lazy load params
   * @return the params map
   */
  private List<HibernateParam> bindVarNameParams() {
    if (this.bindVarNameParams == null) {
      this.bindVarNameParams = new ArrayList<HibernateParam>();
    }
    return this.bindVarNameParams;
  }
  
  /**
   * cache region for cache
   * @param cacheRegion1 the cacheRegion to set
   * @return this object for chaining
   */
  public ByHqlStatic setCacheRegion(String cacheRegion1) {
    this.cacheRegion = cacheRegion1;
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value
   * @return this object for chaining
   */
  public ByHqlStatic setString(String bindVarName, String value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, String.class));
    return this;
  }
  
  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value
   * @return this object for chaining
   */
  public ByHqlStatic setTimestamp(String bindVarName, Date value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Timestamp.class));
    return this;
  }
  
  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHqlStatic setLong(String bindVarName, Long value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Long.class));
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is double
   * @return this object for chaining
   */
  public ByHqlStatic setDouble(String bindVarName, Double value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Double.class));
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHqlStatic setInteger(String bindVarName, Integer value) {
    this.bindVarNameParams().add(new HibernateParam(bindVarName, value, Integer.class));
    return this;
  }

  /**
   * assign data to the bind var
   * @param bindVarName
   * @param value is long, primitive so not null
   * @return this object for chaining
   */
  public ByHqlStatic setScalar(String bindVarName, Object value) {
    
    if (value instanceof Integer) {
      this.setInteger(bindVarName, (Integer)value);
    } else if (value instanceof String) {
      this.setString(bindVarName, (String)value);
    } else if (value instanceof Long) {
      this.setLong(bindVarName, (Long)value);
    } else if (value instanceof Date) {
      this.setTimestamp(bindVarName, (Date)value);
    } else {
      throw new RuntimeException("Unexpected value: " + value + ", " + (value == null ? null : value.getClass()));
    }
    
    return this;
  }

  /**
   * append a certain number of params, and commas, and attach
   * the data.  Note any params before the in clause need to be already attached, since these will
   * attach now (ordering issue)
   * @param theQuery
   * @param params collection of params, note, this is for an inclause, so it cant be null
   * @return this for chaining
   */
  public ByHqlStatic setCollectionInClause(StringBuilder theQuery, Collection<?> params) {
    collectionInClauseHelper(this, theQuery, params);
    return this;
  }

  /**
   * helper method for collection in calue method
   * @param scalarable
   * @param query
   * @param params
   */
  static void collectionInClauseHelper(HqlQuery scalarable, StringBuilder query, Collection<?> params) {
    int numberOfParams = params.size();
    
    if (numberOfParams == 0) {
      throw new RuntimeException("Cant have 0 params for an in clause");
    }
    
    int index = 0;
    
    String paramPrefix = TwoFactorServerUtils.uniqueId();
    
    for (Object param : params) {
      
      String paramName = paramPrefix + index;
      
      query.append(":").append(paramName);
      
      if (index < numberOfParams - 1) {
        query.append(", ");
      }
      
      //cant have an in clause with something which is null
      if (param == null) {
        throw new RuntimeException("Param cannot be null: " + query);
      }
      
      scalarable.setScalar(paramName, param);
      
      index++;
    }

  }
  
  /**
   * <pre>
   * call hql unique result (returns one or null)
   * 
   * e.g.
   * 
   * Hib3GroupDAO hib3GroupDAO = HibernateSession.byHqlStatic()
   * .createQuery("from Hib3GroupDAO as g where g.uuid = :uuid")
   *  .setCacheable(false)
   *  .setCacheRegion(KLASS + ".Exists")
   *  .setString("uuid", uuid).uniqueResult(Hib3GroupDAO.class);
   * 
   * </pre>
   * @param returnType type of the result (in future can use this for typecasting)
   * @param <Q> is the template
   * @return the object or null if none found
   * @throws TfDaoException
   */
  @SuppressWarnings("unchecked")
  public <Q> Q uniqueResult(final Class<Q> returnType) throws TfDaoException {
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READONLY_OR_USE_EXISTING);
      
      Q result = (Q)HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              ByHql byHql = ByHqlStatic.this.byHql(hibernateSession);
              return byHql.uniqueResult(returnType);
            }
        
      });
      
      return result;
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (TfDaoException e) {
      TwoFactorServerUtils.injectInException(e, "Exception in uniqueResult: (" + returnType + "), " + this);
      throw e;
    } catch (RuntimeException e) {
      TwoFactorServerUtils.injectInException(e, "Exception in uniqueResult: (" + returnType + "), " + this);
      throw e;
    }
    
  }
  
  /**
   * <pre>
   * call hql list result 
   * 
   * e.g.
   * 
   * List<Hib3GroupTypeTupleDAO> hib3GroupTypeTupleDAOs = 
   *  HibernateSession.byHqlStatic()
   *    .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
   *    .setCacheable(false).setString("group", uuid).list(Hib3GroupTypeTupleDAO.class);
   * </pre>
   * @param returnType type of the result (can typecast)
   * @param <R> is the template
   * @return the list or the empty list if not found (never null)
   * @throws TfDaoException
   */
  @SuppressWarnings("unchecked")
  public <R> List<R> list(final Class<R> returnType) throws TfDaoException {
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READONLY_OR_USE_EXISTING);
      
      List<R> result = (List<R>)HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              ByHql byHql = ByHqlStatic.this.byHql(hibernateSession);
              byHql.options(ByHqlStatic.this.queryOptions);
              List<R> list = byHql.list(returnType);
              return list;
            }
        
      });
      
      return result;
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in list: (" + returnType + "), " + this;

      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * <pre>
   * call hql list result, and put the results in an ordered set
   * 
   * e.g.
   * 
   * Set<GroupTypeTupleDTO> groupTypeTupleDTOs = 
   *  HibernateSession.byHqlStatic()
   *    .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
   *    .setCacheable(false).setString("group", uuid).listSet(Hib3GroupTypeTupleDAO.class);
   * </pre>
   * @param returnType type of the result (can typecast)
   * @param <S> is the template
   * @return the ordered set or the empty set if not found (never null)
   * @throws TfDaoException
   */
  public <S> Set<S> listSet(final Class<S> returnType) throws TfDaoException {
    List<S> list = this.list(returnType);
    Set<S> result = new LinkedHashSet<S>(list);
    return result;
  }

  /**
   * <pre>
   * call hql list result, and put the results in map with the key as one of the fields
   * 
   * </pre>
   * @param valueClass type of the result (can typecast)
   * @param keyClass is the type of the key of the map
   * @param <K> is the template of the key of the map
   * @param <V> is the template of the value of the map
   * @param keyPropertyName name of the javabeans property for the key in the map
   * @return the ordered set or the empty set if not found (never null)
   * @throws TfDaoException
   */
  public <K, V> Map<K, V> listMap(final Class<K> keyClass, final Class<V> valueClass, String keyPropertyName) throws TfDaoException {
    List<V> list = this.list(valueClass);
    Map<K,V> map = TwoFactorServerUtils.listToMap(list, keyClass, valueClass, keyPropertyName);
    return map;
  }

  /**
   * <pre>
   * call hql executeUpdate, e.g. delete or update statement
   * 
   * </pre>
   * @throws TfDaoException
   */
  public void executeUpdate() throws TfDaoException {
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
              
              ByHql byHql = ByHqlStatic.this.byHql(hibernateSession);
              byHql.executeUpdate();
              return null;
            }
        
      });
      
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in executeUpdate: " + this;
      
      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    }
    
  }

  /**
   * create a byhql
   * @param hibernateSession
   * @return the byhql
   */
  private ByHql byHql(HibernateSession hibernateSession) {
    
    ByHql byHql = new ByHql(hibernateSession);
    //transfer all data over
    byHql.setBindVarNameParams(ByHqlStatic.this.bindVarNameParams);
    byHql.setCacheable(ByHqlStatic.this.cacheable);
    byHql.setCacheRegion(ByHqlStatic.this.cacheRegion);
    byHql.setQuery(ByHqlStatic.this.query);
    byHql.options(ByHqlStatic.this.queryOptions);
    return byHql;
    
  }
  
  /**
   * add a paging/sorting/resultSetSize, etc to the query
   * @param queryOptions1
   * @return this for chaining
   */
  public ByHqlStatic options(TfQueryOptions queryOptions1) {
    this.queryOptions = queryOptions1;
    return this;
  }
  
  /**
   * constructor
   *
   */
  ByHqlStatic() {}
  
  
  
}
