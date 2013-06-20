
package org.openTwoFactor.server.hibernate;


/**
 * @version $Id: HibernateDelegate.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 * @author mchyzer
 */
class HibernateDelegate extends ByQueryBase {


  /**
   * 
   * @param theHibernateSession
   */
  public HibernateDelegate(HibernateSession theHibernateSession){
    this.set(theHibernateSession);
  }

}
