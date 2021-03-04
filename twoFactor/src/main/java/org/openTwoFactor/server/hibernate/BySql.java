package org.openTwoFactor.server.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.databasePerf.TfDatabasePerfLog;
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
public class BySql extends HibernateDelegate {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = TwoFactorServerUtils.getLog(BySql.class);

  /** assign a transaction type, default use the transaction modes
   * TwoFactorTransactionType.READONLY_OR_USE_EXISTING, and 
   * TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING depending on
   * if a transaction is needed */
  private TwoFactorTransactionType transactionType = null;
  
  /**
   * assign a different transactionType (e.g. for autonomous transactions)
   * @param theTransactionType
   * @return the same object for chaining
   */
  public BySql setTransactionType(TwoFactorTransactionType 
      theTransactionType) {
    this.transactionType = theTransactionType;
    return this;
  }
  
  /**
   * string value for error handling
   * @return the string value
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder("BySql, query: '");
    result.append(this.query);
    result.append(", tx type: ").append(this.transactionType);
    //dont use bindVars() method so it doesnt lazy load
    if (this.bindVars != null) {
      int index = 0;
      int size = this.bindVars().size();
      for (Object object : this.bindVars()) {
        result.append("Bind var[").append(index++).append("]: '");
        result.append(TwoFactorServerUtils.toStringForLog(object, 50));
        if (index!=size-1) {
          result.append(", ");
        }
      }
    }
    return result.toString();
  }
  /**
   * map of params to attach to the query.
   * access this with the bindVarNameParams method
   */
  private List<Object> bindVars = null;

  /**
   * query to execute
   */
  private String query = null;

  /**
   * set the query to run
   * @param theHqlQuery
   * @return this object for chaining
   */
  public BySql createQuery(String theHqlQuery) {
    this.query = theHqlQuery;
    return this;
  }
  
  /**
   * lazy load params
   * @return the params map
   */
  private List<Object> bindVars() {
    if (this.bindVars == null) {
      this.bindVars = new ArrayList<Object>();
    }
    return this.bindVars;
  }
  
  /** query count exec queries, used for testing */
  public static int queryCountQueries = 0;
  

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public int executeSql(final String sql, final List<Object> params) {
  
    HibernateSession hibernateSession = this.getHibernateSession();
    hibernateSession.misc().flush();
    long start = System.nanoTime();
    PreparedStatement preparedStatement = null;
    int result = -1;
    try {
      
      //we dont close this connection or anything since could be pooled
      Connection connection = hibernateSession.getSession().connection();
      preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setFetchSize(1000);
      BySqlStatic.attachParams(preparedStatement, params);
      
      result = preparedStatement.executeUpdate();
      
      return result;

    } catch (Exception e) {
      throw new RuntimeException("Problem with query in bysqlstatic: " + sql, e);
    } finally {
      TwoFactorServerUtils.closeQuietly(preparedStatement);
      if (TfDatabasePerfLog.LOG.isDebugEnabled()) {
        TfDatabasePerfLog.dbPerfLog(((System.nanoTime()-start)/1000000L) + "ms SQL: " + sql + ", params: " + TwoFactorServerUtils.toStringForLog(params) + ", result: " + result);
      }
    }
  
  }


  /**
   * @param theHibernateSession
   */
  public BySql(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }


  
  /**
   * @param bindVars1 the bindVars to set
   */
  void setBindVars(List<Object> bindVars1) {
    this.bindVars = bindVars1;
  }


  
  /**
   * @param query1 the query to set
   */
  void setQuery(String query1) {
    this.query = query1;
  }
  
  
  
}
