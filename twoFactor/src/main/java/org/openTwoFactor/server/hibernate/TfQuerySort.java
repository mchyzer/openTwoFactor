/*
 * @author mchyzer
 * $Id: TfQuerySort.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 *
 */
public class TfQuerySort {

  /** 
   * list of sort fields... generally it would just be one 
   */
  private List<TfQuerySortField> querySortFields = new ArrayList<TfQuerySortField>();

  /**
   * list of sort fields... generally it would just be one
   * @return the sort fields
   */
  public List<TfQuerySortField> getQuerySortFields() {
    return this.querySortFields;
  } 
  
  /** max cols to store */
  private int maxCols = 10;

  /**
   * max cols to store
   * @return max cols
   */
  public int getMaxCols() {
    return this.maxCols;
  }

  /**
   * max cols to store
   * @param maxCols1
   */
  public void setMaxCols(int maxCols1) {
    this.maxCols = maxCols1;
  }

  /**
   * shortcut for ascending col
   * @param column
   * @return the query sort
   */
  public static TfQuerySort asc(String column) {
    return new TfQuerySort(column, true);
  }
  
  /**
   * shortcut for descending col
   * @param column
   * @return the query sort
   */
  public static TfQuerySort desc(String column) {
    return new TfQuerySort(column, false);
  }
  
  /**
   * 
   * @param column
   * @param ascending
   */
  public TfQuerySort(String column, boolean ascending) {
    this.querySortFields.add(new TfQuerySortField(column, ascending));
  }

  /**
   * 
   * @param column
   * @param ascending
   */
  public void assignSort(String column, boolean ascending) {
    this.querySortFields.clear();
    this.querySortFields.add(new TfQuerySortField(column, ascending));
  }
  
  /**
   * 
   * @param column
   * @param ascending
   * @deprecated use insertSortToBeginning
   */
  @Deprecated
  public void addSort(String column, boolean ascending) {
    this.insertSortToBeginning(column, ascending);
  }
  
  /**
   * insert sort to beginning of sort order...
   * @param column
   * @param ascending
   */
  public void insertSortToBeginning(String column, boolean ascending) {
    Iterator<TfQuerySortField> iterator = this.querySortFields.iterator();
    
    //remove elements that are the same column
    while (iterator.hasNext()) {
      TfQuerySortField querySortField = iterator.next();
      if (StringUtils.equals(column, querySortField.getColumn())) {
        iterator.remove();
      }
    }
    
    //insert into the front of the list
    this.querySortFields.add(0, new TfQuerySortField(column, ascending));
    
    //max sure less than max size
    for (int i = this.querySortFields.size()-1; i >= this.maxCols; i--) {
      this.querySortFields.remove(i);
    }
  }

  /**
   * see if we are sorting
   * @return true if sorting
   */
  public boolean isSorting() {
    return this.querySortFields.size() > 0;
  }
  
  /** 
   * get the sort string based on the cols, add space before perhaps
   * @param includePreSpaceIfSorting if we should add a whitespace char before sortstring if it exists
   * @return the sort string
   */
  public String sortString(boolean includePreSpaceIfSorting) {
    
    StringBuilder result = new StringBuilder();
    for (TfQuerySortField querySortField : this.querySortFields) {
      
      result.append(querySortField.getColumn()).append(" ").append(querySortField.isAscending() ? "asc" : "desc").append(", ");
      
    }
    
    //remove the last comma
    if (result.length() >= 2) {
      result.delete(result.length()-2, result.length());
      
      //we are sorting if in this block, so add a space if supposed to
      if (includePreSpaceIfSorting) {
        result.insert(0, " ");
      }
    }
    
    return result.toString();
  }
  
}
