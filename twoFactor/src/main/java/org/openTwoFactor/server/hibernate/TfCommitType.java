package org.openTwoFactor.server.hibernate;

import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * Various types of committing
 * @author mchyzer
 *
 */
public enum TfCommitType {
  /** always commit right now */
  COMMIT_NOW,
  
  /** only commit if this is a new transaction */
  COMMIT_IF_NEW_TRANSACTION;
  
  /**
   * do a case-insensitive matching
   * 
   * @param string
   * @return the enum or null or exception if not found
   */
  public static TfCommitType valueOfIgnoreCase(String string) {
    return TwoFactorServerUtils.enumValueOfIgnoreCase(TfCommitType.class,string, false );
  }

}
