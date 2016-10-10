package org.openTwoFactor.server.ws.rest;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.TwoFactorCheckPass;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditAction;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.duo.DuoLog;
import org.openTwoFactor.server.exceptions.TfStaleObjectStateException;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.j2ee.TwoFactorFilterJ2ee;
import org.openTwoFactor.server.j2ee.TwoFactorRestServlet;
import org.openTwoFactor.server.ui.beans.TextContainer;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorPassResult;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordRequest;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordResponse;
import org.openTwoFactor.server.ws.corebeans.TfCheckPasswordResponseCode;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;


/**
 * logic for web service
 */
public class TfRestLogic {

  /**
   * keep a list of username and minute of day to the number of times the WS has been called for that minute
   */
  private static ExpirableCache<MultiKey, Integer> rateLimitStats = new ExpirableCache<MultiKey, Integer>(5);
  
  /**
   * semaphore to synchronize on
   */
  private static final Object rateLimitSemaphore = new Object();
  
  /**
   * see if rate limiting for user
   * @param userName
   * @return true if rate limiting for user
   */
  private static boolean rateLimit(String userName) {

    //cant deal with this
    if (StringUtils.isBlank(userName)) {
      return false;
    }
    
    //# if we should rate limit per user being checked (note, this happens per node in a cluster)
    //twoFactorServer.ws.rateLimit = true
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.ws.rateLimit", true)) {
      //dont rate limit
      return false;
    }
    
    //see if ignore this user
    if (TwoFactorServerConfig.retrieveConfig().ignoreRateLimitOnUsers().contains(userName)) {
      return false;
    }
    
    //
    //# this should be just more than the expected rate the a user would authn (per node)
    //twoFactorServer.ws.rateLimitUserTxPerMinute = 10
    int maxTxPerMinutePerUser = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.ws.rateLimitUserTxPerMinute", 10);
    Calendar calendar = new GregorianCalendar();
    MultiKey userMinute = new MultiKey(userName, calendar.get(Calendar.MINUTE) );
    
