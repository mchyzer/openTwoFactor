package org.openTwoFactor.server.hibernate;

import org.openTwoFactor.server.util.TwoFactorServerUtils;

/**
 * Various types of rolling back
 * @author mchyzer
 *
 */
public enum TfRollbackType {
  /** always rollback right now */
  ROLLBACK_NOW,
  
  /** only rollback if this is a new transaction */
  ROLLBACK_IF_NEW_TRANSACTION;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static TfRollbackType valueOfIgnoreCase(String string) {
    return TwoFactorServerUtils.enumValueOfIgnoreCase(TfRollbackType.class,string, false );
  }

}
