/*
 * @author mchyzer
 * $Id: TfQueryOptions.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * <pre>
 * options on a query (e.g. sorting, paging, total result size, etc)
 * 
 * Sorting example:
 *    queryOptions = new TfQueryOptions().sortAsc("m.subjectIdDb");
 *
 *    Set&lt;Member&gt; members = group.getImmediateMembers(field, queryOptions);
 *
 * Paging example:
 *    TfQueryPaging queryPaging = new TfQueryPaging();
 *    queryPaging.setPageSize(pageSize);
 *    queryPaging.setPageNumber(pageNumberOneIndexed);
 *    -or- queryPaging.setFirstIndexOnPage(startZeroIndexed);
 *    queryOptions = new TfQueryOptions().paging(queryPaging);
 *
 *    Set&lt;Member&gt; members = group.getImmediateMembers(field, queryOptions);
 *
 * Query count example:
 * 
 *    TfQueryOptions queryOptions = new TfQueryOptions().retrieveCount(true).retrieveResults(false);
 *    group.getImmediateMembers(field, queryOptions);
 *    int totalSize = queryOptions.getCount().intValue();
 * 
 * </pre>
 */
public class TfQueryOptions {

  /**
   * 
   */
  public TfQueryOptions() {
    
  }
  
  /**
   * @param sortString 
   * @param ascending 
   * @param pageNumber 
   * @param pageSize 
   * @return the query options if needed
   * 
   */
  public static TfQueryOptions create(String sortString, Boolean ascending, Integer pageNumber, Integer pageSize) {
    TfQueryOptions queryOptions = null;
    
    if (ascending != null || !StringUtils.isBlank(sortString) 
        || pageNumber != null || pageSize != null) {
      
      queryOptions = new TfQueryOptions();
      
      if (ascending != null || !StringUtils.isBlank(sortString)) {
        
        TfQuerySort querySort = new TfQuerySort(sortString, TwoFactorServerUtils.defaultIfNull(ascending, Boolean.TRUE));
        queryOptions.sort(querySort);
        
      }

      if (pageNumber != null || pageSize != null) {
        
        TfQueryPaging queryPaging = new TfQueryPaging();
        queryPaging.setPageNumber(pageNumber);
        queryPaging.setPageSize(pageSize);
        queryOptions.paging(queryPaging);
      }
      
    }
    return queryOptions;
  }
  
  /**
   * if this query is sorted (by options), and what the col(s) are
   */
  private TfQuerySort querySort;

  /**
   * If this is a paged query, and what are specs
   */
  private TfQueryPaging queryPaging;
  
  /**
   * If the results should be retrieved (generally only false for size queries).
   * default to true
   */
  private Boolean retrieveResults;
  
  /**
   * If the count of the query should be retrieved (sometimes paging will get
   * the count)
   * default to false
   */
  private Boolean retrieveCount;
  
  /**
   * if hibernate should second level cache this query
   */
  private Boolean secondLevelCache;
  
  /**
   * if hibernate should second level cache this query, this is the region
   */
  private String secondLevelCacheRegion;
  
  /**
   * 
   * @param secondLevelCache1
   * @return this for chaining
   */
  public TfQueryOptions secondLevelCache(boolean secondLevelCache1) {
    this.secondLevelCache = secondLevelCache1;
    return this;
  }
  
  /**
   * 
   * @return if second level cache
   */
  public Boolean getSecondLevelCache() {
    return this.secondLevelCache;
  }
  
  /**
   * 
   * @param secondLevelCacheRegion1
   * @return this for chaining
   */
  public TfQueryOptions secondLevelCacheRegion(String secondLevelCacheRegion1) {
    this.secondLevelCacheRegion = secondLevelCacheRegion1;
    return this;
  }
  
  /**
   * 
   * @return if second level cache
   */
  public String getSecondLevelCacheRegion() {
    return this.secondLevelCacheRegion;
  }
  
  /**
   * count of the query if it is being calculated.  Note the hibernateSession API
   * is what sets this
   */
  private Long count = null;
  