    //synchronize so we get an exact count and so the cache works correctly
    synchronized(rateLimitSemaphore) {
      Integer hitsInMinute = rateLimitStats.get(userMinute);
      if (hitsInMinute == null) {
        hitsInMinute = 0;
      }
      rateLimitStats.put(userMinute, hitsInMinute+1);
      return hitsInMinute > maxTxPerMinutePerUser;
    }

  }
  
  /**
   * populate the check password request from request params
   * @param tfCheckPasswordRequest
   * @param params
   */
  private static void populateTfCheckPasswordRequest(TfCheckPasswordRequest tfCheckPasswordRequest, Map<String, String> params) {
    {
      //browserUserAgent: whatever is sent from browser.  Note, if this changes, the twoFactorService could invalidate the uuid...
      String browserUserAgent = params.get("browserUserAgent");
      tfCheckPasswordRequest.assignBrowserUserAgent(browserUserAgent);
    }

    {
      //debug: true|false If true or if a param is set on the server, then a debug message will be returned
      String debugString = params.get("debug");
      
      Boolean debug = TwoFactorServerUtils.booleanObjectValue(debugString);
      
      tfCheckPasswordRequest.assignDebug(debug);
    }

    {
      //requireReauth: true|false if the service requires reauth
      String requireReauthString = params.get("requireReauth");

      Boolean requireReauth = TwoFactorServerUtils.booleanObjectValue(requireReauthString);

      tfCheckPasswordRequest.assignRequireReauth(requireReauth);
    }
    
    {
      String requireTwoFactorString = params.get("requireTwoFactor");
      
      Boolean requireTwoFactor = TwoFactorServerUtils.booleanObjectValue(requireTwoFactorString);
      
      tfCheckPasswordRequest.assignRequireTwoFactor(requireTwoFactor);
    }

    {
      String duoDontPushString = params.get("duoDontPush");
      
      Boolean duoDontPush = TwoFactorServerUtils.booleanObjectValue(duoDontPushString);
      
      tfCheckPasswordRequest.assignDuoDontPush(duoDontPush);
    }

    {
      //serviceId: is some unchanging ID of the service which is requesting authentication
      String serviceId = params.get("serviceId");
      tfCheckPasswordRequest.assignServiceId(serviceId);
    }

    {
      //serviceName: is a friendly service name requesting authentication that could be shown on screen, this could change
      String serviceName = params.get("serviceName");
      tfCheckPasswordRequest.assignServiceName(serviceName);
    }

    {
      //spRequiredFactors: ABC,DEF,GHI Comma separated factors required from the SP
      String spRequiredFactors = params.get("spRequiredFactors");
      
      tfCheckPasswordRequest.assignSpRequiredFactors(spRequiredFactors);
    }

    {
      //trustedBrowser: true|false depending on what the user selected
      String trustedBrowserString = params.get("trustedBrowser");
      
      Boolean trustedBrowser = TwoFactorServerUtils.booleanObjectValue(trustedBrowserString);
      
      tfCheckPasswordRequest.assignTrustedBrowser(trustedBrowser);
    }

    {
      //twoFactorPass: the two factor password that the user entered.  This could be blank or omitted if the user did not enter one
      String twoFactorPass = params.get("twoFactorPass");
      tfCheckPasswordRequest.assignTwoFactorPassUnencrypted(twoFactorPass);
    }

    {
      //userUuid: value is a uuid which is assigned to the user e.g. by cookie
      String userBrowserUuid = params.get("userBrowserUuid");
      tfCheckPasswordRequest.assignUserBrowserUuidUnencrypted(userBrowserUuid);
    }

    {
      //userIpAddress: 1.2.3.4 or ipv6, this is generally used for logging
      String userIpAddress = params.get("userIpAddress");
      tfCheckPasswordRequest.assignUserIpAddress(userIpAddress);
    }

    {
      //username: pennname of the authenticated user
      String username = params.get("username");

      //before resolving
      String originalUserName = username;
      
      tfCheckPasswordRequest.assignOriginalUsername(originalUserName);

      boolean rateLimit = rateLimit(username);
      
      tfCheckPasswordRequest.assignRateLimit(rateLimit);
      
      //dont lookup the username if we are rate limiting
      if (!rateLimit) {
        
        //see if we need to resolve the subject id
        if (!StringUtils.isBlank(username) 
            && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.resolveOnWsSubject", true)) {
          username = TfSourceUtils.resolveSubjectId(TfSourceUtils.mainSource(), username, false);
        }
        
        tfCheckPasswordRequest.assignUsername(username);
      }
    }
    
    {
      HttpServletRequest httpServletRequest = TwoFactorFilterJ2ee.retrieveHttpServletRequest();
      if (httpServletRequest != null) {
        tfCheckPasswordRequest.assignClientSourceIpAddress(httpServletRequest.getRemoteAddr());
      }
    }
    {
      tfCheckPasswordRequest.assignClientUsername(TwoFactorRestServlet.retrievePrincipalLoggedIn());
    }
    
    tfCheckPasswordRequest.assignSubjectSource(TfSourceUtils.mainSource());

  }
  
  /**
   * check a password
   * @param twoFactorDaoFactory 
   * @param params
   * @return the bean
   */
  public static TfCheckPasswordResponse checkPassword(TwoFactorDaoFactory twoFactorDaoFactory, Map<String, String> params) {

    TwoFactorRestServlet.assertLoggedOnPrincipalTfServer();
    
    TfCheckPasswordRequest tfCheckPasswordRequest = new TfCheckPasswordRequest();

    populateTfCheckPasswordRequest(tfCheckPasswordRequest, params);
    
    return checkPasswordLogic(twoFactorDaoFactory, tfCheckPasswordRequest);
  }

  /**
   * since we cant store tx ids to the database in readonly mode, this is the map cache of browser id to duo tx id
   */
  private static ExpirableCache<String, String> readonlyBrowserIdToDuoTxIdTemp = new ExpirableCache<String, String>();
  
  /**
   * @param twoFactorDaoFactory data access
   * @param tfCheckPasswordRequest 
   * @return the response
   */
  public static TfCheckPasswordResponse checkPasswordLogic(
      TwoFactorDaoFactory twoFactorDaoFactory, TfCheckPasswordRequest tfCheckPasswordRequest) {

    Map<String, Object> trafficLogMap = new LinkedHashMap<String, Object>();
    
    long start = System.nanoTime();
    
    TfCheckPasswordResponse tfCheckPasswordResponse = new TfCheckPasswordResponse();
    
    try {
      
      trafficLogMap.put("originalUsername", tfCheckPasswordRequest.getOriginalUsername());
      
      //if set for everyone, or set for this request
      boolean debug = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.debugAllRequests", false) 
        || (tfCheckPasswordRequest.getDebug() != null && tfCheckPasswordRequest.getDebug());

      if (tfCheckPasswordRequest.isRateLimit()) {
        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage(null);
        tfCheckPasswordResponse.setResponseMessage("User is rate limited since two many requests per minute on this node");
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
        tfCheckPasswordResponse.setTwoFactorUserAllowed(false);
        tfCheckPasswordResponse.setTwoFactorRequired(true);
        //note, they really arent, but we need to send them to the screen since we arent checking
        tfCheckPasswordResponse.setUserEnrolledInTwoFactor(true);
        tfCheckPasswordResponse.setSuccess(true);
        tfCheckPasswordResponse.setRateLimitedUser(true);

        trafficLogMap.put("userAllowed", false);
        trafficLogMap.put("success", true);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name());
        trafficLogMap.put("rateLimit", true);

        return tfCheckPasswordResponse;
      }

      tfCheckPasswordResponse.setRateLimitedUser(false);
      
      String username = tfCheckPasswordRequest.getUsername();
      
      trafficLogMap.put("username", username);
      
      DuoLog.assignUsername(StringUtils.defaultIfEmpty(tfCheckPasswordRequest.getOriginalUsername(), username));
      
      String twoFactorPassUnencrypted = tfCheckPasswordRequest.getTwoFactorPass();


      logTwoFactorLogic(tfCheckPasswordRequest, trafficLogMap, tfCheckPasswordResponse, debug);

      String ipAddress = StringUtils.trimToNull(tfCheckPasswordRequest.getUserIpAddress());
      String userAgent = StringUtils.trimToNull(tfCheckPasswordRequest.getBrowserUserAgent());
      String serviceProviderId = StringUtils.trimToNull(tfCheckPasswordRequest.getServiceId());
      String serviceProviderName = StringUtils.trimToNull(tfCheckPasswordRequest.getServiceName());
      
      //############# if no username
      if (StringUtils.isBlank(username)) {
        
        String responseMessage = "Invalid request, username required, not sent in or not resolvable as subject";
        TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
            TwoFactorAuditAction.AUTHN_ERROR, ipAddress, userAgent, 
            null, responseMessage, serviceProviderId, serviceProviderName, null, null);
  
        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage("username required");
        tfCheckPasswordResponse.setResponseMessage(responseMessage);
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.INVALID_REQUEST.name());
        tfCheckPasswordResponse.setSuccess(false);
        tfCheckPasswordResponse.setTwoFactorUserAllowed(null);

        trafficLogMap.put("success", false);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.INVALID_REQUEST.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_ERROR.name());
        trafficLogMap.put("error", responseMessage);
        
        return tfCheckPasswordResponse;
      }

      //see if there is a testing error or timeout
      TwoFactorRestServlet.testingErrorOrTimeout(tfCheckPasswordRequest.getSubjectSource(), username);
      
      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginidOrCreate(twoFactorDaoFactory, username);
  
      //############# if no serviceId but yes to serviceName
      if (StringUtils.isBlank(tfCheckPasswordRequest.getServiceId()) && !StringUtils.isBlank(tfCheckPasswordRequest.getServiceName())) {
        
        String responseMessage = "Invalid request, if serviceName is sent, then serviceId must be sent";
        TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
            TwoFactorAuditAction.AUTHN_ERROR, ipAddress, userAgent, 
            twoFactorUser.getUuid(), responseMessage, serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), null);
  
        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage("serviceId is required if serviceName is sent");
        tfCheckPasswordResponse.setResponseMessage(responseMessage);
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.INVALID_REQUEST.name());
        tfCheckPasswordResponse.setSuccess(false);
        tfCheckPasswordResponse.setTwoFactorUserAllowed(null);
        
        trafficLogMap.put("success", false);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.INVALID_REQUEST.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_ERROR.name());
        trafficLogMap.put("error", responseMessage);

        return tfCheckPasswordResponse;
      }
  
      //############### see if two factor required by request
      boolean twoFactorRequired = false;
      boolean serviceProviderRequiresTwoFactor = false;
      boolean serviceProviderForbidsTwoFactor = false;
      boolean optedInConsideringRequired = false;
      
      //############## see if serviceId is configured to forbid two factor
      if (!StringUtils.isBlank(tfCheckPasswordRequest.getServiceId())) {
        String configServiceIdsThatForbidTwoFactor = TwoFactorServerConfig.retrieveConfig().propertyValueString(
            "twoFactorServer.serviceProviderIdsThatForbidTwoFactor");
        if (!StringUtils.isBlank(configServiceIdsThatForbidTwoFactor)) {
          Set<String> configServiceIdsThatForbidTwoFactorSet = TwoFactorServerUtils.splitTrimToSet(configServiceIdsThatForbidTwoFactor, ",");
          if (configServiceIdsThatForbidTwoFactorSet.contains(tfCheckPasswordRequest.getServiceId())) {
            serviceProviderForbidsTwoFactor = true;
            
            trafficLogMap.put("serviceIdForbidsTwoFactorInConfig", true);
            if (debug) {
              tfCheckPasswordResponse.appendDebug("serviceId forbids two factor in config");
            }

          }
        }
      }

      if (!serviceProviderForbidsTwoFactor) {
        
        if (tfCheckPasswordRequest.getRequireTwoFactor() != null && tfCheckPasswordRequest.getRequireTwoFactor()) {
          twoFactorRequired = true;
          serviceProviderRequiresTwoFactor = true;
        }
  
        //############## see if serviceId is configured to require two factor
        if (!StringUtils.isBlank(tfCheckPasswordRequest.getServiceId())) {
          String configServiceIdsThatRequireTwoFactor = TwoFactorServerConfig.retrieveConfig().propertyValueString(
              "twoFactorServer.serviceProviderIdsThatRequireTwoFactor");
          if (!StringUtils.isBlank(configServiceIdsThatRequireTwoFactor)) {
            Set<String> configServiceIdsThatRequireTwoFactorSet = TwoFactorServerUtils.splitTrimToSet(configServiceIdsThatRequireTwoFactor, ",");
            if (configServiceIdsThatRequireTwoFactorSet.contains(tfCheckPasswordRequest.getServiceId())) {
              twoFactorRequired = true;
              serviceProviderRequiresTwoFactor = false;
              
              trafficLogMap.put("serviceIdRequiresTwoFactorInConfig", true);
              if (debug) {
                tfCheckPasswordResponse.appendDebug("serviceId requires two factor in config");
              }
  
            }
          }
        }
        
        //############## see if factor requires two factor
        @SuppressWarnings("unused")
        boolean spRequiredFactorRequiresTwoFactor = false;
        if (!StringUtils.isBlank(tfCheckPasswordRequest.getSpRequiredFactors())) {
          String configPropertiesThatRequireTwoFactorString = TwoFactorServerConfig.retrieveConfig().propertyValueString(
              "twoFactorServer.factorsThatRequireTwoFactor");
          if (!StringUtils.isBlank(configPropertiesThatRequireTwoFactorString)) {
            Set<String> configPropertiesThatRequireTwoFactorStringSet = TwoFactorServerUtils
              .splitTrimToSet(configPropertiesThatRequireTwoFactorString, ",");
            Set<String> spRequiredFactorsSet = TwoFactorServerUtils
              .splitTrimToSet(tfCheckPasswordRequest.getSpRequiredFactors(), ",");
            for (String spRequiredFactor : spRequiredFactorsSet) {
              if (configPropertiesThatRequireTwoFactorStringSet.contains(spRequiredFactor)) {
                if (debug) {
                  tfCheckPasswordResponse.appendDebug("spRequiredFactor: " + spRequiredFactor + " requires two factor per WS config");
                }
                twoFactorRequired = true;
                spRequiredFactorRequiresTwoFactor = true;
                serviceProviderRequiresTwoFactor = true;
              }
            }
          }
        }
      }
      tfCheckPasswordResponse.setTwoFactorRequired(twoFactorRequired);

      StringBuilder responseMessage = new StringBuilder();

      if (serviceProviderForbidsTwoFactor) {
        TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "service forbids two factor", null);
      } else {
        if (twoFactorRequired) {
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "service requires two factor", null);
        } else {
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "service does not require two factor", null);
        }
      }
      
      tfCheckPasswordResponse.setSuccess(true);

      
      if (twoFactorUser.isOptedIn()) {
        
        boolean optinOnlyForRequiredApplications = TwoFactorServerUtils.booleanValue(twoFactorUser.getOptInOnlyIfRequired(), false);

        if (optinOnlyForRequiredApplications) {
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "user enrolled in two factor for required applications", null);

        } else {
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "user enrolled in two factor", null);
          
        }
        
        if (!twoFactorRequired && optinOnlyForRequiredApplications && StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass())) {
        
          tfCheckPasswordResponse.setUserEnrolledInTwoFactor(false);
          optedInConsideringRequired = false;
          
        } else {
          
          tfCheckPasswordResponse.setUserEnrolledInTwoFactor(true);
          optedInConsideringRequired = true;
        }

        if (optedInConsideringRequired) {
          if (!serviceProviderForbidsTwoFactor) {
            twoFactorRequired = true;
          }
        }
        
      } else {
  
        tfCheckPasswordResponse.setUserEnrolledInTwoFactor(false);
        TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "user not enrolled in two factor", null);
  
      }
      
      tfCheckPasswordResponse.setTwoFactorRequired(twoFactorRequired);
      tfCheckPasswordResponse.setUserBrowserUuidIsNew(false);

      if (serviceProviderForbidsTwoFactor && StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass())) {
        
        TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
            TwoFactorAuditAction.AUTHN_TWO_FACTOR_FORBIDDEN, ipAddress, userAgent, 
            twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), null);

        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage(null);
        tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.TWO_FACTOR_FORBIDDEN.name());
        tfCheckPasswordResponse.setTwoFactorUserAllowed(true);

        trafficLogMap.put("userAllowed", true);
        trafficLogMap.put("success", true);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.TWO_FACTOR_FORBIDDEN.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_TWO_FACTOR_FORBIDDEN.name());

        return tfCheckPasswordResponse;
        
        
      }
      
      //###############  if not opted in
      if (!optedInConsideringRequired) {
  
        if (twoFactorRequired) {
  
          TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
              TwoFactorAuditAction.AUTHN_WRONG_PASSWORD, ipAddress, userAgent, 
              twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), null);
  
          tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
          tfCheckPasswordResponse.setErrorMessage(null);
          tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
          tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
          tfCheckPasswordResponse.setTwoFactorUserAllowed(false);

          trafficLogMap.put("userAllowed", false);
          trafficLogMap.put("success", true);
          trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
          trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name());

          return tfCheckPasswordResponse;
  
        }
  
        if (StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass())) {
  
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "password not sent", null);
  
        } else {
          
          tfCheckPasswordResponse.setTwoFactorPasswordCorrect(false);
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "password was sent", null);
  
        }
        
        //lets see if password is sent
        if (!StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass())) {
          
          TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
              TwoFactorAuditAction.AUTHN_WRONG_PASSWORD, ipAddress, userAgent, 
              twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), null);
          tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
          tfCheckPasswordResponse.setErrorMessage(null);
          tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
          tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
          tfCheckPasswordResponse.setTwoFactorUserAllowed(false);

          trafficLogMap.put("userAllowed", false);
          trafficLogMap.put("success", true);
          trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
          trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name());

          return tfCheckPasswordResponse;
  
        }
  
        //not opted in, service doesnt require two factor
        TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
             TwoFactorAuditAction.AUTHN_NOT_OPTED_IN, ipAddress, userAgent, 
             twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), null);
        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage(null);
        tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name());
        tfCheckPasswordResponse.setTwoFactorUserAllowed(true);

        trafficLogMap.put("userAllowed", true);
        trafficLogMap.put("success", true);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_NOT_OPTED_IN.name());

        return tfCheckPasswordResponse;
      }
      
      if (!serviceProviderForbidsTwoFactor) {
      
        twoFactorRequired = true;
      }
      tfCheckPasswordResponse.setTwoFactorRequired(twoFactorRequired);

      String secret = twoFactorUser.getTwoFactorSecretUnencrypted();
      String phonePassword = twoFactorUser.getPhoneCodeUnencryptedIfNotExpired();
      
      TwoFactorPassResult twoFactorPassResult = StringUtils.isBlank(twoFactorPassUnencrypted) ?
          new TwoFactorPassResult() :
          checkPassword(twoFactorPassUnencrypted, twoFactorUser, secret, phonePassword);

      {
        //log if we went to Duo or not if we are even configured to try
        Class<? extends TwoFactorCheckPass> checkPassClass = twoFactorPassResult.getTwoFactorCheckPassImplementation();
        trafficLogMap.put("checkPassImpl", checkPassClass == null ? null : checkPassClass.getSimpleName());
      }          
      
      //if phone pass, expire it
      if (twoFactorPassResult.isPhonePass()) {
        twoFactorUser.setDatePhoneCodeSent(null);
        twoFactorUser.setPhoneCodeEncrypted(null);
        twoFactorUser.store(twoFactorDaoFactory);
      }
      //if it was hotp, store this to the DB
      if (twoFactorPassResult.getNextHotpIndex() != null) {
        //dont log this
        twoFactorUser.setSequentialPassIndex(twoFactorPassResult.getNextHotpIndex());
        twoFactorUser.store(twoFactorDaoFactory);
      }
  
      //if it was token, store this to the db
      if (twoFactorPassResult.getNextTokenIndex() != null) {
        twoFactorUser.setTokenIndex(twoFactorPassResult.getNextTokenIndex());
        twoFactorUser.store(twoFactorDaoFactory);
      }
      
      //if it was totp 30, store this to the DB
      if (twoFactorPassResult.getLastTotp30TimestampUsed() != null) {
        //dont log this
        twoFactorUser.setLastTotpTimestampUsed(twoFactorPassResult.getLastTotp30TimestampUsed());
        twoFactorUser.store(twoFactorDaoFactory);
      }
      
      //if it was totp 60, store this to the DB
      if (twoFactorPassResult.getLastTotp60TimestampUsed() != null) {
        //dont log this
        twoFactorUser.setLastTotp60TimestampUsed(twoFactorPassResult.getLastTotp60TimestampUsed());
        twoFactorUser.store(twoFactorDaoFactory);
      }

      //lets get the right user uuid
      String cookieUserUuid = tfCheckPasswordRequest.getUserBrowserUuid();
      boolean needsNewCookieUuid = false;
      TwoFactorBrowser twoFactorBrowser = null;

      boolean performedTrustedBrowserLogic = false;
      boolean browserPreviouslyTrusted = false;
  
      if (StringUtils.isBlank(cookieUserUuid)) {
        needsNewCookieUuid = true;
      } else {
        twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuid(twoFactorDaoFactory, cookieUserUuid, false);
        //if there is no browser
        if (twoFactorBrowser == null) {
          
          String txId = null;
          
          if (HibernateSession.isReadonlyMode() && !StringUtils.isBlank(cookieUserUuid)) {
            
            txId = readonlyBrowserIdToDuoTxIdTemp.get(TwoFactorBrowser.encryptBrowserUserUuid(cookieUserUuid));
          }
          
          //if there is an in memory transactino id
          if (!StringUtils.isBlank(txId)) {
            //fake a browser?
            twoFactorBrowser = new TwoFactorBrowser();
            twoFactorBrowser.setBrowserTrustedUuidUnencrypted(cookieUserUuid);
          } else {
          
            
            if (debug) {
              tfCheckPasswordResponse.appendDebug("cant find active twoFactorBrowser by cookie uuid");
            }
            trafficLogMap.put("foundBrowser", false);
            needsNewCookieUuid = true;
          }
        } else {
          trafficLogMap.put("foundBrowser", true);
          //make sure the username matches up
          if (!StringUtils.equals(twoFactorBrowser.getUserUuid(), twoFactorUser.getUuid())) {
            trafficLogMap.put("userMatchesBrowser", false);
            if (debug) {
              tfCheckPasswordResponse.appendDebug("twoFactorBrowser user uuid doesnt match logged in user");
            }
            needsNewCookieUuid = true;
            
            //lets delete the current one by date
            twoFactorBrowser.deleteByDate(twoFactorDaoFactory);
            
          } else {
            trafficLogMap.put("userMatchesBrowser", true);
            if (debug) {
              tfCheckPasswordResponse.appendDebug("found twoFactorBrowser by cookie uuid");
            }
            browserPreviouslyTrusted = twoFactorBrowser.isTrustedBrowserCalculateDate();
            
            //if this used to be trusted, but it wore off, then issue a new cookie id
            if (!browserPreviouslyTrusted && twoFactorBrowser.isTrustedBrowser()) {
              needsNewCookieUuid = true;
            }
            
          }
        }
      }
  
      tfCheckPasswordResponse.setUserBrowserUuidIsNew(needsNewCookieUuid);
      boolean requestIsTrusted = tfCheckPasswordRequest.getTrustedBrowser() != null && tfCheckPasswordRequest.getTrustedBrowser();
      if (browserPreviouslyTrusted) {
  
        if (tfCheckPasswordRequest.getRequireTwoFactor() != null && tfCheckPasswordRequest.getRequireTwoFactor()) {
          if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.clientRequireTwoFactorAllowTrustedBrowsers", false)) {
            browserPreviouslyTrusted = false;
          }
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "browser was already trusted but two factor code required", null);
          trafficLogMap.put("browserPreviouslyTrustedButTwoFactorCodeRequired", true);
        } else {
          
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "browser was already trusted", null);
          trafficLogMap.put("browserPreviouslyTrusted", true);
        }
        
      
      } else {
  
        TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "browser was not previously trusted", null);
        trafficLogMap.put("browserPreviouslyTrusted", false);
  
      }

      boolean requireReauth = tfCheckPasswordRequest.getRequireReauth() != null 
          && tfCheckPasswordRequest.getRequireReauth() && serviceProviderRequiresTwoFactor;

      boolean storeUser = false;
      
      //if not good so far
      if (optedInConsideringRequired && (!browserPreviouslyTrusted || requireReauth) && (StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass()) 
          || !twoFactorPassResult.isPasswordCorrect())) {
        
        //check duo
        boolean duoRegisterUsers = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duo.registerUsers", true);
        boolean duoPushByDefaultEnabled = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duo.pushByDefaultEnabled", true);

        String browserId = StringUtils.defaultIfEmpty(tfCheckPasswordResponse.getChangeUserBrowserUuid(), cookieUserUuid);

        String timestampBrowserTxId = twoFactorUser.getDuoPushTransactionId();

        //lets see if we are readonly, switch to readonly if needed
        if (HibernateSession.isReadonlyMode() && !StringUtils.isBlank(browserId)) {
          String memoryTimestampBrowserId = readonlyBrowserIdToDuoTxIdTemp.get(TwoFactorBrowser.encryptBrowserUserUuid(browserId));
          timestampBrowserTxId = TwoFactorBrowser.pickCurrentDuoTransactionId(timestampBrowserTxId, memoryTimestampBrowserId);
        }
        
        
        //clear it out - dont clear it out since subsequent requests should ignore push
        if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duoDeletePushTransactionId", true)) {
          if (!StringUtils.isBlank(timestampBrowserTxId)) {
            //clear it out
            twoFactorUser.setDuoPushTransactionId(null);
            if (HibernateSession.isReadonlyMode() && !StringUtils.isBlank(browserId)) {
              readonlyBrowserIdToDuoTxIdTemp.put(TwoFactorBrowser.encryptBrowserUserUuid(browserId), null);
            }
            storeUser = true;
          }
          
        }
        
        if (duoRegisterUsers && duoPushByDefaultEnabled 
            && TwoFactorServerUtils.booleanValue(twoFactorUser.getDuoPushByDefault(), false)
            && !StringUtils.isBlank(twoFactorUser.getDuoPushPhoneId())
            && !TwoFactorServerUtils.booleanValue(tfCheckPasswordRequest.getDuoDontPush(), false)) {
          try {
             
            boolean needsPush = true;
            
            if (!StringUtils.isBlank(timestampBrowserTxId)) {
              
              String[] pieces = TwoFactorServerUtils.splitTrim(timestampBrowserTxId, "__");
              boolean alreadyUsed = TwoFactorServerUtils.length(pieces) == 4;
              
              if (TwoFactorServerUtils.length(pieces) != 3 && !alreadyUsed) {
  
                //clear it out
                if (!TwoFactorServerUtils.isBlank(timestampBrowserTxId)) {
                  twoFactorUser.setDuoPushTransactionId(null);
                  if (HibernateSession.isReadonlyMode() && !StringUtils.isBlank(browserId)) {
                    readonlyBrowserIdToDuoTxIdTemp.put(TwoFactorBrowser.encryptBrowserUserUuid(browserId), null);
                  }
                  storeUser = true;
                }
  
              } else {
                String browserIdFromDb = pieces[1];
                
                if (StringUtils.isBlank(browserId) || !StringUtils.equals(TwoFactorBrowser.encryptBrowserUserUuid(browserId), browserIdFromDb)) {
                  TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "duo push not valid browser id mismatch", null);
                  trafficLogMap.put("duoPushNotValidBrowserIdMismatch", true);
  
                  //clear it out
                  if (!TwoFactorServerUtils.isBlank(timestampBrowserTxId)) {
                    twoFactorUser.setDuoPushTransactionId(null);
                    if (HibernateSession.isReadonlyMode() && !StringUtils.isBlank(browserId)) {
                      readonlyBrowserIdToDuoTxIdTemp.put(TwoFactorBrowser.encryptBrowserUserUuid(browserId), null);
                    }
                    storeUser = true;
                  }
  
                } else {
  
                  if (alreadyUsed) {
                    //see if used recently enough...
  
                    //  # after the push transaction id is used, it can be used again for this many seconds
                    //  # note, this is only used if duoDeletePushTransactionId is false
                    //  duoTransactionIdLastsAfterFirstUseSeconds = 3
                    String timestampString = pieces[3];
                    long timestampLong = TwoFactorServerUtils.longValue(timestampString);
                    int duoTransactionIdLastsAfterFirstUseSeconds = TwoFactorServerConfig.retrieveConfig().propertyValueInt("duoTransactionIdLastsAfterFirstUseSeconds", 3);
                    if (((System.currentTimeMillis() - timestampLong) / 1000) < duoTransactionIdLastsAfterFirstUseSeconds) {

                      
                      //see if trusted browser
                      twoFactorBrowser = trustBrowserLogic(twoFactorDaoFactory, tfCheckPasswordRequest,
                          tfCheckPasswordResponse, debug, twoFactorUser,
                          cookieUserUuid, needsNewCookieUuid, twoFactorBrowser,
                          requestIsTrusted, true);
                      performedTrustedBrowserLogic = true;
                      TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "successful duo push active", null);
                      trafficLogMap.put("duoPushSuccessActive", duoTransactionIdLastsAfterFirstUseSeconds);
  
                      tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
                      tfCheckPasswordResponse.setErrorMessage(null);
                      tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
                      tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.CORRECT_PASSWORD.name());
                      tfCheckPasswordResponse.setTwoFactorUserAllowed(true);
  
                      trafficLogMap.put("userAllowed", true);
                      trafficLogMap.put("success", true);
                      trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.CORRECT_PASSWORD.name());
                      trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_TWO_FACTOR.name());

                      if (storeUser) {
                        twoFactorUser.store(twoFactorDaoFactory);
                      }

                      return tfCheckPasswordResponse;
                    }
  
                    TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "duo push already used timed out", null);
                    trafficLogMap.put("duoPushAlreadyUsedTimedOut", duoTransactionIdLastsAfterFirstUseSeconds);
  
                    //clear it out
                    if (!TwoFactorServerUtils.isBlank(timestampBrowserTxId)) {
                      twoFactorUser.setDuoPushTransactionId(null);
                      if (HibernateSession.isReadonlyMode() && !StringUtils.isBlank(browserId)) {
                        readonlyBrowserIdToDuoTxIdTemp.put(TwoFactorBrowser.encryptBrowserUserUuid(browserId), null);
                      }
                      storeUser = true;
                    }
  
                  } else {
                    
                    //if not already used
                    String timestampString = pieces[0];
  
                    long timestampLong = TwoFactorServerUtils.longValue(timestampString);
                    String txId = pieces[2];
  
                    int pushLastsForSeconds = TwoFactorServerConfig.retrieveConfig().propertyValueInt("duo.pushLastsForSeconds", 60);
                    
                    //push timed out
                    if (((System.currentTimeMillis() - timestampLong) / 1000) > pushLastsForSeconds) {
                      TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "duo push timed out", null);
                      trafficLogMap.put("duoPushTimedOutAfterSeconds", pushLastsForSeconds);
                      
                      //clear it out
                      if (!TwoFactorServerUtils.isBlank(timestampBrowserTxId)) {
                        twoFactorUser.setDuoPushTransactionId(null);
                        if (HibernateSession.isReadonlyMode() && !StringUtils.isBlank(browserId)) {
                          readonlyBrowserIdToDuoTxIdTemp.put(TwoFactorBrowser.encryptBrowserUserUuid(browserId), null);
                        }
                        storeUser = true;
                      }
                    } else {
  
                      //see if valid
                      boolean validPush = false;
                      
                      try {
                        Integer timeoutSeconds = TwoFactorServerConfig.retrieveConfig().propertyValueInt("tfWsDuoPushTimeoutSeconds", 3);
                        if (timeoutSeconds == -1) {
                          timeoutSeconds = null;
                        }
                        validPush = DuoCommands.duoPushSuccess(txId, timeoutSeconds);
                      } catch (RuntimeException re) {
                        //if its a timeout, then validPush is false, else rethrow
                        if (ExceptionUtils.getFullStackTrace(re).toLowerCase().contains("timeout")) {
                          validPush = false;
                        } else {
                          throw re;
                        }
                      }
                      if (validPush) {
  
                        //see if trusted browser
                        twoFactorBrowser = trustBrowserLogic(twoFactorDaoFactory, tfCheckPasswordRequest,
                            tfCheckPasswordResponse, debug, twoFactorUser,
                            cookieUserUuid, needsNewCookieUuid, twoFactorBrowser,
                            requestIsTrusted, true);
                        performedTrustedBrowserLogic = true;
                        //store this timestamp as when it was used
                        String duoPushTransactionId = timestampBrowserTxId + "__" + System.currentTimeMillis();
                        twoFactorUser.setDuoPushTransactionId(duoPushTransactionId);
                        if (HibernateSession.isReadonlyMode()) {
                          //note this field is already hashed
                          readonlyBrowserIdToDuoTxIdTemp.put(twoFactorBrowser.getBrowserTrustedUuid(), duoPushTransactionId);
                        }
                        storeUser = true;
                        
                        TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "duo push valid", null);
                        trafficLogMap.put("duoPushValid", pushLastsForSeconds);
  
                        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
                        tfCheckPasswordResponse.setErrorMessage(null);
                        tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
                        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.CORRECT_PASSWORD.name());
                        tfCheckPasswordResponse.setTwoFactorUserAllowed(true);
  
                        trafficLogMap.put("userAllowed", true);
                        trafficLogMap.put("success", true);
                        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.CORRECT_PASSWORD.name());
                        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_TWO_FACTOR.name());

                        if (storeUser) {
                          twoFactorUser.store(twoFactorDaoFactory);
                        }

                        return tfCheckPasswordResponse;
  
                      }
                      TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "duo push invalid", null);
                      trafficLogMap.put("duoPushInvalid", pushLastsForSeconds);
                      
                      //dontpush again, this one might be valid at some point
                      needsPush = false;
                    }
  
                  }
                  
                }
              }              
            }
            
            if (needsPush) {
              String message = TextContainer.retrieveFromRequest().getText().get("duoPushWebPrompt");
              
              String txId = null;
              
              try {
                Integer timeoutSeconds = TwoFactorServerConfig.retrieveConfig().propertyValueInt("tfWsDuoPushTimeoutSeconds", 3);
                if (timeoutSeconds == -1) {
                  timeoutSeconds = null;
                }
                txId = DuoCommands.duoInitiatePushByPhoneId(twoFactorUser.getDuoUserId(),
                    twoFactorUser.getDuoPushPhoneId(), message, timeoutSeconds);
              } catch (RuntimeException re) {
                //if its a timeout, then no tx id, else rethrow
                if (ExceptionUtils.getFullStackTrace(re).toLowerCase().contains("timeout")) {
                  txId = null;
                } else {
                  throw re;
                }
              }

              if (!StringUtils.isBlank(txId)) {

                //20150622 MCH had trouble with no browserId giving problems
                if (StringUtils.isBlank(browserId)) {
                  
                  twoFactorBrowser = trustBrowserLogic(twoFactorDaoFactory, tfCheckPasswordRequest,
                      tfCheckPasswordResponse, debug, twoFactorUser,
                      cookieUserUuid, needsNewCookieUuid, twoFactorBrowser,
                      requestIsTrusted, false);
                  performedTrustedBrowserLogic = true;
                  if (twoFactorBrowser != null) {

                    browserId = tfCheckPasswordResponse.getChangeUserBrowserUuid();
                    if (StringUtils.isBlank(browserId)) {
                      throw new RuntimeException("Why is browserId null????");
                    }
                    
                  } else {
                    //shouldnt happen
                    throw new RuntimeException("why is two factor browser null????");
                  }

                }
                
                trafficLogMap.put("duoPush", true);
                TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "duo push initiated", null);
                String duoTxId = System.currentTimeMillis() + "__" + TwoFactorBrowser.encryptBrowserUserUuid(browserId) + "__" + txId;
                twoFactorUser.setDuoPushTransactionId(duoTxId);
                if (HibernateSession.isReadonlyMode()) {
                  //note this field is already hashed
                  readonlyBrowserIdToDuoTxIdTemp.put(twoFactorBrowser.getBrowserTrustedUuid(), duoTxId);
                }
                storeUser = true;
              }
            }
          } catch (Exception e) {
            LOG.error("Cant push for user: " + twoFactorUser.getLoginid(), e);
            trafficLogMap.put("duoPushError", true);
            TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "duo push error", null);
          }
        }
        
      }
      //if there is no code,  or it is wrong, then not a trusted browser
      boolean userAuthenticated = !StringUtils.isBlank(twoFactorPassUnencrypted) && twoFactorPassResult != null & twoFactorPassResult.isPasswordCorrect();

      twoFactorBrowser = (twoFactorBrowser != null && performedTrustedBrowserLogic) ? twoFactorBrowser
          : trustBrowserLogic(twoFactorDaoFactory, tfCheckPasswordRequest,
          tfCheckPasswordResponse, debug, twoFactorUser,
          cookieUserUuid, needsNewCookieUuid, twoFactorBrowser,
          requestIsTrusted, userAuthenticated);

      if (twoFactorBrowser != null) {
        trafficLogMap.put("browserUuid", twoFactorBrowser.getUuid());
        trafficLogMap.put("twoFactorBrowser.isTrustedBrowser()", twoFactorBrowser.isTrustedBrowser());
      }
      
      if (storeUser) {
        twoFactorUser.store(twoFactorDaoFactory);
      }
      
      if (!StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass())) {
        if (!twoFactorPassResult.isPasswordCorrect()) {

          trafficLogMap.put("passwordCorrect", false);
          TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "password not correct", null);
          tfCheckPasswordResponse.setTwoFactorPasswordCorrect(false);
    
          TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
                TwoFactorAuditAction.AUTHN_WRONG_PASSWORD, ipAddress, userAgent, 
                twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), 
                twoFactorBrowser == null ? null : twoFactorBrowser.getUuid());
          tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
          tfCheckPasswordResponse.setErrorMessage(null);
          tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
          tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
          tfCheckPasswordResponse.setTwoFactorUserAllowed(false);

          trafficLogMap.put("userAllowed", false);
          trafficLogMap.put("success", true);
          trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.WRONG_PASSWORD.name());
          trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_WRONG_PASSWORD.name());

          return tfCheckPasswordResponse;
          
          
        }
      }
      
      //ok, at this point, the service requires or doesnt require two factor, and the pass was sent and is correct
      if (twoFactorPassResult.isPasswordCorrect()) {

        trafficLogMap.put("passwordCorrect", true);
        TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "password correct", null);
        tfCheckPasswordResponse.setTwoFactorPasswordCorrect(true);

        TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
             TwoFactorAuditAction.AUTHN_TWO_FACTOR, ipAddress, userAgent, 
             twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), 
             twoFactorBrowser == null ? null : twoFactorBrowser.getUuid());
        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage(null);
        tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.CORRECT_PASSWORD.name());
        tfCheckPasswordResponse.setTwoFactorUserAllowed(true);

        trafficLogMap.put("userAllowed", true);
        trafficLogMap.put("success", true);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.CORRECT_PASSWORD.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_TWO_FACTOR.name());

        return tfCheckPasswordResponse;
      }
        
      if (requireReauth) {
        TwoFactorServerUtils.appendIfNotBlank(responseMessage, null, ", ", "require reauthentication", null);
      }
  
      //send this back even if require reauth
      if (StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass()) && browserPreviouslyTrusted) {
        
        //update the browser trust if inactivity date
        if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.browserTrustIsInactivityBased", true)) {
          trafficLogMap.put("previousBrowserTrustedOn", new Date(twoFactorBrowser.getWhenTrusted()).toString());
          {
            //we were getting stale object state right here... loop and make sure it works
            int max = 5;
            for (int i=0;i<max;i++) {
              try {
                if (twoFactorBrowser == null) {
                  twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuid(twoFactorDaoFactory, cookieUserUuid, false);
                }
                twoFactorBrowser.setWhenTrusted(System.currentTimeMillis());
                twoFactorBrowser.store(twoFactorDaoFactory);
                //we are good
                break;
              } catch (TfStaleObjectStateException tsose) {
                LOG.warn("Stale object exception on browser: " + cookieUserUuid);
                if (i < max-1) {
                  twoFactorBrowser = null;
                  TwoFactorServerUtils.sleep(20);
                  //its ok, try again
                } else {
                  //not good, its not working
                  throw tsose; 
                }
              }
            }
          }
          trafficLogMap.put("newBrowserTrustedOn", new Date(twoFactorBrowser.getWhenTrusted()).toString());
        }
        tfCheckPasswordResponse.setWhenTrusted(TwoFactorServerUtils.convertToIso8601(new Date(twoFactorBrowser.getWhenTrusted())));
        trafficLogMap.put("requireReauthOverall", true);
      }
      
      //no password sent in, and trusted browser, ok
      if (!requireReauth && StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass()) && browserPreviouslyTrusted) {
        TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
            TwoFactorAuditAction.AUTHN_TRUSTED_BROWSER, ipAddress, userAgent, 
            twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), 
            twoFactorBrowser == null ? null : twoFactorBrowser.getUuid());
        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage(null);
        tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.TRUSTED_BROWSER.name());
        tfCheckPasswordResponse.setTwoFactorUserAllowed(true);

        trafficLogMap.put("userAllowed", true);
        trafficLogMap.put("success", true);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.TRUSTED_BROWSER.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_TRUSTED_BROWSER.name());

        return tfCheckPasswordResponse;
        
      }
      
      //finally, the service requires two factor, and the pass was not sent, and user opted in
      if (StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass()) && optedInConsideringRequired) {
  
        TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
             TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED, ipAddress, userAgent, 
             twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), 
             twoFactorBrowser == null ? null : twoFactorBrowser.getUuid());
        tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
        tfCheckPasswordResponse.setErrorMessage(null);
        tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name());
        tfCheckPasswordResponse.setTwoFactorUserAllowed(false);

        tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());

        trafficLogMap.put("userAllowed", false);
        trafficLogMap.put("success", true);
        trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.TWO_FACTOR_REQUIRED.name());
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_TWO_FACTOR_REQUIRED.name());

        return tfCheckPasswordResponse;
        
      }
  
      //finally, the service requires two factor, and the user is not enrolled
      TwoFactorAudit.createAndStoreFailsafe(twoFactorDaoFactory, 
           TwoFactorAuditAction.AUTHN_NOT_OPTED_IN, ipAddress, userAgent, 
           twoFactorUser.getUuid(), responseMessage.toString(), serviceProviderId, serviceProviderName, twoFactorUser.getUuid(), 
           twoFactorBrowser == null ? null : twoFactorBrowser.getUuid());
      tfCheckPasswordResponse.setDebugMessage(TwoFactorServerUtils.trimToNull(tfCheckPasswordResponse.getDebugMessage()));
      tfCheckPasswordResponse.setErrorMessage(null);
      tfCheckPasswordResponse.setResponseMessage(responseMessage.toString());
      tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name());
      tfCheckPasswordResponse.setTwoFactorUserAllowed(false);

      trafficLogMap.put("userAllowed", false);
      trafficLogMap.put("success", true);
      trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.USER_NOT_ENROLLED.name());
      trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_NOT_OPTED_IN.name());

      return tfCheckPasswordResponse;
    } catch (RuntimeException re) {
      
      tfCheckPasswordResponse.setResultCode(TfCheckPasswordResponseCode.ERROR.name());
      tfCheckPasswordResponse.setErrorMessage(ExceptionUtils.getFullStackTrace(re));
      
      trafficLogMap.put("resultCode", TfCheckPasswordResponseCode.ERROR.name());

      //maybe we should just allow in the case of runtime exceptions
      if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.ws.allowOnException")) {
        
        TwoFactorServerUtils.injectInException(re, "");
        
        tfCheckPasswordResponse.setSuccess(true);
        tfCheckPasswordResponse.setTwoFactorUserAllowed(true);

        trafficLogMap.put("userAllowed", true);
        trafficLogMap.put("success", true);
        trafficLogMap.put("auditAction", TwoFactorAuditAction.AUTHN_ALLOW_WITH_ERROR.name());
      } else {

        tfCheckPasswordResponse.setSuccess(false);
        tfCheckPasswordResponse.setTwoFactorUserAllowed(null);

        trafficLogMap.put("userAllowed", "null");
        trafficLogMap.put("success", false);
        trafficLogMap.put("auditAction", TwoFactorAuditAction.ERROR.name());

      }

      LOG.error("error", re);

      return tfCheckPasswordResponse;

    } finally {
      DuoLog.assignUsername(null);
      
      if (!StringUtils.isBlank(tfCheckPasswordRequest.getClientSourceIpAddress())) {
        trafficLogMap.put("wsClientIp", tfCheckPasswordRequest.getClientSourceIpAddress());
      }
      if (!StringUtils.isBlank(tfCheckPasswordRequest.getClientUsername())) {
        trafficLogMap.put("wsClientUser", tfCheckPasswordRequest.getClientUsername());
      }
      trafficLogMap.put("logicTime", ((System.nanoTime() - start)/1000000) + "ms");
      TfRestLogicTrafficLog.wsRestTrafficLog(trafficLogMap);

      TwoFactorRestServlet.addLogMapParamsIfLogging("tfRestServlet", trafficLogMap);

    }
  }

  /**
   * logic to trust a browser
   * @param twoFactorDaoFactory
   * @param tfCheckPasswordRequest
   * @param tfCheckPasswordResponse
   * @param debug
   * @param twoFactorUser
   * @param cookieUserUuid
   * @param needsNewCookieUuid
   * @param twoFactorBrowser
   * @param requestIsTrusted
   * @param userAuthenticated
   * @return the browser (marked as trusted)
   */
  private static TwoFactorBrowser trustBrowserLogic(TwoFactorDaoFactory twoFactorDaoFactory,
      TfCheckPasswordRequest tfCheckPasswordRequest,
      TfCheckPasswordResponse tfCheckPasswordResponse, 
      boolean debug, TwoFactorUser twoFactorUser, 
      String cookieUserUuid, boolean needsNewCookieUuid, TwoFactorBrowser twoFactorBrowser,
      boolean requestIsTrusted, boolean userAuthenticated) {

    //if there is no code,  or it is wrong, then not a trusted browser
    if (!userAuthenticated) {
      requestIsTrusted = false;
    }
        
    if (needsNewCookieUuid) {
      String newCookieUuid = TwoFactorServerUtils.uuid();
      tfCheckPasswordResponse.setChangeUserBrowserUuid(newCookieUuid);
      
      twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuidOrCreate(twoFactorDaoFactory, 
          newCookieUuid, twoFactorUser.getUuid(), requestIsTrusted, false);
      
      tfCheckPasswordResponse.setChangeUserBrowserUuid(newCookieUuid);
 
      if (debug) {
        tfCheckPasswordResponse.appendDebug("created new two factor browser");
      }
      
    } else {

      //dont change trusted status unless it is sent in (not sent if not on the screen)
      if (tfCheckPasswordRequest.getTrustedBrowser() != null && twoFactorBrowser.isTrustedBrowser() != requestIsTrusted ) {
        twoFactorBrowser = TwoFactorBrowser.retrieveByBrowserTrustedUuidOrCreate(twoFactorDaoFactory, 
            cookieUserUuid, twoFactorUser.getUuid(), requestIsTrusted, false);
        if (debug) {
          tfCheckPasswordResponse.appendDebug("updating db trusted browser status to " + requestIsTrusted);
        }

      }
    }
    return twoFactorBrowser;
  }

  /**
   * @param twoFactorPassUnencrypted
   * @param twoFactorUser
   * @param secret
   * @param phonePassword
   * @return the result
   */
  private static TwoFactorPassResult checkPassword(String twoFactorPassUnencrypted,
      TwoFactorUser twoFactorUser, String secret, String phonePassword) {
    
    TwoFactorCheckPass twoFactorCheckPass = TwoFactorServerConfig.retrieveConfig().twoFactorCheckPass();
    TwoFactorPassResult twoFactorPassResult = twoFactorCheckPass.twoFactorCheckPassword(
        secret, twoFactorPassUnencrypted, twoFactorUser.getSequentialPassIndex(), 
        twoFactorUser.getLastTotpTimestampUsed(), twoFactorUser.getLastTotp60TimestampUsed(), 
        twoFactorUser.getTokenIndex(), phonePassword, twoFactorUser.getDuoUserId());
    return twoFactorPassResult;
  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfRestLogic.class);

  /**
   * 
   * @param tfCheckPasswordRequest
   * @param trafficLogMap
   * @param tfCheckPasswordResponse
   * @param debug
   */
  private static void logTwoFactorLogic(TfCheckPasswordRequest tfCheckPasswordRequest, Map<String, Object> trafficLogMap, 
      TfCheckPasswordResponse tfCheckPasswordResponse, boolean debug) {
    
    //echo back the inputs
    if (debug) {
      tfCheckPasswordResponse.appendDebug("Inputs: ");
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getBrowserUserAgent())) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("browserUserAgent: '" + tfCheckPasswordRequest.getBrowserUserAgent() + "'");
      }
      trafficLogMap.put("browserUserAgent", StringUtils.abbreviate(tfCheckPasswordRequest.getBrowserUserAgent(), 60));
    }
    if (tfCheckPasswordRequest.getDebug() != null) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("debug: '" + tfCheckPasswordRequest.getDebug() + "'");
      }
    }
    if (tfCheckPasswordRequest.getRequireReauth() != null) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("requireReauth: '" + tfCheckPasswordRequest.getRequireReauth() + "'");
      }
      trafficLogMap.put("requireReauth", tfCheckPasswordRequest.getRequireReauth());
    }
    if (tfCheckPasswordRequest.getRequireTwoFactor() != null) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("requireTwoFactor: '" + tfCheckPasswordRequest.getRequireTwoFactor() + "'");
      }
      trafficLogMap.put("requireTwoFactor", tfCheckPasswordRequest.getRequireTwoFactor());
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getServiceId())) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("serviceId: '" + tfCheckPasswordRequest.getServiceId() + "'");
      }
      trafficLogMap.put("serviceId", tfCheckPasswordRequest.getServiceId());
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getServiceName())) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("serviceName: '" + tfCheckPasswordRequest.getServiceName() + "'");
      }
      trafficLogMap.put("serviceName", tfCheckPasswordRequest.getServiceName());
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getSpRequiredFactors())) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("spRequiredFactors: '" + tfCheckPasswordRequest.getSpRequiredFactors() + "'");
      }
      trafficLogMap.put("spRequiredFactors", tfCheckPasswordRequest.getSpRequiredFactors());
    }
    if (tfCheckPasswordRequest.getTrustedBrowser() != null) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("trustedBrowser: '" + tfCheckPasswordRequest.getTrustedBrowser() + "'");
      }
      trafficLogMap.put("trustedBrowser", tfCheckPasswordRequest.getTrustedBrowser());
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getTwoFactorPass())) {
      String passAsterisk = StringUtils.repeat("*", tfCheckPasswordRequest.getTwoFactorPass().length());
      if (debug) {
        tfCheckPasswordResponse.appendDebug("twoFactorPass: '" 
          + passAsterisk + "'");
      }
      trafficLogMap.put("twoFactorPass", passAsterisk);
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getUserIpAddress())) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("userIpAddress: '" + tfCheckPasswordRequest.getUserIpAddress() + "'");
      }
      trafficLogMap.put("userIpAddress", tfCheckPasswordRequest.getUserIpAddress());
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getUsername())) {
      if (debug) {
        tfCheckPasswordResponse.appendDebug("username: '" + tfCheckPasswordRequest.getUsername() + "'");
      }
      //this is already logged
    }
    if (!StringUtils.isBlank(tfCheckPasswordRequest.getUserBrowserUuid())) {
      if (tfCheckPasswordRequest.getUserBrowserUuid().length() > 10) {
        String userBrowserUuid = tfCheckPasswordRequest.getUserBrowserUuid().substring(0, 3) + StringUtils.repeat("*", tfCheckPasswordRequest.getUserBrowserUuid().length()-3);
        if (debug) {
          tfCheckPasswordResponse.appendDebug("userBrowserUuid: '" + userBrowserUuid + "'");
        }
        trafficLogMap.put("userBrowserUuid", userBrowserUuid);
      } else {
        String userBrowserUuid = StringUtils.repeat("*", tfCheckPasswordRequest.getUserBrowserUuid().length());
        if (debug) {
          tfCheckPasswordResponse.appendDebug("userBrowserUuidUnencrypted: '" + userBrowserUuid + "'");
        }
        trafficLogMap.put("userBrowserUuid", userBrowserUuid);
      }
    }

  }
}
