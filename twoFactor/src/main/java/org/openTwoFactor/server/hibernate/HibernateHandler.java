
package org.openTwoFactor.server.hibernate;

import org.openTwoFactor.server.exceptions.TfDaoException;


/**
 * Implement this (usually in an anonymous inner class) to get a 
 * reference to the hibernate session object
 * @version $Id: HibernateHandler.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 * @author mchyzer
 */

public interface HibernateHandler {

  /**
   * This method will be called with the hibernate session object to do 
   * what you wish.  Note, HibernateException or RuntimeExceptions can be
   * thrown by this method... others should be handled somehow..
   * @param hibernateHandlerBean holds the hibernate session and other things 
   * will be the same as passed in if it existed, else a new one
   * @return the return value to be passed to return value of callback method
   * @throws TfDaoException if there is a problem
   */
  public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException;
}
