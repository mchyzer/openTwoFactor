/**
 * @author mchyzer
 * $Id: UiMainAdmin.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorUser;
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

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 * admin methods for two factor
 */
public class UiMainAdmin extends UiServiceLogicBase {

  /**
   * admin page combobox
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void personPicker(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    UiMain.personPickerHelper(httpServletRequest, httpServletResponse, true);
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

    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);

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

    return (AdminSubmitView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
        
        TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

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
        
        Subject subject = subjectSource.getSubjectByIdOrIdentifier(userIdOperatingOn, false);

        TwoFactorUser twoFactorUserOperatingOn = null;

        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        twoFactorUserOperatingOn = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserOperatingOn == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return AdminSubmitView.admin;

          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return AdminSubmitView.admin;
        }
        
        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserOperatingOn);
 
        twoFactorUserOperatingOn.setTwoFactorSecretTemp(null);
        
        if (StringUtils.isBlank(twoFactorUserOperatingOn.getTwoFactorSecret())) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserWasNotOptedIn"));
          
        } else {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminOptOutUserSuccess"));

        }
        
        twoFactorUserOperatingOn.setTwoFactorSecret(null);
        
        twoFactorUserOperatingOn.setOptedIn(false);
        twoFactorUserOperatingOn.setSequentialPassIndex(1L);

        twoFactorUserOperatingOn.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTOUT_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUserOperatingOn.getUuid(), twoFactorUserLoggedIn.getUuid());

        
        return AdminSubmitView.admin;
      }
    });
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

    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);
    
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

        Subject subject = subjectSource.getSubjectByIdOrIdentifier(userIdOperatingOn, false);
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

    Source subjectSource = SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME);

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

        Subject subject = subjectSource.getSubjectByIdOrIdentifier(userIdOperatingOn, false);
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
              userAgent, twoFactorUserOperatingOn.getUuid(), twoFactorUserLoggedIn.getUuid());
          
          
        }
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
