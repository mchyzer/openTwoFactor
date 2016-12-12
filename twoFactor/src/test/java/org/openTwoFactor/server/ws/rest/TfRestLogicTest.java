/**
 * @author mchyzer
 * $Id: TfRestLogicTest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.daoTest.TfMemoryAuditDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryBrowserDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryDaoFactory;
import org.openTwoFactor.server.beans.daoTest.TfMemoryRequiredUserDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryServiceProviderDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryUserAgentDao;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.email.TwoFactorEmail;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.ui.serviceLogic.UiMain;
import org.openTwoFactor.server.ui.serviceLogic.UiMain.OptinTestSubmitView;
import org.openTwoFactor.server.ui.serviceLogic.UiMain.OptinView;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordRequest;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordResponse;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordResponseCode;



/**
 *
 */
public class TfRestLogicTest extends TestCase {

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().clear();
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactorServer.factorsThatRequireTwoFactor", 
        "TEST_FACTOR_TF1, TEST_FACTOR_TF2, TEST_FACTOR_TF3");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactorServer.checkPassInterfaceImplementation", 
        "org.openTwoFactor.server.TwoFactorCheckPassDb");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("duo.registerUsers", 
        "false");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("mail.smtp.server", 
        "testing");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactor.api.readonly", 
        "false");
    
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactorServer.ws.restrictUsersRequiredToBeOptedInWhoArentOptedIn", 
        "false");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedIn", 
        "false");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedInEveryDays", 
        "1");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedInEveryDaysBcc", 
        "");
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put("twoFactorServer.ws.restrictUsersQueryRefreshSeconds", 
        "300");
    
    TfMemoryDaoFactory.clear();
    TfRestLogic.refreshUsersNotOptedInButRequiredIfNeeded(TfMemoryDaoFactory.getFactory(), true, false);

  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() throws Exception {
    TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().clear();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new TfRestLogicTest("testCheckPassword"));
    
    TestRunner.run(new TfRestLogicTest("testCheckPasswordRequireOptInUserOptSome"));
    //TestRunner.run(new TfRestLogicTest("testCheckPasswordRequireOptInUserOptIn"));
  }
  
  /**
   * @param name
   */
  public TfRestLogicTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testCheckPassword() {

    TwoFactorRequestContainer.storeInThreadLocalForTesting(true);
    try {
      TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
      String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
      int auditsSize = -1;
  
      int servicesSize = -1;
      
      int browsersSize = -1;
      
      int sleepTimeAfterCall = 200;
      
      TfCheckPasswordResponse tfCheckPasswordResponse = null;
      
      int browserUserAgentSize = -1;
      
      TwoFactorUser twoFactorUserLoggedIn = null;
      
      TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
      
      String browserUserCookieUuid = null;
      
      //############################ test that no loginid is an error
      TfMemoryDaoFactory daoFactory = TfMemoryDaoFactory.getFactory();
      TfCheckPasswordRequest tfCheckPasswordRequest = null;
      
      for (String loginid : new String[]{null, ""}) {
  
        auditsSize = TfMemoryAuditDao.audits.size();
  
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername(loginid);
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
            daoFactory, tfCheckPasswordRequest);
        
        //let audit happen
        
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
        
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertEquals(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage(), "username required");
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertTrue(tfCheckPasswordResponse.getResponseMessage(), tfCheckPasswordResponse.getResponseMessage().contains("username required"));
        assertEquals(TfCheckPasswordResponseCode.INVALID_REQUEST.name(), tfCheckPasswordResponse.getResultCode());
        assertFalse(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertNull(tfCheckPasswordResponse.getTwoFactorRequired());
        assertNull(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertNull(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertNull(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        //assertEquals("index: " + index, auditsSize+1, TfMemoryAuditDao.audits.size());
        
  //      assertEquals(TwoFactorAuditAction.AUTHN_ERROR.name(), 
  //          TfMemoryAuditDao.audits.get(auditsSize).getAction());
  //      //within the last minute
  //      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      }
  
  
      //############################ test that someone who has not opted in is ok for service that doesnt require two factor
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize, TfMemoryAuditDao.audits.size());
      
      //############################ test that someone who has not opted in has wrong pass if password is sent
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer").assignTwoFactorPassUnencrypted("123456");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getResponseMessage(), tfCheckPasswordResponse.getResponseMessage().contains(
          "service does not require two factor, user not enrolled in two factor, password was sent"));
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
      //############################ test that someone who has not opted in, when service requires pass gets bad pass, when pass blank
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer")
        .assignRequireTwoFactor(true);
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals( tfCheckPasswordResponse.getResponseMessage(), 
          "service requires two factor, user not enrolled in two factor", 
          tfCheckPasswordResponse.getResponseMessage());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
  
      //############################ test that someone who has not opted in, when service requires pass by factor gets bad pass, when pass blank
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer")
        .assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR_TF2,TEST_FACTOR3");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals( tfCheckPasswordResponse.getResponseMessage(), "service requires two factor, user not enrolled in two factor");
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
      //############################ test that someone who has not opted in, when service doesnt require pass by factor gets bad pass, when pass blank
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer")
        .assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR3");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals("service does not require two factor, user not enrolled in two factor, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize, TfMemoryAuditDao.audits.size());
      
  
      //############################ test that someone who has not opted in, when service requires pass by factor gets bad pass, when pass blank,
      //############################ browser user agent should be added
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      browserUserAgentSize = TfMemoryUserAgentDao.userAgents.size();
      
      assertEquals(0, browserUserAgentSize);
      
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer").assignBrowserUserAgent(userAgent1)
        .assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR_TF2,TEST_FACTOR3");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest );
  
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals( "service requires two factor, user not enrolled in two factor",
          tfCheckPasswordResponse.getResponseMessage());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
      assertEquals(browserUserAgentSize+1, TfMemoryUserAgentDao.userAgents.size());
  
      assertEquals(userAgent1, TfMemoryUserAgentDao.userAgents.get(browserUserAgentSize).getUserAgent());
      assertEquals("Chrome 23", TfMemoryUserAgentDao.userAgents.get(browserUserAgentSize).getBrowser());
      assertEquals("Windows 7", TfMemoryUserAgentDao.userAgents.get(browserUserAgentSize).getOperatingSystem());
      assertFalse(TfMemoryUserAgentDao.userAgents.get(browserUserAgentSize).getMobile());
      
      //############################ test that someone who has not opted in, when service requires pass by factor gets bad pass, when pass blank,
      //############################ run browser agent again, should not be added
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      browserUserAgentSize = TfMemoryUserAgentDao.userAgents.size();
      
      assertEquals(1, browserUserAgentSize);
      
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer").assignBrowserUserAgent(userAgent1)
        .assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR_TF2,TEST_FACTOR3");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);
  
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals( "service requires two factor, user not enrolled in two factor",
          tfCheckPasswordResponse.getResponseMessage());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
      assertEquals(browserUserAgentSize, TfMemoryUserAgentDao.userAgents.size());
  
      //############################ test that someone who has not opted in, when service requires pass by config, when pass blank,
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      browserUserAgentSize = TfMemoryUserAgentDao.userAgents.size();
      
      assertEquals(1, browserUserAgentSize);
      
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.serviceProviderIdsThatRequireTwoFactor", "someServiceIdRequiresTwoFactor");
       try {
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer").assignBrowserUserAgent(userAgent1)
          .assignSpRequiredFactors("TEST_FACTOR1").assignServiceId("someServiceIdRequiresTwoFactor");
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
            daoFactory, tfCheckPasswordRequest);
    
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
        
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals( "service requires two factor, user not enrolled in two factor",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(browserUserAgentSize, TfMemoryUserAgentDao.userAgents.size());
      } finally {
        TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().remove(
            "twoFactorServer.serviceProviderIdsThatRequireTwoFactor");
      }
      
      //############################ test that someone who has not opted in, when service requires pass by factor gets bad pass, when pass blank,
      //############################ test that debug brings back debug info
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer").assignBrowserUserAgent(userAgent1)
        .assignDebug(true)
        .assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR_TF2,TEST_FACTOR3");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);
  
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNotNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals("service requires two factor, user not enrolled in two factor",
          tfCheckPasswordResponse.getResponseMessage());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
      //############################ test that service name without service id is an error
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer")
        .assignBrowserUserAgent(userAgent1).assignServiceName("someTestServiceName");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest
          );
  
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertEquals("serviceId is required if serviceName is sent", tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.INVALID_REQUEST.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals("Invalid request, if serviceName is sent, then serviceId must be sent", tfCheckPasswordResponse.getResponseMessage());
      assertFalse(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertNull(tfCheckPasswordResponse.getTwoFactorRequired());
      assertNull(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertNull(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertNull(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_ERROR.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      
      //############################ test that service id and service name add a service record
  
      
      auditsSize = TfMemoryAuditDao.audits.size();
      servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
          
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer")
        .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest
          );
  
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals("service does not require two factor, user not enrolled in two factor, password not sent", tfCheckPasswordResponse.getResponseMessage());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      
      assertEquals(servicesSize+1, TfMemoryServiceProviderDao.serviceProviders.size());
      assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(servicesSize).getServiceProviderId());
      assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(servicesSize).getServiceProviderName());
  
      //############################ test that service id and service name shouldnt add a service record should already be there
  
      
      auditsSize = TfMemoryAuditDao.audits.size();
      servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
      
      tfCheckPasswordRequest =  new TfCheckPasswordRequest().assignUsername("mchyzer")
        .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId")
        .assignServiceName("someTestServiceName");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest
          );
  
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals("service does not require two factor, user not enrolled in two factor, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
      assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
      assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      //############################ the rest is for opted in, so opt the user in...
  
      
      //first we need to optin step 1
      twoFactorRequestContainer.setError(null);
      twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "mchyzer");
  
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      twoFactorUser.setPhone0("123 456 7890");
      twoFactorUser.setPhoneIsText0(false);
      twoFactorUser.setPhoneIsVoice0(true);
      twoFactorUser.setPhone1("223 456 7890");
      twoFactorUser.setPhoneIsText1(false);
      twoFactorUser.setPhoneIsVoice1(true);
      
      OptinView optinView = new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
          twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, TfSourceUtils.mainSource());
      
      assertEquals(OptinView.optin, optinView);
      
      twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
      
      byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
      
      long timeDiv30 = System.currentTimeMillis()/30000;
      
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30-3);
          
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
      
        twoFactorRequestContainer.setError(null);
    
        auditsSize = TfMemoryAuditDao.audits.size();
    
        OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
            twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, passString, TfSourceUtils.mainSource(), null, false);
      
        assertEquals(optinTestSubmitView.name() 
            + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
            OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    
        assertTrue(twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
    
        assertEquals(TwoFactorAuditAction.OPTIN_TWO_FACTOR.name(), TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      
        assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
        assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
      }
      
      //################################## try a few valid passes
  
      //dont change the order, checking things about it
      String[] potentialPasses = new String[]{
  /* 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
  /* 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
  /* 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
  /* 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
  /* 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
  /* 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
  /* 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
  /* 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
  /* 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
  /* 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
  /* 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
  /* 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
  /* 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
          };
      
      int i = 0;
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        if (i==0) {
          //there are no printed out codes until we print them out
          assertEquals(i + ": " + pass, TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
          new UiMain().showOneTimeCodesLogic(TfMemoryDaoFactory.getFactory(), 
              twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1);
  
          tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
          TwoFactorServerUtils.sleep(sleepTimeAfterCall);
          
          
        }
        assertEquals(i + "", "service does not require two factor, user enrolled in two factor, browser was not previously trusted, password correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertEquals(i + ": " + pass + ", " + twoFactorUserLoggedIn.getLastTotpTimestampUsed(), 
            TfCheckPasswordResponseCode.CORRECT_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNotNull(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        //i 0 has multiple audits
        if (i != 0) {
          assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
          
          assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR.name(), 
              TfMemoryAuditDao.audits.get(auditsSize).getAction());
          //within the last minute
          assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
        }
        
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
        //refresh this
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
  
        // 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
        if (i == 0) {
          assertEquals(500002, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
          
        // 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
        if (i == 1) {
          assertEquals(500003, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
        if (i == 2) {
          assertEquals(500004, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
        if (i == 3) {
          assertEquals(1, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
        if (i == 4) {
          assertEquals(2, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
        if (i == 5) {
          assertEquals(3, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
        if (i == 6) {
          assertEquals(4, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
        if (i == 7) {
          assertEquals(timeDiv30-2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
        if (i == 8) {
          assertEquals(timeDiv30-1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
        if (i == 9) {
          assertEquals(timeDiv30, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
        if (i == 10) {
          assertEquals(timeDiv30+1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
        if (i == 11) {
          assertEquals(timeDiv30+2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
        if (i == 12) {
          assertEquals(12, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        i++;
      }
      
      //################################## try the same passes, they should all be invalid
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
      
      //################################## try some invalid passes
  
      potentialPasses = new String[]{"123456", "123", "99999999", "-1", "123,234"};
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
            
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
      
      //######################### trust the browser
  
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
  
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignTrustedBrowser(true)
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(passString)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.CORRECT_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals(
            "service does not require two factor, user enrolled in two factor, browser was not previously trusted, password correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertTrue(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
      //######################### test that opted in, trusted browser already, and no pass is ok
      {
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer")
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
            TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
        
        assertEquals(TfCheckPasswordResponseCode.TRUSTED_BROWSER.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TRUSTED_BROWSER.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
      //######################### test that opted in, trusted browser already, dontTrustBrowser=true, and no pass is NOT ok
      {
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignDontTrustBrowser(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, dont trust browser, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
  
      //######################### test that opted in, not trusted browser already, service forbids two factor, and no pass is ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
  
        TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
            "twoFactorServer.serviceProviderIdsThatForbidTwoFactor", "someServiceIdRequiresTwoFactor");
        try {
        
          tfCheckPasswordRequest = new TfCheckPasswordRequest()
            .assignUsername("mchyzer")
            .assignBrowserUserAgent(userAgent1).assignServiceId("someServiceIdRequiresTwoFactor");
          
          tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
          tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
    
          //let audit happen
          TwoFactorServerUtils.sleep(sleepTimeAfterCall);
    
          assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
          assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
          
          assertNull(tfCheckPasswordResponse.getWhenTrusted());
          
          assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_FORBIDDEN.name(), tfCheckPasswordResponse.getResultCode());
          assertEquals("service forbids two factor, user enrolled in two factor",
              tfCheckPasswordResponse.getResponseMessage());
          assertTrue(tfCheckPasswordResponse.getSuccess());
          assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
          assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
          assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
          assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
          assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
          
          assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
          
          assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_FORBIDDEN.name(), 
              TfMemoryAuditDao.audits.get(auditsSize).getAction());
          //within the last minute
          assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      
        } finally {
  
          TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().remove(
              "twoFactorServer.serviceProviderIdsThatForbidTwoFactor");
  
        }
      }
      
      //######################### test that opted in, trusted browser already, and service requires two factor, and no pass is ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TRUSTED_BROWSER.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
            TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
        
  
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TRUSTED_BROWSER.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
      
      //######################### test that opted in, trusted browser already, and service requires two factor, and dont trust browser and no pass is NOT ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignRequireTwoFactor(true).assignDontTrustBrowser(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor, dont trust browser, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
    
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
      
      //######################### test that opted in, trusted browser already, and service factors requires two factor, and no pass is ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR_TF2,TEST_FACTOR3")
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TRUSTED_BROWSER.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
            TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
        
  
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TRUSTED_BROWSER.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
      
      //######################### test that opted in, trusted browser already, and service factors requires two factor, and dont trust browser, and no pass is NOT ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR_TF2,TEST_FACTOR3").assignDontTrustBrowser(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor, dont trust browser, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        
  
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }

      //######################### test that opted in, trusted browser already, and service requires two factor, and no pass, and reauth, is ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignRequireTwoFactor(true).assignRequireReauth(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor, browser was already trusted, require reauthentication",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
            TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
        
  
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
      //######################### test that opted in, trusted browser already, and service requires two factor, and no pass, and reauth, and dont trust browser, is not ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignRequireTwoFactor(true).assignRequireReauth(true).assignDontTrustBrowser(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor, dont trust browser, browser was already trusted, require reauthentication",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
          
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
      //######################### test that opted in, trusted browser already, and factors requires two factor, and no pass, and reauth, is not ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignSpRequiredFactors("TEST_FACTOR1,TEST_FACTOR_TF2,TEST_FACTOR3").assignRequireReauth(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor, browser was already trusted, require reauthentication",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
            TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
        
  
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
      //######################### test that opted in, trusted browser already, and no pass, and reauth, and dontTrustBrowser is NOT ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignRequireReauth(true).assignDontTrustBrowser(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, dont trust browser, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        
  
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
      //######################### test that opted in, trusted browser already, and no pass, and reauth, is ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        assertEquals(2, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignRequireReauth(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertEquals(TfCheckPasswordResponseCode.TRUSTED_BROWSER.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
  
        assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
            TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
        
  
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TRUSTED_BROWSER.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }

      //############################ the rest is for opted in, so opt the user harveycg in...
      //first we need to optin step 1
      twoFactorRequestContainer.setError(null);

      TwoFactorRequestContainer.storeInThreadLocalForTesting(false);
      TwoFactorRequestContainer.storeInThreadLocalForTesting(true);

      twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
      
      //lets retrieve first
      TwoFactorUser.retrieveByLoginidOrCreate(TfMemoryDaoFactory.getFactory(), "harveycg");
      twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "harveycg");

      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      twoFactorUser.setPhone0("123 456 7890");
      twoFactorUser.setPhoneIsText0(false);
      twoFactorUser.setPhoneIsVoice0(true);
      twoFactorUser.setPhone1("223 456 7890");
      twoFactorUser.setPhoneIsText1(false);
      twoFactorUser.setPhoneIsVoice1(true);
      
      if (!twoFactorUser.isOptedIn()) {
        optinView = new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
            twoFactorRequestContainer, "harveycg", "130.91.219.176", userAgent1, TfSourceUtils.mainSource());
        
        assertEquals(OptinView.optin, optinView);
      }
      
      twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
      
      key = new Base32().decode(twoFactorSecretTempUnencrypted);
      
      timeDiv30 = System.currentTimeMillis()/30000;
      
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30);
          
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
      
        twoFactorRequestContainer.setError(null);
    
        auditsSize = TfMemoryAuditDao.audits.size();
    
        OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
            twoFactorRequestContainer, "harveycg", "130.91.219.176", userAgent1, passString, TfSourceUtils.mainSource(), null, false);
      
        assertEquals(optinTestSubmitView.name() 
            + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
            OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    
        assertTrue(twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
    
        assertEquals(TwoFactorAuditAction.OPTIN_TWO_FACTOR.name(), TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      
        assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
        assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
      }
  
      
      //######################### test that opted in, trusted browser already, different user, and no pass is not ok
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), "harveycg");
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
  
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        browsersSize = TfMemoryBrowserDao.browsers.size();
  
        assertEquals(2, servicesSize);

        twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "harveycg");

        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("harveycg")
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
  
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
  
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
  
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
  
        assertEquals(browsersSize+1, TfMemoryBrowserDao.browsers.size());
        assertTrue(StringUtils.equals(TwoFactorBrowser.encryptBrowserUserUuid(browserUserCookieUuid), 
            TfMemoryBrowserDao.browsers.get(browsersSize-1).getBrowserTrustedUuid()));
        assertTrue(!StringUtils.equals(TwoFactorBrowser.encryptBrowserUserUuid(browserUserCookieUuid), 
            TfMemoryBrowserDao.browsers.get(browsersSize).getBrowserTrustedUuid()));
  
        //make sure time has had a chance to update
        TwoFactorServerUtils.sleep(20);
        
        assertNotNull(TfMemoryBrowserDao.browsers.get(browsersSize-1).getDeletedOn()); 
  
        assertTrue(new Date(TfMemoryBrowserDao.browsers.get(browsersSize-1).getDeletedOn()).toString(), 
            TfMemoryBrowserDao.browsers.get(browsersSize-1).getDeletedOn() < System.currentTimeMillis()
            && TfMemoryBrowserDao.browsers.get(browsersSize-1).getDeletedOn() > System.currentTimeMillis() - 2000);
        
  //      assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
  //          TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
  
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, browser was not previously trusted",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
  
      }
  
      //######################### test that opted in, trusted browser already, same user, and no pass is not ok since the browser token is dead
      //######################### since someone else tried to use it
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
  
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        browsersSize = TfMemoryBrowserDao.browsers.size();
  
        assertEquals(2, servicesSize);
  
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer")
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
  
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
  
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
  
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
  
        assertEquals(browsersSize+1, TfMemoryBrowserDao.browsers.size());
  
        //make sure time has had a chance to update
        TwoFactorServerUtils.sleep(20);
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service does not require two factor, user enrolled in two factor, browser was not previously trusted",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
        assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderId());
        assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(1).getServiceProviderName());
      }
    } finally {
      TwoFactorRequestContainer.storeInThreadLocalForTesting(false);
    }

  }

  /**
   * 
   */
  public void testCheckPasswordOptSome() {
  
    TwoFactorRequestContainer.storeInThreadLocalForTesting(true);
    try {
      TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
      String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
      int auditsSize = -1;
  
      int servicesSize = -1;
            
      int sleepTimeAfterCall = 200;
      
      TfCheckPasswordResponse tfCheckPasswordResponse = null;
      
      TwoFactorUser twoFactorUserLoggedIn = null;
      
      TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
      
      String browserUserCookieUuid = null;
      
      TfMemoryDaoFactory daoFactory = TfMemoryDaoFactory.getFactory();
      TfCheckPasswordRequest tfCheckPasswordRequest = null;
      
      //############################ test that someone who has not opted in is not ok for service that does require two factor
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignRequireTwoFactor(true);
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordRequest.assignRequireTwoFactor(true);
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service requires two factor, user not enrolled in two factor", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
            
      //############################ the rest is for opted in, so opt the user in... as opt some
  
      
      //first we need to optin step 1
      twoFactorRequestContainer.setError(null);
      twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "mchyzer");
  
      TwoFactorUser twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      twoFactorUser.setPhone0("123 456 7890");
      twoFactorUser.setPhoneIsText0(false);
      twoFactorUser.setPhoneIsVoice0(true);
      twoFactorUser.setPhone1("223 456 7890");
      twoFactorUser.setPhoneIsText1(false);
      twoFactorUser.setPhoneIsVoice1(true);
      twoFactorUser.setOptInOnlyIfRequired(true);
      
      OptinView optinView = new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
          twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, TfSourceUtils.mainSource());
      
      assertEquals(OptinView.optin, optinView);
      
      twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
      
      byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
      
      long timeDiv30 = System.currentTimeMillis()/30000;
      
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30-3);
          
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
      
        twoFactorRequestContainer.setError(null);
    
        auditsSize = TfMemoryAuditDao.audits.size();
    
        OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
            twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, passString, TfSourceUtils.mainSource(), null, false);
      
        assertEquals(optinTestSubmitView.name() 
            + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
            OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    
        assertTrue(twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
    
        assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
        assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
      }
      
      //################################## try a few valid passes
  
      //dont change the order, checking things about it
      String[] potentialPasses = new String[]{
  /* 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
  /* 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
  /* 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
  /* 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
  /* 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
  /* 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
  /* 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
  /* 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
  /* 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
  /* 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
  /* 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
  /* 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
  /* 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
          };
      
      int i = 0;
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setOptInOnlyIfRequired(true);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        if (i==0) {
          //there are no printed out codes until we print them out
          assertEquals(i + ": " + pass, TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
          assertTrue(new UiMain().showOneTimeCodesLogic(TfMemoryDaoFactory.getFactory(), 
              twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1));
  
          tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
          TwoFactorServerUtils.sleep(sleepTimeAfterCall);
          
          
        }
        assertEquals(i + "", "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertEquals(i + ": " + pass + ", " + twoFactorUserLoggedIn.getLastTotpTimestampUsed(), 
            TfCheckPasswordResponseCode.CORRECT_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNotNull(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        //i 0 has multiple audits
        if (i != 0) {
          assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
          
          assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR.name(), 
              TfMemoryAuditDao.audits.get(auditsSize).getAction());
          //within the last minute
          assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
        }
          
        //refresh this
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
  
        // 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
        if (i == 0) {
          assertEquals(500002, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
          
        // 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
        if (i == 1) {
          assertEquals(500003, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
        if (i == 2) {
          assertEquals(500004, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
        if (i == 3) {
          assertEquals(1, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
        if (i == 4) {
          assertEquals(2, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
        if (i == 5) {
          assertEquals(3, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
        if (i == 6) {
          assertEquals(4, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
        if (i == 7) {
          assertEquals(timeDiv30-2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
        if (i == 8) {
          assertEquals(timeDiv30-1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
        if (i == 9) {
          assertEquals(timeDiv30, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
        if (i == 10) {
          assertEquals(timeDiv30+1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
        if (i == 11) {
          assertEquals(timeDiv30+2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
        if (i == 12) {
          assertEquals(12, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        i++;
      }
      
      //################################## try the same passes, they should all be invalid
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        assertEquals(1, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
      }
      
      //################################## try some invalid passes
  
      potentialPasses = new String[]{"123456", "123", "99999999", "-1", "123,234"};
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
            
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      
      }
      
      //############################ test that someone who has opted in for opt some is ok for service that does not require two factor
      
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordRequest.assignRequireTwoFactor(true);

      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());

      
      //######################### trust the browser
  
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30+2);
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
  
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
  
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
          
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignTrustedBrowser(true).assignRequireTwoFactor(true)
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(passString)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.CORRECT_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals(
            "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertTrue(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
  
      }
  
      //######################### test that opted in, trusted browser already, and no pass is ok
      {
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignTrustedBrowser(true).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
            TwoFactorBrowser.retrieveByBrowserTrustedUuid(TfMemoryDaoFactory.getFactory(), browserUserCookieUuid, false).getWhenTrusted());
        
        assertEquals(TfCheckPasswordResponseCode.TRUSTED_BROWSER.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor for required applications, browser was already trusted",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TRUSTED_BROWSER.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
      }
  
      //######################### test that opted in, trusted browser already, dont trust browser, and no pass is NOT ok
      {
  
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
  
        tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
          .assignUsername("mchyzer").assignTrustedBrowser(true).assignRequireTwoFactor(true).assignDontTrustBrowser(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
       
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals(tfCheckPasswordResponse.getResponseMessage(), "service requires two factor, user enrolled in two factor for required applications, dont trust browser, browser was not previously trusted",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() == null || !tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
        assertEquals(servicesSize, TfMemoryServiceProviderDao.serviceProviders.size());
  
      }
  
  
    } finally {
      TwoFactorRequestContainer.storeInThreadLocalForTesting(false);
    }
  
  }

  /**
   * 
   */
  public void testCheckPasswordRequireOptInUserOptSome() {
  
    TwoFactorRequestContainer.storeInThreadLocalForTesting(true);
    try {
      TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
      String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
      int auditsSize = -1;
  
      int servicesSize = -1;
            
      int sleepTimeAfterCall = 200;
      
      TfCheckPasswordResponse tfCheckPasswordResponse = null;
      
      TwoFactorUser twoFactorUserLoggedIn = null;
      
      TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
      
      @SuppressWarnings("unused")
      String browserUserCookieUuid = null;
      
      TwoFactorUser twoFactorUser = null;
      Calendar calendar = null;

      TfMemoryDaoFactory daoFactory = TfMemoryDaoFactory.getFactory();
      TfCheckPasswordRequest tfCheckPasswordRequest = null;

      TfMemoryRequiredUserDao.requiredUsers.put("mchyzer", new TfRestRequiredUser("mchyzer", "Chris Hyzer", "mchyzer@isc.upenn.edu"));
      TfRestLogic.refreshUsersNotOptedInButRequiredIfNeeded(daoFactory, true, false);
      long emailCount = -1;
      
      twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "mchyzer");


      //############################ test that someone who has not opted in but is required to is ok for service that does not require two factor if not configured, no email
      
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;

      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);
      
      
      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, email
  
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedIn", 
          "true");
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedInEveryDays", 
          "1");

      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;

      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      
      
      //IF YOU GET AN ERROR HERE TRY THE TEST AGAIN, NOT SURE WHY INTERMITTENT
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, emailed user who should be opted in, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      //IF YOU GET AN ERROR HERE TRY THE TEST AGAIN, NOT SURE WHY INTERMITTENT
      
      
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount+1, TwoFactorEmail.testingEmailCount);
      
      //############################ no email the second time

      emailCount = TwoFactorEmail.testingEmailCount;

      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);

      //############################ no email 23 hours later

      emailCount = TwoFactorEmail.testingEmailCount;

      auditsSize = TfMemoryAuditDao.audits.size();

      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.add(Calendar.HOUR_OF_DAY, -23);
      
      twoFactorUser.setLastEmailNotOptedInUser(calendar.getTimeInMillis());
      
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);

      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, email since last email was 26 hours ago
      
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;

      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.add(Calendar.HOUR_OF_DAY, -26);
      
      twoFactorUser.setLastEmailNotOptedInUser(calendar.getTimeInMillis());

      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, emailed user who should be opted in, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount+1, TwoFactorEmail.testingEmailCount);

      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, no email
      
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.restrictUsersRequiredToBeOptedInWhoArentOptedIn", 
          "true");

      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;

      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, user required to be enrolled and is not, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);

      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, email
      
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;

      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      twoFactorUser.setLastEmailNotOptedInUser(0L);

      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, user required to be enrolled and is not, emailed user who should be opted in, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount+1, TwoFactorEmail.testingEmailCount);

      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, no email
      
      //configure to not email
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedIn", 
          "false");

      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;

      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

      twoFactorUser.setLastEmailNotOptedInUser(0L);

      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, user required to be enrolled and is not, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());

      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);

      //############################ the rest is for opted in, so opt the user in... as opt some

      
      //first we need to optin step 1
      twoFactorRequestContainer.setError(null);
      twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "mchyzer");
  
      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      twoFactorUser.setPhone0("123 456 7890");
      twoFactorUser.setPhoneIsText0(false);
      twoFactorUser.setPhoneIsVoice0(true);
      twoFactorUser.setPhone1("223 456 7890");
      twoFactorUser.setPhoneIsText1(false);
      twoFactorUser.setPhoneIsVoice1(true);
      twoFactorUser.setOptInOnlyIfRequired(true);
      
      OptinView optinView = new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
          twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, TfSourceUtils.mainSource());
      
      assertEquals(OptinView.optin, optinView);
      
      twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
      
      byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
      
      long timeDiv30 = System.currentTimeMillis()/30000;
      
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30-3);
          
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
      
        twoFactorRequestContainer.setError(null);
    
        auditsSize = TfMemoryAuditDao.audits.size();
    
        OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
            twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, passString, TfSourceUtils.mainSource(), null, false);
      
        assertEquals(optinTestSubmitView.name() 
            + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
            OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    
        assertTrue(twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
    
        assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
        assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
      }
      
      //################################## try a few valid passes
  
      //dont change the order, checking things about it
      String[] potentialPasses = new String[]{
  /* 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
  /* 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
  /* 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
  /* 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
  /* 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
  /* 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
  /* 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
  /* 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
  /* 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
  /* 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
  /* 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
  /* 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
  /* 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
          };
      
      int i = 0;
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setOptInOnlyIfRequired(true);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        if (i==0) {
          //there are no printed out codes until we print them out
          assertEquals(i + ": " + pass, TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
          assertTrue(new UiMain().showOneTimeCodesLogic(TfMemoryDaoFactory.getFactory(), 
              twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1));
  
          tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
          TwoFactorServerUtils.sleep(sleepTimeAfterCall);
          
          
        }
        assertEquals(i + ": " + tfCheckPasswordResponse.getResponseMessage(), 
            "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertEquals(i + ": " + pass + ", " + twoFactorUserLoggedIn.getLastTotpTimestampUsed(), 
            TfCheckPasswordResponseCode.CORRECT_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNotNull(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        //i 0 has multiple audits
        if (i != 0) {
          assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
          
          assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR.name(), 
              TfMemoryAuditDao.audits.get(auditsSize).getAction());
          //within the last minute
          assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
        }
          
        //refresh this
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
  
        // 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
        if (i == 0) {
          assertEquals(500002, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
          
        // 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
        if (i == 1) {
          assertEquals(500003, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
        if (i == 2) {
          assertEquals(500004, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
        if (i == 3) {
          assertEquals(1, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
        if (i == 4) {
          assertEquals(2, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
        if (i == 5) {
          assertEquals(3, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
        if (i == 6) {
          assertEquals(4, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
        if (i == 7) {
          assertEquals(timeDiv30-2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
        if (i == 8) {
          assertEquals(timeDiv30-1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
        if (i == 9) {
          assertEquals(timeDiv30, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
        if (i == 10) {
          assertEquals(timeDiv30+1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
        if (i == 11) {
          assertEquals(timeDiv30+2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
        if (i == 12) {
          assertEquals(12, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        i++;
      }
      
      //################################## try the same passes, they should all be invalid
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        assertEquals(1, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
      }
      
      //################################## try some invalid passes
  
      potentialPasses = new String[]{"123456", "123", "99999999", "-1", "123,234"};
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
            
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals(tfCheckPasswordResponse.getResponseMessage(),
            "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      
      }
      
      //############################ test that someone who has opted in for opt some is ok for service that does not require two factor
      
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordRequest.assignRequireTwoFactor(true);

      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());

      //############################ test that someone who has opted in for some (and required to) is not ok for service that does not require two factor
      
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(),
          "service does not require two factor, user enrolled in two factor for required applications, user required to be enrolled and is not, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());  
  
      //############################ test that someone who has opted in for some (and required to) is ok for service that forbids two factor
      
      auditsSize = TfMemoryAuditDao.audits.size();
      
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.serviceProviderIdsThatForbidTwoFactor", "someServiceIdRequiresTwoFactor");
      try {
      
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer")
          .assignBrowserUserAgent(userAgent1).assignServiceId("someServiceIdRequiresTwoFactor");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());

        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_FORBIDDEN.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
            "service forbids two factor, user enrolled in two factor for required applications",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR_FORBIDDEN.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
      } finally {

        TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().remove(
            "twoFactorServer.serviceProviderIdsThatForbidTwoFactor");

      }

      
      //############################ test that someone who has opted in for some (and required to) is ok for service that forbids two factor
      
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.serviceProviderIdsThatDontRequireUsersToOptIn", "someServiceIdDoesntRequireUsersToOptIn");
      try {
      
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer")
          .assignBrowserUserAgent(userAgent1).assignServiceId("someServiceIdDoesntRequireUsersToOptIn");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());

        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
  
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        
        assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
            "service does not require two factor, user enrolled in two factor for required applications, user required to be enrolled and is not, password not sent",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
                
      } finally {

        TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().remove(
            "twoFactorServer.serviceProviderIdsThatForbidTwoFactor");

      }

      
      
    } finally {
      TwoFactorRequestContainer.storeInThreadLocalForTesting(false);
    }
  
  }

  /**
   * 
   */
  public void testCheckPasswordRequireOptInUserOptIn() {
  
    TwoFactorRequestContainer.storeInThreadLocalForTesting(true);
    try {
      TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
  
      String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
      int auditsSize = -1;
  
      int servicesSize = -1;
            
      int sleepTimeAfterCall = 200;
      
      TfCheckPasswordResponse tfCheckPasswordResponse = null;
      
      TwoFactorUser twoFactorUserLoggedIn = null;
      
      TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
      
      @SuppressWarnings("unused")
      String browserUserCookieUuid = null;
      
      TwoFactorUser twoFactorUser = null;
      Calendar calendar = null;
  
      TfMemoryDaoFactory daoFactory = TfMemoryDaoFactory.getFactory();
      TfCheckPasswordRequest tfCheckPasswordRequest = null;
  
      TfMemoryRequiredUserDao.requiredUsers.put("mchyzer", new TfRestRequiredUser("mchyzer", "Chris Hyzer", "mchyzer@isc.upenn.edu"));
      TfRestLogic.refreshUsersNotOptedInButRequiredIfNeeded(daoFactory, true, false);
      long emailCount = -1;
      
      twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "mchyzer");
  
  
      //############################ test that someone who has not opted in but is required to is ok for service that does not require two factor if not configured, no email
      
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);
      
      
      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, email
  
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedIn", 
          "true");
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedInEveryDays", 
          "1");
  
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      
      
      //IF YOU GET AN ERROR HERE TRY THE TEST AGAIN, NOT SURE WHY INTERMITTENT
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, emailed user who should be opted in, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      //IF YOU GET AN ERROR HERE TRY THE TEST AGAIN, NOT SURE WHY INTERMITTENT
      
      
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount+1, TwoFactorEmail.testingEmailCount);
      
      //############################ no email the second time
  
      emailCount = TwoFactorEmail.testingEmailCount;
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);
  
      //############################ no email 23 hours later
  
      emailCount = TwoFactorEmail.testingEmailCount;
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.add(Calendar.HOUR_OF_DAY, -23);
      
      twoFactorUser.setLastEmailNotOptedInUser(calendar.getTimeInMillis());
      
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);
  
      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, email since last email was 26 hours ago
      
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;
  
      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      calendar = new GregorianCalendar();
      calendar.setTimeInMillis(System.currentTimeMillis());
      calendar.add(Calendar.HOUR_OF_DAY, -26);
      
      twoFactorUser.setLastEmailNotOptedInUser(calendar.getTimeInMillis());
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, emailed user who should be opted in, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertFalse(tfCheckPasswordResponse.getTwoFactorRequired());
      assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount+1, TwoFactorEmail.testingEmailCount);
  
      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, no email
      
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.restrictUsersRequiredToBeOptedInWhoArentOptedIn", 
          "true");
  
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, user required to be enrolled and is not, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);
  
      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, email
      
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;
  
      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      twoFactorUser.setLastEmailNotOptedInUser(0L);
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, user required to be enrolled and is not, emailed user who should be opted in, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount+1, TwoFactorEmail.testingEmailCount);
  
      //############################ test that someone who has not opted in but is required to be is ok for service that does not require two factor if not configured, no email
      
      //configure to not email
      TwoFactorServerConfig.retrieveConfig().propertiesOverrideMap().put(
          "twoFactorServer.ws.emailUsersRequiredToBeOptedInWhoArentOptedIn", 
          "false");
  
      auditsSize = TfMemoryAuditDao.audits.size();
      emailCount = TwoFactorEmail.testingEmailCount;
  
      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
  
      twoFactorUser.setLastEmailNotOptedInUser(0L);
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service does not require two factor, user not enrolled in two factor, user required to be enrolled and is not, password not sent",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
  
      assertEquals(emailCount, TwoFactorEmail.testingEmailCount);
  
      //############################ the rest is for opted in, so opt the user in... as opt some
  
      
      //first we need to optin step 1
      twoFactorRequestContainer.setError(null);
      twoFactorRequestContainer.init(TfMemoryDaoFactory.getFactory(), "mchyzer");
  
      twoFactorUser = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      twoFactorUser.setPhone0("123 456 7890");
      twoFactorUser.setPhoneIsText0(false);
      twoFactorUser.setPhoneIsVoice0(true);
      twoFactorUser.setPhone1("223 456 7890");
      twoFactorUser.setPhoneIsText1(false);
      twoFactorUser.setPhoneIsVoice1(true);
      
      OptinView optinView = new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
          twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, TfSourceUtils.mainSource());
      
      assertEquals(OptinView.optin, optinView);
      
      twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
      
      String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
      
      byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
      
      long timeDiv30 = System.currentTimeMillis()/30000;
      
      {
        int pass = twoFactorLogic.totpPassword(key, timeDiv30-3);
          
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        String passString = Integer.toString(pass);
        passString = StringUtils.leftPad(passString, 6, '0');
      
        twoFactorRequestContainer.setError(null);
    
        auditsSize = TfMemoryAuditDao.audits.size();
    
        OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
            twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1, passString, TfSourceUtils.mainSource(), null, false);
      
        assertEquals(optinTestSubmitView.name() 
            + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
            OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    
        assertTrue(twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
    
        assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
        assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
      }
      
      //################################## try a few valid passes
  
      //dont change the order, checking things about it
      String[] potentialPasses = new String[]{
  /* 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
  /* 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
  /* 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
  /* 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
  /* 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
  /* 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
  /* 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
  /* 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
  /* 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
  /* 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
  /* 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
  /* 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
  /* 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
          };
      
      int i = 0;
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.setOptInOnlyIfRequired(true);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        if (i==0) {
          //there are no printed out codes until we print them out
          assertEquals(i + ": " + pass, TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
          assertTrue(new UiMain().showOneTimeCodesLogic(TfMemoryDaoFactory.getFactory(), 
              twoFactorRequestContainer, "mchyzer", "130.91.219.176", userAgent1));
  
          tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
          TwoFactorServerUtils.sleep(sleepTimeAfterCall);
          
          
        }
        assertEquals(i + ": " + tfCheckPasswordResponse.getResponseMessage(), 
            "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertEquals(i + ": " + pass + ", " + twoFactorUserLoggedIn.getLastTotpTimestampUsed(), 
            TfCheckPasswordResponseCode.CORRECT_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertNotNull(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(i + "", tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertTrue(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        //i 0 has multiple audits
        if (i != 0) {
          assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
          
          assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR.name(), 
              TfMemoryAuditDao.audits.get(auditsSize).getAction());
          //within the last minute
          assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
        }
          
        //refresh this
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
  
        // 0 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500001)),
        if (i == 0) {
          assertEquals(500002, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
          
        // 1 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500002)),
        if (i == 1) {
          assertEquals(500003, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 2 */        Integer.toString(twoFactorLogic.hotpPassword(key, 500003)),
        if (i == 2) {
          assertEquals(500004, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
        }
  
        // 3 */        Integer.toString(twoFactorLogic.hotpPassword(key, 0)),
        if (i == 3) {
          assertEquals(1, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 4 */        Integer.toString(twoFactorLogic.hotpPassword(key, 1)),
        if (i == 4) {
          assertEquals(2, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 5 */        Integer.toString(twoFactorLogic.hotpPassword(key, 2)), 
        if (i == 5) {
          assertEquals(3, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 6 */        Integer.toString(twoFactorLogic.hotpPassword(key, 3)),
        if (i == 6) {
          assertEquals(4, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        // 7 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-2)),
        if (i == 7) {
          assertEquals(timeDiv30-2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 8 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30-1)),
        if (i == 8) {
          assertEquals(timeDiv30-1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 9 */        Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30)),
        if (i == 9) {
          assertEquals(timeDiv30, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 10 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+1)),
        if (i == 10) {
          assertEquals(timeDiv30+1, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 11 */       Integer.toString(twoFactorLogic.totpPassword(key, timeDiv30+2)),
        if (i == 11) {
          assertEquals(timeDiv30+2, twoFactorUserLoggedIn.getLastTotpTimestampUsed().longValue());
        }
  
        // 12 */       twoFactorLogic.hotpPassword(key, 10) + "," + twoFactorLogic.hotpPassword(key, 11)
        if (i == 12) {
          assertEquals(12, twoFactorUserLoggedIn.getTokenIndex().intValue());
        }
  
        i++;
      }
      
      //################################## try the same passes, they should all be invalid
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
        
        pass = StringUtils.leftPad(pass, 6, '0');
  
      
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        assertEquals(1, servicesSize);
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals("service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    
      }
      
      //################################## try some invalid passes
  
      potentialPasses = new String[]{"123456", "123", "99999999", "-1", "123,234"};
  
      for (String pass : potentialPasses) {
        
        //lets retrieve first
        twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
        twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
        twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
            
        auditsSize = TfMemoryAuditDao.audits.size();
        servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();
        
        tfCheckPasswordRequest = new TfCheckPasswordRequest()
          .assignUsername("mchyzer").assignTwoFactorPassUnencrypted(pass).assignRequireTwoFactor(true)
          .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
        
        tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
  
        tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);
        
        browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();
        
        //let audit happen
        TwoFactorServerUtils.sleep(sleepTimeAfterCall);
  
        assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
        assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
        assertNull(tfCheckPasswordResponse.getWhenTrusted());
        assertEquals(TfCheckPasswordResponseCode.WRONG_PASSWORD.name(), tfCheckPasswordResponse.getResultCode());
        assertEquals(tfCheckPasswordResponse.getResponseMessage(),
            "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted, password not correct",
            tfCheckPasswordResponse.getResponseMessage());
        assertTrue(tfCheckPasswordResponse.getSuccess());
        assertFalse(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
        assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
        assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
        assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
        assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
        assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
        assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
        
        assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
        
        assertEquals(TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name(), 
            TfMemoryAuditDao.audits.get(auditsSize).getAction());
        //within the last minute
        assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
      
      }
      
      //############################ test that someone who has opted in for opt some is ok for service that does not require two factor
      
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordRequest.assignRequireTwoFactor(true);
  
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(
          daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(), 
          "service requires two factor, user enrolled in two factor for required applications, browser was not previously trusted",
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertTrue(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
  
      //############################ test that someone who has opted in for some (and required to) is not ok for service that does not require two factor
      
      auditsSize = TfMemoryAuditDao.audits.size();
  
      tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUsername("mchyzer");
      tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());
      tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);    
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(tfCheckPasswordResponse.getResponseMessage(),
          "service does not require two factor, user enrolled in two factor for required applications, user required to be enrolled and is not, password not sent", 
          tfCheckPasswordResponse.getResponseMessage());
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertTrue(tfCheckPasswordResponse.getSuccess());
      assertNull(tfCheckPasswordResponse.getTwoFactorPasswordCorrect());
      assertTrue(tfCheckPasswordResponse.getTwoFactorRequired());
      assertFalse(tfCheckPasswordResponse.getTwoFactorUserAllowed());
      assertFalse(tfCheckPasswordResponse.getUserEnrolledInTwoFactor());
      assertFalse(tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());  
  
    } finally {
      TwoFactorRequestContainer.storeInThreadLocalForTesting(false);
    }
  
  }

}
