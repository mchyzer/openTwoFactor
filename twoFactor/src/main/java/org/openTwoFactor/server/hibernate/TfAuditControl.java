
/*
 * @author mchyzer
 * $Id: TfAuditControl.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate;


/**
 *
 */
public enum TfAuditControl {

  /** will audit this call (or will defer to outside context if auditing */
  WILL_AUDIT, 
  
  /** will not audit */
  WILL_NOT_AUDIT;
  
}
