/**
 * 
 */
package org.openTwoFactor.server.hibernate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.openTwoFactor.server.databasePerf.TfDatabasePerfLog;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.exceptions.TfStaleObjectStateException;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * 
 * for simple criteria queries, use this instead of inverse of control.
 * this will do proper error handling and descriptive exception
 * handling.  This will by default use the transaction modes
 * TwoFactorTransactionType.READONLY_OR_USE_EXISTING, and 
 * TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING depending on
 * if a transaction is needed.
 * 
 * @author mchyzer
 *
 */
public class ByCriteriaStatic {
  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(ByCriteriaStatic.class);

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
   * @param thetransactionType
   * @return the same object for chaining
   */
  public ByCriteriaStatic setTransactionType(TwoFactorTransactionType 
      thetransactionType) {
    this.transactionType = thetransactionType;
    return this;
  }
  
  
  /**
   * assign if this query is cacheable or not.
   * @param cacheable1 the cacheable to set
   * @return this object for chaining
   */
  public ByCriteriaStatic setCacheable(Boolean cacheable1) {
    this.cacheable = cacheable1;
    return this;
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
   * @param theCriterions 
   * @return the ordered set or the empty set if not found (never null)
   * @throws TfDaoException
   */
  public <S> Set<S> listSet(Class<S> returnType, Criterion theCriterions) throws TfDaoException {
    Set<S> result = new LinkedHashSet<S>(this.list(returnType, theCriterions));
    return result;
  }

  /**
   * <pre>
   * call criteria list result, and put the results in map with the key as one of the fields
   * 
   * </pre>
   * @param valueClass type of the result (can typecast)
   * @param theCriterions are the criteria for the query
   * @param keyClass is the type of the key of the map
   * @param <K> is the template of the key of the map
   * @param <V> is the template of the value of the map
   * @param keyPropertyName name of the javabeans property for the key in the map
   * @return the ordered set or the empty set if not found (never null)
   * @throws TfDaoException
   */
  public <K, V> Map<K, V> listMap(final Class<K> keyClass, final Class<V> valueClass, Criterion theCriterions, String keyPropertyName) throws TfDaoException {
    List<V> list = this.list(valueClass, theCriterions);
    Map<K,V> map = TwoFactorServerUtils.listToMap(list, keyClass, valueClass, keyPropertyName);
    return map;
  }

  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("ByCriteriaStatic, persistentClass: '");
    result.append(this.persistentClass).append("', criterions: ").append(this.criterions)
      .append("', cacheable: ").append(this.cacheable);
    result.append(", cacheRegion: ").append(this.cacheRegion);
    result.append(", entityName: ").append(this.entityName);
    result.append(", tx type: ").append(this.transactionType);
    if (this.queryOptions != null) {
      result.append(", options: ").append(this.queryOptions.toString());
    }
    return result.toString();
  }
  
  /**
   * cache region for cache
   */
  private String cacheRegion = null;

  /**
   * alias for class
   */
  private String alias = null;

  /**
   * criterions to query
   */
  private Criterion criterions = null;

  /**
   * class to execute criteria on
   */
  private Class<?> persistentClass = null;

  /** if we are sorting, paging, resultSize, etc */
  private TfQueryOptions queryOptions = null;

  /**
   * assign the entity name to refer to this mapping (multiple mappings per object)
   */
  private String entityName = null;

  /**
   * add a paging/sorting/resultSetSize, etc to the query
   * @param queryOptions1
   * @return this for chaining
   */
  public ByCriteriaStatic options(TfQueryOptions queryOptions1) {
    this.queryOptions = queryOptions1;
    return this;
  }

  /**
   * cache region for cache
   * @param cacheRegion1 the cacheRegion to set
   * @return this object for chaining
   */
  public ByCriteriaStatic setCacheRegion(String cacheRegion1) {
    this.cacheRegion = cacheRegion1;
    return this;
  }

