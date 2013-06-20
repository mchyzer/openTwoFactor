/**
 * 
 */
package org.openTwoFactor.client.corebeans;

import org.openTwoFactor.client.util.TwoFactorClientUtils;



/**
 * response from two factor check password
 * @author mchyzer
 *
 */
public class TfCheckPasswordResponse extends TwoFactorResponseBeanBase {

  /**
   * @param args
   */
  public static void main(String[] args) {

  }

  /**
   * this is the uuid that should be assigned to the browser cookie
   */
  private String changeUserBrowserUuid;
  
  /**
   * this is the uuid that should be assigned to the browser cookie
   * @return the changeUserBrowserUuid
   */
  public String getChangeUserBrowserUuid() {
    return this.changeUserBrowserUuid;
  }
  
  /**
   * this is the uuid that should be assigned to the browser cookie
   * @param changeUserUuid1 the changeUserBrowserUuid to set
   */
  public void setChangeUserBrowserUuid(String changeUserUuid1) {
    this.changeUserBrowserUuid = changeUserUuid1;
  }

  /**
   * true if the cookie in the browser should be changed.  
   * This happens if the uuid is invalid or blank, or assigned to another user
   */
  private Boolean userBrowserUuidIsNew;
  
  /**
   * true if the cookie in the browser should be changed.  
   * @return the userBrowserUuidIsNew
   */
  public Boolean getUserBrowserUuidIsNew() {
    return this.userBrowserUuidIsNew;
  }
  
  /**
   * userEnrolledInTwoFactor: true if the user opted in or is otherwise enrolled
   */
  private Boolean userEnrolledInTwoFactor;
  
  /**
   * true if the user opted in or is otherwise enrolled
   * @return the userEnrolledInTwoFactor
   */
  public Boolean getUserEnrolledInTwoFactor() {
    return this.userEnrolledInTwoFactor;
  }
  
  /**
   * true if the user opted in or is otherwise enrolled
   * @param userEnrolledInTwoFactor1 the userEnrolledInTwoFactor to set
   */
  public void setUserEnrolledInTwoFactor(Boolean userEnrolledInTwoFactor1) {
    this.userEnrolledInTwoFactor = userEnrolledInTwoFactor1;
  }

  /**
   * true if the cookie in the browser should be changed.  
   * @param userUuidIsNew1 the userBrowserUuidIsNew to set
   */
  public void setUserBrowserUuidIsNew(Boolean userUuidIsNew1) {
    this.userBrowserUuidIsNew = userUuidIsNew1;
  }

  /**
   * timestamp of the last time the user has trusted this browser.  
   * Either blank (if not trusted or authentiating), or when the browser was last trusted
   */
  private String whenTrusted;
  
  /**
   * timestamp of the last time the user has trusted this browser.  
   * Either blank (if not trusted or authentiating), or when the browser was last trusted
   * @return the whenTrusted
   */
  public String getWhenTrusted() {
    return this.whenTrusted;
  }
  
  /**
   * timestamp of the last time the user has trusted this browser.  
   * Either blank (if not trusted or authentiating), or when the browser was last trusted
   * @param lastTwoFactorLogin1 the whenTrusted to set
   */
  public void setWhenTrusted(String lastTwoFactorLogin1) {
    this.whenTrusted = lastTwoFactorLogin1;
  }

  /**
   * true if the service requires two factor or if the user is opted in
   */
  private Boolean twoFactorRequired;
  
  /**
   * true if the service requires two factor or if the user is opted in
   * @return the twoFactorRequired
   */
  public Boolean getTwoFactorRequired() {
    return this.twoFactorRequired;
  }
  
  /**
   * true if the service requires two factor or if the user is opted in
   * @param twoFactorRequired1 the twoFactorRequired to set
   */
  public void setTwoFactorRequired(Boolean twoFactorRequired1) {
    this.twoFactorRequired = twoFactorRequired1;
  }

