/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.ui.beans;

import org.openTwoFactor.server.ws.rest.TfRestRequiredUser;


/**
 *
 */
public class TwoFactorWsRequestContainer {

  /**
   * required user
   */
  private TfRestRequiredUser tfRestRequiredUser;
  
  /**
   * @return the tfRestRequiredUser
   */
  public TfRestRequiredUser getTfRestRequiredUser() {
    return this.tfRestRequiredUser;
  }

  /**
   * @param tfRestRequiredUser1 the tfRestRequiredUser to set
   */
  public void setTfRestRequiredUser(TfRestRequiredUser tfRestRequiredUser1) {
    this.tfRestRequiredUser = tfRestRequiredUser1;
  }


  /**
   * 
   */
  public TwoFactorWsRequestContainer() {
  }

}