  /**
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("TfQueryOptions: ");
    if (this.queryPaging != null) {
      result.append("queryPaging: ").append(this.queryPaging.toString()).append(", ");
    }
    if (this.querySort != null) {
      result.append("querySort: ").append(this.querySort.sortString(false)).append(", ");
    }
    if (this.secondLevelCache != null) {
      result.append("secondLevelCache: ").append(this.getSecondLevelCache()).append(", ");
    }
    if (this.retrieveResults != null) {
      result.append("retrieveResults: ").append(this.retrieveResults).append(", ");
    }
    if (this.retrieveCount != null) {
      result.append("retrieveCount: ").append(this.retrieveCount).append(", ");
    }
    if (this.count != null) {
      result.append("count: ").append(this.count).append(", ");
    }
    return result.toString();
  }

  /**
   * if this query is sorted (by options), and what the col(s) are
   * @return sort
   */
  public TfQuerySort getQuerySort() {
    return this.querySort;
  }

  /**
   * if this query is sorted (by options), and what the col(s) are
   * @param querySort1
   * @return this for chaining
   */
  public TfQueryOptions sort(TfQuerySort querySort1) {
    this.querySort = querySort1;
    return this;
  }

  /**
   * If this is a paged query, and what are specs
   * @return paging
   */
  public TfQueryPaging getQueryPaging() {
    return this.queryPaging;
  }

  /**
   * sort ascending on this field
   * @param field
   * @return this for chaining
   */
  public TfQueryOptions sortAsc(String field) {
    if (this.querySort == null) {
      this.querySort = new TfQuerySort(field, true);
    } else {
      this.querySort.insertSortToBeginning(field, true);
    }
    return this;
  }

  /**
   * factory for query paging
   * @param pageSize
   * @param pageNumber 1 indexed page number
   * @param doTotalCount true to do total count, false to not
   * @return this for chaining
   */
  public TfQueryOptions paging(int pageSize, int pageNumber, boolean doTotalCount) {
    this.queryPaging = TfQueryPaging.page(pageSize, pageNumber, doTotalCount);
    return this;
  }
  
  /**
   * sort ascending on this field
   * @param field
   * @return this for chaining
   */
  public TfQueryOptions sortDesc(String field) {
    if (this.querySort == null) {
      this.querySort = new TfQuerySort(field, false);
    } else {
      this.querySort.insertSortToBeginning(field, false);
    }
    return this;
  }
  
  /**
   * If this is a paged query, and what are specs
   * @param queryPaging1
   * @return this for chaining
   */
  public TfQueryOptions paging(TfQueryPaging queryPaging1) {
    this.queryPaging = queryPaging1;
    return this;
  }

  /**
   * If the results should be retrieved (generally only false for size queries).
   * default to true
   * @return retrieve results
   */
  public boolean isRetrieveResults() {
    return this.retrieveResults == null ? true : this.retrieveResults;
  }

  /**
   * If the results should be retrieved (generally only false for size queries).
   * default to true
   * @param retrieveResults1
   * @return this for chaining
   */
  public TfQueryOptions retrieveResults(boolean retrieveResults1) {
    this.retrieveResults = retrieveResults1;
    return this;
  }

  /**
   * If the count of the query should be retrieved (sometimes paging will get
   * the count)
   * default to false
   * @return retrieve count
   */
  public boolean isRetrieveCount() {
    return this.retrieveCount == null ? false : this.retrieveCount;
  }

  /**
   * If the count of the query should be retrieved (sometimes paging will get
   * the count)
   * default to false
   * @param retrieveCount1
   * @return this for chaining
   */
  public TfQueryOptions retrieveCount(boolean retrieveCount1) {
    this.retrieveCount = retrieveCount1;
    return this;
  }

  /**
   * count of the query if it is being calculated.  Note the hibernateSession API
   * is what sets this
   * @return the count or null if not set
   */
  public Long getCount() {
    return this.count;
  }

  /**
   * count of the query if it is being calculated.  Note the hibernateSession API
   * is what sets this
   * @param count1
   */
  public void setCount(Long count1) {
    this.count = count1;
  }
  
}
