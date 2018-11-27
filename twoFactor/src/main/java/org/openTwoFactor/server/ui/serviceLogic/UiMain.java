/**
 * @author mchyzer
 * $Id: UiMain.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import java.io.File;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.validator.routines.EmailValidator;
import org.openTwoFactor.server.TwoFactorLogic;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorAuditView;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.beans.TwoFactorReport;
import org.openTwoFactor.server.beans.TwoFactorReportPrivilege;
import org.openTwoFactor.server.beans.TwoFactorReportRollup;
import org.openTwoFactor.server.beans.TwoFactorReportType;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.dojo.DojoComboDataResponse;
import org.openTwoFactor.server.dojo.DojoComboDataResponseItem;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.email.TwoFactorEmail;
import org.openTwoFactor.server.encryption.TwoFactorOath;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.ui.UiServiceLogicBase;
import org.openTwoFactor.server.ui.beans.TextContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorOneTimePassRow;
import org.openTwoFactor.server.ui.beans.TwoFactorProfileContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorReportStat;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorViewReportContainer;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorCallable;
import org.openTwoFactor.server.util.TwoFactorFuture;
import org.openTwoFactor.server.util.TwoFactorPassResult;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.config.TwoFactorTextConfig;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * UI main 
 */
public class UiMain extends UiServiceLogicBase {

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(UiMain.class);

  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void personPicker(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    personPickerHelper(httpServletRequest, httpServletResponse, false, TfSourceUtils.mainSource(), false);
  }
    

  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   * @param allowInactives 
   * @param source 
   * @param isAdmin 
   */
  public static void personPickerHelper(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, boolean allowInactives, Source source, boolean isAdmin) {
    //{
    //  query: {name: "A*"},
    //  queryOptions: {ignoreCase: true},
    //  sort: [{attribute:"name", descending:false}],
    //    start: 0,
    //    count: 10
    //}
    
    //https://server.url/twoFactorMchyzer/twoFactorUi/app/UiMain.personPicker?name=ab*&start=0&count=Infinity

//    TwoFactorServerUtils.printToScreen("{\"label\":\"name\", \"identifier\":\"id\",\"items\":[{\"id\":\"10021368\",\"name\":\"Chris Hyzer (mchyzer, 10021368) (active) Staff - Astt And Information Security - Application Architect (also: Alumni)\"},{\"id\":\"10193029\",\"name\":\"Chyze-Whee Ang (angcw, 10193029) (active) Alumni\"}]}", "application/json", false, false);

    boolean filteringInactives = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.filteringInactives", false);
    
    String query = TwoFactorServerUtils.defaultString(httpServletRequest.getParameter("id"));
    
    boolean isLookup = true;
    
    if (StringUtils.isBlank(query)) {

      isLookup = false;
      
      query = StringUtils.trimToEmpty(httpServletRequest.getParameter("name"));

    }

    DojoComboDataResponse dojoComboDataResponse = null;

    //if there is no *, then looking for a specific name, return nothing since someone just typed something in and left...
    if (!query.contains("*")) {
      
      isLookup = true;
      
    }

    Set<Subject> subjects = new LinkedHashSet<Subject>();
    boolean enterMoreChars = false;
    
    {
      String subjectId = query.endsWith("*") ? query.substring(0, query.length()-1) : query;
      if (!StringUtils.isBlank(subjectId)) {
        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(TfSourceUtils.mainSource(), subjectId, true, false, isAdmin);
                    
        if (subject != null) {
          
          subjects.add(subject);
        }
      }
    }
    
    if (!isLookup) {
    
      //take out the asterisk
      query = StringUtils.replace(query, "*", "");
  
      //if its a blank query, then dont return anything...
      if (query.length() > 1) {
        subjects.addAll(TfSourceUtils.searchPage(source, query, isAdmin));
        
        //dont filter inactive if not need to
        if (filteringInactives && !allowInactives) {
          
          String queryPrepend = 
            TfSourceUtils.mainSource().getInitParam("statusLabel") + "="
            + TfSourceUtils.mainSource().getInitParam("statusAllFromUser");
          
          
          query = queryPrepend + " " + query;
        }
        
      } else {
        enterMoreChars = true;
      }
    }

    if (enterMoreChars) {
      DojoComboDataResponseItem dojoComboDataResponseItem = new DojoComboDataResponseItem(null, 
          TextContainer.retrieveFromRequest().getText().get("comboNotEnoughChars"));
      dojoComboDataResponse = new DojoComboDataResponse(TwoFactorServerUtils.toList(dojoComboDataResponseItem));
    } else {

      if (subjects.size() > 0 && filteringInactives && !allowInactives) {
        //filter out inactives
        Iterator<Subject> iterator = subjects.iterator();
        while (iterator.hasNext()) {
          Subject subject = iterator.next();
          if (!TfSourceUtils.subjectIsActive(subject)) {
            iterator.remove();
          }
        }
      }
      
      if (subjects.size() == 0) {
        dojoComboDataResponse = new DojoComboDataResponse();
      } else {
        
        List<DojoComboDataResponseItem> items = new ArrayList<DojoComboDataResponseItem>();
  
        //convert subject to item
        for (Subject subject : subjects) {
          
          //description could be null?
          String description = TfSourceUtils.subjectDescription(subject, null);
          
          DojoComboDataResponseItem item = new DojoComboDataResponseItem(subject.getId(), description);
          items.add(item);
          
        }
        
        dojoComboDataResponse = new DojoComboDataResponse(
          TwoFactorServerUtils.toArray(items, DojoComboDataResponseItem.class));
  
      }  
    }
    String json = TwoFactorServerUtils.jsonConvertTo(dojoComboDataResponse, false);
    
    //write json to screen
    TwoFactorServerUtils.printToScreen(json, "application/json", false, false);
    
  }
  
  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void index(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.use.index2", true)) {
      index2(httpServletRequest, httpServletResponse);
      return;
    }

    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
    
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(TfSourceUtils.mainSource(), TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
    
    showJsp("twoFactorIndex.jsp");

  }

  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  private void index2(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
    
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(TfSourceUtils.mainSource(), TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
    
    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("twoFactorIndex2.jsp");

  }

  /**
   * see if user is active, if not print a message
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param subjectSource 
   * @return if user cant login
   */
  public boolean userCantLoginNotActiveLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, Source subjectSource) {
    
    //maybe we arent filtering inactives
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.filteringInactives", false)) {
      return false;
    }
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorUser.isOptedIn()) {
      
      Subject subject =  TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
          twoFactorUser.getLoginid(), true, false, false);
      
      if (subject == null || !TfSourceUtils.subjectIsActive(subject)) {
        
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("cantOptInSinceNotActive"));
        return true;
      }
      
      
    }
    
    return false;
    
  }
  
  /**
   * Main screen for lite admin
   * @param httpServletRequest 
   * @param httpServletResponse 
   */
  public void liteAdmin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    liteAdminLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("liteAdmin.jsp");

  }

  /**
   * Main screen for lite admin
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   * @param subjectSource
   */
  public void liteAdminLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {

    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
        
        TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorUserUsingApp.setSubjectSource(subjectSource);

        if (!TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorAdminContainer().isCanLiteAdmin()) {
          throw new RuntimeException("Not allowed to lite admin! " + twoFactorUserUsingApp.getLoginid());
        }
        
        return null;
      }
    });

  }

  /**
   * Main screen for lite admin
   * @param httpServletRequest 
   * @param httpServletResponse 
   */
  public void liteAdminSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String netId = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("netIdName");

    String checkedIdString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("checkedIdName");
    
    String disclaimerString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("disclaimerName");
    
    
    Source subjectSource = TfSourceUtils.mainSource();

    liteAdminSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), netId, subjectSource, checkedIdString, disclaimerString);

    index(httpServletRequest, httpServletResponse);
  }

  /**
   * Main screen for lite admin
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   * @param netIdGettingCode 
   * @param subjectSource
   * @param checkedIdString 
   * @param disclaimerString
   */
  public void liteAdminSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String netIdGettingCode, final Source subjectSource, final String checkedIdString, final String disclaimerString) {

    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];
    
    final TwoFactorUser[] twoFactorUserGettingCode = new TwoFactorUser[1];

    boolean success = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        boolean innerSuccess = false;
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
        
        twoFactorUserUsingApp[0] = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorUserUsingApp[0].setSubjectSource(subjectSource);
        
        twoFactorUserUsingApp[0] = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorUserUsingApp[0].setSubjectSource(subjectSource);

        if (!TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorAdminContainer().isCanLiteAdmin()) {
          throw new RuntimeException("Not allowed to lite admin! " + twoFactorUserUsingApp[0].getLoginid());
        }

        //make sure they have allowed people to opt them out
        if (!StringUtils.equals("true", checkedIdString)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("liteAdminNeedToAuthenticate"));
          return false;
        }

        if (!StringUtils.equals("true", disclaimerString)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("liteAdminNeedToDisclaimer"));
          return false;
        }

        //if invalid uuid, something fishy is going on
        if(!alphaNumericMatcher.matcher(netIdGettingCode).matches()) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("liteAdminInvalidNetId"));
          return false;
        }
        
        Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, netIdGettingCode, true, false, true);
        
        if (subject == null) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("liteAdminInvalidNetId"));
          return false;
        }
        
        twoFactorUserGettingCode[0] = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, subject.getId());

        if (twoFactorUserGettingCode[0] == null) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("liteAdminInvalidNetId"));
          return false;
        }

        twoFactorUserGettingCode[0].setSubjectSource(subjectSource);

        //make sure they have allowed people to opt them out
        if (!twoFactorUserGettingCode[0].isInvitedColleaguesWithinAllottedTime()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("liteAdminErrorUserDidntAllow"));
          return false;
        }

        twoFactorUserGettingCode[0].setTwoFactorSecretTemp(null);
        
        if (StringUtils.isBlank(twoFactorUserGettingCode[0].getTwoFactorSecret()) || !twoFactorUserGettingCode[0].isOptedIn()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendWarnNotOptedIn"));
          return false;
        }
          
        innerSuccess = true;
        
        //store code and when sent
        String secretCode = Integer.toString(new SecureRandom().nextInt(1000000));
        
        //make this since 9 since thats what duo is
        secretCode = StringUtils.leftPad(secretCode, 9, '0');

        //maybe going from duo
        //opt in to duo
        if (UiMain.duoRegisterUsers()) {

          secretCode = DuoCommands.duoBypassCodeBySomeId(twoFactorUserGettingCode[0].getLoginid());
          
        }

        twoFactorUserGettingCode[0].setPhoneCodeUnencrypted(secretCode);

        twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setTwoFactorUserFriend(twoFactorUserGettingCode[0]); 

        twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setCodeForFriend(secretCode);
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("liteAdminCodeSuccess"));
        
        twoFactorUserGettingCode[0].setDatePhoneCodeSent(System.currentTimeMillis());
  
        twoFactorUserGettingCode[0].setDateInvitedColleagues(null);
        
        twoFactorUserGettingCode[0].store(twoFactorDaoFactory);
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.GENERATED_CODE_AS_A_LITE_ADMIN, ipAddress, 
            userAgent, twoFactorUserUsingApp[0].getUuid(), twoFactorUserUsingApp[0].getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("liteAdminCodeAuditDescriptionForFriendPrefix") 
              + " " + twoFactorUserGettingCode[0].getName() +  " (" + twoFactorUserGettingCode[0].getLoginid() + ")", null);
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.LITE_ADMIN_GENERATED_ME_A_CODE, ipAddress, 
            userAgent, twoFactorUserGettingCode[0].getUuid(), twoFactorUserUsingApp[0].getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("liteAdminCodeAuditDescriptionPrefix")
            + " " + twoFactorUserUsingApp[0].getName() +  " (" + twoFactorUserUsingApp[0].getLoginid() + ")", null);

        return innerSuccess;
      }
    });

    String userEmailLoggedIn = null;
    
    String userEmailColleague = null;
    
    try {
      //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
      if (success && subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForGenerateCodeByLiteAdmin", true)) {
        
        Subject sourceSubjectLoggedIn = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        Subject sourceSubjectPersonPicked = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            twoFactorUserGettingCode[0].getLoginid(), true, false, true);
        
        userEmailLoggedIn = UiMain.retrieveUserEmail(sourceSubjectLoggedIn, twoFactorUserUsingApp[0]);
  
        userEmailColleague = UiMain.retrieveUserEmail(sourceSubjectPersonPicked, twoFactorUserGettingCode[0]);
        
        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailGenerateCodeByAdminSubject");
        subject = TextContainer.massageText("emailGenerateCodeByAdminSubject", subject);
  
        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailGenerateCodeByAdminBody");
        body = TextContainer.massageText("emailGenerateCodeByAdminBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.adminGenerateCodes");
        
        TwoFactorEmail twoFactorMail = new TwoFactorEmail();
        
        boolean sendEmail = true;
        boolean sendToFriend = true;
        //there is no email address????
        if (StringUtils.isBlank(userEmailColleague)) {
          sendToFriend = false;
          LOG.warn("Did not send email to logged in user: " + userEmailColleague + ", no email address...");
          if (StringUtils.isBlank(bccsString)) {
            sendEmail = false;
          } else {
            twoFactorMail.addTo(bccsString);
          }
        } else {
          twoFactorMail.addTo(userEmailColleague);
          twoFactorMail.addBcc(bccsString);
        }
        
        if (sendToFriend && StringUtils.isBlank(userEmailLoggedIn)) {
          LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
        } else {
          twoFactorMail.addBcc(userEmailLoggedIn);
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
  }
  
  /**
   * see if too many users, and this user has never opted in and set a message if so
   * @param subjectSource 
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @return if too many users
   */
  public boolean hasTooManyUsersLockoutLogic(Source subjectSource, final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    int maxRegistrations = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.max.registrations", -1);
    if (maxRegistrations < 0) {
      return false;
    }
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (twoFactorUser.isOptedIn()) {
      return false;
    }
      
    //lets see how many opted in users there are
    int usersOptedIn = twoFactorDaoFactory.getTwoFactorUser().retrieveCountOfOptedInUsers(); 
    
    if (usersOptedIn < maxRegistrations) {
      return false;
    }
      
    //lets see if this user has ever opted in
    int countOfOptinsOptouts = twoFactorUser == null ? 0 
        : twoFactorDaoFactory.getTwoFactorAudit().retrieveCountOptinOptouts(twoFactorUser.getUuid());
    
    //if the user has never opted in...
    if (countOfOptinsOptouts > 0) {
      return false;
    }

    //lets see if the user is an admin... if the user is an admin, let them in
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    if (twoFactorUserLoggedIn.isAdmin()) {
      return false;
    }
    
    //see if they are in a whitelist...
    //  # if there should be a list of people who can opt in even if the max registrations have been met
    //  # can be a subjectId or a netId, comma separated
    //  twoFactorServer.alwaysAllowed.registrationUserIds =
    
    String registrationUserIdsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.alwaysAllowed.registrationUserIds");
    
    if (!StringUtils.isBlank(registrationUserIdsString)) {
      Set<String> registrationUserIds = TwoFactorServerUtils.splitTrimToSet(registrationUserIdsString, ",");
      if (TfSourceUtils.subjectIdOrNetIdInSet(subjectSource, twoFactorUserLoggedIn.getLoginid(), registrationUserIds)
          || TfSourceUtils.subjectIdOrNetIdInSet(subjectSource, loggedInUser, registrationUserIds) ) {
        return false;
      }
    }
    
    //set an error
    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("cantOptInSinceTooManyUsers"));
    return true;
    
  }
  
  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
    //InetAddress ia = null; //InetAddress.getByAddress(new byte[] {(byte)130,91,(byte)219,(byte)176});
    ////ia = InetAddress.getByAddress(new byte[] {(byte)176, (byte)219, 91, (byte)130});
    //// or 
    //ia = InetAddress.getByName("130.91.219.176");
    //System.out.println(ia.getCanonicalHostName());
    

  }

  
  /**
   * show user audits
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void userAudits(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    userAuditsLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("userAudits.jsp");
  }

  /**
   * show recent history of the current user
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource 
   */
  public void userAuditsLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
        final TwoFactorRequestContainer twoFactorRequestContainer,
        final String loggedInUser, final String ipAddress, 
        final String userAgent, final Source subjectSource) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    //generate the codes
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    auditHelper(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorUser, subjectSource);

  }

  /**
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param twoFactorUser
   * @param subjectSource 
   */
  public static void auditHelper(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      TwoFactorUser twoFactorUser, final Source subjectSource) {
    
    if (subjectSource != null) {
      
      //resolve subject
      Subject subject =  TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
          twoFactorUser.getLoginid(), true, false, true);
      if (subject != null) {
        
        //try the name
        if (!StringUtils.isBlank(subject.getName())) {
          twoFactorRequestContainer.getTwoFactorAuditContainer().setUserString(subject.getName());
        }
      }
      //default to the user id
      if (StringUtils.isBlank(twoFactorRequestContainer.getTwoFactorAuditContainer().getUserString())) {
        twoFactorRequestContainer.getTwoFactorAuditContainer().setUserString(twoFactorUser.getLoginid());
      }
    }

    
    //get the most recent 200 items, note a daemon might delete old records
    TfQueryOptions tfQueryOptions = new TfQueryOptions().paging(200, 1, true);
    tfQueryOptions.sortDesc("the_timestamp");

    List<TwoFactorAuditView> twoFactorAuditViews = twoFactorDaoFactory
      .getTwoFactorAudit().retrieveByUser(twoFactorUser.getUuid(), 
        tfQueryOptions);

    for (TwoFactorAuditView twoFactorAuditView : TwoFactorServerUtils.nonNull(twoFactorAuditViews)) {
      twoFactorAuditView.setSubjectSource(subjectSource);
    }
    
    int count = tfQueryOptions.getQueryPaging().getTotalRecordCount();
    if (count < 0) {
      throw new RuntimeException("Why is there no count for audits retrieved???");
    }
    twoFactorRequestContainer.getTwoFactorAuditContainer().setTwoFactorAuditViewsTotalCount(count);
    twoFactorRequestContainer.getTwoFactorAuditContainer().setTwoFactorAuditViewsDisplayedCount(TwoFactorServerUtils.length(twoFactorAuditViews));
    
    twoFactorRequestContainer.getTwoFactorAuditContainer().setTwoFactorAuditViews(twoFactorAuditViews);
  }

  /**
   * show one time codes
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void showOneTimeCodes(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    boolean success = showOneTimeCodesLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));

    if (success) {

      showJsp("showOneTimeCodes.jsp");

    } else {
      index(httpServletRequest, httpServletResponse);
    }
  }

  /**
   * show one time codes
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean showOneTimeCodesLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    boolean result = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
        //generate the codes
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("codesErrorAlreadyOptedIn"));
          return false;
        }
        
        setupOneTimeCodesOnOptin(twoFactorDaoFactory, twoFactorUser, twoFactorRequestContainer, ipAddress, userAgent);
        
        return true;
      }
    });
    
    return result;
  }
  
  /**
   * duo push test
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushTest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    boolean success = duoPushTestLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));

    if (success) {

      showJsp("duoPush.jsp");

    } else {
    
      index(httpServletRequest, httpServletResponse);
    }
  }

  /**
   * duo push test
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean duoPushTestLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()
        || StringUtils.isBlank(twoFactorUser.getDuoPushPhoneId())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }

    String txId = DuoCommands.duoInitiatePushByPhoneId(twoFactorUser.getDuoUserId(), 
        twoFactorUser.getDuoPushPhoneId(), null, null);
    
    if (StringUtils.isBlank(txId)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }

    for (int i=0;i<20;i++) {
    
      TwoFactorServerUtils.sleep(1000);

      if (DuoCommands.duoPushOrPhoneSuccess(txId, null)) {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushTestSuccess"));

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.DUO_ENABLE_PUSH_TEST_SUCCESS, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return true;
      }
      
    }
    
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.DUO_ENABLE_PUSH_TEST_FAILURE, ipAddress, 
        userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
    
    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushTestFailure"));
    return true;
  }

  /**
   * duo push unenroll.
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushChangePhone(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    boolean success = duoPushChangePhoneLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));

    if (success) {

      showJsp("optinAppInstall2.jsp");

    } else {
    
      index(httpServletRequest, httpServletResponse);
    }

  }

  /**
   * unenroll from duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean duoPushUnenrollLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }

    JSONObject duoUser = twoFactorRequestContainer.getTwoFactorDuoPushContainer().getDuoUser();
    
    String phoneId = DuoCommands.duoPushPhoneId(duoUser);
    
    twoFactorUser.setDuoPushPhoneId(null);
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());

    DuoCommands.deleteDuoPhone(phoneId);

    //clear this out so we get a clear shake
    twoFactorRequestContainer.setTwoFactorDuoPushContainer(null);
    
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.DUO_DISABLE_PUSH, ipAddress, 
        userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);

    //init again since not enrolled
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushUnenrollSuccess"));

    return true;
  }

  /**
   * duo push enroll.
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushEnroll(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    boolean success = duoPushEnrollLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), true, subjectSource);

    if (success) {

      showJsp("duoPush.jsp");

    } else {
    
      index(httpServletRequest, httpServletResponse);
    }
  }

  /**
   * enroll to duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param requireOptIn 
   * @param subjectSource
   * @return true if ok, false if not
   */
  public boolean duoPushEnrollLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, boolean requireOptIn, final Source subjectSource) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (requireOptIn && (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted()))) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    if (twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()) {
      if (!requireOptIn) {
        twoFactorUser.setDuoPushByDefault(false);
        twoFactorUser.store(twoFactorDaoFactory);
      } else {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorAlreadyInPush"));
        return true;
      }
    }

    String barcode = DuoCommands.enrollUserInPushBySomeId(twoFactorUser.getUuid(), requireOptIn);

    twoFactorRequestContainer.getTwoFactorDuoPushContainer().setEnrolling(true);
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().setBarcodeUrl(barcode);

    //barcode is e.g. https://api-84f782e2.duosecurity.com/frame/qr?value=duo%3A%2F%2F6nlhjDGKLCXz05eW0PSh-YXBpLTg0Zjc4MmUyLmR1b3NlY3VyaXR5LmNvbQ
    //get the url from that
    String duoUrlText = TwoFactorServerUtils.prefixOrSuffix(barcode, "?value=", false);
    //url decode
    duoUrlText = TwoFactorServerUtils.escapeUrlDecode(duoUrlText);
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().setDuoQrUrlText(duoUrlText);
    
    String twoStepLogQrCodesPath = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoStepLogQrCodesPath");
    if (!TwoFactorServerUtils.isBlank(twoStepLogQrCodesPath)) {
      
      twoStepLogQrCodesPath = TwoFactorServerUtils.stripLastSlashIfExists(twoStepLogQrCodesPath);
      
      String loginId = TfSourceUtils.convertSubjectIdToNetId(subjectSource, loggedInUser, false);

      File twoStepLogQrCodesPathFile = new File(twoStepLogQrCodesPath);
      TwoFactorServerUtils.mkdirs(twoStepLogQrCodesPathFile);
      
      Date currentDate = new Date();
      File twoStepLogQrCodesLogFile = new File(twoStepLogQrCodesPath 
          + File.separatorChar + "twoStepQrLog_" + loginId + "__" 
          + TwoFactorServerUtils.timestampToFileString(currentDate) + ".txt");
      
      String contents = "Loginid: " + loginId +  "\nBarcode: " + barcode + "\nDuoUrlText: " + duoUrlText + "\nCurrent date: " + currentDate;
      TwoFactorServerUtils.saveStringIntoFile(twoStepLogQrCodesLogFile, contents);
    }
    
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.DUO_ENABLE_PUSH, ipAddress, 
        userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);

    //if we are enrolling for the first time, default to on
    if (twoFactorUser.getDuoPushByDefault() == null) {

      twoFactorUser.setDuoPushByDefault(true);
      twoFactorUser.store(twoFactorDaoFactory);
      
      TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
          TwoFactorAuditAction.DUO_ENABLE_PUSH_FOR_WEB, ipAddress, 
          userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
      
    }
    
    //init again since not enrolled
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    if (requireOptIn) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushEnrolling"));
    }
    
    return true;
  }

  /**
   * duo push enroll.
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushEnroll2(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    boolean success = duoPushEnroll2Logic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    if (success) {
      showJsp("optinAppInstall2.jsp");
    } else {
      index(httpServletRequest, httpServletResponse);
    }
  }

  /**
   * enroll to duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @return true if ok, false if not
   */
  public boolean duoPushEnroll2Logic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
    
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (userOk && !twoFactorUser.isOptedIn()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("index2ErrorNotOptedIn"));
      userOk = false;
    }
    
    if (!userOk) {
      return false;
    }

    return true;
  }

  /**
   * enroll in push for web
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushEnrollWeb(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    boolean success = duoPushEnrollWebLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));

    if (success) {

      showJsp("duoPush.jsp");

    } else {
    
      index(httpServletRequest, httpServletResponse);
    }
  }

  /**
   * unenroll in push for web
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushUnenrollWeb(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    boolean success = duoPushUnenrollWebLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));

    if (success) {

      showJsp("duoPush.jsp");

    } else {
    
      index(httpServletRequest, httpServletResponse);
    }
  }

  /**
   * enroll in push for web
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean duoPushEnrollWebLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }

    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        twoFactorUser.setDuoPushByDefault(true);
        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.DUO_ENABLE_PUSH_FOR_WEB, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return null;
      }
    });
    
    //init again
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushEnrollWebSuccess"));

    return true;
  }

  /**
   * enroll in push for web
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean duoPushUnenrollWebLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }

    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        twoFactorUser.setDuoPushByDefault(false);
        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.DUO_DISABLE_PUSH_FOR_WEB, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return null;
      }
    });
    
    //init again since not enrolled
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushUnenrollWebSuccess"));

    return true;
  }

  
  /**
   * duo push main screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPush(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    boolean success = duoPushLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), true);

    if (success) {

      showJsp("duoPush.jsp");

    } else {
    
      index(httpServletRequest, httpServletResponse);
    }
  }

  /**
   * manage duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param requireTwoFactorSecret if needs to require opt in
   * @return true if ok, false if not
   */
  public boolean duoPushLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, boolean requireTwoFactorSecret) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }

    if (requireTwoFactorSecret && !twoFactorUser.isOptedIn()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    if (requireTwoFactorSecret && StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
        
    return true;
  }
  
  
  /**
   * optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optin(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    OptinView optinView = optinLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    showJsp(optinView.getJsp());
    
  }

  /**
   * optout of the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optout(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    optoutLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    index(httpServletRequest, httpServletResponse);
  }

  /**
   * add phone or device
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addPhoneOrDevice(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    AddPhoneOrDeviceView addPhoneOrDeviceView = addPhoneOrDeviceLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp(addPhoneOrDeviceView.getJsp());
  }

  /**
   * view from add phone or device
   */
  public static enum AddPhoneOrDeviceView {
    
    /**
     */
    addPhoneOrDevice("addPhoneOrDevice.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp");

    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private AddPhoneOrDeviceView(String theJsp) {
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
   * add phone or device logic
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress
   * @param userAgent
   * @param subjectSource 
   * @return which view to go to
   * 
   */
  private AddPhoneOrDeviceView addPhoneOrDeviceLogic(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, final String userAgent,
      final Source subjectSource) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    if (!twoFactorUser.isOptedIn()) {

      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneOrDeviceNotOptedIn"));

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return AddPhoneOrDeviceView.index;
    }
    
    return AddPhoneOrDeviceView.addPhoneOrDevice;
  }

  /**
   * add phone to the profile
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addPhone(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    AddPhoneView addPhoneView = addPhoneLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource, true);

    showJsp(addPhoneView.getJsp());
  }

  /**
   * view from add phone
   */
  public static enum AddPhoneView {
    
    /**
     */
    addPhone("addPhone.jsp") {

      /**
       * @see AddPhoneView#toAddPhoneTestView()
       */
      @Override
      public AddPhoneTestSubmitView toAddPhoneTestView() {
        return AddPhoneTestSubmitView.addPhone;
      }

    },
    
    /**
     */
    index("twoFactorIndex2.jsp") {

      /**
       * @see AddPhoneView#toAddPhoneTestView()
       */
      @Override
      public AddPhoneTestSubmitView toAddPhoneTestView() {
        
        return AddPhoneTestSubmitView.index;
      }

    };

    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private AddPhoneView(String theJsp) {
      this.jsp = theJsp;
    }
    
    /**
     * 
     * @return jsp
     */
    public String getJsp() {
      return this.jsp;
    }
    
    /**
     * convert to add phone test view
     * @return the view for the test click
     */
    public abstract AddPhoneTestSubmitView toAddPhoneTestView();
    
  }

  /**
   * add phone
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress
   * @param userAgent
   * @param subjectSource 
   * @param audit if should audit
   * @return which view to go to
   * 
   */
  private AddPhoneView addPhoneLogic(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, final String userAgent,
      final Source subjectSource, final boolean audit) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    twoFactorUser.setSubjectSource(subjectSource);

    if (!twoFactorUser.isOptedIn()) {

      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneOrDeviceNotOptedIn"));

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return AddPhoneView.index;
    }
    
    String imageId = TwoFactorServerUtils.uuid();
    
    twoFactorRequestContainer.getTwoFactorAddPhoneContainer().setQrCodeUniqueId(imageId);
    
    String userEmail = null;
    try {

      MultiKey multiKey = new MultiKey(loggedInUser, imageId);
      
      //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
      if (subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForAddPhone", true)) {
        
        Subject sourceSubject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        
        userEmail = retrieveUserEmail(sourceSubject, twoFactorUser);
        
        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailAddPhoneSubject");
        subject = TextContainer.massageText("emailAddPhoneSubject", subject);

        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailAddPhoneBody");
        body = TextContainer.massageText("emailAddPhoneBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.optins");
        
        TwoFactorEmail twoFactorMail = new TwoFactorEmail();
        
        boolean sendEmail = true;
        
        //there is no email address????
        if (StringUtils.isBlank(userEmail)) {
          LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
          if (StringUtils.isBlank(bccsString)) {
            sendEmail = false;
          } else {
            twoFactorMail.addTo(bccsString);
          }
        } else {
          twoFactorMail.addTo(userEmail);
          twoFactorMail.addBcc(bccsString);
        }

        if (sendEmail) {
          twoFactorMail.assignBody(body);
          twoFactorMail.assignSubject(subject);
          twoFactorMail.send();

        }

      }
      //add to cache
      addPhoneSecretEmailCache().put(multiKey, Boolean.TRUE);
      
      if (audit) {
        //audit to keep track of what happened
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADD_PHONE, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
      }
      
    } catch (Exception e) {
      //non fatal, just log this
      LOG.error("Error sending email to: " + userEmail + ", loggedInUser id: " + loggedInUser, e);

      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneError"));

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      //if you cant send email, dont do it
      return AddPhoneView.index;
    }
    
    return AddPhoneView.addPhone;
  }

  /**
   * @param sourceSubject
   * @param twoFactorUser
   * @return the emails
   */
  public static String retrieveUserEmail(Subject sourceSubject, TwoFactorUser twoFactorUser) {
    
    StringBuilder userEmail = new StringBuilder();
    
    String emailAddressFromSubject = TfSourceUtils.retrieveEmail(sourceSubject);
    String emailAddressFromDatabase = twoFactorUser.getEmail0();

    if (!StringUtils.isBlank(emailAddressFromSubject)) {
      userEmail.append(emailAddressFromSubject);
    }
    
    if (!StringUtils.equalsIgnoreCase(emailAddressFromSubject, emailAddressFromDatabase)) {
      if (!StringUtils.isBlank(emailAddressFromDatabase)) {
        if (userEmail.length() > 0) {
          userEmail.append(", ");
        }
        userEmail.append(emailAddressFromDatabase);
      }
    }
    return userEmail.toString();
  }

  /**
   * dont send two emails for image and secret on screen...  multikey is loginid, and imageId
   */
  private static ExpirableCache<MultiKey, Boolean> addPhoneSecretEmailCache = null;
  
  /**
   * dont send two emails for image and secret on screen...  key is loginid and imageId
   * cache lazy loaded
   * @return the cache, lazy loaded
   */
  private static ExpirableCache<MultiKey, Boolean> addPhoneSecretEmailCache() {
    if (addPhoneSecretEmailCache == null) {
      addPhoneSecretEmailCache = new ExpirableCache<MultiKey, Boolean>(1);
    }
    return addPhoneSecretEmailCache;
  }
    
  /**
   * optout of the service, then start the optin process
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void changeDevice(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    optoutLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    //no matter the result of that, start the optin process
    
    OptinView optinView = optinLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    showJsp(optinView.getJsp());
  }

  
  
  /**
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource 
   * @return the view
   */
  public OptinView optinLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    new UiMainPublic().setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorRequestContainer.getTwoFactorUserLoggedIn());
    
    return optinSetup(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, 
        TwoFactorOath.twoFactorGenerateTwoFactorPass(), subjectSource);
  }
  
  /**
   * show qrCode image
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void qrCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    File qrImageFile = TwoFactorServerUtils.tempFile(".gif", "qrCodes");

    qrCodeFile(qrImageFile);

    TwoFactorServerUtils.sendFileToBrowser(qrImageFile.getAbsolutePath(), false, true, true, null, null, true);

  }


  /**
   * generate qr code file
   * @param qrImageFile 
   */
  private void qrCodeFile(File qrImageFile) {
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
    
    int qrImageWidth = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.qrImageWidth", 400);
        
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    String twoFactorSecret = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();

    String accountName = twoFactorRequestContainer.getTwoFactorProfileContainer().getAccountName();
    
    //http://invariantproperties.com/2011/12/23/using-google-authenticator-totp-on-your-site/
    String uri = "otpauth://totp/" + accountName + "?secret=" + twoFactorSecret;
    TwoFactorServerConfig.retrieveConfig().twoFactorLogic().generateQrFile(uri, qrImageFile, qrImageWidth);
  }

  /**
   * show qrCode image of the actual secret when adding a phone
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void qrCodeSecret(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    File qrImageFile = TwoFactorServerUtils.tempFile(".gif", "qrCodes");

    String imageId = httpServletRequest.getParameter("imageId");

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    MultiKey multiKey = new MultiKey(loggedInUser, imageId);
    
    //see if we just emailed about the use of this image
    if (Boolean.TRUE != addPhoneSecretEmailCache().get(multiKey)) {
      throw new RuntimeException("Not sending QR code to browser since not expecting request!  Reload the containing page.");
    }
    
    qrCodeSecretFile(qrImageFile);

    TwoFactorServerUtils.sendFileToBrowser(qrImageFile.getAbsolutePath(), false, true, true, null, null, true);

  }

  /**
   * generate qr code file
   * @param qrImageFile 
   */
  private void qrCodeSecretFile(File qrImageFile) {
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
    
    int qrImageWidth = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.qrImageWidth", 400);
        
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    String twoFactorSecret = twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted();

    String accountName = twoFactorRequestContainer.getTwoFactorProfileContainer().getAccountName();
    
    //http://invariantproperties.com/2011/12/23/using-google-authenticator-totp-on-your-site/
    String uri = "otpauth://totp/" + accountName + "?secret=" + twoFactorSecret;
    TwoFactorServerConfig.retrieveConfig().twoFactorLogic().generateQrFile(uri, qrImageFile, qrImageWidth);
  }

  /**
   * optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void addPhoneTestSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String twoFactorPass = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");

    Source subjectSource = TfSourceUtils.mainSource();
    
    AddPhoneTestSubmitView addPhoneTestSubmitView = addPhoneTestSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), twoFactorPass, subjectSource);

    showJsp(addPhoneTestSubmitView.getJsp());

  }

  /**
   * When someone opts out their colleague
   * @param httpServletRequest 
   * @param httpServletResponse 
   */
  public void optOutColleague(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    String userIdOperatingOn = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("userIdOperatingOn");

    String checkedApprovalString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("checkedApproval");
    
    
    Source subjectSource = TfSourceUtils.mainSource();

    optOutColleagueLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource, checkedApprovalString);

    showJsp("helpColleague.jsp");

  }

  /**
   * When someone opts out their colleague
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   * @param userIdOperatingOn 
   * @param subjectSource
   * @param userCheckedCheckbox 
   */
  public void optOutColleagueLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource, final String userCheckedCheckbox) {

    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];
    
    final TwoFactorUser[] twoFactorUserGettingOptedOut = new TwoFactorUser[1];

    boolean success = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        boolean innerSuccess = false;
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
        
        twoFactorUserUsingApp[0] = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorUserUsingApp[0].setSubjectSource(subjectSource);
        
        //if invalid uuid, something fishy is going on
        if(!alphaNumericMatcher.matcher(userIdOperatingOn).matches()) {
          throw new RuntimeException("Why is userIdOperatingOn not alphanumeric???? '" + userIdOperatingOn + "'");
        }
        
        twoFactorUserGettingOptedOut[0] = TwoFactorUser.retrieveByUuid(twoFactorDaoFactory, userIdOperatingOn);

        if (twoFactorUserGettingOptedOut[0] == null) {
          throw new RuntimeException("Why is uuid not found??? '" + userIdOperatingOn + "'");
        }

        twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setTwoFactorUserFriend(twoFactorUserGettingOptedOut[0]); 

        twoFactorUserGettingOptedOut[0].setSubjectSource(subjectSource);

        //make sure they have allowed people to opt them out
        if (!twoFactorUserGettingOptedOut[0].isInvitedColleaguesWithinAllottedTime()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserDidntAllow"));
          return false;
        }


        //make sure they have allowed people to opt them out
        if (!StringUtils.equals("true", userCheckedCheckbox)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserDidntCheckCheckbox"));
          return false;
        }
        
        //make sure the user has identified this user to opt them out
        if (!StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingOptedOut[0].getColleagueUserUuid0())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingOptedOut[0].getColleagueUserUuid1())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingOptedOut[0].getColleagueUserUuid2())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingOptedOut[0].getColleagueUserUuid3())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingOptedOut[0].getColleagueUserUuid4()) ) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserNotFriend"));
          return false;
          
        }
        
        
        twoFactorUserGettingOptedOut[0].setTwoFactorSecretTemp(null);
        
        if (StringUtils.isBlank(twoFactorUserGettingOptedOut[0].getTwoFactorSecret()) || !twoFactorUserGettingOptedOut[0].isOptedIn()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendWarnNotOptedIn"));
          return false;
        }
          
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendSuccess"));
        innerSuccess = true;
        
        twoFactorUserGettingOptedOut[0].setTwoFactorSecret(null);
        
        twoFactorUserGettingOptedOut[0].setOptedIn(false);
        
        duoClearOutAttributes(twoFactorUserGettingOptedOut[0]);

        twoFactorUserGettingOptedOut[0].setDateInvitedColleagues(null);
        
        twoFactorUserGettingOptedOut[0].store(twoFactorDaoFactory);

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTED_OUT_A_COLLEAGUE, ipAddress, 
            userAgent, twoFactorUserUsingApp[0].getUuid(), twoFactorUserUsingApp[0].getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("helpFriendAuditDescriptionPrefix") 
              + " " + twoFactorUserGettingOptedOut[0].getName() +  " (" + twoFactorUserGettingOptedOut[0].getLoginid() + ")", null);

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.COLLEAGUE_OPTED_ME_OUT, ipAddress, 
            userAgent, twoFactorUserGettingOptedOut[0].getUuid(), twoFactorUserUsingApp[0].getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("helpFriendAuditDescriptionForFriendPrefix")
            + " " + twoFactorUserUsingApp[0].getName() +  " (" + twoFactorUserUsingApp[0].getLoginid() + ")", null);

        return innerSuccess;
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
      if (success && subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForOptoutFriend", true)) {
        
        Subject sourceSubjectLoggedIn = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        Subject sourceSubjectColleaguePicked = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            twoFactorUserGettingOptedOut[0].getLoginid(), true, false, true);
        
        userEmailLoggedIn = UiMain.retrieveUserEmail(sourceSubjectLoggedIn, twoFactorUserUsingApp[0]);
        
        userEmailColleague = UiMain.retrieveUserEmail(sourceSubjectColleaguePicked, twoFactorUserGettingOptedOut[0]);
        
        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptOutFriendSubject");
        subject = TextContainer.massageText("emailOptOutFriendSubject", subject);

        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptOutFriendBody");
        body = TextContainer.massageText("emailOptOutFriendBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.friendOptouts");
        
        TwoFactorEmail twoFactorMail = new TwoFactorEmail();
        
        boolean sendEmail = true;
        boolean sendToFriend = true;
        //there is no email address????
        if (StringUtils.isBlank(userEmailColleague)) {
          sendToFriend = false;
          LOG.warn("Did not send email to logged in user: " + userEmailColleague + ", no email address...");
          if (StringUtils.isBlank(bccsString)) {
            sendEmail = false;
          } else {
            twoFactorMail.addTo(bccsString);
          }
        } else {
          twoFactorMail.addTo(userEmailColleague);
          twoFactorMail.addBcc(bccsString);
        }
        
        if (sendToFriend && StringUtils.isBlank(userEmailLoggedIn)) {
          LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
        } else {
          twoFactorMail.addCc(userEmailLoggedIn);
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
    
    helpColleagueLogic(twoFactorDaoFactory, 
        twoFactorRequestContainer,
        loggedInUser, subjectSource);

  }

  /**
   * draw the screen to help a colleague
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void helpColleague(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    helpColleagueLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, subjectSource);
    
    showJsp("helpColleague.jsp");
    
  }
  
  /**
   * logic to help a colleague opt out
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param loggedInUser
   * @param subjectSource 
   */
  public void helpColleagueLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, Source subjectSource) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    List<TwoFactorUser> usersWhoPickedThisUserToOptOut = twoFactorDaoFactory.getTwoFactorUser().retrieveUsersWhoPickedThisUserToOptThemOut(twoFactorUser.getUuid());
    
    boolean hasAuthorized = false;
    boolean hasNotAuthorized = false;
    
    for (int i=0;i<TwoFactorServerUtils.length(usersWhoPickedThisUserToOptOut);i++) {
      TwoFactorUser current = usersWhoPickedThisUserToOptOut.get(i);
      current = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, current.getLoginid());
      current.setSubjectSource(subjectSource);
      usersWhoPickedThisUserToOptOut.set(i, current);
      
      if (current.isInvitedColleaguesWithinAllottedTime()) {
        hasAuthorized = true;
      } else {
        hasNotAuthorized = true;
      }
      
    }
    
    twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setColleaguesIdentifiedUser(usersWhoPickedThisUserToOptOut);
    twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setHasColleaguesAuthorizedUser(hasAuthorized);
    twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setHasColleaguesNotAuthorizedUser(hasNotAuthorized);
    
    twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setHasColleaguesIdentifiedUser(TwoFactorServerUtils.length(usersWhoPickedThisUserToOptOut) > 0);
    
  }
  
  /**
   * 
   */
  public static enum OptinTestSubmitView {
    
    /**
     */
    optin("optin.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinSuccess("optinSuccess.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinTestSubmitView(String theJsp) {
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
  public static enum AddPhoneTestSubmitView {
    
    /**
     */
    addPhone("addPhone.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private AddPhoneTestSubmitView(String theJsp) {
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
  public static enum OptinView {
    
    /**
     */
    optin("optin.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),

    /**
     */
    profile("profile.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinView(String theJsp) {
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
  public static enum OptinWizardWelcomeView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),

    /**
     */
    profile("profile.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardWelcomeView(String theJsp) {
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
   * matcher for numbers and whitespace
   */
  private static Pattern numberMatcher = Pattern.compile("^[0-9 ]+$");

  /**
   * matcher for numbers and whitespace
   */
  private static Pattern alphaNumericMatcher = Pattern.compile("^[0-9a-zA-Z ]+$");

  /**
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param twoFactorPass 
   * @param subjectSource
   * @param serialNumber if opting in by serial number, this is the serial number
   * @param optinBySerialNumber true to optin by serial number
   * @param SUBMITTED_BIRTHDAY_MONTH 
   * @param SUBMITTED_BIRTHDAY_DAY 
   * @param SUBMITTED_BIRTHDAY_YEAR 
   * @param birthdayTextfield 
   * @return error message if there is one and jsp
   */
  public OptinTestSubmitView optinTestSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String twoFactorPass, final Source subjectSource, 
      final String serialNumber, final boolean optinBySerialNumber, final String SUBMITTED_BIRTHDAY_MONTH,
      final String SUBMITTED_BIRTHDAY_DAY, final String SUBMITTED_BIRTHDAY_YEAR, final String birthdayTextfield) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {

      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinTestSubmitView.index;
    }

    OptinTestSubmitView result =  (OptinTestSubmitView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        String twoFactorSecret = null;

        if (optinBySerialNumber) {
          twoFactorRequestContainer.getTwoFactorAdminContainer().setShowSerialSection(true);
        }
        
        if (!checkBirthday(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, 
            userAgent, subjectSource, twoFactorUser, 
            SUBMITTED_BIRTHDAY_MONTH, SUBMITTED_BIRTHDAY_DAY, 
            SUBMITTED_BIRTHDAY_YEAR, birthdayTextfield)) {
          return OptinTestSubmitView.optin;
        }
        
        if (StringUtils.isBlank(twoFactorPass)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCodeRequired"));
          return OptinTestSubmitView.optin;
        }

        TwoFactorDeviceSerial twoFactorDeviceSerial = null;

        if (optinBySerialNumber) {
          
          if (StringUtils.isBlank(serialNumber)) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialRequired"));
            twoFactorRequestContainer.getTwoFactorAdminContainer().setShowSerialSection(true);
            return OptinTestSubmitView.optin;
            
          }

          //lets make sure the serial number exists
          twoFactorDeviceSerial = TwoFactorDeviceSerial.retrieveBySerial(twoFactorDaoFactory, serialNumber);
          
          if (twoFactorDeviceSerial == null) {
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialNotFound"));
            twoFactorRequestContainer.getTwoFactorAdminContainer().setShowSerialSection(true);
            return OptinTestSubmitView.optin;
            
          }

          //lets see if this device has been registered to someone else
          if (!StringUtils.isBlank(twoFactorDeviceSerial.getUserUuid()) 
              && !StringUtils.equals(twoFactorDeviceSerial.getUserUuid(), twoFactorUser.getUuid())) {
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialRegisteredToSomeoneElse"));
            twoFactorRequestContainer.getTwoFactorAdminContainer().setShowSerialSection(true);
            return OptinTestSubmitView.optin;
            
          }
 
          twoFactorSecret = twoFactorDeviceSerial.getTwoFactorSecretUnencrypted();
          
        } else {

          twoFactorSecret = twoFactorUser.getTwoFactorSecretTempUnencrypted();

          if (StringUtils.isBlank(twoFactorSecret)) {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinSubmitErrorInconsistent"));
            
            twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
            
            profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
                loggedInUser, ipAddress, 
                userAgent, subjectSource);

            return OptinTestSubmitView.index;
            
          }
        }

        //lets see if the secret is registered to another user
        {
          TwoFactorDeviceSerial twoFactorDeviceSerialTemp = TwoFactorDeviceSerial.retrieveBySecretUnencrypted(
              twoFactorDaoFactory, twoFactorSecret);
          if (twoFactorDeviceSerialTemp != null && !StringUtils.isBlank(twoFactorDeviceSerialTemp.getUserUuid()) 
              && !StringUtils.equals(twoFactorUser.getUuid(), twoFactorDeviceSerialTemp.getUserUuid())) {
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSecretRegisteredToSomeoneElse"));
            return OptinTestSubmitView.optin;
          }

          //if we are registering by secret, and circumventing the serial number, register that one as taken
          if (twoFactorDeviceSerial == null) {
            twoFactorDeviceSerial = twoFactorDeviceSerialTemp;
          }
        }
        
        //validate
        if (!numberMatcher.matcher(twoFactorPass).matches()) {
          
          String loginId = TfSourceUtils.convertSubjectIdToNetId(subjectSource, loggedInUser, false);
          LOG.error("Error for " + loginId + " validating code not number, now: " 
              + System.currentTimeMillis() 
              + ", user-agent: " + userAgent);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get(
              optinBySerialNumber ? "optinErrorCodeInvalidFromSerial" : "optinErrorCodeInvalid"));
          return OptinTestSubmitView.optin;
        }
        
        //no need to validate the password, the password checker will do that
        TwoFactorPassResult twoFactorPassResult = TwoFactorOath.twoFactorCheckPassword(
            twoFactorSecret, twoFactorPass, null, null, null, 0L, null);

        twoFactorUser.setPhoneOptIn(false);
        
        if (!twoFactorPassResult.isPasswordCorrect()) {
          
          String phonePassword = twoFactorUser.getPhoneCodeUnencryptedIfNotExpired();
          if (StringUtils.equals(twoFactorPass, phonePassword)) {
            twoFactorPassResult.setPasswordCorrect(true);
            
            //keep track of the phone opt in
            twoFactorUser.setPhoneOptIn(true);
          }
          
        }
        
        if (!twoFactorPassResult.isPasswordCorrect()) {

          String loginId = TfSourceUtils.convertSubjectIdToNetId(subjectSource, loggedInUser, false);
          LOG.error("Error for " + loginId + " validating code, now: " 
              + System.currentTimeMillis() + ": " + TwoFactorServerUtils.hostname()
              + ", user-agent: " + userAgent);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get(
              optinBySerialNumber ? "optinErrorCodeInvalidFromSerial" : "optinErrorCodeInvalid"));
          return OptinTestSubmitView.optin;
        }
        
        //set the object
        twoFactorUser.setDatePhoneCodeSent(null);
        twoFactorUser.setPhoneCodeEncrypted(null);
        twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorSecret);
        twoFactorUser.setTwoFactorSecretTemp(null);
        twoFactorUser.setOptedIn(true);
        twoFactorUser.setTokenIndex(0L);
        twoFactorUser.setLastTotpTimestampUsed(null);
        duoClearOutAttributes(twoFactorUser);

        if (twoFactorPassResult.getNextHotpIndex() != null) {
          twoFactorUser.setSequentialPassIndex(twoFactorPassResult.getNextHotpIndex());
        }
        if (twoFactorPassResult.getLastTotp30TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp30TimestampUsed());
        }
        if (twoFactorPassResult.getLastTotp60TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp60TimestampUsed());
        }
        if (twoFactorPassResult.getNextTokenIndex() != null) {
          twoFactorUser.setTokenIndex(twoFactorPassResult.getNextTokenIndex());
        }
        twoFactorUser.store(twoFactorDaoFactory);
        
        //opt in to duo
        if (duoRegisterUsers()) {

          DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);

        }

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        //register the device as assigned to the current user, add another audit
        //note, wont be null if registering by secret, but matches a registered serial number
        if (optinBySerialNumber || twoFactorDeviceSerial != null) {
          twoFactorDeviceSerial.setUserUuid(twoFactorUser.getUuid());
          twoFactorDeviceSerial.setWhenRegistered(System.currentTimeMillis());
          twoFactorDeviceSerial.store(twoFactorDaoFactory);
          
          twoFactorRequestContainer.getTwoFactorAdminContainer().setImportSerial(serialNumber);
          
          TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
              TwoFactorAuditAction.REGISTER_FOB_SERIAL_NUMBER, ipAddress, 
              userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), 
              TextContainer.retrieveFromRequest().getText().get("optionStep2auditRegisterFob"),
              null);
        }
        
        return OptinTestSubmitView.optinSuccess;
      }
    });
    
    if (result == OptinTestSubmitView.optinSuccess) {
      
      //opt in to duo
      if (duoRegisterUsers()) {

        DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, true);

      }
      
      emailUserAfterOptin(twoFactorRequestContainer, loggedInUser, subjectSource);
    }    

    return result;
  }


  /**
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param subjectSource
   */
  private static void emailUserAfterOptin(final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final Source subjectSource) {
    String userEmail = null;
    try {
      
      //see if there
      
      //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
      if (subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForOptin", true)) {
        
        Subject sourceSubject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        
        userEmail = retrieveUserEmail(sourceSubject, twoFactorRequestContainer.getTwoFactorUserLoggedIn());
        
        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptInSubject");
        subject = TextContainer.massageText("emailOptInSubject", subject);
 
        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptInBody");
        body = TextContainer.massageText("emailOptInBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.optins");
        
        TwoFactorEmail twoFactorMail = new TwoFactorEmail();
        
        boolean sendEmail = true;
        
        //there is no email address????
        if (StringUtils.isBlank(userEmail)) {
          LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
          if (StringUtils.isBlank(bccsString)) {
            sendEmail = false;
          } else {
            twoFactorMail.addTo(bccsString);
          }
        } else {
          twoFactorMail.addTo(userEmail);
          twoFactorMail.addBcc(bccsString);
        }
        
        if (sendEmail) {
          twoFactorMail.assignBody(body);
          twoFactorMail.assignSubject(subject);
          twoFactorMail.send();
        }
        
      }
      
    } catch (Exception e) {
      //non fatal, just log this
      LOG.error("Error sending email to: " + userEmail + ", loggedInUser id: " + loggedInUser, e);
    }
  }
  
  /**
   * return true if ok and false if problem
   * @param twoFactorDaoFactory 
   * @param twoFactorRequestContainer 
   * @param loggedInUser 
   * @param ipAddress 
   * @param userAgent 
   * @param subjectSource 
   * @param twoFactorUser user
   * @param submittedBirthYearString year 
   * @param submittedBirthMonthString month
   * @param submittedBirthDayString day
   * @param birthdayTextfield
   * @return true if ok and false if problem
   */
  public static boolean checkBirthday(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, TwoFactorUser twoFactorUser, 
      String submittedBirthMonthString, String submittedBirthDayString, 
      String submittedBirthYearString, String birthdayTextfield) {

    boolean showBirthdayOnScreen = true;

    //check birthday
    if (twoFactorUser.isRequireBirthdayOnOptin()) {
      
      if (!StringUtils.isBlank(birthdayTextfield)) {
        //try yyyy/mm/dd
        Pattern birthdayPattern = Pattern.compile("^(\\d{4})[^\\d]+(\\d{1,2})[^\\d]+(\\d{1,2})$");
        Matcher matcher = birthdayPattern.matcher(birthdayTextfield);
        
        if (matcher.matches()) {
          submittedBirthYearString = matcher.group(1);
          submittedBirthMonthString = matcher.group(2);
          submittedBirthDayString = matcher.group(3);
          showBirthdayOnScreen = false;
        } else {
        
          //try mm/dd/yyyy
          birthdayPattern = Pattern.compile("^(\\d{1,2})[^\\d]+(\\d{1,2})[^\\d]+(\\d{4})$");
          matcher = birthdayPattern.matcher(birthdayTextfield);

          if (matcher.matches()) {
            submittedBirthMonthString = matcher.group(1);
            submittedBirthDayString = matcher.group(2);
            submittedBirthYearString = matcher.group(3);
            showBirthdayOnScreen = false;
          }
        }
      }
      
      Integer submittedBirthMonth = null;
      
      {
        submittedBirthMonth = TwoFactorServerUtils.intObjectValue(submittedBirthMonthString, true);
      }

      Integer submittedBirthDay = null;

      {
        submittedBirthDay = TwoFactorServerUtils.intObjectValue(submittedBirthDayString, true);
      }
      
      Integer submittedBirthYear = null;

      {
        submittedBirthYear = TwoFactorServerUtils.intObjectValue(submittedBirthYearString, true);
      }

      if (showBirthdayOnScreen) {
        twoFactorRequestContainer.getTwoFactorOptinContainer().setBirthDaySubmitted(submittedBirthDay == null ? -1 : submittedBirthDay);
        twoFactorRequestContainer.getTwoFactorOptinContainer().setBirthMonthSubmitted(submittedBirthMonth == null ? -1 : submittedBirthMonth);
        twoFactorRequestContainer.getTwoFactorOptinContainer().setBirthYearSubmitted(submittedBirthYear == null ? -1 : submittedBirthYear);
      }
      
      if (submittedBirthDay == null || submittedBirthMonth == null || submittedBirthYear == null) {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorBirthDayRequired"));
        return false;
      }
      
      //lets see if we are over the limit
      String wrongBdayHistogram = twoFactorUser.getWrongBdayAttemptsInMonth();
      
      int numberToday = TwoFactorServerUtils.histogramValueForDate(null, wrongBdayHistogram);
      
      //was the user incorrect?
      boolean wrongBirthday = !TwoFactorServerUtils.equals(submittedBirthDay, twoFactorUser.getBirthDay()) 
          || !TwoFactorServerUtils.equals(submittedBirthMonth, twoFactorUser.getBirthMonth())
          || !TwoFactorServerUtils.equals(submittedBirthYear, twoFactorUser.getBirthYear());

      //if we havent done too many or wrong bday
      boolean passedThreshold = numberToday >= TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.maxWrongBdaysPerDayPerUser", 5);
      
      if (passedThreshold || wrongBirthday) {
        
        //if not right increment the histogram
        if (wrongBirthday) {
          wrongBdayHistogram = TwoFactorServerUtils.histogramIncrementForDate(null, wrongBdayHistogram);
          twoFactorUser.setWrongBdayAttemptsInMonth(wrongBdayHistogram);
          twoFactorUser.store(twoFactorDaoFactory);
          
          //audit this
          TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
              TwoFactorAuditAction.WRONG_BIRTHDAY, ipAddress, 
              userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), 
              submittedBirthYear + "/" + submittedBirthMonth + "/" + submittedBirthDay, null);
          
        }

        // might need to send email
        if (passedThreshold) {
          
          if (subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
              && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.emailUsersPassedThresholdOfWrongBirthdays", true)) {
            
            Subject sourceSubjectLoggedIn = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
            
            String userEmail = retrieveUserEmail(sourceSubjectLoggedIn, twoFactorRequestContainer.getTwoFactorUserLoggedIn());

            //set the default text container...
            String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailWrongBdaySubject");
            subject = TextContainer.massageText("emailWrongBdaySubject", subject);

            String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailWrongBdayBody");
            body = TextContainer.massageText("emailWrongBdayBody", body);
            
            String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.emailUsersPassedThresholdOfWrongBirthdaysBcc");
            
            TwoFactorEmail twoFactorMail = new TwoFactorEmail();
            
            boolean sendEmail = true;
            //there is no email address????
            if (StringUtils.isBlank(userEmail)) {
              LOG.warn("Did not send email to logged in user: " + sourceSubjectLoggedIn + ", no email address...");
              if (StringUtils.isBlank(bccsString)) {
                sendEmail = false;
              } else {
                twoFactorMail.addTo(bccsString);
              }
            } else {
              twoFactorMail.addTo(userEmail);
              twoFactorMail.addBcc(bccsString);
            }
            
            if (sendEmail) {
              twoFactorMail.assignBody(body);
              twoFactorMail.assignSubject(subject);
              twoFactorMail.send();
            }
            
          }
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorBirthDayPassedThreshold"));
        } else {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorBirthDayInvalid"));
        }
        return false;
      }
    }
    return true;
  }

  /**
   * return true if ok and false if problem
   * @param twoFactorRequestContainer 
   * @param twoFactorUser user
   * @param birthDayUuid if they have a birthDay submitted successfully
   * @return true if ok and false if problem
   */
  private boolean checkBirthday(final TwoFactorRequestContainer twoFactorRequestContainer,
      TwoFactorUser twoFactorUser, 
      String birthDayUuid) {

    if (StringUtils.isBlank(birthDayUuid) || !StringUtils.equals(twoFactorUser.getBirthDayUuid(), birthDayUuid)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinNeedsBirthDayUuid"));
      return false;
    }

    return true;
  }

  /**
   * if we are integrating with duo to register users
   * @return true or false
   */
  public static boolean duoRegisterUsers() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duo.registerUsers", false);
  }
  
  /**
   * optout of two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   */
  public void optoutLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {

    String duoUserId = UiMain.duoRegisterUsers() ? DuoCommands.retrieveDuoUserIdBySomeId(loggedInUser) : null;
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        twoFactorUser.setTwoFactorSecretTemp(null);
        
        twoFactorUser.setDuoUserId(null);
        
        String resultMessage = null;
        
        if (StringUtils.isBlank(twoFactorUser.getTwoFactorSecret())) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optoutErrorNotOptedIn"));
          
        } else {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optoutSuccessMessage"));

        }
        
        twoFactorUser.setTwoFactorSecret(null);
        
        twoFactorUser.setOptedIn(false);
        twoFactorUser.setDateInvitedColleagues(null);

        duoClearOutAttributes(twoFactorUser);


        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTOUT_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return resultMessage;
      }
    });
    
    
    String userEmail = null;
    try {
      
      //see if there
      
      //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
      if (subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForOptout", true)) {
        
        Subject sourceSubject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        
        userEmail = retrieveUserEmail(sourceSubject, twoFactorRequestContainer.getTwoFactorUserLoggedIn());
        
        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptOutSubject");
        subject = TextContainer.massageText("emailOptOutSubject", subject);

        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailOptOutBody");
        body = TextContainer.massageText("emailOptOutBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.optouts");
        
        TwoFactorEmail twoFactorMail = new TwoFactorEmail();
        
        boolean sendEmail = true;
        
        //there is no email address????
        if (StringUtils.isBlank(userEmail)) {
          LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
          if (StringUtils.isBlank(bccsString)) {
            sendEmail = false;
          } else {
            twoFactorMail.addTo(bccsString);
          }
        } else {
          twoFactorMail.addTo(userEmail);
          twoFactorMail.addBcc(bccsString);
        }
        
        if (sendEmail) {
          twoFactorMail.assignBody(body);
          twoFactorMail.assignSubject(subject);
          twoFactorMail.send();
        }
        
      }
      
    } catch (Exception e) {
      //non fatal, just log this
      LOG.error("Error sending email to: " + userEmail + ", loggedInUser id: " + loggedInUser, e);
    }

    if (UiMain.duoRegisterUsers() && !StringUtils.isBlank(duoUserId)) { 
      //delete from duo
      DuoCommands.deleteDuoUserAndPhonesAndTokensByUserId(duoUserId);
    }
  }


  /**
   * untrust browsers
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void untrustBrowsers(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    untrustBrowsersLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
    
    index(httpServletRequest, httpServletResponse);
  }
  
  /**
   * untrust browsers of two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   */
  public void untrustBrowsersLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setTwoFactorSecretTemp(null);
        String resultMessage = null;
        
        if (!twoFactorUser.isOptedIn()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("untrustBrowserErrorNotOptedIn"));
          
        } else {
          
          List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser()
            .retrieveTrustedByUserUuid(twoFactorUser.getUuid());
          
          //untrust browsers since opting in, dont want orphans from last time
          for (TwoFactorBrowser twoFactorBrowser : twoFactorBrowsers) {
            twoFactorBrowser.setWhenTrusted(0);
            twoFactorBrowser.setTrustedBrowser(false);
            twoFactorBrowser.store(twoFactorDaoFactory);
          }

          twoFactorRequestContainer.getTwoFactorUntrustBrowserContainer().setNumberOfBrowsers(TwoFactorServerUtils.length(twoFactorBrowsers));
          String error = TextContainer.retrieveFromRequest().getText().get("untrustBrowserSuccess");
          twoFactorRequestContainer.setError(error);

        }

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.UNTRUST_BROWSERS, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return resultMessage;
      }
    });
  }

  /**
   * show the profile screen
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void profile(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();
    
    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("profile.jsp");
  }

  /**
   * view reports
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void reports(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    String reportUuid = httpServletRequest.getParameter("reportUuid");
    
    ReportsView reportsView = reportsLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource, reportUuid);

    showJsp(reportsView.getJsp());
  }

  /**
   * view report
   */
  public static enum ReportsView {
    
    /**
     */
    reports("reports.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp");

    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private ReportsView(String theJsp) {
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
   * reportsLogic
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress
   * @param userAgent
   * @param subjectSource 
   * @param reportUuid
   * @return which view to go to
   * 
   */
  private ReportsView reportsLogic(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, final String userAgent,
      final Source subjectSource, final String reportUuid) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    twoFactorUser.setSubjectSource(subjectSource);

    if (!twoFactorUser.isOptedIn()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("viewReportsErrorLoggedInSubjectNotOptedIn"));

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return ReportsView.index;
    }

    if (!twoFactorUser.isHasReportPrivilege()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("viewReportsErrorLoggedInSubjectHasNoReports"));

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return ReportsView.index;
    }

    TwoFactorViewReportContainer twoFactorViewReportContainer = twoFactorRequestContainer.getTwoFactorViewReportContainer();
    
    List<TwoFactorReport> twoFactorReports = TwoFactorReport.retrieveAll(twoFactorDaoFactory);
    
    final Map<String, TwoFactorReport> twoFactorReportMap = new HashMap<String, TwoFactorReport>();
    
    for (TwoFactorReport twoFactorReport : twoFactorReports) {
      twoFactorReportMap.put(twoFactorReport.getUuid(), twoFactorReport);
    }

    List<TwoFactorReport> reportsAllowedToView = new ArrayList<TwoFactorReport>();
    twoFactorViewReportContainer.setReportsAllowedToView(reportsAllowedToView);
    
    //there arent that many privs, so just loop through them
    for (TwoFactorReportPrivilege twoFactorReportPrivilege : TwoFactorReportPrivilege.retrieveAllPrivileges(twoFactorDaoFactory)) {
      if (StringUtils.equals(twoFactorUser.getUuid(), twoFactorReportPrivilege.getUserUuid())) {
        TwoFactorReport twoFactorReport = twoFactorReportMap.get(twoFactorReportPrivilege.getReportUuid());
        reportsAllowedToView.add(twoFactorReport);
      }
    }

    if (!StringUtils.isBlank(reportUuid)) {
      
      TwoFactorReport twoFactorReport = twoFactorReportMap.get(reportUuid);
      
      if (twoFactorReport == null) {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("viewReportsErrorReportNotFound"));
        
      } else {
        
        TwoFactorReportStat twoFactorReportStat = new TwoFactorReportStat();
        twoFactorReportStat.setTwoFactorReport(twoFactorReport);
        twoFactorViewReportContainer.setMainReportStat(twoFactorReportStat);
        
        final Map<String, TwoFactorReportRollup> allRollups = TwoFactorReportRollup.retrieveAllRollups(twoFactorDaoFactory);

        Set<String> usersNotOptedIn = new TreeSet<String>();
        
        twoFactorReportStat.calculateStats(twoFactorDaoFactory, twoFactorReportMap, allRollups, usersNotOptedIn, subjectSource);
        
        twoFactorViewReportContainer.setSubjectDescriptionsNotOptedIn(new ArrayList<String>(usersNotOptedIn));
        
        if (twoFactorReport.getReportTypeEnum() == TwoFactorReportType.rollup) {
          
          List<TwoFactorReportRollup> childRollups = TwoFactorReportRollup.retrieveChildRollups(allRollups, twoFactorReport.getUuid());
          if (TwoFactorServerUtils.length(childRollups) > 0) {
            List<TwoFactorReportStat> childReportStats = new ArrayList<TwoFactorReportStat>();
            twoFactorViewReportContainer.setChildReportStats(childReportStats);

            long startTime = System.currentTimeMillis();
            
            boolean useThreads = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorReportsUseThreads", true);
            int reportThreadPoolSize = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorReportThreadPoolSize", 10);
            int twoFactorReportMaxSubreportsSeconds = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorReportMaxSubreportsSeconds", 120);
            //see when threads are done processing
            List<TwoFactorFuture> futures = new ArrayList<TwoFactorFuture>();

            //if there were thread problems, run those again
            List<TwoFactorCallable> callablesWithProblems = new ArrayList<TwoFactorCallable>();

            for (TwoFactorReportRollup twoFactorReportRollup : childRollups) {

              final TwoFactorReportStat theTwoFactorReportStat =  new TwoFactorReportStat();
              TwoFactorReport theTwoFactorReport = twoFactorReportMap.get(twoFactorReportRollup.getChildReportUuid());
              theTwoFactorReportStat.setTwoFactorReport(theTwoFactorReport);
              childReportStats.add(theTwoFactorReportStat);

              //dont take more than 2 minutes
              
              if (twoFactorReportMaxSubreportsSeconds != -1 && System.currentTimeMillis() - startTime > (twoFactorReportMaxSubreportsSeconds * 1000)) {
                
                theTwoFactorReportStat.setTotal(-1);
                theTwoFactorReportStat.setTotalNotOptedIn(-1);
                
              } else {
                
                TwoFactorCallable<Void> grouperCallable = new TwoFactorCallable<Void>("subreport calculate stats: " + twoFactorReportStat.getTwoFactorReport().getReportNameSystem()) {

                  @Override
                  public Void callLogic() {
                    theTwoFactorReportStat.calculateStats(twoFactorDaoFactory, twoFactorReportMap, allRollups, null, subjectSource);
                    return null;
                  }

                };
                if (!useThreads || reportThreadPoolSize == 1 || reportThreadPoolSize == 0) {
                  grouperCallable.callLogic();
                } else {
                  TwoFactorFuture<Void> future = TwoFactorServerUtils.executorServiceSubmit(TwoFactorServerUtils.retrieveExecutorService(), grouperCallable);
                  futures.add(future);
                  
                  TwoFactorFuture.waitForJob(futures, reportThreadPoolSize, callablesWithProblems);
                }

                
              }

            }
            
            //wait for the rest
            TwoFactorFuture.waitForJob(futures, 0, callablesWithProblems);

          }          
        }

        
      }
      
    }
    
    
    return ReportsView.reports;
  }


  
  /**
   * show the profile screen readonly
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void profileView(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    Source subjectSource = TfSourceUtils.mainSource();

    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    showJsp("profileView.jsp");
  }

  /**
   * show the profile screen
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   */
  public void profileLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    twoFactorRequestContainer.getTwoFactorProfileContainer().setTwoFactorUserOperatingOn(twoFactorUser);
    
    TwoFactorProfileContainer twoFactorProfileContainer = twoFactorRequestContainer.getTwoFactorProfileContainer();

    twoFactorProfileContainer.setEmail0(twoFactorUser.getEmail0());

    String dbEmail = twoFactorUser.getEmail0();
    String subjectEmail = null;
    
    if (subjectSource != null) {
      
      //resolve subject
      Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
          loggedInUser, false, false, true);
      
      if (subject == null) {
        throw new RuntimeException("Cant find subject by login id: '" + loggedInUser +"'");
      }
      
      subjectEmail = TfSourceUtils.retrieveEmail(subject);
    }

    if (!StringUtils.equals(dbEmail, subjectEmail) && !StringUtils.isBlank(subjectEmail)) {
      
      twoFactorProfileContainer.setEmail0(subjectEmail);
      //assign the db email
      twoFactorUser.setEmail0(subjectEmail);
      twoFactorUser.store(twoFactorDaoFactory);
      //assign an audit

//      auditsAssignProfileEmailFromSubjectBlank = none
//      auditsAssignProfileEmailFromSubjectPrefix1 = From: 
//      auditsAssignProfileEmailFromSubjectPrefix2 = , to:
      StringBuilder descriptionBuilder = new StringBuilder();
      descriptionBuilder.append(TextContainer.retrieveFromRequest().getText().get("auditsAssignProfileEmailFromSubjectPrefix1")).append(" ");
      descriptionBuilder.append(StringUtils.defaultIfEmpty(dbEmail, TextContainer.retrieveFromRequest().getText().get("auditsAssignProfileEmailFromSubjectBlank")));
      descriptionBuilder.append(TextContainer.retrieveFromRequest().getText().get("auditsAssignProfileEmailFromSubjectPrefix2"));
      descriptionBuilder.append(" ").append(StringUtils.defaultIfEmpty(subjectEmail, TextContainer.retrieveFromRequest().getText().get("auditsAssignProfileEmailFromSubjectBlank")));
      
      TwoFactorAudit.createAndStore(twoFactorDaoFactory, TwoFactorAuditAction.SET_PROFILE_EMAIL_FROM_SUBJECT, 
          ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), descriptionBuilder.toString(), null);
    }
    
    twoFactorProfileContainer.setPhone0(twoFactorUser.getPhone0());
    twoFactorProfileContainer.setPhone1(twoFactorUser.getPhone1());
    twoFactorProfileContainer.setPhone2(twoFactorUser.getPhone2());

    twoFactorProfileContainer.setPhoneText0((twoFactorUser.getPhoneIsText0() != null && twoFactorUser.getPhoneIsText0()) ? "true" : "");
    twoFactorProfileContainer.setPhoneText1((twoFactorUser.getPhoneIsText1() != null && twoFactorUser.getPhoneIsText1()) ? "true" : "");
    twoFactorProfileContainer.setPhoneText2((twoFactorUser.getPhoneIsText2() != null && twoFactorUser.getPhoneIsText2()) ? "true" : "");

    twoFactorProfileContainer.setPhoneVoice0((twoFactorUser.getPhoneIsVoice0() != null && twoFactorUser.getPhoneIsVoice0()) ? "true" : "");
    twoFactorProfileContainer.setPhoneVoice1((twoFactorUser.getPhoneIsVoice1() != null && twoFactorUser.getPhoneIsVoice1()) ? "true" : "");
    twoFactorProfileContainer.setPhoneVoice2((twoFactorUser.getPhoneIsVoice2() != null && twoFactorUser.getPhoneIsVoice2()) ? "true" : "");

    boolean optinOnlyIfRequired = TwoFactorServerUtils.booleanValue(twoFactorUser.getOptInOnlyIfRequired(), false);
    
    twoFactorProfileContainer.setOptinForApplicationsWhichRequire(optinOnlyIfRequired);
    twoFactorProfileContainer.setOptinForAll(!optinOnlyIfRequired);
    
    twoFactorProfileContainer.setPhoneAutoCalltext(twoFactorUser.getPhoneAutoCalltext());
    
    {
      String colleagueUserUuid0 = twoFactorUser.getColleagueUserUuid0();
      if (!StringUtils.isBlank(colleagueUserUuid0)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid0);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin0(colleagueUser.getLoginid());
          
          Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              colleagueUser.getLoginid(), true, false, false);
          twoFactorProfileContainer.setColleagueDescription0(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));
          twoFactorProfileContainer.setColleagueName0(TfSourceUtils.subjectName(subject, colleagueUser.getLoginid()));

        }
      }
    }

    {
      String colleagueUserUuid1 = twoFactorUser.getColleagueUserUuid1();
      if (!StringUtils.isBlank(colleagueUserUuid1)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid1);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin1(colleagueUser.getLoginid());

          Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              colleagueUser.getLoginid(), true, false, false);
          twoFactorProfileContainer.setColleagueDescription1(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));
          twoFactorProfileContainer.setColleagueName1(TfSourceUtils.subjectName(subject, colleagueUser.getLoginid()));

        }
      }
    }

    {
      String colleagueUserUuid2 = twoFactorUser.getColleagueUserUuid2();
      if (!StringUtils.isBlank(colleagueUserUuid2)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid2);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin2(colleagueUser.getLoginid());
          
          Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              colleagueUser.getLoginid(), true, false, false);
          twoFactorProfileContainer.setColleagueDescription2(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));
          twoFactorProfileContainer.setColleagueName2(TfSourceUtils.subjectName(subject, colleagueUser.getLoginid()));

        }
      }
    }

    {
      String colleagueUserUuid3 = twoFactorUser.getColleagueUserUuid3();
      if (!StringUtils.isBlank(colleagueUserUuid3)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid3);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin3(colleagueUser.getLoginid());
          
          Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              colleagueUser.getLoginid(), true, false, false);
          twoFactorProfileContainer.setColleagueDescription3(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));
          twoFactorProfileContainer.setColleagueName3(TfSourceUtils.subjectName(subject, colleagueUser.getLoginid()));

        }
      }
    }
    {
      String colleagueUserUuid4 = twoFactorUser.getColleagueUserUuid4();
      if (!StringUtils.isBlank(colleagueUserUuid4)) {
        TwoFactorUser colleagueUser = twoFactorDaoFactory.getTwoFactorUser().retrieveByUuid(colleagueUserUuid4);
        if (colleagueUser != null) {
          twoFactorProfileContainer.setColleagueLogin4(colleagueUser.getLoginid());
          
          Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              colleagueUser.getLoginid(), true, false, false);
          twoFactorProfileContainer.setColleagueDescription4(TfSourceUtils.subjectDescription(subject, colleagueUser.getLoginid()));
          twoFactorProfileContainer.setColleagueName4(TfSourceUtils.subjectName(subject, colleagueUser.getLoginid()));

        }
      }
    }



  }

  /**
   * profile submit
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void profileSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String email0 = null;
    
    if (twoFactorRequestContainer.isEditableEmail()) {
      email0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("email0");
    }
    String colleagueLogin0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin0Name");
    if (StringUtils.isBlank(colleagueLogin0)) {
      colleagueLogin0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin0DisplayName");
    }
    String colleagueLogin1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin1Name");
    if (StringUtils.isBlank(colleagueLogin1)) {
      colleagueLogin1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin1DisplayName");
    }
    String colleagueLogin2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin2Name");
    if (StringUtils.isBlank(colleagueLogin2)) {
      colleagueLogin2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin2DisplayName");
    }
    String colleagueLogin3 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin3Name");
    if (StringUtils.isBlank(colleagueLogin3)) {
      colleagueLogin3 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin3DisplayName");
    }
    String colleagueLogin4 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin4Name");
    if (StringUtils.isBlank(colleagueLogin4)) {
      colleagueLogin4 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin4DisplayName");
    }
    String phone0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone0");
    //this is "on" if submitted, or null if not
    String phoneVoice0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice0");
    String phoneText0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText0");
    String phone1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone1");
    String phoneVoice1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice1");
    String phoneText1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText1");
    String phone2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone2");
    String phoneVoice2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice2");
    String phoneText2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText2");

    //String phoneAutoVoiceText = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneAutoVoiceText");
    
    String optinTypeName = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("optinTypeName");

    boolean optinForApplicationsWhichRequire = StringUtils.equals(optinTypeName, "optinForApplicationsWhichRequire");
    
    boolean profileForOptin = TwoFactorServerUtils.booleanValue(
        TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("profileForOptin"), false);
    
    Source subjectSource = TfSourceUtils.mainSource();
    
    boolean success = profileSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), email0, colleagueLogin0, colleagueLogin1,
        colleagueLogin2, colleagueLogin3, colleagueLogin4, phone0, phoneVoice0, phoneText0, 
        phone1, phoneVoice1, phoneText1, phone2, phoneVoice2, phoneText2, subjectSource, profileForOptin, optinForApplicationsWhichRequire);
  
    if (success) {
      
      if (profileForOptin) {
        optin(httpServletRequest, httpServletResponse);
        return;
      }
      showJsp("profileView.jsp");
    } else {
    
      showJsp("profile.jsp");
    }
  }

  /**
   * 
   * @param colleagueLogin0 
   * @param colleagueLogin1 
   * @param colleagueLogin2 
   * @param colleagueLogin3 
   * @param colleagueLogin4 
   * @param phone0 
   * @param phone1 
   * @param phone2 
   * @return the count of lifelines
   */
  private static int lifelineCount(String colleagueLogin0, String colleagueLogin1,
      String colleagueLogin2, String colleagueLogin3, String colleagueLogin4,
      String phone0, String phone1, String phone2) {
    int lifelineCount = 0;
    lifelineCount += StringUtils.isBlank(colleagueLogin0) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin1) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin2) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin3) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(colleagueLogin4) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(phone0) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(phone1) ? 0 : 1;
    lifelineCount += StringUtils.isBlank(phone2) ? 0 : 1;
    
    return lifelineCount;
  }
  
  /**
   * profile submit
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param email0 
   * @param colleagueLogin0 
   * @param colleagueLogin1 
   * @param colleagueLogin2 
   * @param colleagueLogin3 
   * @param colleagueLogin4 
   * @param phone0 
   * @param phoneVoice0 
   * @param phoneText0 
   * @param phone1 
   * @param phoneVoice1 
   * @param phoneText1 
   * @param phone2 
   * @param phoneVoice2 
   * @param phoneText2 
   * @param subjectSource 
   * @param profileForOptin 
   * @param optinForApplicationsWhichRequire
   * @return true if ok, false if not
   */
  public boolean profileSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String email0, final String colleagueLogin0, final String colleagueLogin1,
      final String colleagueLogin2, final String colleagueLogin3, final String colleagueLogin4, 
      final String phone0, final String phoneVoice0, final String phoneText0, 
      final String phone1, final String phoneVoice1, final String phoneText1, final String phone2, 
      final String phoneVoice2, final String phoneText2, final Source subjectSource, final boolean profileForOptin, 
      final boolean optinForApplicationsWhichRequire) {
    
    final Set<TwoFactorUser> newColleagues = new HashSet<TwoFactorUser>();
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    

    boolean result = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        String localEmail0 = email0;
        
        //generate the codes
        twoFactorUser.setSubjectSource(subjectSource);
        
        TwoFactorProfileContainer twoFactorProfileContainer = twoFactorRequestContainer.getTwoFactorProfileContainer();
        
        twoFactorProfileContainer.setEmail0(localEmail0);
        twoFactorProfileContainer.setProfileForOptin(profileForOptin);
        
        Subject loggedInSubject = null;
        
        if (StringUtils.isBlank(twoFactorProfileContainer.getEmail0()) && subjectSource != null) {
          
          //resolve subject
          loggedInSubject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              loggedInUser, true, false, true);

          if (loggedInSubject != null) {
            
            twoFactorProfileContainer.setEmail0(TfSourceUtils.retrieveEmail(loggedInSubject));
            localEmail0 = twoFactorProfileContainer.getEmail0();
          }
        }
        
        twoFactorProfileContainer.setColleagueLogin0(colleagueLogin0);
        twoFactorProfileContainer.setColleagueLogin1(colleagueLogin1);
        twoFactorProfileContainer.setColleagueLogin2(colleagueLogin2);
        twoFactorProfileContainer.setColleagueLogin3(colleagueLogin3);
        twoFactorProfileContainer.setColleagueLogin4(colleagueLogin4);
        twoFactorProfileContainer.setPhone0(phone0);
        twoFactorProfileContainer.setPhoneText0(phoneText0);
        twoFactorProfileContainer.setPhoneVoice0(phoneVoice0);
        twoFactorProfileContainer.setPhone1(phone1);
        twoFactorProfileContainer.setPhoneText1(phoneText1);
        twoFactorProfileContainer.setPhoneVoice1(phoneVoice1);
        twoFactorProfileContainer.setPhone2(phone2);
        twoFactorProfileContainer.setPhoneText2(phoneText2);
        twoFactorProfileContainer.setPhoneVoice2(phoneVoice2);
        twoFactorProfileContainer.setOptinForAll(!optinForApplicationsWhichRequire);
        twoFactorProfileContainer.setOptinForApplicationsWhichRequire(optinForApplicationsWhichRequire);
        //twoFactorProfileContainer.setPhoneAutoCalltext(phoneAutoVoiceText);
        
        String errorMessage = null;
        
        //if email address isnt editable, then validate it
        if (twoFactorRequestContainer.isEditableEmail()) {
          errorMessage = validateEmail(localEmail0);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone0, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone1"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone1, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone2"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone2, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone3"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone0, phoneText0, phoneVoice0, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone1"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone1, phoneText1, phoneVoice1, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone2"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone2, phoneText2, phoneVoice2, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone3"));
        }
        
        String selfErrorMessage = TextContainer.retrieveFromRequest().getText().get("profileErrorFriendIsSelf");
        
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin0, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend1invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin1, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend2invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin2, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend3invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin3, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend4invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin4, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend5invalid"), selfErrorMessage);
        }
        
        if (StringUtils.isBlank(errorMessage) && lifelineCount(colleagueLogin0, colleagueLogin1, colleagueLogin2, colleagueLogin3, colleagueLogin4,
            phone0, phone1, phone2) < 2) {
          errorMessage = TextContainer.retrieveFromRequest().getText().get("profileErrorNotEnoughLifelines");
        }

        if (StringUtils.isBlank(errorMessage) && StringUtils.isBlank(phone0) && StringUtils.isBlank(phone1) && StringUtils.isBlank(phone2)) {
          errorMessage = TextContainer.retrieveFromRequest().getText().get("profileErrorNotEnoughPhones");
        }

