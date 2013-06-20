
/**
 * 
 */
package org.openTwoFactor.server.hibernate.dao;

import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TfCommitType;
import org.openTwoFactor.server.hibernate.TfRollbackType;
import org.openTwoFactor.server.hibernate.TwoFactorTransaction;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionHandler;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;


/**
 * @author mchyzer
 *
 */
public class Hib3TransactionDAO implements TransactionDAO {
  
  /**
   * @see TransactionDAO#transactionActive(TwoFactorTransaction)
   */
  public boolean transactionActive(TwoFactorTransaction transaction) {
    HibernateSession hibernateSession = (HibernateSession)transaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.isTransactionActive();
  }

  /**
   * any runtime exceptions will propagate to the outer method call
   * @see TransactionDAO#transactionCallback(TwoFactorTransactionType, TwoFactorTransactionHandler, TwoFactorTransaction)
   */
  public Object transactionCallback(
      final TwoFactorTransactionType transactionType,
      final TwoFactorTransactionHandler transactionHandler,
      final TwoFactorTransaction transaction) throws TfDaoException {
    
    Object result = HibernateSession.callbackHibernateSession(
        transactionType, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws TfDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

        //set the session object
        transaction._internal_setPayload(hibernateSession);
        try {
          return transactionHandler.callback(transaction);
        } finally {
          //clear this
          transaction._internal_setPayload(null);
        }
      }
      
    });
    
    return result;
  }

  /** 
   * @see TransactionDAO#transactionCommit(TwoFactorTransaction, TfCommitType)
   */
  public boolean transactionCommit(TwoFactorTransaction transaction,
      TfCommitType commitType) {
    HibernateSession hibernateSession = (HibernateSession)transaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.commit(commitType);
  }

  /**
   * @see TransactionDAO#transactionRollback(TwoFactorTransaction, TfRollbackType)
   */
  public boolean transactionRollback(TwoFactorTransaction transaction,
      TfRollbackType rollbackType) {
    HibernateSession hibernateSession = (HibernateSession)transaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.rollback(rollbackType);
  }

}
