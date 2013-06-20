
package org.openTwoFactor.server.hibernate;




/**
 * @version $Id: HibernateMisc.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 * @author mchyzer
 */
public class HibernateMisc extends HibernateDelegate {

  /**
   * @param theHibernateSession
   */
  public HibernateMisc(HibernateSession theHibernateSession) {
    super(theHibernateSession);
  }

  /**
   * Flush the underlying hibernate session (sync the object model with the DB).
   * This doesnt commit or anything, it just sends the bySql across
   */
  public void flush() {
    
    HibernateSession.assertNotReadonly();
    
    this.getHibernateSession().getSession().flush();
  }

}