  /**
   * this is the Boolean that should be checked by the calling service, 
   * true if the user is allowed to continue authentication, false if not.  
   * Note, if the two factor pass is correct, or it is a trusted browser, 
   * or if two factor is not required, this will be true.
   */
  private Boolean twoFactorUserAllowed;

  /**
   * this is the Boolean that should be checked by the calling service, 
   * true if the user is allowed to continue authentication, false if not.  
   * Note, if the two factor pass is correct, or it is a trusted browser, 
   * or if two factor is not required, this will be true.
   * @return the twoFactorUserAllowed
   */
  public Boolean getTwoFactorUserAllowed() {
    return this.twoFactorUserAllowed;
  }

  
  /**
   * this is the Boolean that should be checked by the calling service, 
   * true if the user is allowed to continue authentication, false if not.  
   * Note, if the two factor pass is correct, or it is a trusted browser, 
   * or if two factor is not required, this will be true.
   * @param twoFactorUserAllowed1 the twoFactorUserAllowed to set
   */
  public void setTwoFactorUserAllowed(Boolean twoFactorUserAllowed1) {
    this.twoFactorUserAllowed = twoFactorUserAllowed1;
  }

  /**
   * true if the password was passed in, and if the user 
   * is enrolled, and the password is a correct password
   */
  private Boolean twoFactorPasswordCorrect;

  /**
   * true if the password was passed in, and if the user 
   * is enrolled, and the password is a correct password
   * @return if correct
   */
  public Boolean getTwoFactorPasswordCorrect() {
    return this.twoFactorPasswordCorrect;
  }

  /**
   * true if the password was passed in, and if the user 
   * is enrolled, and the password is a correct password
   * @param twoFactorPasswordCorrect1
   */
  public void setTwoFactorPasswordCorrect(Boolean twoFactorPasswordCorrect1) {
    this.twoFactorPasswordCorrect = twoFactorPasswordCorrect1;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {

    StringBuilder result = new StringBuilder();
    
    if (!TwoFactorClientUtils.isBlank(this.changeUserBrowserUuid)) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "changeUserBrowserUuid=" + TwoFactorClientUtils.abbreviate(this.changeUserBrowserUuid, 6), null);
    }
    if (!TwoFactorClientUtils.isBlank(this.getDebugMessage())) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "debugMessge=" + this.getDebugMessage(), null);
    }
    if (!TwoFactorClientUtils.isBlank(this.getErrorMessage())) {

      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "errorMessage=" + this.getErrorMessage(), null);
    }
    if (!TwoFactorClientUtils.isBlank(this.getResponseMessage())) {
  
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "responseMessage=" + this.getResponseMessage(), null);
    }
    if (!TwoFactorClientUtils.isBlank(this.getResultCode())) {
  
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "resultCode=" + this.getResultCode(), null);
    }

    if (this.getSuccess() != null) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "success=" + this.getSuccess().toString(), null);
    }
    
    if (this.getTwoFactorPasswordCorrect() != null) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "twoFactorPasswordCorrect=" + this.getTwoFactorPasswordCorrect().toString(), null);
    }
    
    if (!TwoFactorClientUtils.isBlank(this.getWarning())) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "warning=" + this.getWarning(), null);
    }

    if (this.getTwoFactorRequired() != null) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "twoFactorRequired=" + this.getTwoFactorRequired().toString(), null);
    }

    if (this.getTwoFactorUserAllowed() != null) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "twoFactorUserAllowed=" + this.getTwoFactorUserAllowed().toString(), null);
    }
    
    if (this.getUserBrowserUuidIsNew() != null) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "userBrowserUuidIsNew=" + this.getUserBrowserUuidIsNew().toString(), null);
    }
    
    if (this.getUserEnrolledInTwoFactor() != null) {
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "userEnrolledInTwoFactor=" + this.getUserEnrolledInTwoFactor().toString(), null);
    }
    
    if (this.getWhenTrusted() != null) {
    
      TwoFactorClientUtils.appendIfNotBlank(result, null, ", ", 
          "whenTrusted=" + this.getWhenTrusted(), null);
    }    
    return result.toString();
  }


}