//        if (StringUtils.isBlank(errorMessage) && !StringUtils.isBlank(phoneAutoVoiceText)) {
//          if (StringUtils.equals("0t", phoneAutoVoiceText)) {
//            if (StringUtils.isBlank(phone0) || !StringUtils.equals(phoneText0, "true") ) {
//              errorMessage = TextContainer.retrieveFromRequest().getText().get("profileAutoVoiceTextInvalid");
//            }
//          } else if (StringUtils.equals("0v", phoneAutoVoiceText)) {
//            if (StringUtils.isBlank(phone0) || !StringUtils.equals(phoneVoice0, "true") ) {
//              errorMessage = TextContainer.retrieveFromRequest().getText().get("profileAutoVoiceTextInvalid");
//            }
//          } else if (StringUtils.equals("1t", phoneAutoVoiceText)) {
//            if (StringUtils.isBlank(phone1) || !StringUtils.equals(phoneText1, "true") ) {
//              errorMessage = TextContainer.retrieveFromRequest().getText().get("profileAutoVoiceTextInvalid");
//            }
//          } else if (StringUtils.equals("1v", phoneAutoVoiceText)) {
//            if (StringUtils.isBlank(phone1) || !StringUtils.equals(phoneVoice1, "true") ) {
//              errorMessage = TextContainer.retrieveFromRequest().getText().get("profileAutoVoiceTextInvalid");
//            }
//          } else if (StringUtils.equals("2t", phoneAutoVoiceText)) {
//            if (StringUtils.isBlank(phone2) || !StringUtils.equals(phoneText2, "true") ) {
//              errorMessage = TextContainer.retrieveFromRequest().getText().get("profileAutoVoiceTextInvalid");
//            }
//          } else if (StringUtils.equals("2v", phoneAutoVoiceText)) {
//            if (StringUtils.isBlank(phone2) || !StringUtils.equals(phoneVoice2, "true") ) {
//              errorMessage = TextContainer.retrieveFromRequest().getText().get("profileAutoVoiceTextInvalid");
//            }
//          } else {
//            throw new RuntimeException("Invalid value for phoneAutoVoiceText!!!!! '" + phoneAutoVoiceText + "'");
//          }
//        }

        if (!StringUtils.isBlank(errorMessage) ) {
          twoFactorRequestContainer.setError(errorMessage);
          return false;
        }

        twoFactorUser.setEmail0(localEmail0);

        twoFactorUser.setPhone0(phone0);
        twoFactorUser.setPhone1(phone1);
        twoFactorUser.setPhone2(phone2);
        twoFactorUser.setPhoneIsText0(StringUtils.equals(phoneText0, "true") ? true : false);
        twoFactorUser.setPhoneIsText1(StringUtils.equals(phoneText1, "true") ? true : false);
        twoFactorUser.setPhoneIsText2(StringUtils.equals(phoneText2, "true") ? true : false);
        twoFactorUser.setPhoneIsVoice0(StringUtils.equals(phoneVoice0, "true") ? true : false);
        twoFactorUser.setPhoneIsVoice1(StringUtils.equals(phoneVoice1, "true") ? true : false);
        twoFactorUser.setPhoneIsVoice2(StringUtils.equals(phoneVoice2, "true") ? true : false);

        //stwoFactorUser.setPhoneAutoCalltext(phoneAutoVoiceText);
                
        twoFactorUser.setOptInOnlyIfRequired(optinForApplicationsWhichRequire);
        
        Set<String> previousColleagueUuids = new HashSet<String>();
        
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid0())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid0());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid1())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid1());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid2())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid2());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid3())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid3());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid4())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid4());
        }

        //get the new colleagues
        
        
        if (StringUtils.isBlank(colleagueLogin0)) {
          twoFactorUser.setColleagueUserUuid0(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin0);
          twoFactorUser.setColleagueUserUuid0(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin1)) {
          twoFactorUser.setColleagueUserUuid1(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin1);
          twoFactorUser.setColleagueUserUuid1(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin2)) {
          twoFactorUser.setColleagueUserUuid2(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin2);
          twoFactorUser.setColleagueUserUuid2(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin3)) {
          twoFactorUser.setColleagueUserUuid3(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin3);
          twoFactorUser.setColleagueUserUuid3(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin4)) {
          twoFactorUser.setColleagueUserUuid4(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin4);
          twoFactorUser.setColleagueUserUuid4(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.EDIT_PROFILE, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("profileSuccessMessage"));
  
        profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
            loggedInUser, ipAddress, 
            userAgent, subjectSource);

        return true;
      }
    });
    
    if (result) {
      
      notifyNewColleagues(twoFactorRequestContainer, loggedInUser, subjectSource, newColleagues,
          twoFactorUser);
      
    }
    
    //change phones in duo
    if (duoRegisterUsers()) {
      String duoUserId = DuoCommands.retrieveDuoUserIdBySomeId(twoFactorRequestContainer.getTwoFactorUserLoggedIn().getLoginid());
      
      //if user is registered, edit phone
      if (!StringUtils.isBlank(duoUserId)) {
        DuoCommands.migratePhonesToDuoBySomeId(twoFactorRequestContainer.getTwoFactorUserLoggedIn().getLoginid(), false);
        
//        String newPhoneAutoDuoPhoneId = null;
//        
//        String phoneNumber = null;
//        if (StringUtils.equals("0v", phoneAutoVoiceText)) {
//          phoneNumber = phone0;
//        } else if (StringUtils.equals("1v", phoneAutoVoiceText)) {
//          phoneNumber = phone1;
//        } else if (StringUtils.equals("2v", phoneAutoVoiceText)) {
//          phoneNumber = phone2;
//        }
//
//        if (!StringUtils.isBlank(phoneNumber)) {
//          JSONObject duoPhone = DuoCommands.duoPhoneByIdOrNumber(phoneNumber, false);
//          
//          newPhoneAutoDuoPhoneId = StringUtils.trimToNull(duoPhone == null ? null : duoPhone.getString("phone_id"));
//        }
//        
//        if (!StringUtils.equals(newPhoneAutoDuoPhoneId, twoFactorUser.getPhoneAutoDuoPhoneId())) {
//          twoFactorUser.setPhoneAutoDuoPhoneId(newPhoneAutoDuoPhoneId);
//          twoFactorUser.store(twoFactorDaoFactory);
//        }
      }
    }
    
    return result;
    
  }


  /**
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param subjectSource
   * @param newColleagues
   * @param twoFactorUser
   */
  private void notifyNewColleagues(final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final Source subjectSource,
      final Set<TwoFactorUser> newColleagues, final TwoFactorUser twoFactorUser) {
    
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twostep.notify.newColleagues", false)) {
      return;
    }
    
    twoFactorUser.setSubjectSource(subjectSource);
    
    
    for (TwoFactorUser newColleague : newColleagues) {

      newColleague.setSubjectSource(subjectSource);
      
      twoFactorRequestContainer.getTwoFactorProfileContainer().setTwoFactorUserFriend(newColleague);
      
      String userEmailLoggedIn = null;
      String userEmailNewColleague = null;
      try {
        
        //see if there
        
        //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
        if (subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
            && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForSelectFriend", true)) {
          
          Subject sourceSubjectLoggedIn = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
          Subject sourceSubjectColleaguePicked = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, newColleague.getLoginid(), true, false, false);
          
          userEmailLoggedIn = retrieveUserEmail(sourceSubjectLoggedIn, twoFactorRequestContainer.getTwoFactorUserLoggedIn());
          userEmailNewColleague = retrieveUserEmail(sourceSubjectColleaguePicked, newColleague);
          
          //set the default text container...
          String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailFriendSubject");
          subject = TextContainer.massageText("emailFriendSubject", subject);

          String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailFriendBody");
          body = TextContainer.massageText("emailFriendBody", body);
          
          String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.selectFriends");
          
          TwoFactorEmail twoFactorMail = new TwoFactorEmail();
          
          boolean sendEmail = true;
          boolean sendToFriend = true;
          //there is no email address????
          if (StringUtils.isBlank(userEmailNewColleague)) {
            sendToFriend = false;
            LOG.warn("Did not send email to logged in user: " + newColleague + ", no email address...");
            if (StringUtils.isBlank(bccsString)) {
              sendEmail = false;
            } else {
              twoFactorMail.addTo(bccsString);
            }
          } else {
            twoFactorMail.addTo(userEmailNewColleague);
            twoFactorMail.addBcc(bccsString);
          }
          
          if (sendToFriend && StringUtils.isBlank(userEmailLoggedIn)) {
            LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
          } else {
            twoFactorMail.addCc(userEmailLoggedIn);
          }
          
          if (sendEmail) {
            twoFactorMail.assignBody(body);
            twoFactorMail.assignSubject(subject);
            twoFactorMail.send();
          }
          
        }
        
      } catch (Exception e) {
        //non fatal, just log this
        LOG.error("Error sending email to: " + userEmailNewColleague + ", (logged in): " + userEmailLoggedIn + ", loggedInUser id: " + loggedInUser, e);
      }

    }
  }
  
  /**
   * validate an email
   * @param email
   * @return the error or null
   */
  public static String validateEmail(String email) {
    
    if (StringUtils.isBlank(email)) {
      return TextContainer.retrieveFromRequest().getText().get("profileErrorEmailRequired");
    }
    {
      int atIndex = email.indexOf('@');
      if (atIndex <= 0 || email.endsWith("@")) {
        return TextContainer.retrieveFromRequest().getText().get("profileErrorEmailInvalid");
      }
      int dotIndex = email.indexOf('@', atIndex);
      if (dotIndex <= 0) {
        return TextContainer.retrieveFromRequest().getText().get("profileErrorEmailInvalid");
      }
      
      //validate with commons email validator
      if (!EmailValidator.getInstance().isValid(email)) {
        return TextContainer.retrieveFromRequest().getText().get("profileErrorEmailInvalid");
      }
      
    }
    return null;
  }
  
  /**
   * pattern of phone number validator
   */
  private static Pattern phonePattern = Pattern.compile("^[0-9+()\\- \\.]+$");
  
  /**
   * validate a phone
   * @param phone
   * @param label
   * @return the error or null
   */
  public static String validatePhone(String phone, String label) {
    if (StringUtils.isBlank(phone)) {
      return null;
    }
    //see if invalid chars
    Matcher matcher = phonePattern.matcher(phone);
    if (!matcher.matches()) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorPhoneInvalidChars");
    }
    //lets count the number of digits... must be at least 10 (note, intl number might have fewer... hmm)
    int digitCount = 0;
    for (int i=0;i<phone.length();i++) {
      if (Character.isDigit(phone.charAt(i))) {
        digitCount++;
      }
    }
    if (digitCount < 10) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorPhoneTooShort");
    }
    return null;
  }
  
  /**
   * restrict these people from others inviting them to be friends
   */
  private static ExpirableCache<Boolean, Set<String>> friendsRestrictSubjectIdsCache = null;
  
  /**
   * restrict VIPs from being selected as friends
   * @param subjectSource 
   * @param subjectId subject id to restrict
   * @return if the subjectId is restricted
   */
  private static boolean friendRestricted(Source subjectSource, String subjectId) {
    if (friendsRestrictSubjectIdsCache == null) {
      synchronized(UiMain.class) {
        
        if (friendsRestrictSubjectIdsCache == null) {
          friendsRestrictSubjectIdsCache = new ExpirableCache<Boolean, Set<String>>(5);
        }
      }
    }
    Set<String> subjectIdsToRestrict = friendsRestrictSubjectIdsCache.get(Boolean.TRUE);
    
    if (subjectIdsToRestrict == null) {
      
      synchronized(UiMain.class) {
        
        subjectIdsToRestrict = friendsRestrictSubjectIdsCache.get(Boolean.TRUE);
        
        if (subjectIdsToRestrict == null) {
          subjectIdsToRestrict = new HashSet<String>();
          
          String restrictSubjectIdsOrIdentifiersString = TwoFactorServerUtils.defaultString(TwoFactorServerConfig.retrieveConfig().propertyValueString("twoStep.restrictFromFriends"));
          
          Set<String> restrictSubjectIdsOrIdentifiersSet = TwoFactorServerUtils.splitTrimToSet(restrictSubjectIdsOrIdentifiersString, ",");
          
          for (String restrictSubjectIdOrIdentifier: restrictSubjectIdsOrIdentifiersSet) {
            
            Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, restrictSubjectIdOrIdentifier, true, false, true);
            if (subject == null) {
              LOG.error("Cant find subject to be restricted from friend lookup: '" + restrictSubjectIdOrIdentifier + "'");
            } else {
              subjectIdsToRestrict.add(subject.getId());
            }
          }
          
          friendsRestrictSubjectIdsCache.put(Boolean.TRUE, subjectIdsToRestrict);
        }
      }
    }
    return subjectIdsToRestrict.contains(subjectId);
  }

  /**
   * validate a friend lookup
   * @param loggedInSubject 
   * @param subjectSource to look up the friend
   * @param friendLookup 
   * @param errorMessage
   * @param errorMessageIfSelf
   * @return the error or null
   */
  public static String validateFriend(Subject loggedInSubject, Source subjectSource, String friendLookup, String errorMessage, String errorMessageIfSelf) {
    if (StringUtils.isBlank(friendLookup)) {
      return null;
    }
    //see if valid subject
    Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, friendLookup, true, false, false);
    if (subject == null) {
      return errorMessage;
    }
    if (!TfSourceUtils.subjectIsActive(subject)) {
      return errorMessage;
    }
    if (StringUtils.equals(subject.getId(), loggedInSubject.getId()) && StringUtils.equals(subject.getSourceId(), loggedInSubject.getSourceId())) {
      
      return errorMessageIfSelf;
      
    }
    // see if vip
    if (friendRestricted(subjectSource, subject.getId())) {
      return errorMessage;
    }
    
    return null;
  }

  /**
   * validate phone type checkboxes
   * @param phone
   * @param phoneText
   * @param phoneVoice
   * @param label
   * @return the error or null if no errors
   */
  public static String validatePhoneType(String phone, String phoneText, String phoneVoice, String label) {
    if (!StringUtils.isBlank(phone) && StringUtils.isBlank(phoneText) && StringUtils.isBlank(phoneVoice)) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorTextOrVoiceRequired");
    }
    if (StringUtils.isBlank(phone) && (!StringUtils.isBlank(phoneText) || !StringUtils.isBlank(phoneVoice))) {
      TwoFactorRequestContainer.retrieveFromRequest().getTwoFactorProfileContainer().setErrorFieldLabel(label);
      return TextContainer.retrieveFromRequest().getText().get("profileErrorNumberRequiredIfChecked");
    }
    return null;
  }
  
  /**
   * custom secret optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinCustom(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {

    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String twoFactorCustomCode = httpServletRequest.getParameter("twoFactorCustomCode");
    Source subjectSource = TfSourceUtils.mainSource();

    OptinView optinView = optinCustomLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), twoFactorCustomCode, subjectSource);

    showJsp(optinView.getJsp());

  }

  /**
   * submit custom code
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param twoFactorCustomCode
   * @param subjectSource 
   * @return the view to go to
   */
  public OptinView optinCustomLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, String twoFactorCustomCode, final Source subjectSource) {

    String[] error = new String[1];
    
    twoFactorCustomCode = validateCustomCode(twoFactorCustomCode, error);

    if (!StringUtils.isBlank(error[0])) {
      twoFactorRequestContainer.setError(error[0]);
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinView.index;
    }

    if (StringUtils.isBlank(twoFactorCustomCode)) {
      throw new RuntimeException("Why blank?");
    }

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinSubmitSecretValueSuccess"));

    return optinSetup(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress,
        userAgent, twoFactorCustomCode, subjectSource);

  }

  /**
   * clear out duo attributes
   * @param twoFactorUser
   */
  public static void duoClearOutAttributes(TwoFactorUser twoFactorUser) {
    twoFactorUser.setDuoPushByDefault(null);
    twoFactorUser.setDuoPushPhoneId(null);
    twoFactorUser.setDuoPushTransactionId(null);
    twoFactorUser.setDuoUserId(null);
  }

  /**
   * validate a custom code and convert to a standard format
   * @param twoFactorCustomCode
   * @param error
   * @return the code
   */
  public static String validateCustomCode(String twoFactorCustomCode, String[] error) {
    
    //validation
    if (StringUtils.isBlank(twoFactorCustomCode)) {
      error[0] = TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretRequired");
      return null;
    }
    
    //strip whitespace and validate chars
    boolean isBase32 = false;
    {
      twoFactorCustomCode = twoFactorCustomCode.toLowerCase();
      StringBuilder newString = new StringBuilder();
      for (int i=0;i<twoFactorCustomCode.length(); i++) {
        
        char theChar = twoFactorCustomCode.charAt(i);
        
        if (Character.isWhitespace(theChar)) {
          continue;
        }
        
        //Note: these arent the exact base32 rules, but close enough...
        if ((theChar >= '0' && theChar <= '9') || (theChar >= 'a' && theChar <= 'f')) {
          newString.append(theChar);
          continue;
        }
        if (theChar >= 'g' && theChar <= 'z') {
          newString.append(theChar);
          isBase32 = true;
          continue;
        }
        //bad char
        error[0] = TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretInvalid");
        return null;
      }
      twoFactorCustomCode = newString.toString();
    }
    
    if (twoFactorCustomCode.length() < 6) {
      error[0] = TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretNotLongEnough");
      return null;
    }

    if (!isBase32) {
      //convert to base 32:
      byte[] plainText = null;
      try {
        plainText = Hex.decodeHex(twoFactorCustomCode.toCharArray());
      } catch (DecoderException de) {
        error[0] = TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretInvalid");
        return null;
      }

      Base32 codec = new Base32();
      twoFactorCustomCode = codec.encodeAsString(plainText);
      
      //strip whitespace again since base32 puts it in there
      {
        twoFactorCustomCode = twoFactorCustomCode.toLowerCase();
        StringBuilder newString = new StringBuilder();
        for (int i=0;i<twoFactorCustomCode.length(); i++) {
          
          char theChar = twoFactorCustomCode.charAt(i);
          
          if (Character.isWhitespace(theChar)) {
            continue;
          }
          newString.append(theChar);

        }
        twoFactorCustomCode = newString.toString();
      }

    }

    twoFactorCustomCode = twoFactorCustomCode.toUpperCase();
    
    //make sure this is a valid secret.  if this doesnt run, its not valid
    try {
      TwoFactorOath.twoFactorCheckPassword(
          twoFactorCustomCode, "000000", null, null, null, 0L, null);
    } catch (Exception e) {
      error[0] = TextContainer.retrieveFromRequest().getText().get("optinErrorCustomSecretInvalid");
      return null;
    }

    
    return twoFactorCustomCode;
    
  }
  
  /**
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress
   * @param userAgent
   * @param twoFactorCode
   * @param subjectSource 
   * @return which view to go to
   * 
   */
  private OptinView optinSetup(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, final String userAgent,
      final String twoFactorCode, final Source subjectSource) {
    
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
    
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinView.index;
    }

    return (OptinView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        if (twoFactorUser.isOptedIn()) {

          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinStep1optedIn"));

          twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
          
          profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource);

          return OptinView.index;
        }
        
        twoFactorRequestContainer.getTwoFactorProfileContainer().setProfileForOptin(true);
        
        boolean hasEmail = false;
        
        if (twoFactorRequestContainer.isEditableEmail()) {
          hasEmail = !StringUtils.isBlank(twoFactorUser.getEmail0());
        } else {

          //if not editable, get from the subject source
          Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              loggedInUser, true, false, true);
          if (subject != null) {
            hasEmail = !StringUtils.isBlank(TfSourceUtils.retrieveEmail(subject));
          }
        }
        //lets validate the profile
        if (!hasEmail) {
          profileLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorEmailRequired"));
          return OptinView.profile;
        }
        
        if (lifelineCount(twoFactorUser.getColleagueUserUuid0(), twoFactorUser.getColleagueUserUuid1(),
            twoFactorUser.getColleagueUserUuid2(), twoFactorUser.getColleagueUserUuid3(), 
            twoFactorUser.getColleagueUserUuid4(), twoFactorUser.getPhone0(), twoFactorUser.getPhone1(),
            twoFactorUser.getPhone2()) < 2) {
          profileLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, subjectSource);
          return OptinView.profile;    
        }

        if (!initNewOathCode(twoFactorRequestContainer, twoFactorDaoFactory, twoFactorCode, twoFactorUser)) {
          
          twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
          
          profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource);

          return OptinView.index;
        }
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR_STEP1, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return OptinView.optin;
      }
    });
  }

  /**
   * test ui stuff
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void uiTestIndex(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    showJsp("uiTest.jsp");

  }
  
  /**
   * test ui stuff
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void uiTestPersonPickerCombo(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    personPicker(httpServletRequest, httpServletResponse);

  }

  

  /**
   * optin to the serviceby serial of token
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinBySerialAndTest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String twoFactorPass = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");
    String serialNumber = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("serialNumber");

    String birthMonthString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthMonth");
    String birthDayString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthDay");
    String birthYearString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthYear");

    String birthdayTextfield = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    Source subjectSource = TfSourceUtils.mainSource();
    
    OptinTestSubmitView optinTestSubmitView = optinTestSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), twoFactorPass, subjectSource, serialNumber, true,
        birthMonthString, birthDayString, birthYearString, birthdayTextfield);
  
    showJsp(optinTestSubmitView.getJsp());
  
  }

  /**
   * optin phone code
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinPhoneCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    int phoneIndex = Integer.parseInt(httpServletRequest.getParameter("phoneIndex"));
    String phoneType = httpServletRequest.getParameter("phoneType");
  
    new UiMainPublic().sendPhoneCode(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, 
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), phoneIndex, phoneType, true, false, null);
        
    showJsp("optin.jsp");
  
  }


  
  /**
   * optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinTestSubmit(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String twoFactorPass = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthMonthString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthMonth");
    String birthDayString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthDay");
    String birthYearString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthYear");
    
    String birthdayTextfield = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    OptinTestSubmitView optinTestSubmitView = optinTestSubmitLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), twoFactorPass, subjectSource, null, false,
        birthMonthString, birthDayString, birthYearString, birthdayTextfield);
  
    showJsp(optinTestSubmitView.getJsp());
  
  }

  /**
   * add phone test code to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param twoFactorPass 
   * @param subjectSource
   * @return error message if there is one and jsp
   */
  public AddPhoneTestSubmitView addPhoneTestSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String twoFactorPass, final Source subjectSource) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    AddPhoneTestSubmitView result =  (AddPhoneTestSubmitView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
    
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        String twoFactorSecret = twoFactorUser.getTwoFactorSecretUnencrypted();
          
        if (StringUtils.isBlank(twoFactorSecret)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneSubmitErrorInconsistent"));
          
          twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
          
          profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource);

          return AddPhoneTestSubmitView.index;
          
        }
          
        if (StringUtils.isBlank(twoFactorPass)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneErrorCodeRequired"));
          
          //go back to add phone screen
          //dont audit again
          AddPhoneView addPhoneView = addPhoneLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource, false);
          
          return addPhoneView.toAddPhoneTestView();
        }
          
        //validate
        if (!numberMatcher.matcher(twoFactorPass).matches()) {
          
          String loginId = TfSourceUtils.convertSubjectIdToNetId(subjectSource, loggedInUser, false);
          LOG.error("Error for " + loginId + " add phone validating code not number, now: " 
              + System.currentTimeMillis() 
              + ", user-agent: " + userAgent);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneErrorCodeInvalid"));

          //go back to add phone screen
          //dont audit again
          AddPhoneView addPhoneView = addPhoneLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource, false);
          
          return addPhoneView.toAddPhoneTestView();

        }
          
        //no need to validate the password, the password checker will do that
        TwoFactorPassResult twoFactorPassResult = TwoFactorOath.twoFactorCheckPassword(
            twoFactorSecret, twoFactorPass, twoFactorUser.getSequentialPassIndex(), 
            twoFactorUser.getLastTotpTimestampUsed(), twoFactorUser.getLastTotp60TimestampUsed(), twoFactorUser.getTokenIndex(), null);
        if (!twoFactorPassResult.isPasswordCorrect()) {
  
          String loginId = TfSourceUtils.convertSubjectIdToNetId(subjectSource, loggedInUser, false);
          LOG.error("Error for " + loginId + " validating code, now: " 
              + System.currentTimeMillis() + ", " + TwoFactorServerUtils.hostname()
              + ", user-agent: " + userAgent);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneErrorCodeInvalid"));

          //go back to add phone screen
          //dont audit again
          AddPhoneView addPhoneView = addPhoneLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource, false);
          
          return addPhoneView.toAddPhoneTestView();
        }
          
        //set the object
        if (twoFactorPassResult.getNextHotpIndex() != null) {
          twoFactorUser.setSequentialPassIndex(twoFactorPassResult.getNextHotpIndex());
        }
        if (twoFactorPassResult.getLastTotp30TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp30TimestampUsed());
        }
        if (twoFactorPassResult.getLastTotp60TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp60TimestampUsed());
        }
        if (twoFactorPassResult.getNextTokenIndex() != null) {
          twoFactorUser.setTokenIndex(twoFactorPassResult.getNextTokenIndex());
        }
        twoFactorUser.store(twoFactorDaoFactory);

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.ADD_PHONE_TEST, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);

        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addPhoneSuccess"));

        twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
        
        profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
            loggedInUser, ipAddress, 
            userAgent, subjectSource);

        return AddPhoneTestSubmitView.index;
      }
    });

    return result;

  }


  /**
   * optin wizard to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource 
   * @return the view
   */
  public OptinWizardWelcomeView optinWizardLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    new UiMainPublic().setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorRequestContainer.getTwoFactorUserLoggedIn());
    
    return optinWizardSetup(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, 
        TwoFactorOath.twoFactorGenerateTwoFactorPass(), subjectSource);
  }


  /**
   * optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizard(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
  
    OptinWizardWelcomeView optinWizardWelcomeView = optinWizardLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    showJsp(optinWizardWelcomeView.getJsp());
    
  }
  
  /**
   * 
   */
  public static enum OptinWizardSubmitTypeView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinAppInstall("optinAppInstall.jsp"),
    
    /**
     */
    optinFobInstall("optinFobRegister.jsp"),
    
    /**
     */
    optinPhoneInstall("optinPhoneInstall.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitTypeView(String theJsp) {
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
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @return error message if there is one and jsp
   */
  public OptinWizardSetupAppDoneView optinWizardSetupAppDoneLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSetupAppDoneView.index;
    }

    OptinWizardSetupAppDoneView result =  (OptinWizardSetupAppDoneView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSetupAppDoneView.optinWelcome;
        }
        
        if (!twoFactorUser.isOptedIn()) {
          String twoFactorCode = TwoFactorOath.twoFactorGenerateTwoFactorPass();
  
          if (!initNewOathCode(twoFactorRequestContainer, 
              twoFactorDaoFactory, twoFactorCode, twoFactorUser)) {
            
            twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
            
            profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
                loggedInUser, ipAddress, 
                userAgent, subjectSource);

            return OptinWizardSetupAppDoneView.index;
          }
  
          //move from temp to real code
          twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorUser.getTwoFactorSecretTempUnencrypted());
          twoFactorUser.setTwoFactorSecretTemp(null);
          twoFactorUser.store(twoFactorDaoFactory);
          
          //sync user to duo
          DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);
        }
        
        boolean success = duoPushEnrollLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
            loggedInUser, ipAddress, 
            userAgent, false, subjectSource);
        if (success) {

          return OptinWizardSetupAppDoneView.optinAppIntegrate;

        }

        twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
        
        profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
            loggedInUser, ipAddress, 
            userAgent, subjectSource);

        return OptinWizardSetupAppDoneView.index;
        
