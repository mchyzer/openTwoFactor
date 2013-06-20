/* 
compile with:
 gcc -fPIC  -DPIC -shared -rdynamic -o pam_twostep.so pam_twostep.c

install with (or wherever your pams are):
 # cp pam_twostep.so /lib64/security/

configure with:
 # emacs /etc/pam.d/sshd
 add this line to the top:

auth       requisite         pam_twostep.so

if you want debug info (to /var/log/secure), use:

auth       requisite         pam_twostep.so debug


*/

/* Define which PAM interfaces we provide */
  #define PAM_SM_ACCOUNT
  #define PAM_SM_AUTH
  #define PAM_SM_PASSWORD
  #define PAM_SM_SESSION

#include <sys/param.h>
#include <sys/types.h>
#include <sys/wait.h>

#include <pwd.h>
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>


/* Include PAM headers */
  #include <security/pam_appl.h>
  #include <security/pam_modules.h>



/* PAM entry point for session creation */
int pam_sm_open_session(pam_handle_t *pamh, int flags, int argc, const char **argv) {
  return(PAM_IGNORE);
}

/* PAM entry point for session cleanup */
int pam_sm_close_session(pam_handle_t *pamh, int flags, int argc, const char **argv) {
  return(PAM_IGNORE);
}

/* PAM entry point for accounting */
int pam_sm_acct_mgmt(pam_handle_t *pamh, int flags, int argc, const char **argv) {
  return(PAM_IGNORE);
}

/*
man 5 passwd: The login name may be up to 31 characters long. For compatibility 
with legacy software, a login name should start with a letter and consist 
solely of letters, numbers, dashes and underscores. The login name 
must never begin with a hyphen (`-'); also, it is strongly suggested that 
neither uppercase characters nor dots (`.') be part of the name, as this 
tends to confuse mailers. 
 */
int validate_username(const char *input) {
  if (input == NULL || strlen(input) == 0) {
    return 0;
  }

  //username must start with alpha
  if (!isalpha(input[0])) {
    return 0;
  }
  int i;
  for (i=0;i<strlen(input);i++) {
    if (!isalpha(input[i]) && !isdigit(input[i]) && input[i] != '-' && input[i] != '_') {
      return 0;
    }
  }
  return 1;
}

/*
6 digit numeric pass
 */
int validate_twosteppass(const char *input) {
  if (input == NULL || strlen(input) != 6) {
    return 0;
  }

  int i;
  for (i=0;i<strlen(input);i++) {
    if (!isdigit(input[i])) {
      return 0;
    }
  }
  return 1;
}

