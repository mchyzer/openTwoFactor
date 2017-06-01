/**
 * @author mchyzer
 * $Id: UiMainTest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.serviceLogic;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.daoTest.TfMemoryAuditDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryBrowserDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryDaoFactory;
import org.openTwoFactor.server.beans.daoTest.TfMemoryIpAddressDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryServiceProviderDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryUserAgentDao;
import org.openTwoFactor.server.beans.daoTest.TfMemoryUserDao;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.ui.serviceLogic.UiMain;
import org.openTwoFactor.server.ui.serviceLogic.UiMain.OptinTestSubmitView;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordRequest;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordResponse;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordResponseCode;
import org.openTwoFactor.server.ws.rest.TfRestLogic;



/**
 *
 */
public class UiMainTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiMainTest("testUntrustBrowsers"));
  }
  
  /**
   * @param name
   */
  public UiMainTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testOptin() {
    
    TfMemoryDaoFactory.clear();
    
    optinHelper(1);
    
    //run twice to make sure not double objects (except audits)
    optinHelper(2);
    
  }

  /**
   * @param timesRun
   */
  private void optinHelper(int timesRun) {
    TwoFactorRequestContainer twoFactorRequestContainer = new TwoFactorRequestContainer();
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";

    new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    assertEquals(1, TfMemoryIpAddressDao.ipAddresses.size());
    assertEquals("130.91.219.176", TfMemoryIpAddressDao.ipAddresses.iterator().next().getIpAddress());
    assertTrue(TfMemoryIpAddressDao.ipAddresses.iterator().next().getDomainName().contains("upenn.edu"));
    
    assertEquals(1, TfMemoryUserDao.users.size());
    TwoFactorUser twoFactorUser = TfMemoryUserDao.users.iterator().next();
    assertEquals("jsmith", twoFactorUser.getLoginid());
    assertTrue(!StringUtils.isBlank(twoFactorUser.getLoginid()));
    assertTrue(!StringUtils.isBlank(twoFactorUser.getTwoFactorSecretTempUnencrypted()));
    
    assertEquals(0, twoFactorUser.getTokenIndex().intValue());
    assertNull(twoFactorUser.getSequentialPassIndex());
    assertNull(twoFactorUser.getSeqPassIndexGivenToUser());
    
    assertEquals(timesRun, TfMemoryAuditDao.audits.size());
    for (int i=0;i<timesRun;i++) {
      assertEquals(TwoFactorAuditAction.OPTIN_TWO_FACTOR_STEP1.name(), TfMemoryAuditDao.audits.get(i).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(i).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
    }
    
    assertEquals(0, TfMemoryBrowserDao.browsers.size());

    assertEquals(1, TfMemoryUserAgentDao.userAgents.size());
    assertEquals(userAgent1, TfMemoryUserAgentDao.userAgents.iterator().next().getUserAgent());
    assertEquals(Boolean.FALSE, TfMemoryUserAgentDao.userAgents.iterator().next().getMobile());
    assertTrue(TfMemoryUserAgentDao.userAgents.iterator().next().getBrowser(), 
        TfMemoryUserAgentDao.userAgents.iterator().next().getBrowser().contains("Chrome"));
    assertTrue(TfMemoryUserAgentDao.userAgents.iterator().next().getOperatingSystem(), 
        TfMemoryUserAgentDao.userAgents.iterator().next().getOperatingSystem().contains("Windows"));

    assertEquals(0, TfMemoryServiceProviderDao.serviceProviders.size());
  }

  /**
   *
   */
  public void testOptinTestSubmit() {
    
    TwoFactorRequestContainer twoFactorRequestContainer = new TwoFactorRequestContainer();
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";

    //########################### test that the temp pass has been generated
    //twoFactorRequestContainer.setError("Inconsistent state, please start over");
    //return OptinTestSubmitView.index;

    twoFactorRequestContainer.setError(null);
    
    OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, "123456", null, null, false, null, null, null, null);

    assertEquals(OptinTestSubmitView.index, optinTestSubmitView);
    assertTrue(twoFactorRequestContainer.getError(), twoFactorRequestContainer.getError().toLowerCase().contains("inconsistent"));
    assertTrue(twoFactorRequestContainer.getError(), twoFactorRequestContainer.getError().toLowerCase().contains("state"));

    //first we need to optin step 1
    twoFactorRequestContainer.setError(null);
    new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    int auditsSize = TfMemoryAuditDao.audits.size();

    //########################### test that password is sent

    twoFactorRequestContainer.setError(null);
    optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, "", null, null, false, null, null, null, null);

    assertEquals(OptinTestSubmitView.optin, optinTestSubmitView);
    assertTrue(twoFactorRequestContainer.getError(), twoFactorRequestContainer.getError().toLowerCase().contains("password"));
    assertTrue(twoFactorRequestContainer.getError(), twoFactorRequestContainer.getError().toLowerCase().contains("required"));
    
    
    //########################### test that the password is a six digit number
    // twoFactorRequestContainer.setError("Password must be a 6 digit number");

    int index = 0;
    for (String pass : new String[]{"1234", "1234567", "12345a", "abcdef"}) {
    
      twoFactorRequestContainer.setError(null);
      optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
          twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, pass, null, null, false, null, null, null, null);
  
      assertEquals(index + ": ", OptinTestSubmitView.optin, optinTestSubmitView);
      assertTrue(index + ": " + twoFactorRequestContainer.getError(), twoFactorRequestContainer.getError().toLowerCase().contains("password"));
      index++;
    }
    
    //########################### test that password is correct

    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
    String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
    byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
    
    index = 0;
    long timeDiv30 = System.currentTimeMillis()/30000;
    int[] potentialPasses = new int[]{twoFactorLogic.hotpPassword(key, 1),
        twoFactorLogic.hotpPassword(key, 2), twoFactorLogic.hotpPassword(key, 3),
        twoFactorLogic.totpPassword(key, timeDiv30),
        twoFactorLogic.totpPassword(key, timeDiv30-1),
        twoFactorLogic.totpPassword(key, timeDiv30+1),
        twoFactorLogic.totpPassword(key, timeDiv30-2),
        twoFactorLogic.totpPassword(key, timeDiv30+2)
        };
    for (int pass : potentialPasses) {
      
      twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
      twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
      
      String passString = Integer.toString(pass);
      passString = StringUtils.leftPad(passString, 6, '0');

      twoFactorRequestContainer.setError(null);
      optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
          twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, passString, null, null, false, null, null, null, null);

      assertEquals(index + ": " + optinTestSubmitView.name() 
          + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
          OptinTestSubmitView.optinSuccess, optinTestSubmitView);
      assertTrue(index + ": " + twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));

      assertEquals(auditsSize+ 1 + index, TfMemoryAuditDao.audits.size());
      assertEquals(TwoFactorAuditAction.OPTIN_TWO_FACTOR.name(), TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));

      assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
      assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));

      index++;
    }
    
    assertEquals(1, TfMemoryUserDao.users.size());
    TwoFactorUser twoFactorUser = TfMemoryUserDao.users.iterator().next();
    assertEquals("jsmith", twoFactorUser.getLoginid());
    assertTrue(!StringUtils.isBlank(twoFactorUser.getLoginid()));
    assertTrue(!StringUtils.isBlank(twoFactorUser.getTwoFactorSecretUnencrypted()));
    
    
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    TfMemoryDaoFactory.clear();
  }

  /**
   *
   */
  public void testOptout() {
    TwoFactorRequestContainer twoFactorRequestContainer = new TwoFactorRequestContainer();
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
    OptinTestSubmitView optinTestSubmitView = null;
    
    int index = -1;
    
    //first we need to optin step 1
    twoFactorRequestContainer.setError(null);
    new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    int auditsSize = TfMemoryAuditDao.audits.size();
  
    //########################### test that password is correct
  
    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
    String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
    byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
    
    index = 0;
    long timeDiv30 = System.currentTimeMillis()/30000;
    
    int pass = twoFactorLogic.totpPassword(key, timeDiv30);
    
    String passString = Integer.toString(pass);
    passString = StringUtils.leftPad(passString, 6, '0');

    twoFactorRequestContainer.setError(null);
    optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, 
        passString, null, null, false, null, null, null, null);
  
    assertEquals(index + ": " + optinTestSubmitView.name() 
        + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
        OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    assertTrue(index + ": " + twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
  
    assertEquals(auditsSize+ 1 + index, TfMemoryAuditDao.audits.size());
    assertEquals(TwoFactorAuditAction.OPTIN_TWO_FACTOR.name(), TfMemoryAuditDao.audits.get(auditsSize).getAction());
    //within the last minute
    assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));

    assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
    assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
    
    //############################### opt out
    
    new UiMain().optoutLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    assertEquals("You were opted out of two factor", twoFactorRequestContainer.getError());
    
    //############################### try again should already be out
    
    new UiMain().optoutLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    assertEquals("Warning: you were not opted in to two factor", twoFactorRequestContainer.getError());
    
  }

  /**
   *
   */
  public void testAudit() {
    
    TwoFactorRequestContainer twoFactorRequestContainer = new TwoFactorRequestContainer();
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
    new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
    
    byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
    
    long timeDiv30 = System.currentTimeMillis()/30000;

    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
    
    int auditsSize = -1;
    
    {
      int pass = twoFactorLogic.totpPassword(key, timeDiv30);
        
      twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
      twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
      
      String passString = Integer.toString(pass);
      passString = StringUtils.leftPad(passString, 6, '0');
    
      twoFactorRequestContainer.setError(null);
  
      auditsSize = TfMemoryAuditDao.audits.size();
  
      OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
          twoFactorRequestContainer, "jsmith", "130.91.219.176", 
          userAgent1, passString, null, null, false, null, null, null, null);
    
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

    //check audits
    new UiMain().userAuditsLogic(TfMemoryDaoFactory.getFactory(), twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    assertTrue(twoFactorRequestContainer.getTwoFactorAuditContainer().getTwoFactorAuditViews().size() > 0);
    assertEquals(twoFactorRequestContainer.getTwoFactorAuditContainer()
        .getTwoFactorAuditViews().size(), twoFactorRequestContainer.getTwoFactorAuditContainer()
        .getTwoFactorAuditViewsTotalCount());
    assertEquals(twoFactorRequestContainer.getTwoFactorAuditContainer()
        .getTwoFactorAuditViews().size(), twoFactorRequestContainer.getTwoFactorAuditContainer()
        .getTwoFactorAuditViewsDisplayedCount());
    
    
  }

  /**
   *
   */
  public void testShowPrintedCodes() {
    
    TwoFactorRequestContainer twoFactorRequestContainer = new TwoFactorRequestContainer();
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
    //first we need to optin step 1
    twoFactorRequestContainer.setError(null);
    new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);
    
    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();
    
    int auditsSize = TfMemoryAuditDao.audits.size();
  
    //########################### test that password is correct
  
    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
    String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
    byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);
    
    int index = 0;
    long timeDiv30 = System.currentTimeMillis()/30000;
    int pass = twoFactorLogic.totpPassword(key, timeDiv30);
    twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
    twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
    
    String passString = Integer.toString(pass);
    passString = StringUtils.leftPad(passString, 6, '0');

    twoFactorRequestContainer.setError(null);
    OptinTestSubmitView optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, 
        passString, null, null, false, null, null, null, null);

    assertEquals(index + ": " + optinTestSubmitView.name() 
        + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
        OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    assertTrue(index + ": " + twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));

    assertEquals(auditsSize+ 1 + index, TfMemoryAuditDao.audits.size());
    assertEquals(TwoFactorAuditAction.OPTIN_TWO_FACTOR.name(), TfMemoryAuditDao.audits.get(auditsSize).getAction());
    //within the last minute
    assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));

    assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
    assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
    assertEquals(0, twoFactorUserLoggedIn.getTokenIndex().intValue());
    assertNull(TwoFactorServerUtils.toStringForLog(twoFactorUserLoggedIn.getSequentialPassIndex()), twoFactorUserLoggedIn.getSequentialPassIndex());
    assertNull(twoFactorUserLoggedIn.getSeqPassIndexGivenToUser());

    boolean result = new UiMain().showOneTimeCodesLogic(TfMemoryDaoFactory.getFactory(), twoFactorRequestContainer, 
        "jsmith", "130.91.219.176", userAgent1);
    
    assertTrue(result);
    
    assertEquals(0, twoFactorUserLoggedIn.getTokenIndex().intValue());
    assertEquals(500001L, twoFactorUserLoggedIn.getSequentialPassIndex().intValue());
    assertEquals(500020L, twoFactorUserLoggedIn.getSeqPassIndexGivenToUser().intValue());
    
    
  }

  /**
   *
   */
  public void testUntrustBrowsers() {
    
    TfMemoryDaoFactory daoFactory = TfMemoryDaoFactory.getFactory();

    String browserUserCookieUuid = null;

    TwoFactorRequestContainer twoFactorRequestContainer = new TwoFactorRequestContainer();
    
    String userAgent1 = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.97 Safari/537.11";
  
    OptinTestSubmitView optinTestSubmitView = null;
    
    int index = -1;
    
    int sleepTimeAfterCall = 200;

    
    //first we need to optin step 1
    twoFactorRequestContainer.setError(null);
    new UiMain().optinLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, null);

    TwoFactorUser twoFactorUserLoggedIn = twoFactorRequestContainer.getTwoFactorUserLoggedIn();

    int auditsSize = TfMemoryAuditDao.audits.size();

    //########################### test that password is correct

    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
    String twoFactorSecretTempUnencrypted = twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted();
    byte[] key = new Base32().decode(twoFactorSecretTempUnencrypted);

    index = 0;
    long timeDiv30 = System.currentTimeMillis()/30000;

    int pass = twoFactorLogic.totpPassword(key, timeDiv30);

    String passString = Integer.toString(pass);
    passString = StringUtils.leftPad(passString, 6, '0');

    twoFactorRequestContainer.setError(null);
    optinTestSubmitView = new UiMain().optinTestSubmitLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1, 
        passString, null, null, false, null, null, null, null);

    assertEquals(index + ": " + optinTestSubmitView.name() 
        + ", " + twoFactorRequestContainer.getError() + ", " + passString, 
        OptinTestSubmitView.optinSuccess, optinTestSubmitView);
    assertTrue(index + ": " + twoFactorRequestContainer.getError(), StringUtils.isBlank(twoFactorRequestContainer.getError()));
  
    assertEquals(auditsSize+ 1 + index, TfMemoryAuditDao.audits.size());
    assertEquals(TwoFactorAuditAction.OPTIN_TWO_FACTOR.name(), TfMemoryAuditDao.audits.get(auditsSize).getAction());
    //within the last minute
    assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
    assertTrue(StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretTempUnencrypted()));
    assertTrue(!StringUtils.isBlank(twoFactorUserLoggedIn.getTwoFactorSecretUnencrypted()));
    
    //############################### trust a browser

    {
      pass = twoFactorLogic.totpPassword(key, timeDiv30+2);

      //lets retrieve first
      twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
      twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
      twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);

      twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
      
      passString = Integer.toString(pass);
      passString = StringUtils.leftPad(passString, 6, '0');

      auditsSize = TfMemoryAuditDao.audits.size();
      int servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();

      assertEquals(0, servicesSize);
      
      TfCheckPasswordRequest tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
        .assignTrustedBrowser(true)
        .assignUsername("jsmith").assignTwoFactorPassUnencrypted(passString)
        .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
      
      TfCheckPasswordResponse tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);

      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNotNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());

      browserUserCookieUuid = tfCheckPasswordResponse.getChangeUserBrowserUuid();

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
      assertTrue(tfCheckPasswordResponse.getUserBrowserUuidIsNew() != null && tfCheckPasswordResponse.getUserBrowserUuidIsNew());
      assertNull(tfCheckPasswordResponse.getWarning(), tfCheckPasswordResponse.getWarning());
      
      assertEquals(auditsSize+1, TfMemoryAuditDao.audits.size());
      
      assertEquals(TwoFactorAuditAction.AUTHN_TWO_FACTOR.name(), 
          TfMemoryAuditDao.audits.get(auditsSize).getAction());
      //within the last minute
      assertTrue(TfMemoryAuditDao.audits.get(auditsSize).getTheTimestamp() > System.currentTimeMillis() - (1000 * 60));
  
      assertEquals(servicesSize + 1, TfMemoryServiceProviderDao.serviceProviders.size());
      assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(0).getServiceProviderId());
      assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(0).getServiceProviderName());

    }

    //######################### test that opted in, trusted browser already, and no pass is ok
    {
      pass = twoFactorLogic.totpPassword(key, timeDiv30+2);

      //lets retrieve first
      twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
      twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
      twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
      twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
      
      passString = Integer.toString(pass);
      passString = StringUtils.leftPad(passString, 6, '0');

      auditsSize = TfMemoryAuditDao.audits.size();
      int servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();

      assertEquals(1, servicesSize);
      
      TfCheckPasswordRequest tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
        .assignUsername("jsmith")
        .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
      
      TfCheckPasswordResponse tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);

     
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNull(browserUserCookieUuid + ", " + tfCheckPasswordResponse.getChangeUserBrowserUuid(), tfCheckPasswordResponse.getChangeUserBrowserUuid());
      
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      
      assertNotNull(tfCheckPasswordResponse.getWhenTrusted());
      
      assertEquals(TwoFactorServerUtils.convertFromIso8601(tfCheckPasswordResponse.getWhenTrusted()).getTime(), 
          TfMemoryBrowserDao.browsers.get(0).getWhenTrusted());
      
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
      assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(0).getServiceProviderId());
      assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(0).getServiceProviderName());

    }

    //############################ untrust browsers
    
    new UiMain().untrustBrowsersLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1);
    
    assertEquals("Your 1 previously trusted browser(s) are now untrusted", twoFactorRequestContainer.getError());
    
    //########################### try to use trusted browser, should fail
    
    //######################### test that opted in, trusted browser already, and no pass is ok
    {
      pass = twoFactorLogic.totpPassword(key, timeDiv30+2);

      //lets retrieve first
      twoFactorUserLoggedIn = TwoFactorUser.retrieveByLoginid(TfMemoryDaoFactory.getFactory(), twoFactorUserLoggedIn.getLoginid());
      twoFactorUserLoggedIn.setTwoFactorSecretTempUnencrypted(twoFactorSecretTempUnencrypted);
      twoFactorUserLoggedIn.setLastTotpTimestampUsed(null);
      twoFactorUserLoggedIn.store(TfMemoryDaoFactory.getFactory());
      
      passString = Integer.toString(pass);
      passString = StringUtils.leftPad(passString, 6, '0');

      auditsSize = TfMemoryAuditDao.audits.size();
      int servicesSize = TfMemoryServiceProviderDao.serviceProviders.size();

      assertEquals(1, servicesSize);
      
      TfCheckPasswordRequest tfCheckPasswordRequest = new TfCheckPasswordRequest().assignUserBrowserUuidUnencrypted(browserUserCookieUuid)
        .assignUsername("jsmith")
        .assignBrowserUserAgent(userAgent1).assignServiceId("someTestServiceId").assignServiceName("someTestServiceName");
      
      TfCheckPasswordResponse tfCheckPasswordResponse = TfRestLogic.checkPasswordLogic(daoFactory, tfCheckPasswordRequest);

     
      //let audit happen
      TwoFactorServerUtils.sleep(sleepTimeAfterCall);

      assertNull(tfCheckPasswordResponse.getChangeUserBrowserUuid());
      assertNull(tfCheckPasswordResponse.getDebugMessage(), tfCheckPasswordResponse.getDebugMessage());
      assertNull(tfCheckPasswordResponse.getErrorMessage(), tfCheckPasswordResponse.getErrorMessage());
      
      assertNull(tfCheckPasswordResponse.getWhenTrusted());
      assertEquals(1, TfMemoryBrowserDao.browsers.size());
      assertEquals(0L, TfMemoryBrowserDao.browsers.get(0).getWhenTrusted());
      
      assertEquals(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name(), tfCheckPasswordResponse.getResultCode());
      assertEquals("service does not require two factor, user enrolled in two factor, browser was not previously trusted",
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
      assertEquals("someTestServiceId", TfMemoryServiceProviderDao.serviceProviders.get(0).getServiceProviderId());
      assertEquals("someTestServiceName", TfMemoryServiceProviderDao.serviceProviders.get(0).getServiceProviderName());

    }

    //############################### try again should already be untrusted
    
    new UiMain().untrustBrowsersLogic(TfMemoryDaoFactory.getFactory(), 
        twoFactorRequestContainer, "jsmith", "130.91.219.176", userAgent1);
    
    assertEquals("Your 0 previously trusted browser(s) are now untrusted", twoFactorRequestContainer.getError());
    
  }

}
