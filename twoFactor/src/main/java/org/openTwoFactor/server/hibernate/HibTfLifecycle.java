/*
 * @author mchyzer
 * $Id: HibTfLifecycle.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate;


/**
 * callbacks for hib lifecycle events
 */
public interface HibTfLifecycle {

  /**
   * after an update occurs
   * @param hibernateSession 
   */
  public void onPostUpdate(HibernateSession hibernateSession);
  
  /**
   * after a save (insert) occurs
   * @param hibernateSession 
   */
  public void onPostSave(HibernateSession hibernateSession);
  
  /**
   * before an update occurs
   * @param hibernateSession 
   */
  public void onPreUpdate(HibernateSession hibernateSession);
  
  /**
   * before a save (insert) occurs
   * @param hibernateSession 
   */
  public void onPreSave(HibernateSession hibernateSession);

  /**
   * after a delete occurs
   * @param hibernateSession 
   */
  public void onPostDelete(HibernateSession hibernateSession);

  /**
   * before a delete (insert) occurs
   * @param hibernateSession 
   */
  public void onPreDelete(HibernateSession hibernateSession);
  
}
