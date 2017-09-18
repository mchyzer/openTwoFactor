/**
 * @author mchyzer
 * $Id: UiMainAdmin.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.beans.TwoFactorReport;
import org.openTwoFactor.server.beans.TwoFactorReportData;
import org.openTwoFactor.server.beans.TwoFactorReportPrivilege;
import org.openTwoFactor.server.beans.TwoFactorReportRollup;
import org.openTwoFactor.server.beans.TwoFactorReportType;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserView;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.daemon.TfReportConfig;
import org.openTwoFactor.server.daemon.TfReportJob;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.email.TwoFactorEmail;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.hibernate.dao.HibernateDaoFactory;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.ui.UiServiceLogicBase;
import org.openTwoFactor.server.ui.beans.TextContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorAdminContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import au.com.bytecode.opencsv.CSVReader;
import edu.internet2.middleware.grouperClient.config.TwoFactorTextConfig;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * admin methods for two factor
 */
public class UiMainAdmin extends UiServiceLogicBase {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(UiMainAdmin.class);

  /**
   * 
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportEnrolled(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    String reportName = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.adminReportName");
    
    final TfReportConfig tfReportConfig = TwoFactorServerConfig.retrieveConfig().tfReportConfigs().get(reportName);
    if (tfReportConfig == null) {
      throw new RuntimeException("Cant find report config: " + reportName);
    }

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);

    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    tfReportConfig.setBccs(null);
    tfReportConfig.setCcs(null);
    
    String twoFactorUserEmail = twoFactorUser.getEmail0();
    if (StringUtils.isBlank(twoFactorUserEmail)) {
      throw new RuntimeException("No email for user");
    }
    tfReportConfig.setTos(twoFactorUserEmail);
    
    new Thread(new Runnable() {

      public void run() {
        
        TfReportJob.runReport(HibernateDaoFactory.getFactory(), tfReportConfig);
        
      }
      
    }).start();

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportScheduled"));

    showJsp("admin.jsp");
    
  }

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
    
    //make sure user is an admin
    if (!twoFactorRequestContainer.getTwoFactorUserLoggedIn().isAdmin()) {
      throw new RuntimeException("Not an admin! " + loggedInUser);
    }

    
    showJsp("admin.jsp");
  }

  /**
   * admin email all page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void adminEmailAllPage(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);

    //make sure user is an admin
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanLoggedInUserEmailAll()) {
      throw new RuntimeException("Cant email all! " + loggedInUser);
    }

    showJsp("adminEmailAll.jsp");
  }

  /**
   * reports index
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsIndex(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);

    //make sure user is an admin
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }

    showJsp("adminReportsIndex.jsp");

  }
  
  
  /**
   * reports privileges edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsPrivilegesEdit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    reportsPrivilegesEditLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource());
    
    showJsp("adminReportsPrivileges.jsp");

  }
  
  /**
   * reports edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsEdit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    reportsEditLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource());
    
    showJsp("adminReportsEdit.jsp");

  }

  
  
  /**
   * reports privileges delete
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsPrivilegesDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String reportPrivilegeUuid = httpServletRequest.getParameter("reportPrivilegeUuid");
    
    reportsPrivilegesDeleteLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource(), reportPrivilegeUuid);
    
    showJsp("adminReportsPrivileges.jsp");

  }

  /**
   * delete a report, show the reports edit screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param reportPrivilegeUuid
   *
   */
  public void reportsPrivilegesDeleteLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String reportPrivilegeUuid) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    twoFactorUserLoggedIn.setSubjectSource(subjectSource);

    //make sure user is an admin
    if (!twoFactorUserLoggedIn.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }

    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        //make sure searching for a user id
        if (StringUtils.isBlank(reportPrivilegeUuid)) {
          throw new RuntimeException("Why is reportUuid blank?");
        }

        TwoFactorReportPrivilege twoFactorReportPrivilege = TwoFactorReportPrivilege.retrieveByUuid(twoFactorDaoFactory, reportPrivilegeUuid);

        
        if (twoFactorReportPrivilege == null) {
          throw new RuntimeException("Why is report not found?");
        }

        TwoFactorReport twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, twoFactorReportPrivilege.getReportUuid());
        TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(twoFactorDaoFactory, twoFactorReportPrivilege.getUserUuid());
        twoFactorUser.setSubjectSource(subjectSource);
        
        twoFactorReportPrivilege.delete(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_REPORT_EDIT, ipAddress, 
            userAgent, twoFactorUserLoggedIn.getUuid(), 
            twoFactorUserLoggedIn.getUuid(), "Deleted privilege to " + twoFactorReport.getReportNameSystem() + ", " 
                + twoFactorReport.getReportNameFriendly() + ", for " + twoFactorUser.getLoginid() 
                + ", " + twoFactorUser.getDescriptionAdmin(), null);

        
        
        return null;
      }
    });

    reportsPrivilegesEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
    
    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsPrivilegeDeleteSuccess"));
      

  }
  
  /**
   * reports edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String reportUuid = httpServletRequest.getParameter("reportUuid");
    
    reportsDeleteLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource(), reportUuid);
    
    showJsp("adminReportsEdit.jsp");

  }

  /**
   * delete a report, show the reports edit screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param reportUuid
   *
   */
  public void reportsDeleteLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String reportUuid) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    twoFactorUserLoggedIn.setSubjectSource(subjectSource);

    //make sure user is an admin
    if (!twoFactorUserLoggedIn.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }

    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        //make sure searching for a user id
        if (StringUtils.isBlank(reportUuid)) {
          throw new RuntimeException("Why is reportUuid blank?");
        }

        TwoFactorReport twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, reportUuid);

        if (twoFactorReport == null) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditReportNotFound"));
          return null;
        }
        
        if (twoFactorReport.retrieveChildRollups(twoFactorDaoFactory).size() > 0) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditReportCantDeleteChildRollups"));
          return null;          
        }

        if (twoFactorReport.retrieveParentRollups(twoFactorDaoFactory).size() > 0) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditReportCantDeleteParentRollups"));
          return null;          
        }

        if (twoFactorReport.retrievePrivileges(twoFactorDaoFactory).size() > 0) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditReportCantDeletePrivileges"));
          return null;          
        }
        twoFactorReport.delete(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_REPORT_EDIT, ipAddress, 
            userAgent, twoFactorUserLoggedIn.getUuid(), 
            twoFactorUserLoggedIn.getUuid(), "Deleted report: " + twoFactorReport.getUuid() + ", " 
                + twoFactorReport.getReportNameSystem() + ", " + twoFactorReport.getReportNameFriendly(), null);
        
        reportsEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
        
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditReportDeleteSuccess"));
          
        return null;
      }
    });
    
  }
  
  
  
  /**
   * show the reports edit screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   *
   */
  public void reportsEditLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
    
    twoFactorUserUsingApp.setSubjectSource(subjectSource);

    //make sure user is an admin
    if (!twoFactorUserUsingApp.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }

    //lets get all the reports and set them in the admin container bean
    List<TwoFactorReport> twoFactorReports = TwoFactorReport.retrieveAll(twoFactorDaoFactory);
    
    twoFactorAdminContainer.setReports(twoFactorReports);
    
    //setup the drop down
    List<String> reportNameSystems = TwoFactorReportData.retrieveReportNameSystems(twoFactorDaoFactory);
    twoFactorAdminContainer.setReportNameSystems(reportNameSystems);
    

  }
  
  /**
   * reports rollups edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsRollupsEdit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    reportsRollupsEditLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource());

    showJsp("adminReportsRollups.jsp");

  }
  
  
  
  /**
   * admin import serials
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void adminImportSerialsPage(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);

    //make sure user is an admin
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanImportSerials()) {
      throw new RuntimeException("Cant import serials! " + loggedInUser);
    }

    showJsp("adminImportSerials.jsp");
  }

  
  
  /**
   * submit an email to all users
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void emailAllUsersSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);

    //make sure user is an admin
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanLoggedInUserEmailAll()) {
      throw new RuntimeException("Not an admin! " + loggedInUser);
    }

    String checkedAdminAllReallySendString = httpServletRequest.getParameter("checkedAdminAllReallySend");

    boolean checkedAdminAllReallySend = TwoFactorServerUtils.booleanValue(checkedAdminAllReallySendString, false);

    String emailSubject = httpServletRequest.getParameter("emailSubject");
    String emailBody = httpServletRequest.getParameter("emailBody");

    Source subjectSource = TfSourceUtils.mainSource();
    
    AdminEmailSubmitView adminEmailSubmitView = emailAllUsersSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource, emailSubject, emailBody, checkedAdminAllReallySend);
    
    showJsp(adminEmailSubmitView.getJsp());
  }

  /**
   * send email to send to all users
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource 
   * @param emailSubject 
   * @param emailBody 
   * @param sendEmailToUsers 
   * @return page to go to
   */
  public AdminEmailSubmitView emailAllUsersSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String emailSubject, final String emailBody, final boolean sendEmailToUsers) {

    return (AdminEmailSubmitView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
        
        TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUserLoggedIn.setSubjectSource(subjectSource);
        
        //make sure user is an admin
        if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanLoggedInUserEmailAll()) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
          return AdminEmailSubmitView.index;
        }

        //lets get the user's email address (current one)
        Subject loggedInSubject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            loggedInUser, false, true, true);
        
        String loggedInEmail = TfSourceUtils.retrieveEmail(loggedInSubject);
        
        if (StringUtils.isBlank(loggedInEmail)) {
          loggedInEmail = twoFactorUserLoggedIn.getEmail0();
        }
        
        if (StringUtils.isBlank(loggedInEmail)) {

          //this shouldnt really happen
          throw new RuntimeException("Cant find email address of logged in user: " + loggedInUser);

        }
        
        if (StringUtils.isBlank(emailSubject)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminEmailAllErrorNoSubject"));
          return AdminEmailSubmitView.adminEmailAll;
        }
        
        if (StringUtils.isBlank(emailBody)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminEmailAllErrorNoBody"));
          return AdminEmailSubmitView.adminEmailAll;
          
        }
        
        //lets get all logged in users
        List<TwoFactorUserView> allLoggedInUsers = twoFactorDaoFactory.getTwoFactorUserView().retrieveAllOptedInUsers();
        if (TwoFactorServerUtils.length(allLoggedInUsers) == 0) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminEmailAllErrorNoLoggedInUsers"));
          return AdminEmailSubmitView.admin;
        }
        
        //lets batch this into batches of e.g. 20
        int batchSize = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.emailAllUsersBatchSize", 20);
        int numberOfBatches = TwoFactorServerUtils.batchNumberOfBatches(allLoggedInUsers, batchSize);
        
        //process the people batch by batch
        for (int i=0;i<numberOfBatches;i++) {
          
          //get a batch
          List<TwoFactorUserView> twoFactorUserViews = TwoFactorServerUtils.batchList(allLoggedInUsers, batchSize, i);
          
          Set<String> subjectIds = new HashSet<String>();
          
          //get all the emails, from the system, and from the subject source
          Set<String> emailAddresses = new LinkedHashSet<String>();
          for (TwoFactorUserView twoFactorUserView : twoFactorUserViews) {
            String email = twoFactorUserView.getEmail0();
            if (!StringUtils.isBlank(email)) {
              emailAddresses.add(email);
            }
            String loginid = twoFactorUserView.getLoginid();
            if (!StringUtils.isBlank(loginid)) {
              subjectIds.add(loginid);
            }
          }
          
          //map by our subject id to the subject
          Map<String, Subject> subjectMap = TfSourceUtils.retrieveSubjectsByIdsOrIdentifiers(subjectSource, subjectIds, true);
          
          for (Subject subject : TwoFactorServerUtils.nonNull(subjectMap).values()) {
            String email = TfSourceUtils.retrieveEmail(subject);
            if (!StringUtils.isBlank(email)) {
              emailAddresses.add(email);
            }
          }
          
          String emailAddressListString = TwoFactorServerUtils.toStringForLog(emailAddresses);
          
          //batch #1
          //real email
            
          {
            TwoFactorEmail twoFactorEmail = new TwoFactorEmail();
            twoFactorEmail.assignFrom(loggedInEmail);
            twoFactorEmail.addTo(loggedInEmail);
  
            if (sendEmailToUsers) {
  
              for (String emailAddress : emailAddresses) {
                twoFactorEmail.addBcc(emailAddress);
              }
            }
              
            twoFactorEmail.assignSubject(emailSubject);
            
            if (sendEmailToUsers) {
              twoFactorEmail.assignBody(emailBody);
            } else {
              twoFactorEmail.assignBody(TextContainer.retrieveFromRequest().getText().get("adminEmailAllEmailNotSentPrefix") + emailAddressListString + "\n\n" + emailBody);
            }
            twoFactorEmail.send();
          }

          //send another to the sender with the list of emails sent
          if (sendEmailToUsers) {
            
            //send another so the user knows who was sent to
            TwoFactorEmail twoFactorEmail = new TwoFactorEmail();
            twoFactorEmail.assignFrom(loggedInEmail);
            twoFactorEmail.addTo(loggedInEmail);
  
            twoFactorEmail.assignSubject(emailSubject);
            
            twoFactorEmail.assignBody(TextContainer.retrieveFromRequest().getText().get("adminEmailAllEmailSentPrefix") + emailAddressListString + "\n\n" + emailBody);
            twoFactorEmail.send();
            
          }
          
        }
        
        twoFactorAdminContainer.setAdminEmailNumberOfUsers(TwoFactorServerUtils.length(allLoggedInUsers));

        //make an audit message
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            sendEmailToUsers ? TwoFactorAuditAction.SEND_EMAIL_TO_ALL_USERS
                : TwoFactorAuditAction.TEST_SEND_EMAIL_TO_ALL_USERS, ipAddress, 
            userAgent, twoFactorUserLoggedIn.getUuid(),
            twoFactorUserLoggedIn.getUuid(),  
            TextContainer.retrieveFromRequest().getText().get("auditsSendEmailToAllUsersDescription"), null);

        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get(sendEmailToUsers ? "auditsSendEmailToAllUsersSuccess" 
            : "auditsSendEmailToAllUsersNotSentSuccess"));
        
        return AdminEmailSubmitView.admin;
      }
    });
  }

  
  /**
   * opt out a user from the admin console
   * @param httpServletRequest
   * @param httServletResponse
   */
  public void optOutSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOn");

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

    String duoUserId = UiMain.duoRegisterUsers() ? DuoCommands.retrieveDuoUserIdBySomeId(loggedInUser) : null;

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
        
        twoFactorUserGettingOptedOut[0].setDuoUserId(null);
        
        
        if (!twoFactorUserGettingOptedOut[0].isOptedIn()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserWasNotOptedIn"));
          
        } else {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminOptOutUserSuccess"));

        }
        
        twoFactorUserGettingOptedOut[0].setTwoFactorSecret(null);
        
        twoFactorUserGettingOptedOut[0].setOptedIn(false);
        
        UiMain.duoClearOutAttributes(twoFactorUserGettingOptedOut[0]);

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
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForOptoutByAdmin", true)) {
        
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

    if (UiMain.duoRegisterUsers() && !StringUtils.isBlank(duoUserId)) { 
      //delete from duo
      DuoCommands.deleteDuoUserAndPhonesAndTokensBySomeId(userIdOperatingOn);
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

    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOnName");

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

    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOnName");

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
        
        if (!twoFactorUserOperatingOn.isOptedIn()) {
          
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
   * import serials
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void importSerialsSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
  
    //make sure user is an admin
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanImportSerials()) {
      throw new RuntimeException("Cant import serials! " + loggedInUser);
    }

    String serialNumbersCsv = httpServletRequest.getParameter("serialNumbers");
    
    AdminImportSerialsSubmitView adminImportSerialsSubmitView = importSerialsSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), serialNumbersCsv);

    showJsp(adminImportSerialsSubmitView.getJsp());

  }

  /**
   * import serials
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param importSerialCsv
   * @return page to go to
   */
  @SuppressWarnings("unchecked")
  public AdminImportSerialsSubmitView importSerialsSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String importSerialCsv) {
  
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
    
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    
    //make sure user is an admin
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanImportSerials()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsErrorUserNotImportSerials"));
      return AdminImportSerialsSubmitView.index;
    }

    if (StringUtils.isBlank(importSerialCsv)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsSerialsBlank"));
      return AdminImportSerialsSubmitView.adminImportSerials;
    }
    
    StringBuilder screenMessage = new StringBuilder();
    
    Reader originalReader = new StringReader(importSerialCsv);

    //convert from CSV to 
    CSVReader csvReader = null;

    int successCount = 0;
    int errorCount = 0;

    try {
      //note, the first row is the title
      List<String[]> csvEntries = null;
    
      try {
        csvReader = new CSVReader(originalReader);
        csvEntries = csvReader.readAll();
      } catch (IOException ioe) {
        throw new RuntimeException("Error processing CSV", ioe);
      }
            
      //lets get the headers
      int serialNumberColumn = -1;
      int secretColumn = -1;
      
      //must have lines
      if (TwoFactorServerUtils.length(csvEntries) == 0) {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsSerialsBlank"));
        return AdminImportSerialsSubmitView.adminImportSerials;
      }
      
      //lets go through the headers
      String[] headers = csvEntries.get(0);
      int headerSize = headers.length;
      
      if (headerSize != 2) {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsErrorColumns"));
        return AdminImportSerialsSubmitView.adminImportSerials;
      }
      
      for (int i=0;i<headerSize;i++) {
        if ("serialNumber".equalsIgnoreCase(StringUtils.trimToEmpty(headers[i]))) {
          serialNumberColumn = i;
        }
        if ("secret".equalsIgnoreCase(StringUtils.trimToEmpty(headers[i]))) {
          secretColumn = i;
        }
      }
      
      //normally start on index 1, if the first row is header
      int startIndex = 1;
      
      //must pass in an id
      if (serialNumberColumn == -1 || secretColumn == -1) {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsErrorColumns"));
        return AdminImportSerialsSubmitView.adminImportSerials;
      }

      //ok, lets go through the rows, start after the headers
      for (int i=startIndex;i<csvEntries.size();i++) {
        
        twoFactorAdminContainer.setImportSerial("-");
        
        String[] csvEntry = csvEntries.get(i);
        int row = i+1;
        
        twoFactorAdminContainer.setImportFobLineNumber(row);
        
        //try catch each one and see where we get
        String serialNumber = null;
        String secret = null;

        try {

          if (csvEntry.length != 2) {
            throw new RuntimeException("Row has " + csvEntry.length + " columns, but it should have 2 columns");
          }
          
          serialNumber = StringUtils.trimToEmpty(csvEntry[serialNumberColumn]); 
          
          twoFactorAdminContainer.setImportSerial(serialNumber);

          secret = StringUtils.trimToEmpty(csvEntry[secretColumn]);
          
          String[] error = new String[1];
          
          if (StringUtils.equals("12345", serialNumber) || StringUtils.equals("23456", serialNumber) 
              || StringUtils.equals("ABCDEFGHIJKLMNOP", secret) || StringUtils.equals("PONMLKJIIHGFEDBA", secret)) {
            throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsNoSamples"));
          }
          
          secret = UiMain.validateCustomCode(secret, error);
          
          if (!StringUtils.isBlank(error[0])) {
            throw new RuntimeException(error[0]);
          }
          
          TwoFactorDeviceSerial twoFactorDeviceSerial = null;
          
          //NOTE: this doesnt really work since the encryption is done with a timestamp
          twoFactorDeviceSerial = TwoFactorDeviceSerial.retrieveBySecretUnencrypted(twoFactorDaoFactory, secret);
          
          if (twoFactorDeviceSerial != null) {
            throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsSecretExists"));
          }

          twoFactorDeviceSerial = TwoFactorDeviceSerial.retrieveBySerial(twoFactorDaoFactory, serialNumber);

          if (twoFactorDeviceSerial != null) {
            throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsSerialExists"));
          }

          //doesnt exist, good to go
          
          twoFactorDeviceSerial = new TwoFactorDeviceSerial();
          twoFactorDeviceSerial.setSerialNumber(serialNumber);
          twoFactorDeviceSerial.setTwoFactorSecretUnencrypted(secret);
          twoFactorDeviceSerial.store(twoFactorDaoFactory);
          
          successCount++;
          
        } catch (Exception e) {
          LOG.warn("error on row: " + row, e);
          
          errorCount++;

          twoFactorAdminContainer.setImportFobError(e.getMessage());
          
          screenMessage.append("<br />").append(TextContainer.retrieveFromRequest().getText().get("adminImportSerialsErrorOnRow"));
          
        }
      
      }
      
    } finally {
      if (originalReader != null) {
        try {
          originalReader.close();
        } catch (Exception e) {
          LOG.warn("error", e);
        }
      }
      if (csvReader != null) {
        try {
          csvReader.close();
        } catch (Exception e) {
          LOG.warn("error", e);
        }
      }
    }
    
    twoFactorAdminContainer.setImportFobErrors(errorCount);
    twoFactorAdminContainer.setImportFobCount(successCount);
    
    //make an audit message
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.IMPORT_FOB_SERIALS, ipAddress, 
        userAgent, twoFactorUserLoggedIn.getUuid(),
        twoFactorUserLoggedIn.getUuid(),  
        TextContainer.retrieveFromRequest().getText().get("auditsImportSerialsDescription"), null);

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsImportSerialsDescription")
        + screenMessage.toString());
    
    return AdminImportSerialsSubmitView.admin;
  }

  
  
  /**
   * reports privileges submit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsPrivilegesSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String reportUuid = httpServletRequest.getParameter("reportUuid");
    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOnName");
    
    reportsPrivilegesSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource(), reportUuid, userIdOperatingOn);
    
    showJsp("adminReportsPrivileges.jsp");
  
  }

  /**
   * show the reports edit screen with add panel
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param reportUuid 
   * @param userIdOperatingOn
   *
   */
  public void reportsPrivilegesSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String reportUuid, final String userIdOperatingOn) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    @SuppressWarnings("unused")
    final TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();

    reportsPrivilegesEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);

    twoFactorUserLoggedIn.setSubjectSource(subjectSource);
    
    //make sure user is an admin
    if (!twoFactorUserLoggedIn.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        
        TwoFactorReport twoFactorReport = null;
        
        if (!StringUtils.isBlank(reportUuid)) {
          
          twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, reportUuid);
        }
          
        if (twoFactorReport == null) { 
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsPrivilegesErrorReportRequired"));
          return null;
        }

        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            userIdOperatingOn, true, false, true);

        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        TwoFactorUser twoFactorUser = null;
        
        if (!StringUtils.isBlank(theUserIdOperatingOn)) {
          twoFactorUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, theUserIdOperatingOn);
        }
        
        if (twoFactorUser == null) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsPrivilegesErrorUserRequired"));
          return null;
        }
        twoFactorUser.setSubjectSource(subjectSource);
        //see if one exists
        for (TwoFactorReportPrivilege twoFactorReportPrivilege : twoFactorDaoFactory.getTwoFactorReportPrivilege().retrieveAll()) {
          if (StringUtils.equals(twoFactorReportPrivilege.getReportUuid(), twoFactorReport.getUuid())
              && StringUtils.equals(twoFactorReportPrivilege.getUserUuid(), twoFactorUser.getUuid())) {
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsPrivilegesErrorPrivilegeExists"));
            return null;
          }
        }

        TwoFactorReportPrivilege twoFactorReportPrivilege = new TwoFactorReportPrivilege();
        twoFactorReportPrivilege.setReportUuid(twoFactorReport.getUuid());
        twoFactorReportPrivilege.setUserUuid(twoFactorUser.getUuid());
        
        twoFactorDaoFactory.getTwoFactorReportPrivilege().store(twoFactorReportPrivilege);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_REPORT_EDIT, ipAddress, 
            userAgent, twoFactorUserLoggedIn.getUuid(), 
            twoFactorUserLoggedIn.getUuid(), "Added privilege to " + twoFactorReport.getReportNameSystem() + ", " 
                + twoFactorReport.getReportNameFriendly() + ", for " + twoFactorUser.getLoginid() 
                + ", " + twoFactorUser.getDescriptionAdmin(), null);
                
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsPrivilegeAddSuccess"));
          
        return null;
      }
    });
    
    //note, this was already done, but do it again to update the list
    reportsPrivilegesEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
    
  }

  /**
   * reports add submit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsAddEditSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    String reportUuid = httpServletRequest.getParameter("reportUuid");
    String reportTypeString = httpServletRequest.getParameter("reportType");
    String reportNameSystem = httpServletRequest.getParameter("reportNameSystem");
    String reportNameSystemSelect = httpServletRequest.getParameter("reportNameSystemSelect");
    String reportNameDisplay = httpServletRequest.getParameter("reportNameDisplay");
    
    reportsAddEditSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource(), reportNameSystem, reportNameSystemSelect, 
        reportNameDisplay, reportTypeString, reportUuid);
    
    showJsp("adminReportsEdit.jsp");
  
  }

  /**
   * show the reports edit screen with add panel
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param theReportNameSystem 
   * @param reportNameSystemSelect
   * @param reportNameDisplay 
   * @param reportTypeString 
   * @param reportUuid 
   *
   */
  public void reportsAddEditSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, String theReportNameSystem, 
      String reportNameSystemSelect,
      final String reportNameDisplay, final String reportTypeString, final String reportUuid) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    @SuppressWarnings("unused")
    final TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();

    reportsAddLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);

    twoFactorUserLoggedIn.setSubjectSource(subjectSource);

    final String reportNameSystem = StringUtils.defaultIfEmpty(theReportNameSystem, reportNameSystemSelect);

    final boolean isAddNotEdit = StringUtils.isBlank(reportUuid);
    
    {
      //set the values so the fields are populated if there is a validation problem
      TwoFactorReport twoFactorReport = new TwoFactorReport();
      twoFactorReport.setReportNameFriendly(reportNameDisplay);
      twoFactorReport.setReportNameSystem(reportNameSystem);
      twoFactorReport.setReportType(reportTypeString);
      twoFactorReport.setUuid(reportUuid);
      twoFactorAdminContainer.setTwoFactorReport(twoFactorReport);
    }
    
    //make sure user is an admin
    if (!twoFactorUserLoggedIn.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }
    
    if (!StringUtils.isBlank(theReportNameSystem) && !StringUtils.isBlank(reportNameSystemSelect)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditErrorKeySelectAndManual"));
      return;
    }
    if (StringUtils.isBlank(theReportNameSystem) && StringUtils.isBlank(reportNameSystemSelect)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditErrorKeyRequired"));
      return;
    }
    if (StringUtils.isBlank(reportNameDisplay)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditErrorNameRequired"));
      return;
    }
    if (StringUtils.isBlank(reportTypeString)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditErrorTypeRequired"));
      return;
    }
    //lets see if the name system or display exists
    for (TwoFactorReport twoFactorReport : TwoFactorReport.retrieveAll(twoFactorDaoFactory)) {
      if (StringUtils.equals(twoFactorReport.getReportNameFriendly(), reportNameDisplay)) {
        //if we are inserting, or if this is not othe same report
        if (isAddNotEdit || !StringUtils.equals(reportUuid, twoFactorReport.getUuid())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditErrorNameExists"));
          return;
        }
      }
      if (StringUtils.equals(twoFactorReport.getReportNameSystem(), reportNameSystem)) {
        //if we are inserting, or if this is not othe same report
        if (isAddNotEdit || !StringUtils.equals(reportUuid, twoFactorReport.getUuid())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditErrorKeyExists"));
          return;
        }
      }
    }

    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        TwoFactorReport twoFactorReport = null;
        
        if (isAddNotEdit) {
          twoFactorReport = new TwoFactorReport();
        } else {
          twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, reportUuid);
        }

        twoFactorReport.setReportNameFriendly(reportNameDisplay);
        twoFactorReport.setReportNameSystem(reportNameSystem);
        twoFactorReport.setReportType(reportTypeString);

        //if editing, and the type is now group, but there are child reports, thats a problem
        if (!isAddNotEdit) {
          if (TwoFactorReportType.valueOf(reportTypeString) == TwoFactorReportType.group) {
            if (twoFactorReport.retrieveChildRollups(twoFactorDaoFactory).size() > 0) {
              
              twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditErrorGroupTypeChildExists"));
              return null;
              
            }
          }
        }
        
        twoFactorDaoFactory.getTwoFactorReport().store(twoFactorReport);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_REPORT_EDIT, ipAddress, 
            userAgent, twoFactorUserLoggedIn.getUuid(), 
            twoFactorUserLoggedIn.getUuid(), (isAddNotEdit ? "Added" : "Edited" ) + " report: " + twoFactorReport.getUuid() + ", " 
                + twoFactorReport.getReportNameSystem() + ", " + twoFactorReport.getReportNameFriendly(), null);
                
        if (isAddNotEdit) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditAddSuccess"));
        } else {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsEditEditSuccess"));
          
        }
          
        return null;
      }
    });
    
    //clear out the fields
    twoFactorAdminContainer.setTwoFactorReport(null);
    
    //note, tihs was already done, but do it again to update the list
    reportsAddLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
    
  }


  /**
   * reports add
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsAdd(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    reportsAddLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource());
    
    showJsp("adminReportsEdit.jsp");
  
  }

  /**
   * show the reports edit screen with add panel
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   *
   */
  public void reportsAddLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
    
    twoFactorUserUsingApp.setSubjectSource(subjectSource);
  
    //make sure user is an admin
    if (!twoFactorUserUsingApp.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }
    
    twoFactorAdminContainer.setReportAdd(true);

    reportsEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);

  }

  /**
   * reports edit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsEditReport(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    String reportUuid = httpServletRequest.getParameter("reportUuid");

    reportsEditReportLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource(), reportUuid);
    
    showJsp("adminReportsEdit.jsp");
  
  }

  /**
   * show the reports edit screen with add/edit panel
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param reportUuid 
   *
   */
  public void reportsEditReportLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String reportUuid) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
    
    twoFactorUserUsingApp.setSubjectSource(subjectSource);
  
    //make sure user is an admin
    if (!twoFactorUserUsingApp.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }
    
    twoFactorAdminContainer.setReportAdd(true);
  
    //make sure searching for a user id
    if (StringUtils.isBlank(reportUuid)) {
      throw new RuntimeException("Why is reportUuid blank?");
    }

    TwoFactorReport twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, reportUuid);

    if (twoFactorReport == null) {
      throw new RuntimeException("Report doesnt exist");
    }

    twoFactorAdminContainer.setTwoFactorReport(twoFactorReport);
    
    reportsEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
  
  }


  /**
   * show the reports privileges edit screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   */
  public void reportsPrivilegesEditLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
    
    twoFactorUserUsingApp.setSubjectSource(subjectSource);
  
    //make sure user is an admin
    if (!twoFactorUserUsingApp.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }

    //lets get all the reports and set them in the admin container bean
    List<TwoFactorReport> twoFactorReports = TwoFactorReport.retrieveAll(twoFactorDaoFactory);
    
    twoFactorAdminContainer.setReports(twoFactorReports);
    
    Map<String, TwoFactorReport> twoFactorReportMap = new HashMap<String, TwoFactorReport>();
    
    for (TwoFactorReport twoFactorReport : twoFactorReports) {
      twoFactorReportMap.put(twoFactorReport.getUuid(), twoFactorReport);
    }

    //get all the users and set them in the privilege object
    List<TwoFactorUser> twoFactorUsers = twoFactorDaoFactory.getTwoFactorUser().retrieveUsersWhoHavePrivilegesInReport();
    
    Map<String, TwoFactorUser> twoFactorUserMap = new HashMap<String, TwoFactorUser>();
    
    List<String> loginids = new ArrayList<String>();
    
    for (TwoFactorUser twoFactorUser : twoFactorUsers) {
      twoFactorUserMap.put(twoFactorUser.getUuid(), twoFactorUser);
      twoFactorUser.setSubjectSource(subjectSource);
      
      loginids.add(twoFactorUser.getLoginid());
    }

    //get all the loginids, this sets up the cache
    TfSourceUtils.retrieveSubjectsByIdsOrIdentifiers(subjectSource, loginids, true);

    List<TwoFactorReportPrivilege> twoFactorReportPrivileges = twoFactorDaoFactory.getTwoFactorReportPrivilege().retrieveAll();
    
    for (TwoFactorReportPrivilege twoFactorReportPrivilege : twoFactorReportPrivileges) {
      TwoFactorUser twoFactorUser = twoFactorUserMap.get(twoFactorReportPrivilege.getUserUuid());
      if (twoFactorUser == null) {
        twoFactorUser = TwoFactorUser.retrieveByUuid(twoFactorDaoFactory, twoFactorReportPrivilege.getUserUuid());
        twoFactorUserMap.put(twoFactorReportPrivilege.getUserUuid(), twoFactorUser);
      }
      twoFactorReportPrivilege.setTwoFactorUser(twoFactorUser);
      
      TwoFactorReport twoFactorReport = twoFactorReportMap.get(twoFactorReportPrivilege.getReportUuid());
      if (twoFactorReport == null) {
        twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, twoFactorReportPrivilege.getReportUuid());
        twoFactorReportMap.put(twoFactorReportPrivilege.getReportUuid(), twoFactorReport);
      }
      twoFactorReportPrivilege.setTwoFactorReport(twoFactorReport);
    }
    twoFactorAdminContainer.setTwoFactorReportPrivileges(twoFactorReportPrivileges);
    
  }


  /**
   * show the reports rollups edit screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   */
  public void reportsRollupsEditLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();

    twoFactorUserUsingApp.setSubjectSource(subjectSource);

    //make sure user is an admin
    if (!twoFactorUserUsingApp.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }

    //lets get all the reports and set them in the admin container bean
    List<TwoFactorReport> twoFactorReports = TwoFactorReport.retrieveAll(twoFactorDaoFactory);
    
    twoFactorAdminContainer.setReports(twoFactorReports);
    
    Map<String, TwoFactorReport> twoFactorReportMap = new HashMap<String, TwoFactorReport>();
    
    for (TwoFactorReport twoFactorReport : twoFactorReports) {
      twoFactorReportMap.put(twoFactorReport.getUuid(), twoFactorReport);
    }

    List<TwoFactorReportRollup> twoFactorReportRollups = twoFactorDaoFactory.getTwoFactorReportRollup().retrieveAll();
    
    for (TwoFactorReportRollup twoFactorReportRollup : twoFactorReportRollups) {
      {
        TwoFactorReport twoFactorReport = twoFactorReportMap.get(twoFactorReportRollup.getChildReportUuid());
        if (twoFactorReport == null) {
          twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, twoFactorReportRollup.getChildReportUuid());
          twoFactorReportMap.put(twoFactorReportRollup.getChildReportUuid(), twoFactorReport);
        }
        twoFactorReportRollup.setChildReport(twoFactorReport);
      }
      
      {
        TwoFactorReport twoFactorReport = twoFactorReportMap.get(twoFactorReportRollup.getParentReportUuid());
        if (twoFactorReport == null) {
          twoFactorReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, twoFactorReportRollup.getParentReportUuid());
          twoFactorReportMap.put(twoFactorReportRollup.getParentReportUuid(), twoFactorReport);
        }
        twoFactorReportRollup.setParentReport(twoFactorReport);
      }
    }
    twoFactorAdminContainer.setTwoFactorReportRollups(twoFactorReportRollups);
    
  }


  /**
   * reports rollups submit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsRollupsSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    String parentReportUuid = httpServletRequest.getParameter("parentReportUuid");
    String childReportUuid = httpServletRequest.getParameter("childReportUuid");
    
    reportsRollupsSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource(), parentReportUuid, childReportUuid);
    
    showJsp("adminReportsRollups.jsp");
  
  }

  /**
   * submit the reports rollups edit screen with add panel
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param parentReportUuid 
   * @param childReportUuid
   *
   */
  public void reportsRollupsSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String parentReportUuid, final String childReportUuid) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    @SuppressWarnings("unused")
    final TwoFactorAdminContainer twoFactorAdminContainer = twoFactorRequestContainer.getTwoFactorAdminContainer();
  
    reportsRollupsEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
  
    twoFactorUserLoggedIn.setSubjectSource(subjectSource);
    
    //make sure user is an admin
    if (!twoFactorUserLoggedIn.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        
        TwoFactorReport parentReport = null;
        
        if (!StringUtils.isBlank(parentReportUuid)) {
          
          parentReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, parentReportUuid);
        }
          
        if (parentReport == null) { 
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsRollupsErrorParentReportRequired"));
          return null;
        }

        if (parentReport.getReportTypeEnum() != TwoFactorReportType.rollup) {
          throw new RuntimeException("Why is this not a rollup???? " + parentReport.getUuid() + ", " 
              + parentReport.getReportNameSystem() + ", " + parentReport.getReportNameFriendly());
        }
        
        TwoFactorReport childReport = null;
        
        if (!StringUtils.isBlank(childReportUuid)) {
          
          childReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, childReportUuid);
        }
          
        if (childReport == null) { 
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsRollupsErrorChildReportRequired"));
          return null;
        }

        //see if one exists
        for (TwoFactorReportRollup twoFactorReportRollup : twoFactorDaoFactory.getTwoFactorReportRollup().retrieveAll()) {
          if (StringUtils.equals(twoFactorReportRollup.getChildReportUuid(), childReportUuid)
              && StringUtils.equals(twoFactorReportRollup.getParentReportUuid(), parentReportUuid)) {
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsRollupsErrorRollupExists"));
            return null;
          }
        }
  
        //parent cant be child
        if (StringUtils.equals(parentReport.getUuid(), childReport.getUuid())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsRollupsErrorParentCantBeChild"));
          return null;
        }
        
        //lets see if it is a circular reference.. if the child implies the parent, then it is circular
        {
          List<TwoFactorReportRollup> childChildrenRollups = childReport.retrieveChildRollups(twoFactorDaoFactory);
          for (TwoFactorReportRollup childChildRollup : childChildrenRollups) {
            
            if (StringUtils.equals(childChildRollup.getChildReportUuid(), parentReportUuid)) {
              twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsRollupsErrorCircularReference"));
              return null;
              
            }
            
          }
        }
        TwoFactorReportRollup twoFactorReportRollup = new TwoFactorReportRollup();
        twoFactorReportRollup.setChildReportUuid(childReportUuid);
        twoFactorReportRollup.setParentReportUuid(parentReportUuid);
        
        twoFactorDaoFactory.getTwoFactorReportRollup().store(twoFactorReportRollup);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_REPORT_EDIT, ipAddress, 
            userAgent, twoFactorUserLoggedIn.getUuid(), 
            twoFactorUserLoggedIn.getUuid(), "Added report rollup parent: " + parentReport.getReportNameSystem() + ", " 
                + parentReport.getReportNameFriendly() + ", to child report: " + childReport.getReportNameSystem()
                + ", " + childReport.getReportNameFriendly(), null);
                
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsRollupsAddSuccess"));
          
        return null;
      }
    });
    
    //note, this was already done, but do it again to update the list
    reportsRollupsEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
    
  }


  /**
   * reports rollups delete
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reportsRollupsDelete(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    String reportRollupUuid = httpServletRequest.getParameter("reportRollupUuid");
    
    reportsRollupsDeleteLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), TfSourceUtils.mainSource(), reportRollupUuid);
    
    showJsp("adminReportsRollups.jsp");
  
  }

  /**
   * delete a report rollup, show the reports edit screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param reportRollupUuid
   *
   */
  public void reportsRollupsDeleteLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String reportRollupUuid) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    twoFactorUserLoggedIn.setSubjectSource(subjectSource);
  
    //make sure user is an admin
    if (!twoFactorUserLoggedIn.isAdmin()) {
      throw new RuntimeException(TextContainer.retrieveFromRequest().getText().get("adminErrorUserNotAdmin"));
    }
    if (!twoFactorRequestContainer.getTwoFactorAdminContainer().isCanAdminReports()) {
      throw new RuntimeException("Cant admin reports! " + loggedInUser);
    }
  
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        //make sure searching for a user id
        if (StringUtils.isBlank(reportRollupUuid)) {
          throw new RuntimeException("Why is reportUuid blank?");
        }
  
        TwoFactorReportRollup twoFactorReportRollup = TwoFactorReportRollup.retrieveByUuid(twoFactorDaoFactory, reportRollupUuid);
  
        
        if (twoFactorReportRollup == null) {
          throw new RuntimeException("Why is report rollup not found?");
        }
  
        TwoFactorReport childReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, twoFactorReportRollup.getChildReportUuid());
        TwoFactorReport parentReport = TwoFactorReport.retrieveByUuid(twoFactorDaoFactory, twoFactorReportRollup.getParentReportUuid());
        
        twoFactorReportRollup.delete(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_REPORT_EDIT, ipAddress, 
            userAgent, twoFactorUserLoggedIn.getUuid(), 
            twoFactorUserLoggedIn.getUuid(), "Deleted report rollup from: " + parentReport.getReportNameSystem()
              + ", " + parentReport.getReportNameFriendly() + ", to: "
              + childReport.getReportNameSystem() + ", " 
                + childReport.getReportNameFriendly(), null);
        
        return null;
      }
    });
  
    reportsRollupsEditLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
    
    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminReportsRollupsDeleteSuccess"));
      
  
  }


  /**
   * opt out a user from the admin console
   * @param httpServletRequest
   * @param httServletResponse
   */
  public void generateCodeSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httServletResponse) {
    
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.allowAdminsToGenerateCodesforUsers", true)) {
      throw new RuntimeException("twoFactorServer.allowAdminsToGenerateCodesforUsers is set to false in config file which disables this feature!");
    }
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOn");
  
    Source subjectSource = TfSourceUtils.mainSource();

    AdminSubmitView adminSubmitView = generateCodeSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
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
  public AdminSubmitView generateCodeSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource) {
  
    final boolean[] success = new boolean[]{false};
  
    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];
    
    final TwoFactorUser[] twoFactorUserGeneratingCode = new TwoFactorUser[1];
  
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
        
        twoFactorUserGeneratingCode[0] = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserGeneratingCode[0] == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return AdminSubmitView.admin;
  
          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return AdminSubmitView.admin;
        }
        
        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserGeneratingCode[0]);
  
        //store code and when sent
        String secretCode = Integer.toString(new SecureRandom().nextInt(1000000));
        
        //make this since 9 since thats what duo is
        secretCode = StringUtils.leftPad(secretCode, 9, '0');

        //maybe going from duo
        //opt in to duo
        if (UiMain.duoRegisterUsers()) {

          secretCode = DuoCommands.duoBypassCodeBySomeId(twoFactorUserGeneratingCode[0].getLoginid());
          
        }
        
        twoFactorUserGeneratingCode[0].setPhoneCodeUnencrypted(secretCode);
        twoFactorUserGeneratingCode[0].setDatePhoneCodeSent(System.currentTimeMillis());
        twoFactorUserGeneratingCode[0].store(twoFactorDaoFactory);

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_GENERATE_CODE, ipAddress, 
            userAgent, twoFactorUserGeneratingCode[0].getUuid(),
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
      twoFactorUserGeneratingCode[0].setSubjectSource(subjectSource);
      
      //see if there
      
      //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
      if (success[0] && subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForGenerateCodeByAdmin", true)) {
        
        Subject sourceSubjectLoggedIn = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        Subject sourceSubjectPersonPicked = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            twoFactorUserGeneratingCode[0].getLoginid(), true, false, true);
        
        String emailAddressFromSubjectLoggedIn = TfSourceUtils.retrieveEmail(sourceSubjectLoggedIn);
        String emailAddressFromDatabaseLoggedIn = twoFactorUserUsingApp[0].getEmail0();
  
        String emailAddressFromSubjectPersonPicked = TfSourceUtils.retrieveEmail(sourceSubjectPersonPicked);
        String emailAddressFromDatabasePersonPicked = twoFactorUserGeneratingCode[0].getEmail0();
  
        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailGenerateCodeByAdminSubject");
        subject = TextContainer.massageText("emailGenerateCodeByAdminSubject", subject);
  
        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailGenerateCodeByAdminBody");
        body = TextContainer.massageText("emailGenerateCodeByAdminBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.adminGenerateCodes");
        
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
  
    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminGenerateCodeForUserSuccess"));
    
    if (adminSubmitView == AdminSubmitView.admin) {
      UiMain.auditHelper(twoFactorDaoFactory, twoFactorRequestContainer, 
          twoFactorUserGeneratingCode[0], subjectSource);
    }
    return adminSubmitView;
    
  }


  /**
   * manage a user from the admin console
   * @param httpServletRequest
   * @param httServletResponse
   */
  public void manageUser(HttpServletRequest httpServletRequest, HttpServletResponse httServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOn");
  
    Source subjectSource = TfSourceUtils.mainSource();
  
    ManageUserView manageUserView = manageUserLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource);
  
    showJsp(manageUserView.getJsp());
    
  }

  /**
   * manage a user
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param userIdOperatingOn 
   * @param subjectSource
   * @return page to go to
   */
  public ManageUserView manageUserLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource) {
  
    final boolean[] success = new boolean[]{false};
  
    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];
    
    final TwoFactorUser[] twoFactorUserManaged = new TwoFactorUser[1];
  
    ManageUserView manageUserView = (ManageUserView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
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
          return ManageUserView.index;
        }
  
        twoFactorAdminContainer.setUserIdOperatingOn(userIdOperatingOn);
  
        //make sure searching for a user id
        if (StringUtils.isBlank(userIdOperatingOn)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnIsRequired"));
          return ManageUserView.admin;
        }
  
        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            userIdOperatingOn, true, false, true);
  
        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        twoFactorUserManaged[0] = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserManaged[0] == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return ManageUserView.admin;
  
          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return ManageUserView.admin;
        }
        
        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserManaged[0]);
  
        twoFactorUserManaged[0].setSubjectSource(subjectSource);
                
        
        if (!twoFactorUserManaged[0].isOptedIn()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserWasNotOptedIn"));
          return ManageUserView.admin;
        }
        
        success[0] = true;
        
        return ManageUserView.adminConfirmUser;
      }
    });
    
    return manageUserView;
    
  }


  /**
   * manage a user from the admin console
   * @param httpServletRequest
   * @param httServletResponse
   */
  public void confirmUser(HttpServletRequest httpServletRequest, HttpServletResponse httServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOn");
    
    String checkedIdString = httpServletRequest.getParameter("checkedIdName");
    
    Source subjectSource = TfSourceUtils.mainSource();
  
    ConfirmUserView confirmUserView = confirmUserLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource, checkedIdString);
  
    showJsp(confirmUserView.getJsp());
    
  }

  /**
   * manage a user
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress
   * @param userAgent
   * @param loggedInUser
   * @param userIdOperatingOn
   * @param subjectSource
   * @param checkedIdString
   * @return page to go to
   */
  public ConfirmUserView confirmUserLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource, final String checkedIdString) {

    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];

    final TwoFactorUser[] twoFactorUserConfirmed = new TwoFactorUser[1];

    ConfirmUserView manageUserView = (ConfirmUserView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
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
          return ConfirmUserView.index;
        }
  
        twoFactorAdminContainer.setUserIdOperatingOn(userIdOperatingOn);
  
        //make sure searching for a user id
        if (StringUtils.isBlank(userIdOperatingOn)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnIsRequired"));
          return ConfirmUserView.admin;
        }
  
        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            userIdOperatingOn, true, false, true);
  
        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        twoFactorUserConfirmed[0] = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserConfirmed[0] == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return ConfirmUserView.admin;
  
          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return ConfirmUserView.admin;
        }
        
        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserConfirmed[0]);
  
        twoFactorUserConfirmed[0].setSubjectSource(subjectSource);

        if (!twoFactorUserConfirmed[0].isOptedIn()) {

          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserWasNotOptedIn"));
          return ConfirmUserView.admin;

        }

        if (StringUtils.isBlank(checkedIdString)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminManageUserCheckedIdRequired"));
          
          return ConfirmUserView.adminConfirmUser;
        }

        if (TwoFactorServerUtils.booleanValue(checkedIdString, false)) {
          
          StringBuilder message = new StringBuilder();
          
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmCheckedPhysicalId") + "<br />");
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteRecommendYes") + "<br />");

          TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
              TwoFactorAuditAction.ADMIN_CONFIRM_USER, ipAddress, 
              userAgent, twoFactorUserConfirmed[0].getUuid(), 
              twoFactorUserUsingApp[0].getUuid(), StringUtils.replace(message.toString(), "<br />", ", "), null);

          twoFactorRequestContainer.setError(message.toString());

          return ConfirmUserView.adminManageUser;
        }

        return ConfirmUserView.adminConfirmUserRemote;
      }
    });

    return manageUserView;
    
  }


  /**
   * manage a user from the admin console
   * @param httpServletRequest
   * @param httServletResponse
   */
  public void confirmUserRemote(HttpServletRequest httpServletRequest, HttpServletResponse httServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    String userIdOperatingOn = httpServletRequest.getParameter("userIdOperatingOn");
    
    String checkedNameString = httpServletRequest.getParameter("checkedNameName");
    String userIdString = httpServletRequest.getParameter("userIdName");
    String netIdString = httpServletRequest.getParameter("netIdName");
    String lastFourString = httpServletRequest.getParameter("lastFourName");
    String checkedDeptString = httpServletRequest.getParameter("checkedDeptName");
    
    
    String birthMonthString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthMonth");
    String birthDayString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthDay");
    String birthYearString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthYear");
    
    String birthdayTextfield = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    Source subjectSource = TfSourceUtils.mainSource();
  
    ConfirmUserRemoteView confirmUserRemoteView = confirmUserRemoteLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource, checkedNameString, checkedDeptString,
        userIdString, netIdString, lastFourString, birthMonthString, birthDayString, birthYearString, birthdayTextfield);

    showJsp(confirmUserRemoteView.getJsp());

  }

  /**
   * manage a user
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress
   * @param userAgent
   * @param loggedInUser
   * @param userIdOperatingOn
   * @param subjectSource
   * @param checkedNameString 
   * @param checkedDeptString 
   * @param userIdString 
   * @param netIdString 
   * @param lastFourString 
   * @param birthMonthString 
   * @param birthDayString 
   * @param birthYearString 
   * @param birthdayTextfield 
   * @return page to go to
   */
  public ConfirmUserRemoteView confirmUserRemoteLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource, final String checkedNameString, 
      final String checkedDeptString,
      final String userIdString, final String netIdString, final String lastFourString, final String birthMonthString, 
      final String birthDayString, final String birthYearString, final String birthdayTextfield) {
  
    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];
  
    final TwoFactorUser[] twoFactorUserConfirmed = new TwoFactorUser[1];
  
    ConfirmUserRemoteView confirmUserRemoteView = (ConfirmUserRemoteView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
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
          return ConfirmUserRemoteView.index;
        }
  
        twoFactorAdminContainer.setUserIdOperatingOn(userIdOperatingOn);
  
        //make sure searching for a user id
        if (StringUtils.isBlank(userIdOperatingOn)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnIsRequired"));
          return ConfirmUserRemoteView.admin;
        }
  
        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            userIdOperatingOn, true, false, true);
  
        String theUserIdOperatingOn = userIdOperatingOn;
        
        if (subject != null) {
          
          theUserIdOperatingOn = subject.getId();
          
        }
        
        twoFactorUserConfirmed[0] = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, theUserIdOperatingOn);
        
        if (twoFactorUserConfirmed[0] == null) {
          
          if (subject == null) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotFound"));
            return ConfirmUserRemoteView.admin;
  
          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserToOperateOnNotInSystem"));
          return ConfirmUserRemoteView.admin;
        }
        
        //we found a user!
        twoFactorAdminContainer.setTwoFactorUserOperatingOn(twoFactorUserConfirmed[0]);
  
        twoFactorUserConfirmed[0].setSubjectSource(subjectSource);
  
        if (!twoFactorUserConfirmed[0].isOptedIn()) {
  
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminErrorUserWasNotOptedIn"));
          return ConfirmUserRemoteView.admin;
  
        }
  
        boolean deptCorrect = StringUtils.equals("true", checkedDeptString);
        boolean nameCorrect = StringUtils.equals("true", checkedNameString);
        boolean idCorrect = StringUtils.equals(userIdString, twoFactorUserConfirmed[0].getLoginid());
        boolean netIdCorrect = StringUtils.equals(netIdString, TfSourceUtils.convertSubjectIdToNetId(subjectSource, twoFactorUserConfirmed[0].getLoginid()));

        Boolean lastFourCorrect = StringUtils.isBlank(twoFactorUserConfirmed[0].getLastFour()) ? null : StringUtils.equals(lastFourString, twoFactorUserConfirmed[0].getLastFour());

        Boolean birthdayCorrect = null;
        
        if (twoFactorUserConfirmed[0].getBirthDate() != null) {
          birthdayCorrect = UiMain.checkBirthday(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, 
              userAgent, subjectSource, twoFactorUserConfirmed[0], birthMonthString, birthDayString, 
              birthYearString, birthdayTextfield);
        }

        StringBuilder message = new StringBuilder();

        boolean recommendYes = false;
        
        if (!nameCorrect) {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteNameIncorrect") + "<br />\n");
        } else {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteNameCorrect") + "<br />\n");
        }

        if (!deptCorrect) {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteDeptIncorrect") + "<br />\n");
        } else {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteDeptCorrect") + "<br />\n");
        }
        
        if (!idCorrect) {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemotePennIdIncorrect") + "<br />\n");
        } else {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemotePennIdCorrect") + "<br />\n");
        }
        if (!netIdCorrect) {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemotePennkeyIncorrect") + "<br />\n");
        } else {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemotePennkeyCorrect") + "<br />\n");
        }
        
        if (lastFourCorrect != null) {
          if (!lastFourCorrect) {
            message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteLastFourIncorrect") + "<br />\n");
          } else {
            recommendYes = true;
            message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteLastFourCorrect") + "<br />\n");
          }
        }
        if (birthdayCorrect != null) {
          if (!birthdayCorrect) {
            message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteBirthdayIncorrect") + "<br />\n");
          } else {
            recommendYes = true;
            message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteBirthdayCorrect") + "<br />\n");
          }
        }

        if (deptCorrect && nameCorrect) {
          recommendYes = true;
        }

        if (!StringUtils.isBlank(userIdString) && !idCorrect) {
          recommendYes = false;
        }
        if (!StringUtils.isBlank(netIdString) && !netIdCorrect) {
          recommendYes = false;
        }

        //if we have last four and its not right
        if (lastFourCorrect != null && !lastFourCorrect) {
          recommendYes = false;
        }
        if (birthdayCorrect != null && !birthdayCorrect) {
          recommendYes = false;
        }

        if (recommendYes) {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteRecommendYes") + "<br />\n");
        } else {
          message.append(TextContainer.retrieveFromRequest().getText().get("adminConfirmRemoteRecommendNo") + "<br />\n");
        }
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADMIN_CONFIRM_USER, ipAddress, 
            userAgent, twoFactorUserConfirmed[0].getUuid(), 
            twoFactorUserUsingApp[0].getUuid(), StringUtils.replace(message.toString(), "<br />", ", "), null);
        
        if (!StringUtils.isBlank(twoFactorRequestContainer.getError())) {
          message.insert(0, twoFactorRequestContainer.getError() + "<br />\n");
        }
        twoFactorRequestContainer.setError(message.toString());
                
        return ConfirmUserRemoteView.adminManageUser;
      }
    });

    return confirmUserRemoteView;

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

  /**
   * 
   */
  public static enum AdminEmailSubmitView {
    
    /**
     */
    admin("admin.jsp"),
    
    /**
     */
    index("index.jsp"),
    
    /**
     */
    adminEmailAll("adminEmailAll.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private AdminEmailSubmitView(String theJsp) {
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

  /**
   * 
   */
  public static enum AdminImportSerialsSubmitView {
    
    /**
     */
    admin("admin.jsp"),
    
    /**
     */
    index("index.jsp"),
    
    /**
     */
    adminImportSerials("adminImportSerials.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private AdminImportSerialsSubmitView(String theJsp) {
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

  /**
   * 
   */
  public static enum ManageUserView {
    
    /**
     */
    adminConfirmUser("adminConfirmUser.jsp"),
    
    /**
     */
    index("index.jsp"),
    
    /**
     */
    admin("admin.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private ManageUserView(String theJsp) {
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

  /**
   * 
   */
  public static enum ConfirmUserView {
    
    /**
     */
    adminManageUser("adminManageUser.jsp"),
    
    /**
     */
    adminConfirmUserRemote("adminConfirmUserRemote.jsp"),

    /**
     */
    adminConfirmUser("adminConfirmUser.jsp"),

    /**
     */
    index("index.jsp"),
    
    /**
     */
    admin("admin.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private ConfirmUserView(String theJsp) {
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

  /**
   * 
   */
  public static enum ConfirmUserRemoteView {
    
    /**
     */
    adminManageUser("adminManageUser.jsp"),
    
    /**
     */
    adminConfirmUserRemote("adminConfirmUserRemote.jsp"),
  
    /**
     */
    index("index.jsp"),
    
    /**
     */
    admin("admin.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private ConfirmUserRemoteView(String theJsp) {
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
