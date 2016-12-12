/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.ws.rest;


/**
 *
 */
public class TfRestRequiredUser {

  /**
   * 
   */
  public TfRestRequiredUser() {
  }

  /**
   * @param loginid1
   * @param name1
   * @param email1
   */
  public TfRestRequiredUser(String loginid1, String name1, String email1) {
    super();
    this.loginid = loginid1;
    this.name = name1;
    this.email = email1;
  }

  /**
   * 
   */
  private String loginid;
  
  /**
   * 
   */
  private String name;
  
  /**
   * 
   */
  private String email;

  
  /**
   * @return the loginid
   */
  public String getLoginid() {
    return this.loginid;
  }

  
  /**
   * @param loginid1 the loginid to set
   */
  public void setLoginid(String loginid1) {
    this.loginid = loginid1;
  }

  
  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  
  /**
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  
  /**
   * @return the email
   */
  public String getEmail() {
    return this.email;
  }

  
  /**
   * @param email1 the email to set
   */
  public void setEmail(String email1) {
    this.email = email1;
  }
  
  
  
}