//        boolean success = duoPushLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
//            loggedInUser, ipAddress, 
//            userAgent, false);
      }
    });

    return result;
  }
  
  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitAppIntegrate(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    OptinWizardSubmitAppIntegrateView optinWizardSubmitAppIntegrateView = optinWizardSubmitAppIntegrateLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid);

    showJsp(optinWizardSubmitAppIntegrateView.getJsp());
  }

  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitAppIntegrateView optinWizardSubmitAppIntegrateLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitAppIntegrateView.index;
    }

    OptinWizardSubmitAppIntegrateView result =  (OptinWizardSubmitAppIntegrateView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorUser.setSubjectSource(subjectSource);

        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitAppIntegrateView.optinWelcome;
        }

        twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
        
        if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()
            || StringUtils.isBlank(twoFactorUser.getDuoPushPhoneId())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
          return OptinWizardSubmitAppIntegrateView.optinWelcome;
        }

        String txId = DuoCommands.duoInitiatePushByPhoneId(twoFactorUser.getDuoUserId(), 
            twoFactorUser.getDuoPushPhoneId(), null, null);

        if (StringUtils.isBlank(txId)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorWithPush"));
          return OptinWizardSubmitAppIntegrateView.optinWelcome;
        }
        
        String duoTxId = System.currentTimeMillis() + "__uiSoNoBrowserId__" + txId;
        twoFactorUser.setDuoPushTransactionId(duoTxId);

        twoFactorUser.store(twoFactorDaoFactory);

        return OptinWizardSubmitAppIntegrateView.optinAppTest;
      }
    });

    return result;
  }
  


  /**
   * submit after install app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSetupAppDone(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    OptinWizardSetupAppDoneView optinWizardSetupAppDoneView = optinWizardSetupAppDoneLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid);

    showJsp(optinWizardSetupAppDoneView.getJsp());
  }

  
  /**
   * submit which type of optin
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitType(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    OptinSubmitType optInType = OptinSubmitType.valueOfIgnoreCase(TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("optInTypeName"));

    OptinWizardSubmitTypeView optinWizardSubmitTypeView = optinWizardSubmitTypeLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource, optInType,
        birthDayUuid);

    showJsp(optinWizardSubmitTypeView.getJsp());
  }
  
  /**
   * type of submit
   */
  private static enum OptinSubmitType {
    
    /**
     * app
     */
    app,
    
    /**
     * phone
     */
    phone,
    
    /**
     * fob
     */
    fob;
    
    /**
     * take a string and convert to enum.  no reason to not find this
     * @param string
     * @return the enum
     */
    public static OptinSubmitType valueOfIgnoreCase(String string) {
      return TwoFactorServerUtils.enumValueOfIgnoreCase(OptinSubmitType.class, string, true, true);
    }

    
  }
  
  /**
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param optinSubmitType
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitTypeView optinWizardSubmitTypeLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final OptinSubmitType optinSubmitType, final String birthDayUuid) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitTypeView.index;
    }

    new UiMainPublic().setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, 
        twoFactorRequestContainer.getTwoFactorUserLoggedIn());

    OptinWizardSubmitTypeView result =  (OptinWizardSubmitTypeView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitTypeView.optinWelcome;
        }

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_SUBMIT_TYPE, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), optinSubmitType.name(), null);

        switch (optinSubmitType) {
          case app:
            return OptinWizardSubmitTypeView.optinAppInstall;
          case fob:
            return OptinWizardSubmitTypeView.optinFobInstall;
          case phone:
            return OptinWizardSubmitTypeView.optinPhoneInstall;
        }
        return null;
      }
    });

    return result;
  }
  
  
  /**
   * optin to the service, submit birthday
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitEmail(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    String email0 = null;
    if (twoFactorRequestContainer.isEditableEmail()) {
      email0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("email0");
    }

    boolean profileForOptin = TwoFactorServerUtils.booleanValue(
        TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("profileForOptin"), false);

    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    OptinWizardSubmitEmailView optinWizardSubmitEmailView = optinWizardSubmitEmailLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource, birthDayUuid, email0, profileForOptin);
  
    showJsp(optinWizardSubmitEmailView.getJsp());

  }

  /**
   * 
   */
  public static enum OptinWizardSubmitEmailView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinProfilePhones("optinProfilePhones.jsp"),
    
    /**
     */
    optinProfileEmail("optinProfileEmail.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitEmailView(String theJsp) {
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
   * optin to the service, submit birthday
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitBirthday(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthMonthString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthMonth");
    String birthDayString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthDay");
    String birthYearString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthYear");
    
    String birthdayTextfield = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");

    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    OptinWizardSubmitBirthdayView optinWizardSubmitBirthdayView = optinWizardSubmitBirthdayLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthMonthString, birthDayString, birthYearString, birthdayTextfield);
  
    showJsp(optinWizardSubmitBirthdayView.getJsp());

  }

  /**
   * 
   */
  public static enum OptinWizardSubmitBirthdayView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinProfileEmail("optinProfileEmail.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitBirthdayView(String theJsp) {
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
  public static enum OptinWizardSetupAppDoneView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinAppIntegrate("optinAppIntegrate.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSetupAppDoneView(String theJsp) {
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
  public static enum OptinWizardSubmitAppIntegrateView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinAppTest("optinAppTest.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitAppIntegrateView(String theJsp) {
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
  public static enum OptinWizardSubmitAppTestView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    //optinAppTest("optinAppTest.jsp"),

    /**
     * install the app
     */
    optinAppInstall("optinAppInstall.jsp"),

    /**
     */
    optinPrintCodes("showOneTimeCodes.jsp");

    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitAppTestView(String theJsp) {
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
  public static enum OptinWizardPhoneCodeSentView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinConfirmPhoneCode("optinConfirmPhoneCode.jsp"),
    
    /**
     */
    optinPhoneInstall("optinPhoneInstall.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardPhoneCodeSentView(String theJsp) {
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
  public static enum OptinWizardTotpAppIntegrateView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinTotpAppIntegrate("optinTotpAppIntegrate.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardTotpAppIntegrateView(String theJsp) {
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
  public static enum OptinWizardTotpAppInstallView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinTotpAppInstall("optinTotpAppInstall.jsp"),
    
    /**
     */
    optinTotpAppInstallUploadSecret("optinTotpAppUploadSecret.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardTotpAppInstallView(String theJsp) {
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
  public static enum OptinWizardSubmitFriendsView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinProfileFriends("optinProfileFriends.jsp"),
    
    /**
     */
    optinSelectType("optinSelectType.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitFriendsView(String theJsp) {
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
  public static enum OptinWizardSubmitPhonesView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),

    /**
     */
    optinProfilePhones("optinProfilePhones.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinProfileFriends("optinProfileFriends.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitPhonesView(String theJsp) {
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
  public static enum OptinWizardSubmitPhoneCodeView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinPrintCodes("showOneTimeCodes.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitPhoneCodeView(String theJsp) {
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
  public static enum OptinWizardSubmitFobSerialView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinFobRegister("optinFobRegister.jsp"),
    
    /**
     */
    optinPrintCodes("showOneTimeCodes.jsp");

    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitFobSerialView(String theJsp) {
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
  public static enum OptinWizardTotpAppInstallSelectTypeView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinTotpAppInstallSelectType("optinTotpAppInstallSelectType.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardTotpAppInstallSelectTypeView(String theJsp) {
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
  public static enum OptinWizardSubmitTotpAppIntegrateView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinTotpAppConfirmCode("optinTotpAppConfirmCode.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitTotpAppIntegrateView(String theJsp) {
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
  public static enum OptinWizardSubmitTotpAppCodeView {
    
    /**
     */
    optinWelcome("optinWelcome.jsp"),
    
    /**
     */
    index("twoFactorIndex2.jsp"),
    
    /**
     */
    optinPrintCodes("showOneTimeCodes.jsp"),
    
    /**
     * 
     */
    optinTotpAppConfirmCode("optinTotpAppConfirmCode.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardSubmitTotpAppCodeView(String theJsp) {
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
  public static enum OptinWizardAddPushView {

    /**
     */
    optinWelcome("optinWelcome.jsp"),

    /**
     */
    index("twoFactorIndex2.jsp"),

    /**
     */
    optinAppInstall("optinAppInstall.jsp");

    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardAddPushView(String theJsp) {
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
  public static enum OptinWizardDoneView {
    
    /**
     */
    index("twoFactorIndex2.jsp"),
  
    /**
     */
    optinWizardDone("optinWizardDone.jsp");
    
    /**
     * 
     */
    private String jsp;
    
    /**
     * 
     * @param theJsp
     */
    private OptinWizardDoneView(String theJsp) {
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
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthMonthString 
   * @param birthDayString 
   * @param birthYearString 
   * @param birthdayTextfield 
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitBirthdayView optinWizardSubmitBirthdayLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthMonthString,
      final String birthDayString, final String birthYearString, final String birthdayTextfield) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitBirthdayView.index;
    }

    OptinWizardSubmitBirthdayView result =  (OptinWizardSubmitBirthdayView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorRequestContainer.getTwoFactorProfileContainer().setProfileForOptin(true);

        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, 
            userAgent, subjectSource, twoFactorUser, 
            birthMonthString, birthDayString, 
            birthYearString, birthdayTextfield)) {
          return OptinWizardSubmitBirthdayView.optinWelcome;
        }
        
        //we are passed bday, make a uuid and store as key
        String birthDayUuid = TwoFactorServerUtils.uuid();
        twoFactorUser.setBirthDayUuid(birthDayUuid);
        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_SUBMIT_BIRTHDAY, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return OptinWizardSubmitBirthdayView.optinProfileEmail;
      }
    });
    
    return result;
  }

  /**
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress
   * @param userAgent
   * @param twoFactorCode
   * @param subjectSource 
   * @return which view to go to
   * 
   */
  private OptinWizardWelcomeView optinWizardSetup(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, final String userAgent,
      final String twoFactorCode, final Source subjectSource) {
    
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
    
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardWelcomeView.index;
    }
  
    return (OptinWizardWelcomeView)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
        if (twoFactorUser.isOptedIn()) {
  
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinStep1optedIn"));
  
          twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
          
          profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource);

          return OptinWizardWelcomeView.index;
        }
        
        twoFactorRequestContainer.getTwoFactorProfileContainer().setProfileForOptin(true);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR_STEP1, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);

        return OptinWizardWelcomeView.optinWelcome;
      }
    });
  }


  /**
   * submit after test app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitAppTest(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    OptinWizardSubmitAppTestView optinWizardSubmitAppTestView = optinWizardSubmitAppTestLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid);
  
    showJsp(optinWizardSubmitAppTestView.getJsp());
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitAppTestView optinWizardSubmitAppTestLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitAppTestView.index;
    }
  
    OptinWizardSubmitAppTestView result =  (OptinWizardSubmitAppTestView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitAppTestView.optinWelcome;
        }
        String timestampBrowserTxId = twoFactorUser.getDuoPushTransactionId();
        String[] pieces = TwoFactorServerUtils.splitTrim(timestampBrowserTxId, "__");

        if (TwoFactorServerUtils.length(pieces) != 3 || !StringUtils.equals("uiSoNoBrowserId", pieces[1])) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinAppTestErrorText"));
          return OptinWizardSubmitAppTestView.optinAppInstall;
          
        }
        
        //if not already used
        String timestampString = pieces[0];

        long timestampLong = TwoFactorServerUtils.longValue(timestampString);
        
        //give them 10 minutes
        if (((System.currentTimeMillis() - timestampLong) / 1000) > (60 * 10)) { 

          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinAppTestErrorTimeoutText"));
          return OptinWizardSubmitAppTestView.optinWelcome;

        }

        String txId = pieces[2];

        //see if valid
        boolean validPush = false;
        
        try {
          validPush = DuoCommands.duoPushOrPhoneSuccess(txId, 5);
        } catch (RuntimeException re) {
          //if its a timeout, then validPush is false, else rethrow
          if (ExceptionUtils.getFullStackTrace(re).toLowerCase().contains("timeout")) {
            validPush = false;
          } else {
            throw re;
          }
        }

        if (validPush) {
          boolean wasOptedIn = twoFactorUser.isOptedIn();
          if (!wasOptedIn) {
            twoFactorUser.setOptedIn(true);
            twoFactorUser.setOptInOnlyIfRequired(null);
            TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
                TwoFactorAuditAction.OPTIN_TWO_FACTOR, ipAddress, 
                userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
          } else {
            TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
                TwoFactorAuditAction.DUO_ENABLE_PUSH, ipAddress, 
                userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
            TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
                TwoFactorAuditAction.DUO_ENABLE_PUSH_FOR_WEB, ipAddress, 
                userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
          }
          twoFactorUser.setDuoPushByDefault(true);
          twoFactorUser.setPhoneOptIn(false);
          twoFactorUser.setDuoPushTransactionId(null);
          twoFactorUser.store(twoFactorDaoFactory);

          //opt in to duo
          if (duoRegisterUsers()) {

            DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);

          }
          
          if (!wasOptedIn) {
            setupOneTimeCodesOnOptin(twoFactorDaoFactory, twoFactorUser, 
                twoFactorRequestContainer, ipAddress, userAgent);
            //opt the user in
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinSuccessMessage"));

            emailUserAfterOptin(twoFactorRequestContainer, loggedInUser, subjectSource);
            
            twoFactorRequestContainer.getTwoFactorOptinContainer().setOptinWizardInProgress(true);
            
            return OptinWizardSubmitAppTestView.optinPrintCodes;
          }
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushEnrolledInPushForWeb"));
          
          twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
          
          profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, subjectSource);

          return OptinWizardSubmitAppTestView.index;
        }

        //problem, try again?
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinAppTestErrorText"));
        return OptinWizardSubmitAppTestView.optinAppInstall;
        
      }
    });
  
    return result;
  }

  /**
   * @param twoFactorDaoFactory 
   * @param twoFactorUser
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   */
  public static void setupOneTimeCodesOnOptin(TwoFactorDaoFactory twoFactorDaoFactory, TwoFactorUser twoFactorUser, 
      TwoFactorRequestContainer twoFactorRequestContainer, String ipAddress, String userAgent) {

    //see what the user already had
    if (twoFactorUser.getSeqPassIndexGivenToUser() == null || twoFactorUser.getSeqPassIndexGivenToUser() < 500000L) {
      //set to 0 so we can add one
      twoFactorUser.setSeqPassIndexGivenToUser(500000L);
    }

    //make sure sequential pass index exists
    if (twoFactorUser.getSequentialPassIndex() == null || twoFactorUser.getSequentialPassIndex() < 500001L) {
      twoFactorUser.setSequentialPassIndex(500001L);
    }

    //make the pass index to use equal to the one given to the user plus one
    if (twoFactorUser.getSeqPassIndexGivenToUser() > twoFactorUser.getSequentialPassIndex()) {
      twoFactorUser.setSequentialPassIndex(twoFactorUser.getSeqPassIndexGivenToUser() + 1);
    }

    int numberOfOneTimePassesShownOnScreen = TwoFactorServerConfig.retrieveConfig()
        .propertyValueInt("twoFactorServer.hotpSecretsShownOnScreen", 20);

    //subtract one since if you show one code, the last you gave to the user is that one...
    twoFactorUser.setSeqPassIndexGivenToUser((twoFactorUser.getSequentialPassIndex() + numberOfOneTimePassesShownOnScreen)-1);
    twoFactorUser.store(twoFactorDaoFactory);

    List<TwoFactorOneTimePassRow> passRows = new ArrayList<TwoFactorOneTimePassRow>();
    twoFactorRequestContainer.setOneTimePassRows(passRows);

    TwoFactorLogicInterface twoFactorLogicInterface = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
    
    Base32 base32 = new Base32();
    byte[] secret = base32.decode(twoFactorUser.getTwoFactorSecretUnencrypted());
    
    long firstNumberLabel = twoFactorUser.getSequentialPassIndex();
    
    if (firstNumberLabel >= 1000) {
      firstNumberLabel = firstNumberLabel % 1000;
    }
    
    for (int i=0;i<numberOfOneTimePassesShownOnScreen/2;i++) {
      TwoFactorOneTimePassRow twoFactorOneTimePassRow = new TwoFactorOneTimePassRow();
      passRows.add(twoFactorOneTimePassRow);
      
      {
        String oneTimePassCol1 = Integer.toString(twoFactorLogicInterface.hotpPassword(secret, twoFactorUser.getSequentialPassIndex()+i));
        oneTimePassCol1 = StringUtils.leftPad(oneTimePassCol1, 6, '0');
        oneTimePassCol1 = StringUtils.leftPad(Long.toString(firstNumberLabel + i), 3, " ") + ". " 
          + oneTimePassCol1;
        twoFactorOneTimePassRow.setOneTimePassCol1(oneTimePassCol1);
      }
      
      {
        String oneTimePassCol2 = Integer.toString(twoFactorLogicInterface.hotpPassword(secret, 
            (twoFactorUser.getSequentialPassIndex()+(numberOfOneTimePassesShownOnScreen/2))+i));
        oneTimePassCol2 = StringUtils.leftPad(oneTimePassCol2, 6, '0');
        oneTimePassCol2 = 
          StringUtils.leftPad(Long.toString(firstNumberLabel
            +(numberOfOneTimePassesShownOnScreen/2) + i), 3, " ") + ". " 
            + oneTimePassCol2;
        twoFactorOneTimePassRow.setOneTimePassCol2(oneTimePassCol2);
      }      
    }
    
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.GENERATE_PASSWORDS, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
    //change codes in duo
    if (duoRegisterUsers()) {
      
      String duoUserId = DuoCommands.retrieveDuoUserIdBySomeId(twoFactorRequestContainer.getTwoFactorUserLoggedIn().getLoginid());
      
      DuoCommands.setupOneTimeCodes(twoFactorRequestContainer.getTwoFactorUserLoggedIn(), duoUserId, true);
      
    }

  }

  /**
   * pick phone code
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardPhoneCodeSent(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    int phoneIndex = Integer.parseInt(httpServletRequest.getParameter("phoneIndex"));
    String phoneType = httpServletRequest.getParameter("phoneType");
  
    OptinWizardPhoneCodeSentView optinWizardPhoneCodeSentView = optinWizardPhoneCodeSentLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid, phoneIndex, phoneType);
  
    showJsp(optinWizardPhoneCodeSentView.getJsp());
  }


  /**
   * optin to two factor send phone code
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @param phoneIndex index of phone (0, 1, 2)
   * @param phoneType voice or text
   * @return error message if there is one and jsp
   */
  public OptinWizardPhoneCodeSentView optinWizardPhoneCodeSentLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, final int phoneIndex,
      final String phoneType) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardPhoneCodeSentView.index;
    }
  
    OptinWizardPhoneCodeSentView result =  (OptinWizardPhoneCodeSentView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardPhoneCodeSentView.optinWelcome;
        }

        //this is 0t, 1v, etc
        twoFactorUser.setPhoneAutoCalltext(Integer.toString(phoneIndex) + phoneType.charAt(0));
        twoFactorUser.store(twoFactorDaoFactory);
        
        new UiMainPublic().sendPhoneCode(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, 
            loggedInUser, ipAddress, 
            userAgent, phoneIndex, phoneType, true, false, null);        

        return OptinWizardPhoneCodeSentView.optinConfirmPhoneCode;
      }
    });

    return result;
  }


  /**
   * integrate totp app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardTotpAppIntegrate(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    OptinWizardTotpAppIntegrateView optinWizardTotpAppIntegrateView = optinWizardTotpAppIntegrateLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid);
  
    showJsp(optinWizardTotpAppIntegrateView.getJsp());
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @return error message if there is one and jsp
   */
  public OptinWizardTotpAppIntegrateView optinWizardTotpAppIntegrateLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardTotpAppIntegrateView.index;
    }
  
    OptinWizardTotpAppIntegrateView result =  (OptinWizardTotpAppIntegrateView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorUser.setSubjectSource(subjectSource);

        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardTotpAppIntegrateView.optinWelcome;
        }

        return OptinWizardTotpAppIntegrateView.optinTotpAppIntegrate;
      }
    });

    return result;
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardTotpAppInstall(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    String optinTotpTypeName = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("optinTotpTypeName");

    String twoFactorCustomCode = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCustomCode");
    
    OptinWizardTotpAppInstallView optinWizardTotpAppInstallView = optinWizardTotpAppInstallLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid, optinTotpTypeName, twoFactorCustomCode);
  
    showJsp(optinWizardTotpAppInstallView.getJsp());
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress
   * @param userAgent
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid
   * @param optinTotpTypeName
   * @param twoFactorCustomCode
   * @return error message if there is one and jsp
   */
  public OptinWizardTotpAppInstallView optinWizardTotpAppInstallLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid,
      final String optinTotpTypeName, final String twoFactorCustomCode) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardTotpAppInstallView.index;
    }

    OptinWizardTotpAppInstallView result =  (OptinWizardTotpAppInstallView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {

      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardTotpAppInstallView.optinWelcome;
        }
  
        if (StringUtils.equals(optinTotpTypeName, "uploaded")) {
          return OptinWizardTotpAppInstallView.optinTotpAppInstallUploadSecret;
        } else if (StringUtils.equals(optinTotpTypeName, "generated")) {

          // if there is already a secret (e.g. from fob), then keep it
          if (!StringUtils.isBlank(twoFactorUser.getTwoFactorSecret())) {
            String[] error = new String[1];
            validateCustomCode(twoFactorUser.getTwoFactorSecretUnencrypted(), error);
            if (!StringUtils.isBlank(error[0])) {
              twoFactorUser.setTwoFactorSecret(null);
            }
          }
          if (StringUtils.isBlank(twoFactorUser.getTwoFactorSecret())) {
            String twoFactorCode = TwoFactorOath.twoFactorGenerateTwoFactorPass();
            if (!initNewOathCode(twoFactorRequestContainer, twoFactorDaoFactory, twoFactorCode, twoFactorUser)) {
              
              twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
              
              profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
                  loggedInUser, ipAddress, 
                  userAgent, subjectSource);

              return OptinWizardTotpAppInstallView.index;
            }
          } else {
            twoFactorUser.setTwoFactorSecretTempUnencrypted(twoFactorUser.getTwoFactorSecretUnencrypted());
          }

        } else if (StringUtils.equals(optinTotpTypeName, "uploadedSubmit")) {
          //submitted code from user
          if (!StringUtils.isBlank(twoFactorCustomCode)) {
            
            String[] error = new String[1];
            
            String twoFactorCustomCodeFormatted = validateCustomCode(twoFactorCustomCode, error);

            if (!StringUtils.isBlank(error[0])) {
              twoFactorRequestContainer.setError(error[0]);
              
              twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
              
              profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
                  loggedInUser, ipAddress, 
                  userAgent, subjectSource);

              return OptinWizardTotpAppInstallView.index;
            }
            twoFactorUser.setTwoFactorSecret(null);
            if (!initNewOathCode(twoFactorRequestContainer, twoFactorDaoFactory, twoFactorCustomCodeFormatted, twoFactorUser)) {
              
              twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
              
              profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
                  loggedInUser, ipAddress, 
                  userAgent, subjectSource);

              return OptinWizardTotpAppInstallView.index;
            }

          } else {
            
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinTotpAppUploadSecretRequired"));
            return OptinWizardTotpAppInstallView.optinTotpAppInstallUploadSecret;
          }
         
        } else {
          throw new RuntimeException("Invalid value for optinTotpTypeName");
        }
        
        twoFactorUser.store(twoFactorDaoFactory);
        return OptinWizardTotpAppInstallView.optinTotpAppInstall;
      }
    });
  
    return result;
  }


  /**
   * optin to two factor, submit email
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress
   * @param userAgent
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid
   * @param email0
   * @param profileForOptin
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitEmailView optinWizardSubmitEmailLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, 
      final String email0, final boolean profileForOptin) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitEmailView.index;
    }
  
    OptinWizardSubmitEmailView result =  (OptinWizardSubmitEmailView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitEmailView.optinWelcome;
        }
        
        String localEmail0 = email0;
        
        twoFactorRequestContainer.getTwoFactorProfileContainer().setProfileForOptin(true);

        //generate the codes
        twoFactorUser.setSubjectSource(subjectSource);
        
        TwoFactorProfileContainer twoFactorProfileContainer = twoFactorRequestContainer.getTwoFactorProfileContainer();
        
        twoFactorProfileContainer.setEmail0(localEmail0);
        twoFactorProfileContainer.setProfileForOptin(profileForOptin);
        
        Subject loggedInSubject = null;
        
        if (StringUtils.isBlank(twoFactorProfileContainer.getEmail0()) && subjectSource != null) {
          
          //resolve subject
          loggedInSubject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
              loggedInUser, true, false, true);

          if (loggedInSubject != null) {
            
            twoFactorProfileContainer.setEmail0(TfSourceUtils.retrieveEmail(loggedInSubject));
            localEmail0 = twoFactorProfileContainer.getEmail0();
          }
        }
        
        String errorMessage = null;
        
        if (twoFactorRequestContainer.isEditableEmail()) {
          errorMessage = validateEmail(localEmail0);
          if (!StringUtils.isBlank(errorMessage) ) {
            twoFactorRequestContainer.setError(errorMessage);
            return OptinWizardSubmitEmailView.optinProfileEmail;
          }
  
          twoFactorUser.setEmail0(localEmail0);
          twoFactorUser.store(twoFactorDaoFactory);

          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("profileSubmitEmailSuccessMessage"));
        }
        
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_SUBMIT_EMAIL, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return OptinWizardSubmitEmailView.optinProfilePhones;
      }
    });
    
    return result;
  }


  /**
   * optin to the service, submit birthday
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitPhones(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    boolean profileForOptin = TwoFactorServerUtils.booleanValue(
        TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("profileForOptin"), false);
  
    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    String phone0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone0");
    //this is "on" if submitted, or null if not
    String phoneVoice0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice0");
    String phoneText0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText0");
    String phone1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone1");
    String phoneVoice1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice1");
    String phoneText1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText1");
    String phone2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phone2");
    String phoneVoice2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneVoice2");
    String phoneText2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("phoneText2");

    OptinWizardSubmitPhonesView optinWizardSubmitPhonesView = optinWizardSubmitPhonesLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource, birthDayUuid, phone0, phoneVoice0, phoneText0, 
        phone1, phoneVoice1, phoneText1, phone2, phoneVoice2, phoneText2, profileForOptin);
  
    showJsp(optinWizardSubmitPhonesView.getJsp());
  
  }


  /**
   * optin to two factor, submit phones
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress
   * @param userAgent
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid
   * @param phone0 
   * @param phoneVoice0 
   * @param phoneText0 
   * @param phone1 
   * @param phoneVoice1 
   * @param phoneText1 
   * @param phone2 
   * @param phoneVoice2 
   * @param phoneText2 
   * @param profileForOptin
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitPhonesView optinWizardSubmitPhonesLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, 
      final String phone0, final String phoneVoice0, final String phoneText0, 
      final String phone1, final String phoneVoice1, final String phoneText1, final String phone2, 
      final String phoneVoice2, final String phoneText2, final boolean profileForOptin) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitPhonesView.index;
    }
  
    OptinWizardSubmitPhonesView result =  (OptinWizardSubmitPhonesView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitPhonesView.optinWelcome;
        }
        
        //generate the codes
        twoFactorUser.setSubjectSource(subjectSource);
        
        TwoFactorProfileContainer twoFactorProfileContainer = twoFactorRequestContainer.getTwoFactorProfileContainer();
        
        twoFactorProfileContainer.setProfileForOptin(profileForOptin);
        
        twoFactorProfileContainer.setPhone0(phone0);
        twoFactorProfileContainer.setPhoneText0(phoneText0);
        twoFactorProfileContainer.setPhoneVoice0(phoneVoice0);
        twoFactorProfileContainer.setPhone1(phone1);
        twoFactorProfileContainer.setPhoneText1(phoneText1);
        twoFactorProfileContainer.setPhoneVoice1(phoneVoice1);
        twoFactorProfileContainer.setPhone2(phone2);
        twoFactorProfileContainer.setPhoneText2(phoneText2);
        twoFactorProfileContainer.setPhoneVoice2(phoneVoice2);

        String errorMessage = null;
        
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone0, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone1"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone1, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone2"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhone(phone2, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone3"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone0, phoneText0, phoneVoice0, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone1"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone1, phoneText1, phoneVoice1, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone2"));
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validatePhoneType(phone2, phoneText2, phoneVoice2, TextContainer.retrieveFromRequest().getText().get("profileErrorLabelPhone3"));
        }
        if (StringUtils.isBlank(errorMessage) && StringUtils.isBlank(phone0) && StringUtils.isBlank(phone1) && StringUtils.isBlank(phone2)) {
          errorMessage = TextContainer.retrieveFromRequest().getText().get("profileErrorNotEnoughPhones");
        }

        if (!StringUtils.isBlank(errorMessage) ) {
          twoFactorRequestContainer.setError(errorMessage);
          return OptinWizardSubmitPhonesView.optinProfilePhones;
        }

        twoFactorUser.setPhone0(phone0);
        twoFactorUser.setPhoneIsText0(TwoFactorServerUtils.booleanObjectValue(phoneText0));
        twoFactorUser.setPhoneIsVoice0(TwoFactorServerUtils.booleanObjectValue(phoneVoice0));
        twoFactorUser.setPhone1(phone1);
        twoFactorUser.setPhoneIsText1(TwoFactorServerUtils.booleanObjectValue(phoneText1));
        twoFactorUser.setPhoneIsVoice1(TwoFactorServerUtils.booleanObjectValue(phoneVoice1));
        twoFactorUser.setPhone2(phone2);
        twoFactorUser.setPhoneIsText2(TwoFactorServerUtils.booleanObjectValue(phoneText2));
        twoFactorUser.setPhoneIsVoice2(TwoFactorServerUtils.booleanObjectValue(phoneVoice2));

        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_SUBMIT_PHONES, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("profileSubmitPhonesSuccessMessage"));

        String duoUserId = DuoCommands.retrieveDuoUserIdBySomeId(twoFactorRequestContainer.getTwoFactorUserLoggedIn().getLoginid());
        
        //if user is registered, edit phone
        if (!StringUtils.isBlank(duoUserId)) {
          DuoCommands.migratePhonesToDuoBySomeId(twoFactorRequestContainer.getTwoFactorUserLoggedIn().getLoginid(), false);
        }
        
        return OptinWizardSubmitPhonesView.optinProfileFriends;
      }
    });
    
    return result;
  }


  /**
   * optin to the service, submit birthday
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitFriends(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    boolean profileForOptin = TwoFactorServerUtils.booleanValue(
        TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("profileForOptin"), false);
  
    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    String colleagueLogin0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin0Name");
    if (StringUtils.isBlank(colleagueLogin0)) {
      colleagueLogin0 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin0DisplayName");
    }
    String colleagueLogin1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin1Name");
    if (StringUtils.isBlank(colleagueLogin1)) {
      colleagueLogin1 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin1DisplayName");
    }
    String colleagueLogin2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin2Name");
    if (StringUtils.isBlank(colleagueLogin2)) {
      colleagueLogin2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin2DisplayName");
    }
    String colleagueLogin3 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin3Name");
    if (StringUtils.isBlank(colleagueLogin3)) {
      colleagueLogin3 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin3DisplayName");
    }
    String colleagueLogin4 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin4Name");
    if (StringUtils.isBlank(colleagueLogin4)) {
      colleagueLogin4 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("colleagueLogin4DisplayName");
    }
    
    OptinWizardSubmitFriendsView optinWizardSubmitFriendsView = optinWizardSubmitFriendsLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource, birthDayUuid, colleagueLogin0, colleagueLogin1,
        colleagueLogin2, colleagueLogin3, colleagueLogin4, profileForOptin);
  
    showJsp(optinWizardSubmitFriendsView.getJsp());
  
  }


  /**
   * optin to two factor, submit phones
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress
   * @param userAgent
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid
   * @param colleagueLogin0 
   * @param colleagueLogin1 
   * @param colleagueLogin2 
   * @param colleagueLogin3 
   * @param colleagueLogin4 
   * @param profileForOptin
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitFriendsView optinWizardSubmitFriendsLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, 
      final String colleagueLogin0, final String colleagueLogin1,
      final String colleagueLogin2, final String colleagueLogin3, final String colleagueLogin4,
      final boolean profileForOptin) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitFriendsView.index;
    }
  
    final Set<TwoFactorUser> newColleagues = new HashSet<TwoFactorUser>();

    OptinWizardSubmitFriendsView result =  (OptinWizardSubmitFriendsView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitFriendsView.optinWelcome;
        }
        
        //generate the codes
        twoFactorUser.setSubjectSource(subjectSource);
        
        TwoFactorProfileContainer twoFactorProfileContainer = twoFactorRequestContainer.getTwoFactorProfileContainer();
        
        twoFactorProfileContainer.setProfileForOptin(profileForOptin);
        
        Subject loggedInSubject = null;
        
        if (subjectSource != null) {
      
          //resolve subject
          loggedInSubject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            loggedInUser, true, false, true);
        }
        
        String phone0 = twoFactorUser.getPhone0();
        String phone1 = twoFactorUser.getPhone1();
        String phone2 = twoFactorUser.getPhone2();
        
        String errorMessage = null;

        twoFactorProfileContainer.setColleagueLogin0(colleagueLogin0);
        twoFactorProfileContainer.setColleagueLogin1(colleagueLogin1);
        twoFactorProfileContainer.setColleagueLogin2(colleagueLogin2);
        twoFactorProfileContainer.setColleagueLogin3(colleagueLogin3);
        twoFactorProfileContainer.setColleagueLogin4(colleagueLogin4);

        String selfErrorMessage = TextContainer.retrieveFromRequest().getText().get("profileErrorFriendIsSelf");
        
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin0, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend1invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin1, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend2invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin2, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend3invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin3, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend4invalid"), selfErrorMessage);
        }
        if (StringUtils.isBlank(errorMessage)) {
          errorMessage = validateFriend(loggedInSubject, subjectSource, colleagueLogin4, 
              TextContainer.retrieveFromRequest().getText().get("profileErrorFriend5invalid"), selfErrorMessage);
        }
        
        if (StringUtils.isBlank(errorMessage) && lifelineCount(colleagueLogin0, colleagueLogin1, colleagueLogin2, colleagueLogin3, colleagueLogin4,
            phone0, phone1, phone2) < 2) {
          errorMessage = TextContainer.retrieveFromRequest().getText().get("profileErrorNotEnoughLifelines");
        }

        if (!StringUtils.isBlank(errorMessage) ) {
          twoFactorRequestContainer.setError(errorMessage);
          return OptinWizardSubmitFriendsView.optinProfileFriends;
        }
  
        Set<String> previousColleagueUuids = new HashSet<String>();

        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid0())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid0());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid1())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid1());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid2())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid2());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid3())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid3());
        }
        if (!StringUtils.isBlank(twoFactorUser.getColleagueUserUuid4())) {
          previousColleagueUuids.add(twoFactorUser.getColleagueUserUuid4());
        }

        //get the new colleagues
        
        
        if (StringUtils.isBlank(colleagueLogin0)) {
          twoFactorUser.setColleagueUserUuid0(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin0);
          twoFactorUser.setColleagueUserUuid0(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin1)) {
          twoFactorUser.setColleagueUserUuid1(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin1);
          twoFactorUser.setColleagueUserUuid1(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin2)) {
          twoFactorUser.setColleagueUserUuid2(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin2);
          twoFactorUser.setColleagueUserUuid2(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin3)) {
          twoFactorUser.setColleagueUserUuid3(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin3);
          twoFactorUser.setColleagueUserUuid3(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }
        
        if (StringUtils.isBlank(colleagueLogin4)) {
          twoFactorUser.setColleagueUserUuid4(null);
        } else {
          TwoFactorUser colleagueUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, colleagueLogin4);
          twoFactorUser.setColleagueUserUuid4(colleagueUser.getUuid());
          
          if (!previousColleagueUuids.contains(colleagueUser.getUuid())) {
            newColleagues.add(colleagueUser);
          }
          
        }

        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_SUBMIT_FRIENDS, ipAddress, userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("profileSubmitFriendsSuccessMessage"));

        notifyNewColleagues(twoFactorRequestContainer, loggedInUser, subjectSource, newColleagues,
            twoFactorUser);
        
        return OptinWizardSubmitFriendsView.optinSelectType;
      }
    });
    
    return result;
  }


  /**
   * @param twoFactorRequestContainer 
   * @param twoFactorDaoFactory
   * @param twoFactorCode
   * @param twoFactorUser
   * @return false if invalid
   */
  private boolean initNewOathCode(TwoFactorRequestContainer twoFactorRequestContainer, 
      final TwoFactorDaoFactory twoFactorDaoFactory,
      final String twoFactorCode, TwoFactorUser twoFactorUser) {
    
    String pass = twoFactorCode.toUpperCase();
    
    String[] error = new String[1];

    validateCustomCode(pass, error);
    
    if (!StringUtils.isBlank(error[0])) {
      twoFactorRequestContainer.setError(error[0]);
      return false;
    }

    twoFactorUser.setTwoFactorSecretTempUnencrypted(pass);
    twoFactorUser.setOptedIn(false);
    twoFactorUser.setDatePhoneCodeSent(null);
    twoFactorUser.setPhoneCodeEncrypted(null);
    twoFactorUser.setLastTotpTimestampUsed(null);
    twoFactorUser.setTokenIndex(0L);
    duoClearOutAttributes(twoFactorUser);
    twoFactorUser.store(twoFactorDaoFactory);

    List<TwoFactorBrowser> twoFactorBrowsers = twoFactorDaoFactory.getTwoFactorBrowser().retrieveTrustedByUserUuid(twoFactorUser.getUuid());

    //untrust browsers since opting in, dont want orphans from last time
    for (TwoFactorBrowser twoFactorBrowser : twoFactorBrowsers) {
      twoFactorBrowser.setTrustedBrowser(false);
      twoFactorBrowser.setWhenTrusted(0);
      twoFactorBrowser.store(twoFactorDaoFactory);
    }
    
    return true;
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitPhoneCode(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    String twoFactorPass = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");

    OptinWizardSubmitPhoneCodeView optinWizardSubmitPhoneCodeView = optinWizardSubmitPhoneCodeLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid, twoFactorPass);
  
    showJsp(optinWizardSubmitPhoneCodeView.getJsp());
  }

  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @param twoFactorPass
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitPhoneCodeView optinWizardSubmitPhoneCodeLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, final String twoFactorPass) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitPhoneCodeView.index;
    }
  
    OptinWizardSubmitPhoneCodeView result =  (OptinWizardSubmitPhoneCodeView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

        twoFactorUser.setSubjectSource(subjectSource);

        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitPhoneCodeView.optinWelcome;
        }

        if (!StringUtils.equals(twoFactorPass, twoFactorUser.getPhoneCodeUnencryptedIfNotExpired())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("phoneCodeDoesntMatchError"));
          return OptinWizardSubmitPhoneCodeView.optinWelcome;
        }

        String twoFactorCode = TwoFactorOath.twoFactorGenerateTwoFactorPass();

        if (!initNewOathCode(twoFactorRequestContainer, twoFactorDaoFactory, twoFactorCode, twoFactorUser)) {
          return OptinWizardSubmitPhoneCodeView.optinWelcome;
        }

        twoFactorUser.setOptedIn(true);
        twoFactorUser.setPhoneOptIn(true);
        twoFactorUser.setOptInOnlyIfRequired(null);
        twoFactorUser.setPhoneCodeEncrypted(null);
        twoFactorUser.setDatePhoneCodeSent(null);
        twoFactorUser.setDuoPushByDefault(false);
        
        //move from temp to real code
        twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorUser.getTwoFactorSecretTempUnencrypted());
        twoFactorUser.setTwoFactorSecretTemp(null);

        twoFactorUser.store(twoFactorDaoFactory);

        //opt in to duo
        if (duoRegisterUsers()) {

          DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);

        }

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);

        setupOneTimeCodesOnOptin(twoFactorDaoFactory, twoFactorUser, 
            twoFactorRequestContainer, ipAddress, userAgent);

        //opt the user in
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinSuccessMessage"));

        emailUserAfterOptin(twoFactorRequestContainer, loggedInUser, subjectSource);
        
        twoFactorRequestContainer.getTwoFactorOptinContainer().setOptinWizardInProgress(true);

        return OptinWizardSubmitPhoneCodeView.optinPrintCodes;
      }
    });
  
    return result;
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitFobSerial(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
    
    String serialNumber = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("serialNumber");
  
    String twoFactorCode = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");
    
    String twoFactorCode2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode2");
    
    OptinWizardSubmitFobSerialView optinWizardSubmitFobSerialView = optinWizardSubmitFobSerialLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid, serialNumber, twoFactorCode, twoFactorCode2);
  
    showJsp(optinWizardSubmitFobSerialView.getJsp());
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @param serialNumber
   * @param twoFactorCodeString
   * @param twoFactorCode2String
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitFobSerialView optinWizardSubmitFobSerialLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, final String serialNumber, final String twoFactorCodeString,
      final String twoFactorCode2String) {

    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);

    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }

    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitFobSerialView.index;
    }

    OptinWizardSubmitFobSerialView result =  (OptinWizardSubmitFobSerialView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
        twoFactorUser.setSubjectSource(subjectSource);
  
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitFobSerialView.optinWelcome;
        }

        if (StringUtils.isBlank(serialNumber)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialRequired"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
          
        }

        //lets make sure the serial number exists
        TwoFactorDeviceSerial twoFactorDeviceSerial = TwoFactorDeviceSerial.retrieveBySerial(twoFactorDaoFactory, serialNumber);
       
        if (twoFactorDeviceSerial == null) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialNotFound"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
        }

        //lets see if this device has been registered to someone else
        if (!StringUtils.isBlank(twoFactorDeviceSerial.getUserUuid()) 
            && !StringUtils.equals(twoFactorDeviceSerial.getUserUuid(), twoFactorUser.getUuid())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialRegisteredToSomeoneElse"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
        }

        String twoFactorSecret = twoFactorDeviceSerial.getTwoFactorSecretUnencrypted();

        Base32 codec = new Base32();
        byte[] twoFactorSecretPlainText = codec.decode(twoFactorSecret);

        if (StringUtils.isBlank(twoFactorCodeString)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode1Required"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
          
        }

        if (StringUtils.isBlank(twoFactorCode2String)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode2Required"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
          
        }

        int twoFactorCode = -1;

        try {
          twoFactorCode = TwoFactorServerUtils.intObjectValue(twoFactorCodeString, true);
        } catch (Exception e) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode1Invalid"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
          
        }

        int twoFactorCode2 = -1;

        try {
          twoFactorCode2 = TwoFactorServerUtils.intObjectValue(twoFactorCode2String, true);
        } catch (Exception e) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode2Invalid"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
          
        }

        int nextIndex = -1;
        boolean foundTokenIndex = false;
        for (int i=0;i<20000;i++) {
          if (twoFactorCode == new TwoFactorLogic().hotpPassword(twoFactorSecretPlainText, i)
              && twoFactorCode2 == new TwoFactorLogic().hotpPassword(twoFactorSecretPlainText, i+1)) {
            nextIndex = i+2;
            foundTokenIndex = true;
            break;
          }
        }
        
        if (!foundTokenIndex) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCodesInvalid"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
        }

        twoFactorUser.setTokenIndex((long)nextIndex);
        twoFactorUser.setPhoneOptIn(false);

        twoFactorUser.setOptedIn(true);
        twoFactorUser.setOptInOnlyIfRequired(null);
        twoFactorUser.setPhoneCodeEncrypted(null);
        twoFactorUser.setDatePhoneCodeSent(null);

        //move from temp to real code
        twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorSecret);
        twoFactorUser.setTwoFactorSecretTemp(null);
  
        twoFactorUser.store(twoFactorDaoFactory);
          
        twoFactorDeviceSerial.setUserUuid(twoFactorUser.getUuid());
        twoFactorDeviceSerial.setWhenRegistered(System.currentTimeMillis());
        twoFactorDeviceSerial.store(twoFactorDaoFactory);

        twoFactorRequestContainer.getTwoFactorAdminContainer().setImportSerial(serialNumber);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.REGISTER_FOB_SERIAL_NUMBER, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("optionStep2auditRegisterFob"),
            null);

        
        //opt in to duo
        if (duoRegisterUsers()) {
  
          DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);
  
        }
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
        setupOneTimeCodesOnOptin(twoFactorDaoFactory, twoFactorUser, 
            twoFactorRequestContainer, ipAddress, userAgent);
  
        //opt the user in
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinSuccessMessage"));
  
        emailUserAfterOptin(twoFactorRequestContainer, loggedInUser, subjectSource);
        
        twoFactorRequestContainer.getTwoFactorOptinContainer().setOptinWizardInProgress(true);

        return OptinWizardSubmitFobSerialView.optinPrintCodes;
      }
    });
  
    return result;
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardTotpAppInstallSelectType(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    OptinWizardTotpAppInstallSelectTypeView optinWizardTotpAppInstallSelectTypeView = optinWizardTotpAppInstallSelectTypeLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid);
  
    showJsp(optinWizardTotpAppInstallSelectTypeView.getJsp());
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @return error message if there is one and jsp
   */
  public OptinWizardTotpAppInstallSelectTypeView optinWizardTotpAppInstallSelectTypeLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardTotpAppInstallSelectTypeView.index;
    }
  
    OptinWizardTotpAppInstallSelectTypeView result =  (OptinWizardTotpAppInstallSelectTypeView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardTotpAppInstallSelectTypeView.optinWelcome;
        }
  
        return OptinWizardTotpAppInstallSelectTypeView.optinTotpAppInstallSelectType;
      }
    });
  
    return result;
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitTotpAppIntegrate(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    OptinWizardSubmitTotpAppIntegrateView optinWizardSubmitTotpAppIntegrateView = optinWizardSubmitTotpAppIntegrateLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid);
  
    showJsp(optinWizardSubmitTotpAppIntegrateView.getJsp());
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitTotpAppIntegrateView optinWizardSubmitTotpAppIntegrateLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitTotpAppIntegrateView.index;
    }
  
    OptinWizardSubmitTotpAppIntegrateView result =  (OptinWizardSubmitTotpAppIntegrateView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
        twoFactorUser.setSubjectSource(subjectSource);
  
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitTotpAppIntegrateView.optinWelcome;
        }
        
        return OptinWizardSubmitTotpAppIntegrateView.optinTotpAppConfirmCode;
      }
    });
  
    return result;
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitTotpAppCode(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    String twoFactorPass = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");
  
    OptinWizardSubmitTotpAppCodeView optinWizardSubmitTotpAppCodeView = optinWizardSubmitTotpAppCodeLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid, twoFactorPass);
  
    showJsp(optinWizardSubmitTotpAppCodeView.getJsp());
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @param twoFactorPass
   * @return error message if there is one and jsp
   */
  public OptinWizardSubmitTotpAppCodeView optinWizardSubmitTotpAppCodeLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, final String twoFactorPass) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardSubmitTotpAppCodeView.index;
    }
  
    OptinWizardSubmitTotpAppCodeView result =  (OptinWizardSubmitTotpAppCodeView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
        twoFactorUser.setSubjectSource(subjectSource);
  
        if (!checkBirthday(twoFactorRequestContainer, twoFactorUser, birthDayUuid)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("auditsWrongBirthday"));
          return OptinWizardSubmitTotpAppCodeView.optinWelcome;
        }
  
        if (StringUtils.isBlank(twoFactorPass)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCodeRequired"));
          return OptinWizardSubmitTotpAppCodeView.optinTotpAppConfirmCode;
        }

        if (!numberMatcher.matcher(twoFactorPass).matches()) {
          
          String loginId = TfSourceUtils.convertSubjectIdToNetId(subjectSource, loggedInUser, false);
          LOG.error("Error for " + loginId + " validating code not number, now: " 
              + System.currentTimeMillis() 
              + ", user-agent: " + userAgent);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get(
              "optinErrorCodeInvalid"));
          return OptinWizardSubmitTotpAppCodeView.optinTotpAppConfirmCode;
        }

        String twoFactorSecret = twoFactorUser.getTwoFactorSecretTempUnencrypted();

        //no need to validate the password, the password checker will do that
        TwoFactorPassResult twoFactorPassResult = TwoFactorOath.twoFactorCheckPassword(
            twoFactorSecret, twoFactorPass, null, null, null, 0L, null);

        if (!twoFactorPassResult.isPasswordCorrect()) {

          String loginId = TfSourceUtils.convertSubjectIdToNetId(subjectSource, loggedInUser, false);
          LOG.error("Error for " + loginId + " validating code, now: " 
              + System.currentTimeMillis() + ": " + TwoFactorServerUtils.hostname()
              + ", user-agent: " + userAgent);
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get(
              "optinErrorCodeInvalid"));
          return OptinWizardSubmitTotpAppCodeView.optinTotpAppConfirmCode;
        }
        
        //set the object
        twoFactorUser.setDatePhoneCodeSent(null);
        twoFactorUser.setPhoneCodeEncrypted(null);
        twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorSecret);
        twoFactorUser.setTwoFactorSecretTemp(null);
        twoFactorUser.setOptedIn(true);
        twoFactorUser.setTokenIndex(0L);
        twoFactorUser.setLastTotpTimestampUsed(null);
        twoFactorUser.setPhoneOptIn(false);
        twoFactorUser.setOptInOnlyIfRequired(null);
        twoFactorUser.setPhoneCodeEncrypted(null);

        if (twoFactorPassResult.getNextHotpIndex() != null) {
          twoFactorUser.setSequentialPassIndex(twoFactorPassResult.getNextHotpIndex());
        }
        if (twoFactorPassResult.getLastTotp30TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp30TimestampUsed());
        }
        if (twoFactorPassResult.getLastTotp60TimestampUsed() != null) {
          twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp60TimestampUsed());
        }
        if (twoFactorPassResult.getNextTokenIndex() != null) {
          twoFactorUser.setTokenIndex(twoFactorPassResult.getNextTokenIndex());
        }

        //move from temp to real code
        if (!StringUtils.isBlank(twoFactorUser.getTwoFactorSecretTempUnencrypted())) {
          twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorUser.getTwoFactorSecretTempUnencrypted());
          twoFactorUser.setTwoFactorSecretTemp(null);
        }

        twoFactorUser.store(twoFactorDaoFactory);

        //opt in to duo
        if (duoRegisterUsers()) {

          DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);

        }

        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.OPTIN_TWO_FACTOR, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
        setupOneTimeCodesOnOptin(twoFactorDaoFactory, twoFactorUser, 
            twoFactorRequestContainer, ipAddress, userAgent);
  
        //opt the user in
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinSuccessMessage"));
  
        emailUserAfterOptin(twoFactorRequestContainer, loggedInUser, subjectSource);
        
        twoFactorRequestContainer.getTwoFactorOptinContainer().setOptinWizardInProgress(true);

        return OptinWizardSubmitTotpAppCodeView.optinPrintCodes;
      }
    });

    return result;
  }


  /**
   * When someone opts out their colleague
   * @param httpServletRequest 
   * @param httpServletResponse 
   */
  public void generateCodeForColleague(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String userIdOperatingOn = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("userIdOperatingOn");
  
    String checkedApprovalString = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("checkedApproval");
    
    Source subjectSource = TfSourceUtils.mainSource();
  
    generateCodeForColleagueLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), userIdOperatingOn, subjectSource, checkedApprovalString);
  
    showJsp("helpColleague.jsp");
  
  }


  /**
   * When someone opts out their colleague
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   * @param userIdOperatingOn 
   * @param subjectSource
   * @param userCheckedCheckbox 
   */
  public void generateCodeForColleagueLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final String userIdOperatingOn, final Source subjectSource, final String userCheckedCheckbox) {
  
    final TwoFactorUser[] twoFactorUserUsingApp = new TwoFactorUser[1];
    
    final TwoFactorUser[] twoFactorUserGettingCode = new TwoFactorUser[1];
  
    boolean success = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        boolean innerSuccess = false;
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
        
        twoFactorUserUsingApp[0] = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
        twoFactorUserUsingApp[0].setSubjectSource(subjectSource);
        
        //if invalid uuid, something fishy is going on
        if(!alphaNumericMatcher.matcher(userIdOperatingOn).matches()) {
          throw new RuntimeException("Why is userIdOperatingOn not alphanumeric???? '" + userIdOperatingOn + "'");
        }
        
        twoFactorUserGettingCode[0] = TwoFactorUser.retrieveByUuid(twoFactorDaoFactory, userIdOperatingOn);
  
        if (twoFactorUserGettingCode[0] == null) {
          throw new RuntimeException("Why is uuid not found??? '" + userIdOperatingOn + "'");
        }
  
        twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setTwoFactorUserFriend(twoFactorUserGettingCode[0]); 
  
        twoFactorUserGettingCode[0].setSubjectSource(subjectSource);
  
        //make sure they have allowed people to opt them out
        if (!twoFactorUserGettingCode[0].isInvitedColleaguesWithinAllottedTime()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserDidntAllow"));
          return false;
        }
  
  
        //make sure they have allowed people to opt them out
        if (!StringUtils.equals("true", userCheckedCheckbox)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserDidntCheckCheckbox"));
          return false;
        }
        
        //make sure the user has identified this user to opt them out
        if (!StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingCode[0].getColleagueUserUuid0())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingCode[0].getColleagueUserUuid1())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingCode[0].getColleagueUserUuid2())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingCode[0].getColleagueUserUuid3())
            && !StringUtils.equals(twoFactorUserUsingApp[0].getUuid(), twoFactorUserGettingCode[0].getColleagueUserUuid4()) ) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendErrorUserNotFriend"));
          return false;
          
        }
        
        
        twoFactorUserGettingCode[0].setTwoFactorSecretTemp(null);
        
        if (StringUtils.isBlank(twoFactorUserGettingCode[0].getTwoFactorSecret()) || !twoFactorUserGettingCode[0].isOptedIn()) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendWarnNotOptedIn"));
          return false;
        }
          
        innerSuccess = true;
        
        //store code and when sent
        String secretCode = Integer.toString(new SecureRandom().nextInt(1000000));
        
        //make this since 9 since thats what duo is
        secretCode = StringUtils.leftPad(secretCode, 9, '0');

        //maybe going from duo
        //opt in to duo
        if (UiMain.duoRegisterUsers()) {

          secretCode = DuoCommands.duoBypassCodeBySomeId(twoFactorUserGettingCode[0].getLoginid());
          
        }

        twoFactorUserGettingCode[0].setPhoneCodeUnencrypted(secretCode);
        twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer().setCodeForFriend(secretCode);
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("helpFriendCodeSuccess"));
        
        twoFactorUserGettingCode[0].setDatePhoneCodeSent(System.currentTimeMillis());
  
        twoFactorUserGettingCode[0].setDateInvitedColleagues(null);
        
        twoFactorUserGettingCode[0].store(twoFactorDaoFactory);
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.GENERATED_CODE_FOR_A_COLLEAGUE, ipAddress, 
            userAgent, twoFactorUserUsingApp[0].getUuid(), twoFactorUserUsingApp[0].getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("helpFriendCodeAuditDescriptionPrefix") 
              + " " + twoFactorUserGettingCode[0].getName() +  " (" + twoFactorUserGettingCode[0].getLoginid() + ")", null);
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.COLLEAGUE_GENERATED_ME_A_CODE, ipAddress, 
            userAgent, twoFactorUserGettingCode[0].getUuid(), twoFactorUserUsingApp[0].getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("helpFriendCodeAuditDescriptionForFriendPrefix")
            + " " + twoFactorUserUsingApp[0].getName() +  " (" + twoFactorUserUsingApp[0].getLoginid() + ")", null);
  
        return innerSuccess;
      }
    });
  
    //send emails if successful
    String userEmailLoggedIn = null;
    String userEmailColleague = null;
    try {
            
      twoFactorUserUsingApp[0].setSubjectSource(subjectSource);
      twoFactorUserGettingCode[0].setSubjectSource(subjectSource);
      
      //see if there
      
      //if this is real mode with a source, and we have email configured, and we are sending emails for optin...
      if (success && subjectSource != null && !StringUtils.isBlank(TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.smtp.server")) 
          && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("mail.sendForOptoutFriend", true)) {
        
        Subject sourceSubjectLoggedIn = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, loggedInUser, true, false, true);
        Subject sourceSubjectColleaguePicked = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
            twoFactorUserGettingCode[0].getLoginid(), true, false, true);
        
        userEmailLoggedIn = retrieveUserEmail(sourceSubjectLoggedIn, twoFactorUserUsingApp[0]);
        
        userEmailColleague = retrieveUserEmail(sourceSubjectColleaguePicked, twoFactorUserGettingCode[0]);
        
        //set the default text container...
        String subject = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailGenerateCodeFriendSubject");
        subject = TextContainer.massageText("emailGenerateCodeFriendSubject", subject);
  
        String body = TwoFactorTextConfig.retrieveText(null).propertyValueStringRequired("emailGenerateCodeFriendBody");
        body = TextContainer.massageText("emailGenerateCodeFriendBody", body);
        
        String bccsString = TwoFactorServerConfig.retrieveConfig().propertyValueString("mail.bcc.friendOptouts");
        
        TwoFactorEmail twoFactorMail = new TwoFactorEmail();
        
        boolean sendEmail = true;
        boolean sendToFriend = true;
        //there is no email address????
        if (StringUtils.isBlank(userEmailColleague)) {
          sendToFriend = false;
          LOG.warn("Did not send email to logged in user: " + userEmailColleague + ", no email address...");
          if (StringUtils.isBlank(bccsString)) {
            sendEmail = false;
          } else {
            twoFactorMail.addTo(bccsString);
          }
        } else {
          twoFactorMail.addTo(userEmailColleague);
          twoFactorMail.addBcc(bccsString);
        }
        
        if (sendToFriend && StringUtils.isBlank(userEmailLoggedIn)) {
          LOG.warn("Did not send email to logged in user: " + loggedInUser + ", no email address...");
        } else {
          twoFactorMail.addCc(userEmailLoggedIn);
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
    
    helpColleagueLogic(twoFactorDaoFactory, 
        twoFactorRequestContainer,
        loggedInUser, subjectSource);
  
  }


  /**
   * submit which type of optin
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardAddPush(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    OptinWizardAddPushView optinWizardAddPushView = optinWizardAddPushLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
  
    showJsp(optinWizardAddPushView.getJsp());
  }


  /**
   * optin to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @return error message if there is one and jsp
   */
  public OptinWizardAddPushView optinWizardAddPushLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress,
      final String userAgent, final Source subjectSource) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);

      return OptinWizardAddPushView.index;
    }
  
    new UiMainPublic().setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, 
        twoFactorRequestContainer.getTwoFactorUserLoggedIn());
  
    OptinWizardAddPushView result =  (OptinWizardAddPushView)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        //add bday so it isnt checked again
        String birthDayUuid = TwoFactorServerUtils.uuid();
        twoFactorUser.setBirthDayUuid(birthDayUuid);
        twoFactorUser.store(twoFactorDaoFactory);
        
        return OptinWizardAddPushView.optinAppInstall;
      }
    });
  
    return result;
  }


  /**
   * optin to the service
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardDone(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
  
    OptinWizardDoneView optinWizardDoneView = optinWizardDoneLogic(TwoFactorDaoFactory.getFactory(), 
        twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    showJsp(optinWizardDoneView.getJsp());
    
  }


  /**
   * optin wizard to two factor
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource 
   * @return the view
   */
  public OptinWizardDoneView optinWizardDoneLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (twoFactorUser.isOptedIn()) {
      return OptinWizardDoneView.optinWizardDone;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
    
    profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, ipAddress, 
        userAgent, subjectSource);

    return OptinWizardDoneView.index;
    
  }


  /**
   * duo push unenroll.
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushUnenroll2(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    @SuppressWarnings("unused")
    boolean success = duoPushUnenrollLogic2(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    index(httpServletRequest, httpServletResponse);
  
  }

  
  /**
   * unenroll fro auto call text
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void defaultCallTextChange(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();

    @SuppressWarnings("unused")
    boolean success = defaultCallTextUnenrollLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    defaultCallTextEnroll(httpServletRequest, httpServletResponse);
  }

  
  /**
   * unenroll fro auto call text
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void defaultCallTextUnenroll(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    @SuppressWarnings("unused")
    boolean success = defaultCallTextUnenrollLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    index(httpServletRequest, httpServletResponse);
  
  }

  /**
   * unenroll from duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean defaultCallTextUnenrollLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("index2EnrollPhoneTextDefaultNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
    
    twoFactorUser.setPhoneAutoCalltext(null);
    twoFactorUser.setPhoneOptIn(false);

    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
  
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.AUTO_CALL_TEXT_UNENROLL, ipAddress, 
        userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
    //opt in to duo
    if (duoRegisterUsers()) {

      DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);

    }

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("index2EnrollPhoneTextUnenrollSuccess"));
  
    return true;
  }



  /**
   * unenroll from duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean duoPushUnenrollLogic2(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
  
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }
  
    JSONObject duoUser = twoFactorRequestContainer.getTwoFactorDuoPushContainer().getDuoUser();
    
    String phoneId = DuoCommands.duoPushPhoneId(duoUser);
    
    twoFactorUser.setDuoPushPhoneId(null);
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
  
    DuoCommands.deleteDuoPhone(phoneId);
  
    //clear this out so we get a clear shake
    twoFactorRequestContainer.setTwoFactorDuoPushContainer(null);
    
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.DUO_DISABLE_PUSH, ipAddress, 
        userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
    //init again since not enrolled
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
  
    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushUnenrollSuccess"));
  
    return true;
  }


  /**
   * submit after install app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSetupAppDone2(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    boolean success = optinWizardSetupAppDoneLogic2(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    if (success) {
      showJsp("optinAppIntegrate2.jsp");
    } else {
      index(httpServletRequest, httpServletResponse);
    }
  }


  /**
     * optin to two factor
     * @param twoFactorDaoFactory
     * @param twoFactorRequestContainer 
     * @param ipAddress 
     * @param userAgent 
     * @param loggedInUser
     * @param subjectSource
     * @return error message if there is one and jsp
     */
    public boolean optinWizardSetupAppDoneLogic2(final TwoFactorDaoFactory twoFactorDaoFactory, 
        final TwoFactorRequestContainer twoFactorRequestContainer,
        final String loggedInUser, final String ipAddress, 
        final String userAgent, final Source subjectSource) {
  
      boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
      if (userOk) {
        userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
      }
  
      if (!userOk) {
        return false;
      }
  
      boolean result =  (Boolean)HibernateSession.callbackHibernateSession(
          TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
          TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
          twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
          TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
          
          twoFactorUser.setSubjectSource(subjectSource);
          
          if (!twoFactorUser.isOptedIn()) {
            twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("index2ErrorNotOptedIn"));
            return false;
          }
          
          boolean success = duoPushEnrollLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
              loggedInUser, ipAddress, 
              userAgent, false, subjectSource);
          if (success) {
  
            return true;
  
          }
  
          return false;
          
  //        boolean success = duoPushLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
  //            loggedInUser, ipAddress, 
  //            userAgent, false);
        }
      });
  
      return result;
    }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitAppIntegrate2(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    boolean success = optinWizardSubmitAppIntegrateLogic2(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);

    if (success) {
      showJsp("optinAppTest2.jsp");
    } else {
      index(httpServletRequest, httpServletResponse);
    }
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @return true if success
   */
  public boolean optinWizardSubmitAppIntegrateLogic2(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      return false;
    }
  
    boolean result = (Boolean)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
        twoFactorUser.setSubjectSource(subjectSource);
  
        twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
        
        if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()
            || StringUtils.isBlank(twoFactorUser.getDuoPushPhoneId())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
          return false;
        }
  
        String txId = DuoCommands.duoInitiatePushByPhoneId(twoFactorUser.getDuoUserId(), 
            twoFactorUser.getDuoPushPhoneId(), null, null);
  
        if (StringUtils.isBlank(txId)) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorWithPush"));
          return false;
        }
        
        String duoTxId = System.currentTimeMillis() + "__uiSoNoBrowserId__" + txId;
        twoFactorUser.setDuoPushTransactionId(duoTxId);
  
        twoFactorUser.store(twoFactorDaoFactory);
  
        return true;
      }
    });
  
    return result;
  }

  /**
   * submit after test app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void optinWizardSubmitAppTest2(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    optinWizardSubmitAppTestLogic2(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource);
    
    index(httpServletRequest, httpServletResponse);
    
  }

  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   */
  public void optinWizardSubmitAppTestLogic2(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      return;
    }
  
    HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        String timestampBrowserTxId = twoFactorUser.getDuoPushTransactionId();
        String[] pieces = TwoFactorServerUtils.splitTrim(timestampBrowserTxId, "__");
  
        if (TwoFactorServerUtils.length(pieces) != 3 || !StringUtils.equals("uiSoNoBrowserId", pieces[1])) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinAppTestErrorText"));
          return null;
          
        }
        
        //if not already used
        String timestampString = pieces[0];
  
        long timestampLong = TwoFactorServerUtils.longValue(timestampString);
        
        //give them 10 minutes
        if (((System.currentTimeMillis() - timestampLong) / 1000) > (60 * 10)) { 
  
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinAppTestErrorTimeoutText"));
          return OptinWizardSubmitAppTestView.optinWelcome;
  
        }
  
        String txId = pieces[2];
  
        //see if valid
        boolean validPush = false;
        
        try {
          validPush = DuoCommands.duoPushOrPhoneSuccess(txId, 5);
        } catch (RuntimeException re) {
          //if its a timeout, then validPush is false, else rethrow
          if (ExceptionUtils.getFullStackTrace(re).toLowerCase().contains("timeout")) {
            validPush = false;
          } else {
            throw re;
          }
        }
  
        if (validPush) {
          TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
              TwoFactorAuditAction.DUO_ENABLE_PUSH, ipAddress, 
              userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
          TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
              TwoFactorAuditAction.DUO_ENABLE_PUSH_FOR_WEB, ipAddress, 
              userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
          twoFactorUser.setDuoPushByDefault(true);
          twoFactorUser.setPhoneOptIn(false);
          twoFactorUser.setDuoPushTransactionId(null);
          twoFactorUser.store(twoFactorDaoFactory);
  
          //opt in to duo
          if (duoRegisterUsers()) {
  
            DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);
  
          }
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushEnrolledInPushForWeb"));
          
          return null;
        }
  
        //problem, try again?
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinAppTestErrorText"));
        return null;
        
      }
    });
  
  }


  /**
   * duo push test
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushTestOnly(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    duoPushTestOnlyLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
//    if (success) {
//  
//      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushOnlyTestSuccess"));
//
//    } else {
//    
//      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushOnlyTestFailure"));
//
//    }
    index(httpServletRequest, httpServletResponse);
  }


  /**
   * duo push test
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean duoPushTestOnlyLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
  
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()
        || StringUtils.isBlank(twoFactorUser.getDuoPushPhoneId())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }
  
    String txId = DuoCommands.duoInitiatePushByPhoneId(twoFactorUser.getDuoUserId(), 
        twoFactorUser.getDuoPushPhoneId(), null, null);
    
    if (StringUtils.isBlank(txId)) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotInPush"));
      return false;
    }
  
    for (int i=0;i<20;i++) {
    
      TwoFactorServerUtils.sleep(1000);
  
      if (DuoCommands.duoPushOrPhoneSuccess(txId, null)) {
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushTestSuccess"));
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.DUO_ENABLE_PUSH_TEST_SUCCESS, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return true;
      }
      
    }
    
    TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
        TwoFactorAuditAction.DUO_ENABLE_PUSH_TEST_FAILURE, ipAddress, 
        userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
    
    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoPushTestFailure"));
    return false;
  }


  /**
   * duo push unenroll.
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushUnenroll(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    boolean success = duoPushUnenrollLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    if (success) {
  
      showJsp("duoPush.jsp");
  
    } else {
    
      index(httpServletRequest, httpServletResponse);
    }
  
  }


  /**
   * unenroll from duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean duoPushChangePhoneLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    boolean success = duoPushUnenrollLogic2(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, ipAddress, userAgent);

    Source subjectSource = TfSourceUtils.mainSource();

    if (success) {
      success = duoPushEnroll2Logic(
          TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
          ipAddress, 
          userAgent, subjectSource);

    }
    
    return success;
  }


  /**
   * unenroll in push for web
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushUnenrollWeb2(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    duoPushUnenrollWebLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    index(httpServletRequest, httpServletResponse);
  }


  /**
   * enroll in push for web
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void duoPushEnrollWeb2(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    duoPushEnrollWebLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    index(httpServletRequest, httpServletResponse);
  }


  /**
   * enroll in auto text call from weblogin
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void defaultCallTextEnroll(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    boolean success = defaultCallTextEnrollLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    if (success) {
  
      showJsp("callTextEnroll.jsp");
  
    } else {
    
      index(httpServletRequest, httpServletResponse);
    }
  
  }


  /**
   * unenroll from duo push
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean defaultCallTextEnrollLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorRequestContainer.getTwoFactorDuoPushContainer().isDuoEnabled()) {
      return false;
    }
    
    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);

    if (twoFactorRequestContainer.getTwoFactorDuoPushContainer().isEnrolledInDuoPush()) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("callTextErrorInPush"));
      return false;
    }
    
    new UiMainPublic().setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorRequestContainer.getTwoFactorUserLoggedIn());

//    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
//      
//      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
//        
//        twoFactorUser.set
//        twoFactorUser.store(twoFactorDaoFactory);
//        
//        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
//            TwoFactorAuditAction.AUTO_CALL_TEXT_UNENROLL, ipAddress, 
//            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
//        
//        return null;
//      }
//    });

    return true;
  }


  /**
   * set or change phone voice text from weblogin
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void defaultCallTextEnrollSubmit(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    int phoneIndex = Integer.parseInt(httpServletRequest.getParameter("phoneIndex"));
    String phoneType = httpServletRequest.getParameter("phoneType");
  
    defaultCallTextEnrollSubmitLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid, phoneIndex, phoneType);
  
    showJsp("callTextConfirmPhoneCode.jsp");
  }


  /**
   * set or change phone voice text from weblogin
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @param phoneIndex index of phone (0, 1, 2)
   * @param phoneType voice or text
   * @return if should proceed
   */
  public boolean defaultCallTextEnrollSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, final int phoneIndex,
      final String phoneType) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);
  
      return false;
    }
  
    return (Boolean)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUser.setSubjectSource(subjectSource);
        
        //this is 0t, 1v, etc
        twoFactorUser.setPhoneAutoCalltext(Integer.toString(phoneIndex) + phoneType.charAt(0));
        twoFactorUser.setPhoneOptIn(false);
        twoFactorUser.store(twoFactorDaoFactory);
        
        new UiMainPublic().sendPhoneCode(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, 
            loggedInUser, ipAddress, 
            userAgent, phoneIndex, phoneType, true, false, null);        

        return true;
      }
    });
  
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void defaultCallTextSubmitPhoneCode(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String birthDayUuid = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("birthdayTextfield");
  
    String twoFactorPass = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");
  
    defaultCallTextSubmitPhoneCodeLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        birthDayUuid, twoFactorPass);
  
    index(httpServletRequest, httpServletResponse);
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param birthDayUuid 
   * @param twoFactorPass
   * @return error message if there is one and jsp
   */
  public boolean defaultCallTextSubmitPhoneCodeLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String birthDayUuid, final String twoFactorPass) {
  
    boolean userOk = !userCantLoginNotActiveLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, subjectSource);
  
    if (userOk) {
      userOk = !hasTooManyUsersLockoutLogic(subjectSource, TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    }
  
    if (!userOk) {
      
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
      twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
      
      profileLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
          loggedInUser, ipAddress, 
          userAgent, subjectSource);
  
      return false;
    }
  
    boolean result = (Boolean)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
        twoFactorUser.setSubjectSource(subjectSource);
  
        if (!StringUtils.equals(twoFactorPass, twoFactorUser.getPhoneCodeUnencryptedIfNotExpired())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("phoneCodeDoesntMatchError"));
          return false;
        }
  
        twoFactorUser.setPhoneOptIn(true);
        
        twoFactorUser.store(twoFactorDaoFactory);
  
        //opt in to duo
        if (duoRegisterUsers()) {
  
          DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);
  
        }
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.AUTO_CALL_TEXT_ENROLL, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
        //opt the user in
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("callTextConfirmSuccessText"));
  
        return true;
      }
    });
  
    return result;
  }


  /**
   * show one time codes
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void showOneTimeCodes2(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    boolean success = showOneTimeCodes2Logic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    if (success) {
  
      showJsp("showOneTimeCodes2.jsp");
  
    } else {
      index(httpServletRequest, httpServletResponse);
    }
  }


  /**
   * show one time codes
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean showOneTimeCodes2Logic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    boolean result = (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
    
        //generate the codes
        TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("codesErrorAlreadyOptedIn"));
          return false;
        }
        
        setupOneTimeCodesOnOptin(twoFactorDaoFactory, twoFactorUser, twoFactorRequestContainer, ipAddress, userAgent);
        
        return true;
      }
    });
    
    return result;
  }


  /**
   * add a fob
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void keychainFobAdd(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
  
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    keychainFobAddLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer,
        loggedInUser, httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
  
    showJsp("fobRegister.jsp");
  }


  /**
   * enroll in push for web
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @return true if ok, false if not
   */
  public boolean keychainFobAddLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {
    
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
  
    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return false;
    }
    
    twoFactorRequestContainer.getTwoFactorDuoPushContainer().init(twoFactorUser);
  
    return true;
  }


  /**
   * submit after integrate app
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void keychainFobAddSubmit(HttpServletRequest httpServletRequest, 
      HttpServletResponse httpServletResponse) {
    
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    Source subjectSource = TfSourceUtils.mainSource();
    
    String serialNumber = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("serialNumber");
  
    String twoFactorCode = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode");
    
    String twoFactorCode2 = TwoFactorFilterJ2ee.retrieveHttpServletRequest().getParameter("twoFactorCode2");
    
    boolean backToIndex = keychainFobAddSubmitLogic(
        TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser, 
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), subjectSource,
        serialNumber, twoFactorCode, twoFactorCode2);
    
    if (backToIndex) {
      index(httpServletRequest, httpServletResponse);
    } else {
      showJsp("fobRegister.jsp");
    }
  }


  /**
   * optin to two factor submit app integrate
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param subjectSource
   * @param serialNumber
   * @param twoFactorCodeString
   * @param twoFactorCode2String
   * @return true to go back to index. or false to stay on this page
   */
  public boolean keychainFobAddSubmitLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final Source subjectSource, final String serialNumber, final String twoFactorCodeString,
      final String twoFactorCode2String) {
  
    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    if (!twoFactorUser.isOptedIn() || StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted())) {
      twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("duoErrorNotOptedIn"));
      return true;
    }
  
    boolean result =  (Boolean)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
  
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
    
        if (StringUtils.isBlank(serialNumber)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialRequired"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
          
        }
  
        //lets make sure the serial number exists
        TwoFactorDeviceSerial twoFactorDeviceSerial = TwoFactorDeviceSerial.retrieveBySerial(twoFactorDaoFactory, serialNumber);
       
        if (twoFactorDeviceSerial == null) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialNotFound"));
          return false;
        }
  
        //lets see if this device has been registered to someone else
        if (!StringUtils.isBlank(twoFactorDeviceSerial.getUserUuid()) 
            && !StringUtils.equals(twoFactorDeviceSerial.getUserUuid(), twoFactorUser.getUuid())) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorSerialRegisteredToSomeoneElse"));
          return false;
        }
  
        String twoFactorSecret = twoFactorDeviceSerial.getTwoFactorSecretUnencrypted();
  
        Base32 codec = new Base32();
        byte[] twoFactorSecretPlainText = codec.decode(twoFactorSecret);
  
        if (StringUtils.isBlank(twoFactorCodeString)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode1Required"));
          return false;
          
        }
  
        if (StringUtils.isBlank(twoFactorCode2String)) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode2Required"));
          return OptinWizardSubmitFobSerialView.optinFobRegister;
          
        }
  
        int twoFactorCode = -1;
  
        try {
          twoFactorCode = TwoFactorServerUtils.intObjectValue(twoFactorCodeString, true);
        } catch (Exception e) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode1Invalid"));
          return false;
          
        }
  
        int twoFactorCode2 = -1;
  
        try {
          twoFactorCode2 = TwoFactorServerUtils.intObjectValue(twoFactorCode2String, true);
        } catch (Exception e) {
          
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCode2Invalid"));
          return false;
          
        }
  
        int nextIndex = -1;
        boolean foundTokenIndex = false;
        for (int i=0;i<20000;i++) {
          if (twoFactorCode == new TwoFactorLogic().hotpPassword(twoFactorSecretPlainText, i)
              && twoFactorCode2 == new TwoFactorLogic().hotpPassword(twoFactorSecretPlainText, i+1)) {
            nextIndex = i+2;
            foundTokenIndex = true;
            break;
          }
        }
        
        if (!foundTokenIndex) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("optinErrorCodesInvalid"));
          return false;
        }
  
        twoFactorUser.setTokenIndex((long)nextIndex);
  
        twoFactorUser.setOptInOnlyIfRequired(null);
        twoFactorUser.setPhoneCodeEncrypted(null);
        twoFactorUser.setDatePhoneCodeSent(null);
  
        //move from temp to real code
        twoFactorUser.setTwoFactorSecretUnencrypted(twoFactorSecret);
        twoFactorUser.setTwoFactorSecretTemp(null);
  
        twoFactorUser.store(twoFactorDaoFactory);
  
        twoFactorDeviceSerial.setUserUuid(twoFactorUser.getUuid());
        twoFactorDeviceSerial.setWhenRegistered(System.currentTimeMillis());
        twoFactorDeviceSerial.store(twoFactorDaoFactory);

        twoFactorRequestContainer.getTwoFactorAdminContainer().setImportSerial(serialNumber);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.REGISTER_FOB_SERIAL_NUMBER, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("optionStep2auditRegisterFob"),
            null);

        //opt in to duo
        if (duoRegisterUsers()) {
  
          DuoCommands.migrateUserAndPhonesAndTokensBySomeId(loggedInUser, false, false);
  
        }
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.FOB_ADD, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
  
        setupOneTimeCodesOnOptin(twoFactorDaoFactory, twoFactorUser, 
            twoFactorRequestContainer, ipAddress, userAgent);
  
        //opt the user in
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("addFobSuccessMessage"));
  
        return true;
      }
    });
  
    return result;
  }


  /**
   * remove a friend
   * @param httpServletRequest
   * @param httServletResponse
   */
  public void removeFriend(HttpServletRequest httpServletRequest, HttpServletResponse httServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    // one of these needs to be there
    String fromUserUuid = httpServletRequest.getParameter("fromUserUuid");
    String toUserUuid = httpServletRequest.getParameter("toUserUuid");
    
    Source subjectSource = TfSourceUtils.mainSource();
    TwoFactorDaoFactory twoFactorDaoFactory = TwoFactorDaoFactory.getFactory();
    String ipAddress = httpServletRequest.getRemoteAddr();
    String userAgent = httpServletRequest.getHeader("User-Agent");
    removeFriendLogic(twoFactorDaoFactory, twoFactorRequestContainer, loggedInUser, 
        ipAddress, 
        userAgent, fromUserUuid, toUserUuid, subjectSource);
  
    index(httpServletRequest, httServletResponse);
  }


  /**
   * remove a friend
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer 
   * @param ipAddress 
   * @param userAgent 
   * @param loggedInUser
   * @param fromUserUuid
   * @param toUserUuid
   * @param subjectSource
   */
  public void removeFriendLogic(final TwoFactorDaoFactory twoFactorDaoFactory, final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, String fromUserUuid, String toUserUuid, final Source subjectSource) {
  
    final String[] fromUserUuidArray = new String[]{fromUserUuid};
    final String[] toUserUuidArray = new String[]{toUserUuid};
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        TwoFactorUser twoFactorUserUsingApp = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
        
        twoFactorUserUsingApp.setSubjectSource(subjectSource);
        
        if (!StringUtils.isBlank(fromUserUuidArray[0]) && !StringUtils.isBlank(toUserUuidArray[0])) {
          throw new RuntimeException("Cant pass in from user uuid and to user uuid!");
        }
        
        boolean removeMyFriend = StringUtils.isBlank(fromUserUuidArray[0]);
        // removeForFriendRequester or removeForFriendRequestee
        String reason = removeMyFriend ? "removeMyFriend" : "removeMeFromSomeonesList";
      
        if (removeMyFriend) {
          fromUserUuidArray[0] = twoFactorUserUsingApp.getUuid();
        } else {
          toUserUuidArray[0] = twoFactorUserUsingApp.getUuid();
        }

        
        TwoFactorUser twoFactorUserLosingFriend = StringUtils.equals(twoFactorUserUsingApp.getUuid(), fromUserUuidArray[0]) ? 
            twoFactorUserUsingApp : TwoFactorUser.retrieveByUuid(twoFactorDaoFactory, fromUserUuidArray[0]);
        twoFactorUserLosingFriend.setSubjectSource(subjectSource);
        TwoFactorUser twoFactorUserLostFriend = StringUtils.equals(twoFactorUserUsingApp.getUuid(), toUserUuidArray[0]) ? 
            twoFactorUserUsingApp : TwoFactorUser.retrieveByUuid(twoFactorDaoFactory, toUserUuidArray[0]);
        twoFactorUserLostFriend.setSubjectSource(subjectSource);
  
        boolean foundFriend = false;
        
        if (StringUtils.equals(twoFactorUserLosingFriend.getColleagueUserUuid0(), toUserUuidArray[0])) {
          foundFriend = true;
          twoFactorUserLosingFriend.setColleagueUserUuid0(null);
        }
        
        if (StringUtils.equals(twoFactorUserLosingFriend.getColleagueUserUuid1(), toUserUuidArray[0])) {
          foundFriend = true;
          twoFactorUserLosingFriend.setColleagueUserUuid1(null);
        }
        
        if (StringUtils.equals(twoFactorUserLosingFriend.getColleagueUserUuid2(), toUserUuidArray[0])) {
          foundFriend = true;
          twoFactorUserLosingFriend.setColleagueUserUuid2(null);
        }
        
        if (StringUtils.equals(twoFactorUserLosingFriend.getColleagueUserUuid3(), toUserUuidArray[0])) {
          foundFriend = true;
          twoFactorUserLosingFriend.setColleagueUserUuid3(null);
        }
        
        if (StringUtils.equals(twoFactorUserLosingFriend.getColleagueUserUuid4(), toUserUuidArray[0])) {
          foundFriend = true;
          twoFactorUserLosingFriend.setColleagueUserUuid4(null);
        }
        
        if (!foundFriend) {
          twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminColleaguesRemoveFriendNotFound"));
          return null;
        }
  
        twoFactorUserLosingFriend.store(twoFactorDaoFactory);
  
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.REMOVE_FRIEND, ipAddress, 
            userAgent, twoFactorUserLostFriend.getUuid(),
            twoFactorUserUsingApp.getUuid(), "Removed friend: " + twoFactorUserLostFriend.getNetId()
            + ": " + twoFactorUserLostFriend.getName() + ", from: " + twoFactorUserLosingFriend.getNetId()
            + ": " + twoFactorUserLosingFriend.getName() + ", reason: " + reason, null);
  
        if (!removeMyFriend) {
          TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
              TwoFactorAuditAction.REMOVE_FRIEND, ipAddress, 
              userAgent, twoFactorUserLosingFriend.getUuid(),
              twoFactorUserUsingApp.getUuid(), "Removed friend: " + twoFactorUserLostFriend.getNetId()
              + ": " + twoFactorUserLostFriend.getName() + ", from: " + twoFactorUserLosingFriend.getNetId()
              + ": " + twoFactorUserLosingFriend.getName() + ", reason: " + reason, null);
        }
        
        twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("adminColleaguesRemoveFriendSuccess"));
  
        return null;
      }
    });
    
          
  }


}
  