  /**
   * alias for queried class
   * @param theAlias the cacheRegion to set
   * @return this object for chaining
   */
  public ByCriteriaStatic setAlias(String theAlias) {
    this.alias = theAlias;
    return this;
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
   * @param theCriterions are the criterions to use (pack multiple with TfHibUtils.listCrit())
   * @param <T> is the template
   * @return the object or null if none found
   * @throws TfDaoException
   */
  @SuppressWarnings("unchecked")
  public <T> T uniqueResult(Class<T> returnType, Criterion theCriterions) throws TfDaoException {
    this.persistentClass = returnType;
    this.criterions = theCriterions;
    long start = System.nanoTime();
    T result = null;
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READONLY_OR_USE_EXISTING);
      
      result = (T)HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              Session session  = hibernateSession.getSession();
              Criteria criteria = ByCriteriaStatic.this.attachCriteriaInfo(session);
              Object object = criteria.uniqueResult();
              TfHibUtils.evict(hibernateSession, object, true);
              return object;
            }
        
      });
      
      return result;
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (RuntimeException e) {
      
      String errorString = "Exception in uniqueResult: (" + returnType + "), " + this;

      if (!TwoFactorServerUtils.injectInException(e, errorString)) {
        LOG.error(errorString, e);
      }

      throw e;
    } finally {
      if (TfDatabasePerfLog.LOG.isDebugEnabled()) {
        TfDatabasePerfLog.dbPerfLog(((System.nanoTime()-start)/1000000L) + "ms criterion: " 
            + TwoFactorServerUtils.toStringForLog(this.criterions) + ", resultType: " + returnType 
            + ", result: " + TwoFactorServerUtils.toStringForLog(result));
      }
      
    }
    
  }

  /** query count exec queries, used for testing */
  public static int queryCountQueries = 0;
  
  /**
   * <pre>
   * call hql unique result (returns one or null)
   * 
   * e.g.
   * 
   * List<Hib3GroupTypeTupleDAO> hib3GroupTypeTupleDAOs = 
   *  HibernateSession.byHqlStatic()
   *    .createQuery("from Hib3GroupTypeTupleDAO as gtt where gtt.groupUuid = :group")
   *    .setCacheable(false).setString("group", uuid).list(Hib3GroupTypeTupleDAO.class);
   * </pre>
   * @param returnType type of the result (can typecast)
   * @param theCriterions are the criterions to use (pack multiple with TfHibUtils.listCrit())
   * @param <T> is the template
   * @return the list or the empty list if not found (never null)
   * @throws TfDaoException
   */
  @SuppressWarnings("unchecked")
  public <T> List<T> list(Class<T> returnType, Criterion theCriterions) throws TfDaoException {
    this.persistentClass = returnType;
    this.criterions = theCriterions;
    long start = System.nanoTime();
    List<T> result = null;
    try {
      TwoFactorTransactionType transactionTypeToUse = 
        (TwoFactorTransactionType)ObjectUtils.defaultIfNull(this.transactionType, 
            TwoFactorTransactionType.READONLY_OR_USE_EXISTING);
      
      result = (List<T>)HibernateSession.callbackHibernateSession(
          transactionTypeToUse, TfAuditControl.WILL_NOT_AUDIT,
          new HibernateHandler() {
  
            @SuppressWarnings("unchecked")
            public Object callback(HibernateHandlerBean hibernateHandlerBean)
                throws TfDaoException {
              HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
              
              Session session  = hibernateSession.getSession();
              List<T> list = null;
              
              //see if we are even retrieving the results
              if (ByCriteriaStatic.this.queryOptions == null || ByCriteriaStatic.this.queryOptions.isRetrieveResults()) {
              Criteria criteria = ByCriteriaStatic.this.attachCriteriaInfo(session);
              //not sure this can ever be null, but make sure not to make iterating results easier
                list = TwoFactorServerUtils.nonNull(criteria.list());
              TfHibUtils.evict(hibernateSession, list, true);
              }
              //no nulls
              list = TwoFactorServerUtils.nonNull(list);
              TfQueryPaging queryPaging = ByCriteriaStatic.this.queryOptions == null ? null : ByCriteriaStatic.this.queryOptions.getQueryPaging();
              
              //now see if we should get the query count
              boolean retrieveQueryCountNotForPaging = ByCriteriaStatic.this.queryOptions != null && ByCriteriaStatic.this.queryOptions.isRetrieveCount();
              boolean findQueryCount = (queryPaging != null && queryPaging.isDoTotalCount()) 
                || (retrieveQueryCountNotForPaging);
              if (findQueryCount) {
                
                long resultSize = -1;
                if (queryPaging != null) {
                  //see if we already know the total size (if less than page size and first page)
                  resultSize = TwoFactorServerUtils.length(list);
                  if (resultSize >= queryPaging.getPageSize()) {
                    resultSize = -1;
                  } else {
                    //we are on the last page, see how many records came before us, add those in
                    resultSize += (queryPaging.getPageSize() * (queryPaging.getPageNumber() - 1)); 
                  }
                }
                
                //do this if we dont have a total, or if we are not caching the total
                if ((queryPaging != null && (queryPaging.getTotalRecordCount() < 0 || !queryPaging.isCacheTotalCount())) 
                    || resultSize > -1 || retrieveQueryCountNotForPaging) {
                  
                  //if we dont already know the size
                  if (resultSize == -1) {
                    queryCountQueries++;

                    Criteria countQuery = StringUtils.isBlank(ByCriteriaStatic.this.alias) ? 
                        session.createCriteria(ByCriteriaStatic.this.persistentClass) 
                        : session.createCriteria(ByCriteriaStatic.this.alias);

                    //turn it into a row count
                    countQuery.setProjection( Projections.projectionList()
                        .add( Projections.rowCount()));

                    //add criterions
                    if (ByCriteriaStatic.this.criterions != null) {
                      countQuery.add(ByCriteriaStatic.this.criterions);
                    }
                    resultSize = (Long)countQuery.list().get(0);
                  }
                  
                  if (queryPaging != null) {
                    queryPaging.setTotalRecordCount((int)resultSize);
            
                    //calculate the page stuff like how many pages etc
                    queryPaging.calculateIndexes();
                  }
                  if (retrieveQueryCountNotForPaging) {
                    ByCriteriaStatic.this.queryOptions.setCount(resultSize);
                  }
                }
              }

              return list;
            }
        
      });
      
      return result;
    } catch (TfStaleObjectStateException e) {
      throw e;
    } catch (TfDaoException e) {
      TwoFactorServerUtils.injectInException(e, "Exception in list: (" + returnType + "), " + this);
      throw e;
    } catch (RuntimeException e) {
      TwoFactorServerUtils.injectInException(e, "Exception in list: (" + returnType + "), " + this);
      throw e;
    } finally {
      if (TfDatabasePerfLog.LOG.isDebugEnabled()) {
        TfDatabasePerfLog.dbPerfLog(((System.nanoTime()-start)/1000000L) + "ms criterion: " 
            + TwoFactorServerUtils.toStringForLog(this.criterions) + ", resultType: " + returnType + ", rows: " + TwoFactorServerUtils.length(result));
      }
      
    }
    
  }

  /**
   * prepare query based on criteria
   * @param session hib session
   * @return the query
   */
  private Criteria attachCriteriaInfo(Session session) {
    Criteria query = null;
    
    if (StringUtils.isBlank(this.entityName)) {
      if (StringUtils.isBlank(this.alias)) {
        query = session.createCriteria(this.persistentClass);
      } else {
        query = session.createCriteria(this.persistentClass, this.alias);
      }
    } else {
      query = session.createCriteria(this.entityName);
    }
    
    //add criterions
    if (this.criterions != null) {
      query.add(this.criterions);
    }
    boolean secondLevelCaching = TfHibUtils.secondLevelCaching(
        ByCriteriaStatic.this.cacheable, ByCriteriaStatic.this.queryOptions);
    query.setCacheable(secondLevelCaching);

    if (secondLevelCaching) {
      String secondLevelCacheRegion = TfHibUtils.secondLevelCacheRegion(ByCriteriaStatic.this.cacheRegion, 
          ByCriteriaStatic.this.queryOptions);
      if (!StringUtils.isBlank(secondLevelCacheRegion)) {
        query.setCacheRegion(secondLevelCacheRegion);
      }
    }
    
    TfQuerySort querySort = this.queryOptions == null ? null : this.queryOptions.getQuerySort();
    if (querySort != null) {
      List<TfQuerySortField> sorts = querySort.getQuerySortFields();
      
      for (TfQuerySortField theSort : TwoFactorServerUtils.nonNull(sorts)) {
        
        Order order = theSort.isAscending() ? Order.asc(theSort.getColumn()) : Order.desc(theSort.getColumn());
        
        query.addOrder(order);
        
    }
    }
    TfQueryPaging queryPaging = this.queryOptions == null ? null : this.queryOptions.getQueryPaging();
    if (queryPaging != null) {
      query.setFirstResult(queryPaging.getFirstIndexOnPage());
      query.setMaxResults(queryPaging.getPageSize());
    }

    return query;
    
  }
  
  /**
   * entity name if the object is mapped to more than one table
   * @param theEntityName the entity name of the object
   * @return this object for chaining
   */
  public ByCriteriaStatic setEntityName(String theEntityName) {
    this.entityName = theEntityName;
    return this;
  }


  /**
   * constructor
   *
   */
  ByCriteriaStatic() {}  
  
}
