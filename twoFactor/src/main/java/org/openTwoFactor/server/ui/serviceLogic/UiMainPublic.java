/**
 * @author mchyzer
 * $Id: UiMainPublic.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.duo.DuoCommands;
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
import org.openTwoFactor.server.ui.beans.TwoFactorHelpLoggingInContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorPhoneForScreen;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.rest.TfRestLogic;

import edu.internet2.middleware.grouperClient.api.GcDeleteMember;
import edu.internet2.middleware.grouperClient.ws.beans.WsSubjectLookup;
import edu.internet2.middleware.subject.Subject;



/**
 * methods that are used from the UI which arent in two factor, just need one factor
 */
public class UiMainPublic extends UiServiceLogicBase {

  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(UiMainPublic.class);

  
  
  /**
   * if a user wants their colleagues to opt them out
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void stopOptInRequirement(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);

    stopOptInRequirementLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser,
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
    
    indexLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    
    showJsp("nonTwoFactorIndex.jsp");

  }

  /**
   * 
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   */
  public void stopOptInRequirementLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
        
        //make call to grouper to get user out of group
        GcDeleteMember gcDeleteMember = new GcDeleteMember();
        
        //  # dont require two factor source id for grouper
        //  twoFactorServer.dontRequireTwoFactorGrouperSourceId = 
        //
        //  # dont require two factor source id for grouper if use subject id
        //  twoFactorServer.dontRequireTwoFactorGrouperUseSubjectId = true
        //
        //  # dont require two factor source id for grouper if use subject identifier
        //  twoFactorServer.dontRequireTwoFactorGrouperUseSubjectIdentifier = false
        //
        //  # dont require two factor source id for grouper group name
        //  twoFactorServer.dontRequireTwoFactorGrouperNameOfGroup = 

        String sourceId = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.dontRequireTwoFactorGrouperSourceId");
        boolean useSubjectId = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.dontRequireTwoFactorGrouperUseSubjectId", true);
        boolean useSubjectIdentifier = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.dontRequireTwoFactorGrouperUseSubjectIdentifier", false);
        String nameOfGroup = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.dontRequireTwoFactorGrouperNameOfGroup");
        
        WsSubjectLookup wsSubjectLookup = new WsSubjectLookup();
        
        if (!TwoFactorServerUtils.isBlank(sourceId)) {
          wsSubjectLookup.setSubjectSourceId(sourceId);
        }

        if (useSubjectId) {
          wsSubjectLookup.setSubjectId(twoFactorUser.getLoginid());
        }
        if (useSubjectIdentifier) {
          wsSubjectLookup.setSubjectIdentifier(twoFactorUser.getLoginid());
        }
        
        gcDeleteMember.addSubjectLookup(wsSubjectLookup);

        gcDeleteMember.assignGroupName(nameOfGroup);
        
