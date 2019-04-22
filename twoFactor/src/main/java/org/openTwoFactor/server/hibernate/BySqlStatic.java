
package org.openTwoFactor.server.hibernate;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.type.Type;
import org.openTwoFactor.server.databasePerf.TfDatabasePerfLog;
import org.openTwoFactor.server.exceptions.TfDaoException;
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
public class BySqlStatic {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = TwoFactorServerUtils.getLog(BySqlStatic.class);

  
  /**
   * constructor
   *
   */
  BySqlStatic() {}

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public int executeSql(final String sql) {
    return executeSql(sql, null);
  }

  /**
   * execute some sql
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public int executeSql(final String sql, final List<Object> params) {
  
    final long start = System.nanoTime();
    int result = (Integer)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws TfDaoException {
        
        //lets flush before the query
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        hibernateSession.misc().flush();
        
        PreparedStatement preparedStatement = null;
        int result1 = -1;
        try {
          
          //we dont close this connection or anything since could be pooled
          Connection connection = hibernateSession.getSession().connection();
          preparedStatement = connection.prepareStatement(sql);
      
          attachParams(preparedStatement, params);
          
          result1 = preparedStatement.executeUpdate();
          
          return result1;

        } catch (Exception e) {
          throw new RuntimeException("Problem with query in bysqlstatic: " + sql, e);
        } finally {
          TwoFactorServerUtils.closeQuietly(preparedStatement);
          if (TfDatabasePerfLog.LOG.isDebugEnabled()) {
            TfDatabasePerfLog.dbPerfLog(((System.nanoTime()-start)/1000000L) + "ms SQL: " + sql + ", params: " + TwoFactorServerUtils.toStringForLog(params) + ", result: " + result1);
          }

        }
      }
      
    });
    return result;
  
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings("deprecation")
  public <T> T select(final Class<T> returnClassType, final String sql) {
    return select(returnClassType, sql, null);
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings({ "deprecation", "unchecked" })
  public <T> T select(final Class<T> returnClassType, final String sql, final List<Object> params) {
  
    T theResult = (T)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws TfDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        T result = null;
        long start = System.nanoTime();
        try {
          
          //we dont close this connection or anything since could be pooled
          Connection connection = hibernateSession.getSession().connection();
          preparedStatement = connection.prepareStatement(sql);
      
          attachParams(preparedStatement, params);
          
          resultSet = preparedStatement.executeQuery();
          
          boolean hasResults = resultSet.next();
          
          if (!hasResults) {
            return null;
          }
          

          if (returnClassType.isArray()) {
            int columnCount = resultSet.getMetaData().getColumnCount();
            result = (T)Array.newInstance(String.class, columnCount);
            for (int i=0;i<columnCount;i++) {
              Array.set(result, i, resultSet.getString(1+i));
            }
          } else {
            boolean isInt = int.class.equals(returnClassType);
            boolean isPrimitive = isInt;
            if (isInt || Integer.class.equals(returnClassType)) {
              BigDecimal bigDecimal = resultSet.getBigDecimal(1);
              if (bigDecimal != null) {
                result = (T)(Object)bigDecimal.intValue();
              }
            } else if (String.class.equals(returnClassType)) {
              result = (T)resultSet.getString(1);
            } else {
              throw new RuntimeException("Unexpected type: " + returnClassType);
            }
            if (result == null && isPrimitive) {
              throw new NullPointerException("expecting primitive (" + returnClassType.getSimpleName() 
                  + "), but received null");
            }
          }
          
          if (resultSet.next()) {
            throw new RuntimeException("Expected 1 row but received multiple");
          }
          
          return result;
  
        } catch (Exception e) {
          throw new RuntimeException("Problem with query in select: " + sql, e);
        } finally {
          TwoFactorServerUtils.closeQuietly(preparedStatement);
          if (TfDatabasePerfLog.LOG.isDebugEnabled()) {
            TfDatabasePerfLog.dbPerfLog(((System.nanoTime()-start)/1000000L) + "ms SQL: " + sql + ", params: " + TwoFactorServerUtils.toStringForLog(params) + ", result: " + result);
          }
        }
      }
    });
    return theResult;
  
  }

  /**
   * select one object from sql (one row, one col
   * @param returnClassType type to be returned (currnetly supports string and int
   * @param <T> the type
   * @param sql can be insert, update, delete, or ddl
   * @param params prepared statement params
   * @return the number of rows affected or 0 for ddl
   */
  @SuppressWarnings({ "deprecation", "unchecked" })
  public <T> List<T> listSelect(final Class<T> returnClassType, final String sql, final List<Object> params) {
  
    List<T> theResult = (List<T>)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      /**
       * @see HibernateHandler#callback(HibernateHandlerBean)
       */
      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws TfDaoException {
        
        long start = System.nanoTime();
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        List<Object> resultList = new ArrayList<Object>();
        try {
          
          //we dont close this connection or anything since could be pooled
          Connection connection = hibernateSession.getSession().connection();
          preparedStatement = connection.prepareStatement(sql);
      
          attachParams(preparedStatement, params);
          
          resultSet = preparedStatement.executeQuery();
          
          int columnCount = resultSet.getMetaData().getColumnCount();
          
          while (resultSet.next()) {
          
            T result = null;
            
            if (returnClassType.isArray()) {
              result = (T)Array.newInstance(String.class, columnCount);
              for (int i=0;i<columnCount;i++) {
                Array.set(result, i, resultSet.getString(1+i));
              }
            } else {
            
              boolean isInt = int.class.equals(returnClassType);
              boolean isPrimitive = isInt;
              if (isInt || Integer.class.equals(returnClassType)) {
                BigDecimal bigDecimal = resultSet.getBigDecimal(1);
                if (bigDecimal != null) {
                  result = (T)(Object)bigDecimal.intValue();
                }
              } else if (String.class.equals(returnClassType)) {
                result = (T)resultSet.getString(1);
              } else {
                throw new RuntimeException("Unexpected type: " + returnClassType);
              }
              if (result == null && isPrimitive) {
                throw new NullPointerException("expecting primitive (" + returnClassType.getSimpleName() 
                    + "), but received null");
              }
            }
            
            resultList.add(result);
          }          
          return resultList;

        } catch (Exception e) {
          throw new RuntimeException("Problem with query in listSelect: " + sql, e);
        } finally {
          TwoFactorServerUtils.closeQuietly(preparedStatement);
          if (TfDatabasePerfLog.LOG.isDebugEnabled()) {
            TfDatabasePerfLog.dbPerfLog(((System.nanoTime()-start)/1000000L) + "ms SQL: " + sql + ", params: " + TwoFactorServerUtils.toStringForLog(params) + ", rows: " + TwoFactorServerUtils.length(resultList));
          }
        }
      }
    });
    return theResult;
  
  }

  /**
   * Attach params for a prepared statement.  The type of the params and types must be the
   * same (e.g. either both array or list, but not one is Array, and the other list
   * @param statement
   * @param params either null, Object, Object[], or List of Objects
   * @throws HibernateException
   * @throws SQLException
   */
  @SuppressWarnings("unchecked")
  public static void attachParams(PreparedStatement statement, Object params)
      throws HibernateException, SQLException {
    if (TwoFactorServerUtils.length(params) == 0) {
      return;
    }
    List<Object> paramList = TwoFactorServerUtils.toList(params);
    List<Type> typeList = TfHibUtils.hibernateTypes(paramList);
    attachParams(statement, paramList, typeList);
  }


  /**
   * Attach params for a prepared statement.  The type of the params and types must be the
   * same (e.g. either both array or list, but not one is Array, and the other list
   * @param statement
   * @param params either null, Object, Object[], or List of Objects
   * @param types either null, Type, Type[], or List of Objects
   * @throws HibernateException
   * @throws SQLException
   */
  public static void attachParams(PreparedStatement statement, Object params, Object types)
      throws HibernateException, SQLException {
    int paramLength = TwoFactorServerUtils.length(params);
    int typeLength = TwoFactorServerUtils.length(types);
    
    //nothing to do if nothing to do
    if (paramLength == 0 && typeLength == 0) {
      return;
    }
  
    if (paramLength != typeLength) {
      throw new RuntimeException("The params length must equal the types length and params " +
      "and types must either both or neither be null");
    }
  
  
    List paramList = TwoFactorServerUtils.toList(params);
    List typeList = TwoFactorServerUtils.toList(types);
  
  
    //loop through, set the params
    Type currentType = null;
    for (int i = 0; i < paramLength; i++) {
      //not sure why the session implementer is null, if this ever fails for a type, 
      //might want to not use hibernate and brute force it
      currentType = (Type) typeList.get(i);
      currentType.nullSafeSet(statement, paramList.get(i), i + 1, null);
    }
  
  }
  
  
  
}
