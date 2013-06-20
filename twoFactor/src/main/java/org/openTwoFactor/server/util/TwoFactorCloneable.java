/**
 * @author mchyzer
 * $Id: TwoFactorCloneable.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;


/**
 *
 */
public interface TwoFactorCloneable extends Cloneable {

  /**
   * @see Object#clone()
   * public clone method
   * @return a clone of this object
   */
  public Object clone();
  
}