        //get the user out of that group
        gcDeleteMember.execute();
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.STOP_OPT_IN_REQUIREMENT, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), 
            null, null);
        
        return null;
      }
    });

    setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorUser);

    
  }

  
  /**
   * if a user wants their colleagues to opt them out
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void phoneCode(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);

    int phoneIndex = Integer.parseInt(httpServletRequest.getParameter("phoneIndex"));
    String phoneType = httpServletRequest.getParameter("phoneType");

    phoneCodeLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser,
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"), phoneIndex, phoneType);
    
//    try {
//      httpServletResponse.sendRedirect("../../twoFactorUi/app/UiMain.index");
//    } catch (IOException ioe) {
//      throw new RuntimeException(ioe);
//    }
    showJsp("nonTwoFactorPhoneCode.jsp");

  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(new Date(1365424973503L));
  }
  
  /**
   * 
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   * @param phoneIndex 0, 1, 2 the phone index to call
   * @param phoneType voice or text
   */
  public void phoneCodeLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent, final int phoneIndex, final String phoneType) {

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    sendPhoneCode(twoFactorDaoFactory,
        twoFactorRequestContainer, loggedInUser, ipAddress, userAgent, phoneIndex, phoneType, true);

    setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorUser);

    
  }

  /**
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress
   * @param userAgent
   * @param phoneIndex
   * @param phoneType
   * @param useDuoForPasscode 
   */
  public void sendPhoneCode(final TwoFactorDaoFactory twoFactorDaoFactory,
      final TwoFactorRequestContainer twoFactorRequestContainer, final String loggedInUser,
      final String ipAddress, final String userAgent, final int phoneIndex, 
      final String phoneType, final boolean useDuoForPasscode) {

    twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);

        boolean isText = StringUtils.equals(phoneType, "text");
        boolean isVoice = StringUtils.equals(phoneType, "voice");
        
        if (!isText && !isVoice) {
          //this should never happen
          throw new RuntimeException("Invalid phone type, should be 'text' or 'voice' '" + phoneType + "'");
        }

        String phoneNumber = null;
        switch(phoneIndex) {
          
          case 0:
            phoneNumber = twoFactorUser.getPhone0();
            if (isText && (twoFactorUser.getPhoneIsText0() == null || !twoFactorUser.getPhoneIsText0())) {
              //this should never happen
              throw new RuntimeException("Not text enabled!");
            }
            if (isVoice && (twoFactorUser.getPhoneIsVoice0() == null || !twoFactorUser.getPhoneIsVoice0())) {
              //this should never happen
              throw new RuntimeException("Not voice enabled!");
            }
            break;
          case 1:
            phoneNumber = twoFactorUser.getPhone1();
            if (isText && (twoFactorUser.getPhoneIsText1() == null || !twoFactorUser.getPhoneIsText1())) {
              //this should never happen
              throw new RuntimeException("Not text enabled!");
            }
            if (isVoice && (twoFactorUser.getPhoneIsVoice1() == null || !twoFactorUser.getPhoneIsVoice1())) {
              //this should never happen
              throw new RuntimeException("Not voice enabled!");
            }
            break;
          case 2:
            phoneNumber = twoFactorUser.getPhone2();
            if (isText && (twoFactorUser.getPhoneIsText2() == null || !twoFactorUser.getPhoneIsText2())) {
              //this should never happen
              throw new RuntimeException("Not text enabled!");
            }
            if (isVoice && (twoFactorUser.getPhoneIsVoice2() == null || !twoFactorUser.getPhoneIsVoice2())) {
              //this should never happen
              throw new RuntimeException("Not voice enabled!");
            }
            break;
          
          default:
            //this should never happen
            throw new RuntimeException("Invalid phone index, must be 0,1,2: " + phoneIndex);
          
        }
        
        //store code and when sent
        String secretCode = Integer.toString(new SecureRandom().nextInt(1000000));
        
        //make this since 9 since thats what duo is
        secretCode = StringUtils.leftPad(secretCode, 9, '0');

        //maybe going from duo
        //opt in to duo
        if (useDuoForPasscode && UiMain.duoRegisterUsers()) {

          secretCode = DuoCommands.duoBypassCodeBySomeId(twoFactorUser.getLoginid());
          
        }
        
        twoFactorUser.setPhoneCodeUnencrypted(secretCode);
        twoFactorUser.setDatePhoneCodeSent(System.currentTimeMillis());

        //send code to phone
        if (isText) {
          String message = TextContainer.retrieveFromRequest().getText().get("havingTroubleTextPhonePrefix") + " " + secretCode;
          TwoFactorServerConfig.retrieveConfig().twoFactorContact().text(phoneNumber, message);
          
        } else if (isVoice) {

          char[] secretCodeCharArray = secretCode.toCharArray();
          StringBuilder secretCodeCommaSeparated = new StringBuilder();
          for (int i=0;i<secretCodeCharArray.length;i++) {
            secretCodeCommaSeparated.append(secretCodeCharArray[i]);
            if (i < secretCodeCharArray.length-1) {
              secretCodeCommaSeparated.append(",");
            } else {
              //note if it is a period, they actually say "period"... weird
              secretCodeCommaSeparated.append(",");
            }
          }
            
          String message = 
              TextContainer.retrieveFromRequest().getText().get("havingTroubleVoicePhonePrefix") + " " + secretCodeCommaSeparated.toString() 
              + ",  " + TextContainer.retrieveFromRequest().getText().get("havingTroubleVoiceInfix") + " " 
              + TextContainer.retrieveFromRequest().getText().get("havingTroubleVoicePhonePrefix") + " " + secretCodeCommaSeparated.toString() 
              + ",  " + TextContainer.retrieveFromRequest().getText().get("havingTroubleVoiceInfix") + " " 
              + TextContainer.retrieveFromRequest().getText().get("havingTroubleVoicePhonePrefix") + " " + secretCodeCommaSeparated.toString() 
              + ",  " + TextContainer.retrieveFromRequest().getText().get("havingTroubleVoiceInfix") + " " 
              + TextContainer.retrieveFromRequest().getText().get("havingTroubleVoicePhonePrefix") + " " + secretCodeCommaSeparated.toString() + "  ";

          TwoFactorServerConfig.retrieveConfig().twoFactorContact().voice(phoneNumber, message);
          
        } else {
          //this should never happen
          throw new RuntimeException("Not text or voice???");
        }

        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.SEND_CODE_TO_PHONE, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), 
            TextContainer.retrieveFromRequest().getText().get("havingTroubleAuditPrefix") + " "
            + secretCode.charAt(0) + "#####", null);
        
        return null;
      }
    });

  }

  
  /**
   * if a user wants their colleagues to opt them out
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void allowColleaguesToOptYouOut(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
    
    allowColleaguesToOptYouOutLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser,
        httpServletRequest.getRemoteAddr(), 
        httpServletRequest.getHeader("User-Agent"));
    
    showJsp("nonTwoFactorIndex.jsp");

  }

  /**
   * 
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   * @param ipAddress 
   * @param userAgent 
   */
  public void allowColleaguesToOptYouOutLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser, final String ipAddress, 
      final String userAgent) {

    final TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, 
        TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {

        twoFactorRequestContainer.init(twoFactorDaoFactory, loggedInUser);
      
        twoFactorUser.setDateInvitedColleagues(System.currentTimeMillis());
        
        twoFactorUser.store(twoFactorDaoFactory);
        
        TwoFactorAudit.createAndStore(twoFactorDaoFactory, 
            TwoFactorAuditAction.INVITE_COLLEAGUES, ipAddress, 
            userAgent, twoFactorUser.getUuid(), twoFactorUser.getUuid(), null, null);
        
        return null;
      }
    });

    twoFactorRequestContainer.setError(TextContainer.retrieveFromRequest().getText().get("havingTroubleAuthSuccess"));
    
    setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorUser);

    
  }

  
  
  /**
   * main page
   * @param httpServletRequest
   * @param httpServletResponse
   */
  public void index(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
    
    TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
    String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest();

    twoFactorRequestContainer.init(TwoFactorDaoFactory.getFactory(), loggedInUser);
    
    indexLogic(TwoFactorDaoFactory.getFactory(), twoFactorRequestContainer, loggedInUser);
    
    showJsp("nonTwoFactorIndex.jsp");
    
  }

  /**
   * 
   * @param twoFactorDaoFactory 
   * @param twoFactorRequestContainer 
   * @param twoFactorUser
   */
  public void setupNonFactorIndex(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorRequestContainer twoFactorRequestContainer, 
      TwoFactorUser twoFactorUser) {
    
    TwoFactorHelpLoggingInContainer twoFactorHelpLoggingInContainer = twoFactorRequestContainer.getTwoFactorHelpLoggingInContainer();
    
    //if configured to do this
    if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean(
        "twoFactorServer.ws.restrictUsersRequiredToBeOptedInWhoArentOptedIn", false)) {
      
      //lets clear this cache
      TfRestLogic.refreshUsersNotOptedInButRequiredIfNeeded(twoFactorDaoFactory, true, false);
      
      //is the user in the list?
      if (TfRestLogic.usersNotOptedInButRequired().get(twoFactorUser.getLoginid()) != null) {
        twoFactorHelpLoggingInContainer.setUserRequiredToOptInButIsNot(true);
      }
      
    }
    
    {
      
      List<TwoFactorPhoneForScreen> phoneNumbersForScreen = new ArrayList<TwoFactorPhoneForScreen>();
      twoFactorHelpLoggingInContainer.setPhonesForScreen(phoneNumbersForScreen);
      
      {
        TwoFactorPhoneForScreen twoFactorPhoneForScreen = new TwoFactorPhoneForScreen();
        twoFactorPhoneForScreen.setPhoneForScreen(twoFactorUser.getPhone0());
        twoFactorPhoneForScreen.setText(twoFactorUser.getPhoneIsText0() != null && twoFactorUser.getPhoneIsText0());
        twoFactorPhoneForScreen.setVoice(twoFactorUser.getPhoneIsVoice0() != null && twoFactorUser.getPhoneIsVoice0());
        phoneNumbersForScreen.add(twoFactorPhoneForScreen);
      }      
      {
        TwoFactorPhoneForScreen twoFactorPhoneForScreen = new TwoFactorPhoneForScreen();
        twoFactorPhoneForScreen.setPhoneForScreen(twoFactorUser.getPhone1());
        twoFactorPhoneForScreen.setText(twoFactorUser.getPhoneIsText1() != null && twoFactorUser.getPhoneIsText1());
        twoFactorPhoneForScreen.setVoice(twoFactorUser.getPhoneIsVoice1() != null && twoFactorUser.getPhoneIsVoice1());
        phoneNumbersForScreen.add(twoFactorPhoneForScreen);
      }      
      {
        TwoFactorPhoneForScreen twoFactorPhoneForScreen = new TwoFactorPhoneForScreen();
        twoFactorPhoneForScreen.setPhoneForScreen(twoFactorUser.getPhone2());
        twoFactorPhoneForScreen.setText(twoFactorUser.getPhoneIsText2() != null && twoFactorUser.getPhoneIsText2());
        twoFactorPhoneForScreen.setVoice(twoFactorUser.getPhoneIsVoice2() != null && twoFactorUser.getPhoneIsVoice2());
        phoneNumbersForScreen.add(twoFactorPhoneForScreen);
      }      
      boolean hasPhones = false;
      for (TwoFactorPhoneForScreen twoFactorPhoneForScreen : phoneNumbersForScreen) {
        
        if (!StringUtils.isBlank(twoFactorPhoneForScreen.getPhoneForScreen())) {
          hasPhones = true;
          
          twoFactorPhoneForScreen.setHasPhone(true);

          //make the number for screen: 21*-*** **23
          StringBuilder phoneForScreen = new StringBuilder();
          int firstDigitIndex = -1;
          int secondDigitIndex = -1;
          int lastDigitIndex = -1;
          int secondToLastDigitIndex = -1;

          //go backwards and get the last and second to last index
          for (int i=twoFactorPhoneForScreen.getPhoneForScreen().length()-1;i>=0; i--) {
            
            char theChar = twoFactorPhoneForScreen.getPhoneForScreen().charAt(i);

            if (!Character.isDigit(theChar)) {
              continue;
            }
            
            if (lastDigitIndex == -1) {
              lastDigitIndex = i;
              continue;
            }
            
            if (secondToLastDigitIndex == -1) {
              secondToLastDigitIndex = i;
              continue;
            }

            //found what we are looking for
            break;
          }
          
          for (int i=0;i<twoFactorPhoneForScreen.getPhoneForScreen().length(); i++) {
            
            char theChar = twoFactorPhoneForScreen.getPhoneForScreen().charAt(i);
            
            //non digit ok
            if (!Character.isDigit(theChar)) {
              phoneForScreen.append(theChar);
              continue;
            }
            
            //see if it is the first digit
            if (firstDigitIndex == -1) {
              firstDigitIndex = i;
              phoneForScreen.append(theChar);
              continue;
              
            }

            //see if it is the second digit
            if (secondDigitIndex == -1) {
              secondDigitIndex = i;
              phoneForScreen.append(theChar);
              continue;
              
            }
            
            if (secondToLastDigitIndex == i) {
              phoneForScreen.append(theChar);
              continue;
            }
            
            if (lastDigitIndex == i) {
              phoneForScreen.append(theChar);
              continue;
            }
            
            //append a pound
            phoneForScreen.append("#");
            
          }
          
          //set it back
          twoFactorPhoneForScreen.setPhoneForScreen(phoneForScreen.toString());
          
        }
      }
      twoFactorHelpLoggingInContainer.setHasPhoneNumbers(hasPhones);
      
    }
    
    {
      
      List<String> colleagueLoginIds = new ArrayList<String>();
      
      TwoFactorUser colleagueUser0 = twoFactorUser.colleagueUser0(twoFactorDaoFactory);
      TwoFactorUser colleagueUser1 = twoFactorUser.colleagueUser1(twoFactorDaoFactory);
      TwoFactorUser colleagueUser2 = twoFactorUser.colleagueUser2(twoFactorDaoFactory);
      TwoFactorUser colleagueUser3 = twoFactorUser.colleagueUser3(twoFactorDaoFactory);
      TwoFactorUser colleagueUser4 = twoFactorUser.colleagueUser4(twoFactorDaoFactory);
      
      //make sure empty users and null
      colleagueLoginIds.add(colleagueUser0 == null ? null : StringUtils.trimToNull(colleagueUser0.getLoginid()));
      colleagueLoginIds.add(colleagueUser1 == null ? null : StringUtils.trimToNull(colleagueUser1.getLoginid()));
      colleagueLoginIds.add(colleagueUser2 == null ? null : StringUtils.trimToNull(colleagueUser2.getLoginid()));
      colleagueLoginIds.add(colleagueUser3 == null ? null : StringUtils.trimToNull(colleagueUser3.getLoginid()));
      colleagueLoginIds.add(colleagueUser4 == null ? null : StringUtils.trimToNull(colleagueUser4.getLoginid()));
      
      //go through the login ids and change them so they look like the screen
      for (int i=0;i<colleagueLoginIds.size();i++) {
        String colleagueLoginId = colleagueLoginIds.get(i);
        if (!StringUtils.isBlank(colleagueLoginId)) {
          twoFactorHelpLoggingInContainer.setHasColleagueLoginids(true);

          //lets resolve and try to get the name
          String nameToEscape = null;
          
          try {
            Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(TfSourceUtils.mainSource(), 
                colleagueLoginId, true, false, false);

            if (subject != null) {
              nameToEscape = subject.getName();
            }
          } catch (RuntimeException re) {
            LOG.error("Error finding subject: " + colleagueLoginId, re);
          }
          if (StringUtils.isBlank(nameToEscape)) {
            nameToEscape = colleagueLoginId;
            //escape it
            String afterAt = TwoFactorServerUtils.prefixOrSuffix(colleagueLoginId, "@", false);
            if (!StringUtils.isBlank(afterAt)) {
              colleagueLoginId = TwoFactorServerUtils.prefixOrSuffix(colleagueLoginId, "@", true);
            }
            
          }
          nameToEscape = TwoFactorServerUtils.obscureName(nameToEscape);
          colleagueLoginIds.set(i, nameToEscape);
        }
      }
      twoFactorHelpLoggingInContainer.setColleagueLoginidsForScreen(colleagueLoginIds);
    }    
    if (twoFactorUser.isInvitedColleaguesWithinAllottedTime()) {

      twoFactorHelpLoggingInContainer.setInvitedColleagues(true);
      
    }
    
  }
  
  /**
   * 
   * @param twoFactorDaoFactory
   * @param twoFactorRequestContainer
   * @param loggedInUser
   */
  public void indexLogic(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final TwoFactorRequestContainer twoFactorRequestContainer,
      final String loggedInUser) {
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(twoFactorDaoFactory, loggedInUser);
    setupNonFactorIndex(twoFactorDaoFactory, twoFactorRequestContainer, twoFactorUser);
  }
}
