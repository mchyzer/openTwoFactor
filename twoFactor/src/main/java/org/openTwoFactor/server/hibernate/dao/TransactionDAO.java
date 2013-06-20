package org.openTwoFactor.server.hibernate.dao;

import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.TfCommitType;
import org.openTwoFactor.server.hibernate.TfRollbackType;
import org.openTwoFactor.server.hibernate.TwoFactorTransaction;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionHandler;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;

/** 
 * methods for dealing with transactions
 * @author mchyzer
 *
 */
public interface TransactionDAO {
  /**
   * call this to send a callback for the methods.  This shouldnt be called directly,
   * it should filter through the TwoFactorTransaction.callback... method
   * 
   * @param transactionType
   *          is enum of how the transaction should work.
   * @param transactionHandler
   *          will get the callback
   *          
   * @param transaction is the state of the transaction, can hold payload
   * @return the object returned from the callback
   * @throws TfDaoException if something wrong inside, its
   * whatever your methods throw
   */
  public abstract Object transactionCallback(
      TwoFactorTransactionType transactionType,
      TwoFactorTransactionHandler transactionHandler,
      TwoFactorTransaction transaction) throws TfDaoException;

  /**
   * call this to commit a transaction
   * 
   * @param commitType
   *          type of commit (now or only under certain circumstances?)
   * @param transaction is the state of the transaction, can hold payload
   * @return if committed
   */
  public boolean transactionCommit(
      TwoFactorTransaction transaction, TfCommitType commitType);

  /**
   * call this to rollback a transaction
   * 
   * @param rollbackType
   *          type of commit (now or only under certain circumstances?)
   * @param transaction is the state of the transaction, can hold payload
   * @return if rolled back
   */
  public boolean transactionRollback(
      TwoFactorTransaction transaction, TfRollbackType rollbackType);

  /**
   * call this to see if a transaction is active (exists and not committed or rolledback)
   * 
   * @param transaction is the state of the transaction, can hold payload
   * @return the object returned from the callback
   */
  public abstract boolean transactionActive(
      TwoFactorTransaction transaction);

}
