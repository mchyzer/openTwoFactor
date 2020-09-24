package org.openTwoFactor.server.beans;

import org.openTwoFactor.server.util.TwoFactorServerUtils;

/**
 * actions
 */
public enum TwoFactorAuditAction {
  
  /**
   * import fobs
   */
  IMPORT_FOB_SERIALS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsImportFobSerials";
    }

  }, 
  
  /**
   * add fob
   */
  FOB_ADD {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsFobAdd";
    }

  }, 
  
  /**
   * remove fob
   */
  FOB_REMOVE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsFobRemove";
    }

  }, 
  /**
   * wrong bday
   */
  WRONG_BIRTHDAY {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsWrongBirthday";
    }

  }, 
  /**
   * send a code to a phone
   */
  SEND_EMAIL_TO_ALL_USERS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsSendEmailToAllUsers";
    }
    
  },

  /**
   * send a code to a phone
   */
  TEST_SEND_EMAIL_TO_ALL_USERS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsSendTestEmailToAllUsers";
    }
    
  },

  /**
   * send a code to a phone
   */
  SEND_CODE_TO_PHONE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsSendCodeToPhone";
    }
    
  },

  /**
   * error
   */
  ERROR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsError";
    }
    
  },

  /**
   * assign the profile email address from subject
   */
  SET_PROFILE_EMAIL_FROM_SUBJECT {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAssignProfileEmailFromSubject";
    }
    
  },

  
  /**
   * error authenticating
   */
  AUTHN_ERROR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsErrorAuthenticating";
    }
    
  },
  
  /**
   * if a colleague opted me out
   */
  COLLEAGUE_OPTED_ME_OUT {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsFriendOptedMeOut";
    }
    
  },
  
  /**
   * error authenticating
   */
  OPTED_OUT_A_COLLEAGUE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptedOutFriend";
    }
    
  },
  
  /**
   * if a colleague generated a code
   */
  COLLEAGUE_GENERATED_ME_A_CODE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsFriendGeneratedMeCode";
    }
    
  },
  
  /**
   * error authenticating
   */
  GENERATED_CODE_FOR_A_COLLEAGUE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsGeneratedCodeForFriend";
    }
    
  },
  
  /**
   * if a lite admin generated a code
   */
  LITE_ADMIN_GENERATED_ME_A_CODE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsLiteAdminGeneratedMeCode";
    }
    
  },
  
  /**
   * error authenticating
   */
  GENERATED_CODE_AS_A_LITE_ADMIN {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsGeneratedCodeAsLiteAdmin";
    }
    
  },
  
  /**
   * enable push for web
   */
  DUO_ENABLE_PUSH_FOR_WEB {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsDuoEnablePushForWeb";
    }
    
  },
  
  /**
   * disable push for web
   */
  DUO_DISABLE_PUSH_FOR_WEB {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsDuoDisablePushForWeb";
    }
    
  },

  /**
   * enable duo push
   */
  DUO_ENABLE_PUSH {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsDuoPushEnable";
    }
    
  },
  
  /**
   * disable duo push
   */
  DUO_DISABLE_PUSH {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsDuoPushDisable";
    }
    
  },
  
  /**
   * successful test of duo push
   */
  DUO_ENABLE_PUSH_TEST_SUCCESS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsDuoPushEnableTestSuccess";
    }
    
  },

  /**
   * unsuccessful test of duo push
   */
  DUO_ENABLE_PUSH_TEST_FAILURE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsDuoPushEnableTestFailure";
    }
    
  },

  
  
  /**
   * view profile
   */
  VIEW_PROFILE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsViewProfile";
    }
    
  },
  
  /**
   * edit profile
   */
  EDIT_PROFILE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsEditProfile";
    }
    
  },
  
  /**
   * two factor authn
   */
  AUTHN_TWO_FACTOR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTwoStepAuthentication";
    }
    
  }, 
  
  /**
   * authned but with error
   */
  AUTHN_ALLOW_WITH_ERROR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAllowWithError";
    }
    
  }, 
  
  /**
   * one factor authn
   */
  AUTHN_TWO_FACTOR_FORBIDDEN {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTwoStepAuthenticationForbidden";
    }
    
  }, 
  
  /**
   * two factor authn is required
   */
  AUTHN_TWO_FACTOR_REQUIRED {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTwoStepAuthenticationRequired";
    }
    
  }, 
  
  /**
   * authn trusted browser no pass
   */
  AUTHN_TRUSTED_BROWSER {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsTrustedBrowserUse";
    }
    
  }, 
  
  /**
   * not opted in authn
   */
  AUTHN_NOT_OPTED_IN {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsNonTwoStepAuthentication";
    }
    
  }, 
  
  /**
   * optin to two factor
   */
  OPTIN_TWO_FACTOR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptIn";
    }
    
  }, 
  
  /**
   * optin to two factor submit birthday
   */
  OPTIN_SUBMIT_BIRTHDAY {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptInSubmitBirthday";
    }
    
  }, 
  
  /**
   * optin to two factor submit email
   */
  OPTIN_SUBMIT_EMAIL {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptInSubmitEmail";
    }
    
  }, 
  
  /**
   * optin to two factor submit type
   */
  OPTIN_SUBMIT_TYPE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptInSubmitType";
    }
    
  }, 
  
  /**
   * stop opt in requirement temporarily
   */
  STOP_OPT_IN_REQUIREMENT {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsStopOptInRequirement";
    }
    
  }, 
  
  /**
   * optin to two factor registering a fob serial number
   */
  REGISTER_FOB_SERIAL_NUMBER {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsRegisterFobBySerial";
    }
    
  }, 
  
  /**
   * test adding phone
   */
  ADD_PHONE_TEST {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAddPhoneTest";
    }

  }, 
  
  /**
   * add totp
   */
  ADD_TOTP {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAddTotp";
    }
    
  }, 

  /**
   * change totp
   */
  CHANGE_TOTP {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsChangeTotp";
    }
    
  }, 
  

  /**
   * add phone
   */
  ADD_PHONE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAddPhone";
    }
    
  }, 
  
  /**
   * optin to two factor
   */
  INVITE_COLLEAGUES {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsInviteFriendsToOptMeOut";
    }
    
  }, 
  
  /**
   * optin to two factor first part before verified
   */
  OPTIN_TWO_FACTOR_STEP1 {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptInStep1";
    }
    
  }, 
  
  /**
   * opt out of two factor
   */
  OPTOUT_TWO_FACTOR {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptOut";
    }
    
  }, 

  /**
   * edit report
   */
  ADMIN_REPORT_EDIT {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAdminReportEdit";
    }
    
  }, 
  
  /**
   * confirm user
   */
  ADMIN_CONFIRM_USER {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAdminConfirmUser";
    }
    
  }, 
  
  /**
   * confirm user
   */
  ADMIN24_CONFIRM_USER {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAdmin24ConfirmUser";
    }
    
  }, 
  
  /**
   * admin generate code for user
   */
  ADMIN_GENERATE_CODE {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAdminGenerateCode";
    }
    
  },

  /**
   * remove friend
   */
  REMOVE_FRIEND {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsRemoveFriend";
    }
    
  },


  /**
   * view report
   */
  REPORT_VIEW {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsReportView";
    }
    
  }, 
  

  /**
   * untrust browsers
   */
  UNTRUST_BROWSERS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsUntrustBrowsers";
    }
    
  }, 
  
  /**
   * auto call text enroll
   */
  AUTO_CALL_TEXT_ENROLL {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAutoCallTextEnroll";
    }
    
  }, 

  
  /**
   * auto call text unenroll
   */
  AUTO_CALL_TEXT_UNENROLL {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsAutoCallTextUnenroll";
    }
    
  }, 

  /**
   * authn wrong password
   */
  AUTHN_WRONG_PASSWORD {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsWrongCode";
    }
    
  }, 
  
  /**
   * invalidate passwords
   */
  INVALIDATE_PASSWORDS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsInvalidateSingleUseCodes";
    }
    
  }, 
  
  /**
   * generated passwords (and invalidate old ones)
   */
  GENERATE_PASSWORDS {

    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsGenerateSingleUseCodes";
    }
    
  }, 
  
  /**
   * optin to two factor submit phones
   */
  OPTIN_SUBMIT_PHONES {
  
    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptInSubmitPhones";
    }
    
  }, 
  
  /**
   * optin to two factor submit friends
   */
  OPTIN_SUBMIT_FRIENDS{
  
    /**
     * 
     * @see org.openTwoFactor.server.beans.TwoFactorAuditAction#toStringForUi()
     */
    @Override
    public String toStringForUi() {
      return "auditsOptInSubmitFriends";
    }
    
  };
  
  /**
   * get screen action
   * @return the screen action
   */
  public abstract String toStringForUi();
  
  /**
   * take a string and convert to enum
   * @param string
   * @return the enum
   */
  public static TwoFactorAuditAction valueOfIgnoreCase(String string) {
    return TwoFactorServerUtils.enumValueOfIgnoreCase(TwoFactorAuditAction.class, string, false, true);
  }

}