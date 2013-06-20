
package org.openTwoFactor.server.hibernate;

import org.openTwoFactor.server.exceptions.TfDaoException;

/**
 * Use this class to make your anonymous inner class for 
 * transactions with (if transactions are supported by your DAO strategy
 * configured in properties files
 * 
 * 
 * @author mchyzer
 *
 */
public interface TwoFactorTransactionHandler {
  
  /**
   * This method will be called with the transaction object to do 
   * what you wish.  Note, RuntimeExceptions can be
   * thrown by this method... others should be handled somehow.
   * @param transaction is the transaction
   * @return the return value to be passed to return value of callback method
   * @throws TfDaoException if there is a problem, or runtime ones
   */
  public Object callback(TwoFactorTransaction transaction) throws TfDaoException;

}