/* PAM entry point for authentication verification */
int pam_sm_authenticate(pam_handle_t *pamh, int flags, int argc, const char **argv) {

  const char *user;

  int pam_err;

  // somepass::123456
  char *originalPassword;
  
  char log[1000];
  log[0] = '\0';
  
  int has_pam_err = 0;

  /* identify user */
  if ((pam_err = pam_get_user(pamh, &user, NULL)) != PAM_SUCCESS) {
    has_pam_err = 1;
    strcpy(log, "Trouble getting user");
  }

  if (has_pam_err == 0 && strlen(user) > 100) {
    pam_err = PAM_SYSTEM_ERR;
    has_pam_err = 1;
    strcpy(log, "User length too long");
  }

  //dont know why this wouldnt validate, but just in case
  if (has_pam_err == 0 && !validate_username(user)) {
   
    pam_err = PAM_SYSTEM_ERR;
    has_pam_err = 1;
    strcpy(log, "Invalid username");
  }

  char truncatedPassword[1024];
  char twoStepPassword[7];
  
  //get the password into originalPassword
  if (has_pam_err == 0 && ((pam_err = pam_get_authtok(pamh, PAM_AUTHTOK,
               (const char **)&originalPassword, NULL)) != PAM_SUCCESS)) {
    has_pam_err = 1;   
    strcpy(log, "Cannot get password");
  }

  //if blank, then SSH shared secret?  I dont know, fail
  if (originalPassword == NULL || strlen(originalPassword) > 100) {
   
    pam_err = PAM_SYSTEM_ERR;
    has_pam_err = 1;
    strcpy(log, "Password blank");
  }

  int originalLength = 0;
  //at first, assume blank
  twoStepPassword[0] = '\0';
  truncatedPassword[0] = '\0';

  if (has_pam_err == 0) {
    originalLength = strlen(originalPassword);    
    //at first, assume truncated pass is just the original pass
    strncpy(truncatedPassword, originalPassword, originalLength);
    truncatedPassword[originalLength] = '\0';
  }
  if (has_pam_err == 0 && strlen(originalPassword) > 9) {
    
    //see if this is the right password format: somepass::123456    
    if (originalPassword[originalLength-8] == ':' && originalPassword[originalLength-7] == ':'
        && (isdigit(originalPassword[originalLength-6]))
        && (isdigit(originalPassword[originalLength-5]))
        && (isdigit(originalPassword[originalLength-4]))
        && (isdigit(originalPassword[originalLength-3]))
        && (isdigit(originalPassword[originalLength-2]))
        && (isdigit(originalPassword[originalLength-1]))
    ) {
      strncpy(twoStepPassword, &originalPassword[originalLength-6], 6);
      //end string
      twoStepPassword[6] = '\0';
      strncpy(truncatedPassword, originalPassword, originalLength-8);
      //end string
      truncatedPassword[originalLength-8] = '\0';

      //set the prefix of the pass back to pam
      if ((pam_err = pam_set_item(pamh, PAM_AUTHTOK, truncatedPassword)) != PAM_SUCCESS) {
   
        has_pam_err = 1;
        strcpy(log, "Cannot set password");
      }
    }
  }
  
  //lets see if the user is enrolled in two factor
  char command[1024];
  if (has_pam_err == 0) {

    // /home/mchyzer/twoFactor/twoFactorPam.sh
    strcpy (command,"/usr/local/sbin/twoFactorPam.sh ");

    //not sure why this wouldnt be numeric... but check just in case
    if (twoStepPassword != NULL && strlen(twoStepPassword) > 0 && !validate_twosteppass(twoStepPassword)) {
   
      pam_err = PAM_SYSTEM_ERR;
      has_pam_err = 1;
      strcpy(log, "Invalid password chars (should never happen)");
    }
  }
  int ret = -1;
  if (has_pam_err == 0) {
    strcat (command,user);
    strcat (command," ");

    strcat (command,twoStepPassword);
    ret = WEXITSTATUS(system(command));
  }
  
  // ##########################  
  // UNCOMMENT THIS TO GET SOME DEBUG INFO, NOTE, PASSWORDS ARE LOGGED HERE!
  //  FILE * debug;
  //debug = fopen("/tmp/pam_twostep.debug", "a");

  //fprintf(debug, "pam_twostep: uid=%d, euid=%d, user=%s, truncatedPassword=%s, twoStep=%s, ret=%d, has_pam_err=%d, pam_err=%d, log=%s\n", getuid(), geteuid(), user, truncatedPassword, twoStepPassword, ret, has_pam_err, pam_err, log );
  //fflush(debug);
  // ##########################
  
  //if password is correct or if the two factor system is having trouble
  if (has_pam_err == 0 && (ret == 0 || ret == 2)) {
    return(PAM_IGNORE);
  }
  
  if (has_pam_err != 0 && pam_err != PAM_IGNORE && pam_err != PAM_SUCCESS) { 
    return pam_err;
  }

  //ret wasnt 0 (ok), or 2 (two-factor system has problems)
  return(PAM_SYSTEM_ERR);
}

/*
     PAM entry point for setting user credentials (that is, to actually
     establish the authenticated user's credentials to the service provider)
*/
int pam_sm_setcred(pam_handle_t *pamh, int flags, int argc, const char **argv) {
  return(PAM_IGNORE);
}

/* PAM entry point for authentication token (password) changes */
int pam_sm_chauthtok(pam_handle_t *pamh, int flags, int argc, const char **argv) {
  return(PAM_IGNORE);
}