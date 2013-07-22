/**
 * @author mchyzer
 * $Id: UiMainAdmin.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.email.TwoFactorEmail;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.ui.UiServiceLogicBase;
import org.openTwoFactor.server.ui.beans.TextContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorAdminContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.config.TwoFactorTextConfig;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * admin methods for two factor
 */
public class UiMainAdmin extends UiServiceLogicBase {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(UiMainAdmin.class);


  /**
   * admin page combobox
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void personPicker(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    UiMain.personPickerHelper(httpServletRequest, httpServletResponse, true, TfSourceUtils.mainSource(), true);
  }

  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void adminIndex(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
    
    showJsp("admin.jsp");
  }

  /**
   * opt out a user from the admin console
   * @param httpServletRequest
   * @param httServletResponse
   */
  public void optOutSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String userIdOperatingOn = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("userIdOperatingOn");

    Source subjectSource = TfSourceUtils.mainSource();

    AdminSubmitView adminSubmitView = optOutSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource);

    showJsp(adminSubmitView.getJsp());
    
  }
  
  /**
   * opt out a user
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param userIdOperatingOn 
   * @param subjectSource
   * @return page to go to
   */
  public AdminSubmitView optOutSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource) {

    final boolean[] success = new boolean[]{false};

    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];
    
    final TwoFactorUser[] twoFactorUserGettingOptedOut = new TwoFactorUser[1];

    AdminSubmitView adminSubmitView = (AdminSubmitView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
        
        twoFactorUserUsingApp[0] = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUserUsingApp[0].setSubjectSource(subjectSource);
        
        //make sure user is an admin
        if (!twoFactorUserUsingApp[0].isAdmin()) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
          return AdminSubmitView.index;
        }

        twoFactorAdminContainer.setUserIdOperatingOn(userIdOperatingOn);

        //make sure searching for a user id
        if (StringUtils.isBlank(userIdOperatingOn)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnIsRequired"));
          return AdminSubmitView.admin;
        }

        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            userIdOperatingOn, true, false, true);

        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        twoFactorUserGettingOptedOut[0] = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserGettingOptedOut[0] == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return AdminSubmitView.admin;

          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return AdminSubmitView.admin;
        }
        
        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserGettingOptedOut[0]);
 
        twoFactorUserGettingOptedOut[0].setTwoFactorSecretTemp(null);
        twoFactorUserGettingOptedOut[0].setSubjectSource(subjectSource);
        
        if (StringUtils.isBlank(twoFactorUserGettingOptedOut[0].getTwoFactorSecret())) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserWasNotOptedIn"));
          
        } else {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminOptOutUserSuccess"));

        }
        
        twoFactorUserGettingOptedOut[0].setTwoFactorSecret(null);
        
        twoFactorUserGettingOptedOut[0].setOptedIn(false);
        twoFactorUserGettingOptedOut[0].setSequentialPassIndex(1L);

        twoFactorUserGettingOptedOut[0].store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTOUT_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUserGettingOptedOut[0].getUuid(),
            twoFactorUserUsingApp[0].getUuid(), null, null);
        
        success[0] = true;
        
        return AdminSubmitView.admin;
      }
    });
    
    //send emails if successful
    String userEmailLoggedIn = null;
    String userEmailColleague = null;
    try {
      
      twoFactorUserUsingApp[0].setSubjectSource(subjectSource);
      twoFactorUserGettingOptedOut[0].setSubjectSource(subjectSource);
      
      //see if there
      
      //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
      if (success[0] && subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorTextConfig.retrieveText(null).propertyValueBoolean("mail.sendForOptoutByAdmin", true)) {
        
        Subject sourceSubjectLoggedIn = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        Subject sourceSubjectPersonPicked = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            twoFactorUserGettingOptedOut[0].getLoginid(), true, false, true);
        
        String emailAddressFromSubjectLoggedIn = TfSourceUtils.retrieveEmail(sourceSubjectLoggedIn);
        String emailAddressFromDatabaseLoggedIn = twoFactorUserUsingApp[0].getEmail0();

        String emailAddressFromSubjectPersonPicked = TfSourceUtils.retrieveEmail(sourceSubjectPersonPicked);
        String emailAddressFromDatabasePersonPicked = twoFactorUserGettingOptedOut[0].getEmail0();

        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptOutByAdminSubject");
        subject = TextContainer.massageText("emailOptOutByAdminSubject", subject);

        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptOutByAdminBody");
        body = TextContainer.massageText("emailOptOutByAdminBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.adminOptouts");
        
        TwoFactorEmail twoFactorMail = new TwoFactorEmail();
        
        if (StringUtils.equalsIgnoreCase(emailAddressFromSubjectLoggedIn, emailAddressFromDatabaseLoggedIn)) {
          emailAddressFromDatabaseLoggedIn = null;
        }
        
        userEmailLoggedIn = emailAddressFromSubjectLoggedIn + ", " + emailAddressFromDatabaseLoggedIn;
        
        if (StringUtils.equalsIgnoreCase(emailAddressFromSubjectPersonPicked, emailAddressFromDatabasePersonPicked)) {
          emailAddressFromDatabasePersonPicked = null;
        }
        
        userEmailColleague = emailAddressFromSubjectPersonPicked + ", " + emailAddressFromDatabasePersonPicked;
        
        boolean sendEmail = true;
        boolean sendToFriend = true;
        //there is no email address????
        if (StringUtils.isBlank(emailAddressFromSubjectPersonPicked) && StringUtils.isBlank(emailAddressFromDatabasePersonPicked)) {
          sendToFriend = false;
          LOG.warn("Did not send email to logged in user: " + userEmailColleague + ", no email address...");
          if (StringUtils.isBlank(bccsString)) {
            sendEmail = false;
          } else {
            twoFactorMail.addTo(bccsString);
          }
        } else {
          twoFactorMail.addTo(emailAddressFromSubjectPersonPicked).addTo(emailAddressFromDatabasePersonPicked);
          twoFactorMail.addBcc(bccsString);
        }
        
        if (sendToFriend && StringUtils.isBlank(emailAddressFromSubjectLoggedIn) && StringUtils.isBlank(emailAddressFromDatabaseLoggedIn)) {
          LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
        } else {
          twoFactorMail.addCc(emailAddressFromSubjectLoggedIn).addTo(emailAddressFromDatabaseLoggedIn);
        }
        
        if (sendEmail) {
          twoFactorMail.assignBody(body);
          twoFactorMail.assignSubject(subject);
          twoFactorMail.send();
        }
        
      }
      
    } catch (Exception e) {
      //non fatal, just log this
      LOG.error("Error sending email to: " + userEmailColleague + ", (logged in): " + userEmailLoggedIn + ", loggedInUser id: " + loggedInUser, e);
    }
    
    
    return adminSubmitView;
    
  }

  /**
   * submit a pennkey
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void userIdSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String userIdOperatingOn = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("userIdOperatingOnName");

    Source subjectSource = TfSourceUtils.mainSource();
    
    AdminSubmitView adminSubmitView = userIdSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource);

    showJsp(adminSubmitView.getJsp());

  }
  
  /**
   * submit a user id
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param userIdOperatingOn 
   * @param subjectSource 
   * @return page to go to
   */
  public AdminSubmitView userIdSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource) {

    return (AdminSubmitView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
        
        TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUserLoggedIn.setSubjectSource(subjectSource);
        
        //make sure user is an admin
        if (!twoFactorUserLoggedIn.isAdmin()) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
          return AdminSubmitView.index;
        }

        twoFactorAdminContainer.setUserIdOperatingOn(userIdOperatingOn);
        
        //make sure searching for a user id
        if (StringUtils.isBlank(userIdOperatingOn)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnIsRequired"));
          return AdminSubmitView.admin;
        }

        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            userIdOperatingOn, true, false, true);
        
        twoFactorAdminContainer.setSubjectOperatingOn(subject);
        
        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        TwoFactorUser twoFactorUserOperatingOn = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserOperatingOn == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return AdminSubmitView.admin;

          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return AdminSubmitView.admin;
        }
        
        twoFactorUserOperatingOn.setSubjectSource(subjectSource);

        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserOperatingOn);

        UiMain.auditHelper(twoFactorDaoFactory, twoFactorRequestContainer, 
            twoFactorUserOperatingOn, subjectSource);
        
        return AdminSubmitView.admin;
      }
    });
  }


  /**
   * untrust browsers
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void untrustBrowsers(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String userIdOperatingOn = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("userIdOperatingOn");

    Source subjectSource = TfSourceUtils.mainSource();

    AdminSubmitView adminSubmitView = untrustBrowsersLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource);

    showJsp(adminSubmitView.getJsp());
    
  }

  /**
   * untrust browsers of two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param userIdOperatingOn
   * @param subjectSource
   * @return which page to display
   */
  public AdminSubmitView untrustBrowsersLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource) {
    
    return (AdminSubmitView) HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();

        TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        twoFactorUserLoggedIn.setSubjectSource(subjectSource);
        
        //make sure user is an admin
        if (!twoFactorUserLoggedIn.isAdmin()) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
          return AdminSubmitView.index;
        }

        twoFactorAdminContainer.setUserIdOperatingOn(userIdOperatingOn);
        
        //make sure searching for a user id
        if (StringUtils.isBlank(userIdOperatingOn)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnIsRequired"));
          return AdminSubmitView.admin;
        }

        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            userIdOperatingOn, true, false, true);
        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        TwoFactorUser twoFactorUserOperatingOn = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserOperatingOn == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return AdminSubmitView.admin;

          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return AdminSubmitView.admin;
        }

        twoFactorUserOperatingOn.setSubjectSource(subjectSource);
        
        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserOperatingOn);

        
        twoFactorUserOperatingOn.setTwoFactorSecretTemp(null);
        
        if (StringUtils.isBlank(twoFactorUserOperatingOn.getTwoFactorSecret())) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserWasNotOptedIn"));
          return AdminSubmitView.admin;
          
        }
  
        List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser()
          .retrieveTrustedByUserUuid(twoFactorUserOperatingOn.getUuid());
        
        if (TwoFactorServerUtils.length(twoFactorBrowsers) > 0) {

          //untrust browsers since opting in, dont want orphans from last time
          for (TwoFactorBrowser twoFactorBrowser : twoFactorBrowsers) {
            twoFactorBrowser.setWhenTrusted(0);
            twoFactorBrowser.setTrustedBrowser(false);
            twoFactorBrowser.store(twoFactorDaoFactory);
          }

          TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
              TwoFactorAuditAction.UNTRUST_BROWSERS, ipAddress, 
              userAgent, twoFactorUserOperatingOn.getUuid(), 
              twoFactorUserLoggedIn.getUuid(), null, null);
          
          
        }
        
        UiMain.auditHelper(twoFactorDaoFactory, twoFactorRequestContainer, 
            twoFactorUserOperatingOn, subjectSource);
        
        twoFactorRequestContainer.getTwoFactorUntrustBrowserContainer().setNumberOfBrowsers(TwoFactorServerUtils.length(twoFactorBrowsers));
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminUntrustBrowserSuccess"));
          
        return AdminSubmitView.admin;
      }
    });
  }


  /**
   * 
   */
  public static enum AdminSubmitView {
    
    /**
     */
    admin("admin.jsp"),
    
    /**
     */
    index("index.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private AdminSubmitView(String theJsp) {
      this.jsp = theJsp;
    }
    
    /**
     * 
     * @return jsp
     */
    public String getJsp() {
      return this.jsp;
    }
  }

}
