/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.duo;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserView;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import com.duosecurity.client.Http;


/**
 * migrate users from open two factor to duo.  note, this can be run twice will sync things up
 * <pre>Run:
 * [appadmin@server WEB-INF]$ /opt/appserv/tomcat/apps/twoFactor/java/bin/java -cp ./lib/*:classes:/opt/appserv/local/tomcat/letters/tomcat_l/webapps/ppol/WEB-INF/lib/j2ee.jar org.openTwoFactor.server.duo.DuoCommands duoInitiatePush user
 * adb55bb3-e1e5-4b8b-bb4e-e5c7ccca4506
 * </pre>
 */
public class DuoCommands {

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @return the http
   */
  private static Http httpAdmin(String method, String path) {
    return httpAdmin(method, path, null);
  }

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @param timeoutSeconds
   * @return the http
   */
  private static Http httpAdmin(String method, String path, Integer timeoutSeconds) {
    
    String domain = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.adminDomainName");
    
    Http request = (timeoutSeconds != null && timeoutSeconds > 0) ? 
        new Http(method, domain, path, timeoutSeconds) : new Http(method, domain, path);

    return request;
  }

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @return the http
   */
  private static Http httpAuth(String method, String path) {
    return httpAuth(method, path, null);
  }    

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @param timeoutSeconds seconds
   * @return the http
   */
  private static Http httpAuth(String method, String path, Integer timeoutSeconds) {
    
    String domain = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.authDomainName");
    
    Http request = (timeoutSeconds != null && timeoutSeconds > 0) ? new Http(method, domain, path, timeoutSeconds) 
      : new Http(method, domain, path);
    return request;
  }
  
  
  /**
   * execute response raw without checked exception
   * @param request
   * @return the string
   */
  private static String executeRequestRaw(Http request) {
    try {
      return request.executeRequestRaw();
    } catch (Exception e) {
      throw new RuntimeException("Problem with duo", e);
    }
  }
  
  /**
   * sign the http request
   * @param request
   */
  private static void signHttpAdmin(Http request) {
    String integrationKey = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.adminIntegrationKey");
    String secretKey = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.adminSecretKey");
    try {
      request.signRequest(integrationKey,
          secretKey);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Error signing request", uee);
    }
    
  }
  
  /**
   * sign the http request
   * @param request
   */
  private static void signHttpAuth(Http request) {
    String integrationKey = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.authIntegrationKey");
    String secretKey = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.authSecretKey");
    try {
      request.signRequest(integrationKey,
          secretKey);
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("Error signing request", uee);
    }
    
  }

  /**
   * @param code
   * 
   */
  public static void createDeleteProblem(String code) {
    
    String userId = null;
    
    {
      //create user
      Http request = httpAdmin("POST", "/admin/v1/users");
      request.addParam("username", "testUser");
      signHttpAdmin(request);
      String result = executeRequestRaw(request);
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      jsonObject = (JSONObject)jsonObject.get("response");
      userId = jsonObject.getString("user_id");
    }

    for (int i=0;i<10;i++) {
    
      String tokenId = null;
      
      {
        //create a token
        Http request = httpAdmin("POST", "/admin/v1/tokens");
        request.addParam("type", "h6");
        request.addParam("serial", "10021368_hotp_500000");
        request.addParam("secret", "05050505050505050505");
        request.addParam("counter", "500000");
        signHttpAdmin(request);
        
        String result = executeRequestRaw(request);
        
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
   
        if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
          
          throw new RuntimeException("Bad response from Duo: " + result);
        }
        
        jsonObject = (JSONObject)jsonObject.get("response");
        
        tokenId = jsonObject.getString("token_id");
      }
  
      {
        //associate user with token
        Http request = httpAdmin("POST", "/admin/v1/users/" + userId + "/tokens");
        request.addParam("token_id", tokenId);
        signHttpAdmin(request);
        
        String result = executeRequestRaw(request);
        
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
     
        if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
          throw new RuntimeException("Bad response from Duo: " + result);
        }
      }
      
      {
        //use token
        Http request = httpAuth("POST", "/auth/v2/auth");
        
        request.addParam("user_id", userId);
        request.addParam("factor", "passcode");
        
        request.addParam("passcode", code);
  
        signHttpAuth(request);
        
        String result = executeRequestRaw(request);
        
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
        if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
          throw new RuntimeException("Bad response from Duo: " + result);
        }
        
        jsonObject = (JSONObject)jsonObject.get("response");
  
        String resultString = jsonObject.getString("result");
        
        System.out.println("Code allowed? " + StringUtils.equals("allow", resultString));
  
      }
      
      {
        //delete token
        Http request = httpAdmin("DELETE", "/admin/v1/tokens/" + tokenId);
        
        signHttpAdmin(request);
        
        String result = executeRequestRaw(request);
        
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
        
        if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
          throw new RuntimeException("Bad response from Duo: " + result);
        }
      }
      
    }    
    
  }
  
  /**
   * 
   */
  public static void loadTestCheckToken() {
    JSONObject jsonObject = retrieveDuoUserBySomeId("mchyzer");
    String userId = jsonObject.getString("user_id");
    verifyDuoCode(userId, "000010");
    long now = System.nanoTime();
    for (int i=0;i<10;i++) {
      verifyDuoCode(userId, "00000" + i);
    }
    long nanos = System.nanoTime() - now;
    System.out.println("Took " + ((nanos/1000000L)/10) + "ms per call");
  }
  
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
//    if (true) {
//      //lookup the token
//      //  GET /admin/v1/tokens
//      //  type   Optional*  
//      //  serial Optional
//      Http request = httpAdmin("GET", "/admin/v1/tokens");
//         
//      signHttpAdmin(request);
//      
//      String result = executeRequestRaw(request);
//      
//      System.out.println(result);
//      
//      return;
//    }

//    if (true) {
//      migrateAllToDuo();
////      deleteAllFromDuo();
//      return;
//    }

//    if (true) {
//
//      System.out.println(verifyDuoCodeBySomeId("couch", "77548"));
//      
//      return;
//    }
    
//  if (true) {
//  //createDeleteProblem("888674");
//  //return;
//}

//    if (true) {
//      loadTestCheckToken();
//      return;
//    }
    
    if (args.length == 1 && StringUtils.equals("migrateAllToDuo", args[0])) {
      migrateAllToDuo();
      
    } else if (args.length == 3 && StringUtils.equals("addDuoUserAlias", args[0])) {
      String duoUserId = retrieveDuoUserIdBySomeId(args[1]);
      addDuoUserAlias(duoUserId, args[2]);
    } else if (args.length == 3 && StringUtils.equals("deleteDuoUserAlias", args[0])) {
      String duoUserId = retrieveDuoUserIdBySomeId(args[1]);
      deleteDuoUserAlias(duoUserId, args[2]);
    } else if (args.length == 1 && StringUtils.equals("migrateAllTokensToDuo", args[0])) {
      migrateAllTokensToDuo();
    } else if (args.length == 1 && StringUtils.equals("migrateAllPhonesToDuo", args[0])) {
      migrateAllPhonesToDuo();
    } else if (args.length == 3 && StringUtils.equals("testCode", args[0])) {
      boolean validCode = verifyDuoCodeBySomeId(args[1], args[2]);
      System.out.println("Valid code? " + validCode);
    } else if (args.length == 3 && StringUtils.equals("duoInitiatePhoneCallByNumber", args[0])) {
      duoInitiatePhoneCallBySomeId(args[1], args[2], false, 30);
    } else if (args.length == 1 && StringUtils.equals("deleteAllTokensFromDuo", args[0])) {
      deleteAllTokensFromDuo();
    } else if (args.length == 2 && StringUtils.equals("deleteTokensByUserFromDuo", args[0])) {
      deleteDuoTokensBySomeId(args[1]);
    } else if (args.length == 2 && StringUtils.equals("migrateTokensByUserToDuo", args[0])) {
      migrateTokensBySomeId(args[1], true);
    } else if (args.length == 2 && StringUtils.equals("deleteDuoToken", args[0])) {
      deleteDuoToken(args[1]);
    } else if (args.length == 1 && StringUtils.equals("deleteAllFromDuo", args[0])) {
      deleteAllFromDuo();
    } else if (args.length == 2 && StringUtils.equals("migrateUserAndTokensToDuo", args[0])) {
      migrateUserAndPhonesAndTokensBySomeId(args[1], true, true);
    } else if (args.length == 2 && StringUtils.equals("viewUser", args[0])) {
      String userLookupId = args[1];
      JSONObject duoUser = retrieveDuoUserBySomeId(userLookupId);
      if (duoUser != null) {
        System.out.println(TwoFactorServerUtils.indent(duoUser.toString(), true));
      } else {
        System.out.println("User not found");
      }
    } else if (args.length == 2 && StringUtils.equals("deleteUserAndPhonesAndTokens", args[0])) {
      String userLookupId = args[1];
      deleteDuoUserAndPhonesAndTokensBySomeId(userLookupId);
    } else if (args.length == 2 && StringUtils.equals("deleteUser", args[0])) {
      String userLookupId = args[1];
      deleteDuoUserBySomeId(userLookupId);
    } else if (args.length == 2 && StringUtils.equals("enrollUserInDuoPush", args[0])) {
      String userLookupId = args[1];
      String barcode = enrollUserInPushBySomeId(userLookupId, true);
      System.out.println(barcode);
      
    } else if (args.length == 2 && StringUtils.equals("migratePhonesToDuo", args[0])) {
      String userLookupId = args[1];
      migratePhonesToDuoBySomeId(userLookupId, true);

    } else if (args.length == 2 && StringUtils.equals("duoBypassCode", args[0])) {
      String userLookupId = args[1];
      String code = duoBypassCodeBySomeId(userLookupId);
      System.out.println(code);

    } else if ((args.length == 2 || args.length == 3) && StringUtils.equals("duoInitiatePush", args[0])) {
      String userLookupId = args[1];
      String message = args.length == 2 ? null : args[2];
      String txid = duoInitiatePushBySomeId(userLookupId, true, message, null);
      System.out.println(txid);

    } else if (args.length == 2 && StringUtils.equals("duoPushOrPhoneSuccess", args[0])) {
      String txId = args[1];
      boolean allowed = duoPushOrPhoneSuccess(txId, null);
      System.out.println("Allowed? " + allowed);

    } else if (args.length == 3 && StringUtils.equals("duoPushByDefault", args[0])) {
      String userId = args[1];
      boolean allowed = TwoFactorServerUtils.booleanValue(args[2]);

      duoPushByDefaultForSomeId(userId, allowed);

    } else if (args.length == 1 && StringUtils.equals("syncDuoUserIds", args[0])) {
      syncDuoUserIds();

    } else if (args.length == 2 && StringUtils.equals("duoPhoneByNumber", args[0])) {
      String number = args[1];
      JSONObject phoneObject = duoPhoneByIdOrNumber(number, false);
      System.out.println(phoneObject);

    } else if (args.length == 3 && StringUtils.equals("migrateHotpTokenBySomeId", args[0])) {
      String someId = args[1];
      Long tokenIndex = TwoFactorServerUtils.longObjectValue(args[2], true);
      migrateHotpTokenBySomeId(someId, tokenIndex);
      
    } else {

      System.out.println("Enter arg: migrateAllToDuo to migrate all users to duo");
      System.out.println("Enter arg: migrateAllTokensToDuo to migrate all users to duo");
      System.out.println("Enter arg: deleteAllTokensFromDuo to delete all tokens from duo");
      System.out.println("Enter arg: deleteAllFromDuo to delete all data from duo");
      System.out.println("Enter arg: deleteDuoToken <tokenId> to delete a token");
      System.out.println("Enter arg: viewUser <someUserId> to view a user");
      System.out.println("Enter arg: deleteUserAndPhonesAndTokens <someUserId> to delete a user and their phones and tokens");
      System.out.println("Enter arg: deleteTokensByUserFromDuo <someUserId> to delete tokens for a user");
      System.out.println("Enter arg: migrateTokensByUserToDuo <someUserId> to migrate tokens for a user");
      System.out.println("Enter arg: migrateUserAndTokensToDuo <someUserId> to migrate tokens for a user");
      System.out.println("Enter arg: deleteUserAndTokens <someUserId> to delete a user and their tokens");
      System.out.println("Enter arg: deleteUser <someUserId> to delete a user object");
      System.out.println("Enter arg: enrollUserInDuoPush <someUserId> to enroll a user in push");
      System.out.println("Enter arg: migratePhonesToDuo <someUserId> to migrate phones to duo");
      System.out.println("Enter arg: duoInitiatePush <someUserId> to initiate push to phone");
      System.out.println("Enter arg: duoPushByDefault <someUserId> true|false to set default push flag");
      System.out.println("Enter arg: syncDuoUserIds to sync all duo user ids");
      System.out.println("Enter arg: duoPhoneByNumber <phoneNumber> to retrieve a phone");
      System.out.println("Enter arg: migrateHotpTokenBySomeId <someUserId> <tokenIndex> to setup a hardware token a specific index");
      System.exit(1);
    }
    
  }

  /**
   * 
   * @param someId
   * @param printOutput 
   */
  private static void migrateTokensBySomeId(String someId, boolean printOutput) {
    
    String duoUserId = retrieveDuoUserIdBySomeId(someId);
    
    String tfUserId = retrieveTfUserUuidBySomeId(someId, true);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), tfUserId);
    
    String netId = TfSourceUtils.convertSubjectIdToNetId(TfSourceUtils.mainSource(), twoFactorUser.getLoginid());

    try {
      //setup HOTP token
      setupOneTimeCodes(twoFactorUser, duoUserId, false);
      setupHotpToken(twoFactorUser, duoUserId, false);
      setupTotp(twoFactorUser, duoUserId, false, 30);
      setupTotp(twoFactorUser, duoUserId, false, 60);
      
      if (printOutput) {
        System.out.println("Created tokens for: " + twoFactorUser.getLoginid() + ", " + netId);
      }
    } catch (Exception e) {
      System.out.println("Error: " + twoFactorUser.getLoginid() + ", " + netId);
      e.printStackTrace();
    }

  }
  
  /**
   * 
   * @param someId
   * @param printOutput 
   * @param requireOptin 
   */
  public static void migrateUserAndPhonesAndTokensBySomeId(String someId, boolean printOutput, boolean requireOptin) {
    
    migrateUserBySomeId(someId, requireOptin);
    
    migratePhonesToDuoBySomeId(someId, printOutput);
    
    migrateTokensBySomeId(someId, printOutput);
  }
  
  /**
   * 
   * @param someId
   * @param requireOptIn
   * @return url of barcode
   */
  public static String enrollUserInPushBySomeId(String someId, boolean requireOptIn) {
    
    String userUuid = retrieveTfUserUuidBySomeId(someId, true);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), userUuid);

    if (requireOptIn && !twoFactorUser.isOptedIn()) {
      throw new RuntimeException("User is not enrolled in twostep! " + twoFactorUser.getLoginid());
    }
    
    String netId = TfSourceUtils.convertSubjectIdToNetId(TfSourceUtils.mainSource(), twoFactorUser.getLoginid());

    if (StringUtils.isBlank(netId)) {
      throw new RuntimeException("Cant find netId for user: " + someId);
    }

    JSONObject duoUser = retrieveDuoUserByIdOrUsername(netId, false);

    //  boolean userNeedsDelete = false;
    
    if (duoUser == null) {
      //userNeedsDelete = true;
      //we need to delete this user and re-enroll
      
      //deleteDuoUserAndTokensBySomeId(someId);
      
      throw new RuntimeException("Cant find duo user: " + netId);
    }

    //    //create user
    //    Http request = httpAuth("POST", "/auth/v2/enroll");
    //    request.addParam("username", netId);
    //    
    //    signHttpAuth(request);
    //    
    //    String result = executeRequestRaw(request);
    //        
    //    System.out.println(result);
    //    
    //    //  {
    //    //    "stat": "OK",
    //    //    "response": {
    //    //      "activation_barcode": "https://api-eval.duosecurity.com/frame/qr?value=8LIRa5danrICkhHtkLxi-cKLu2DWzDYCmBwBHY2YzW5ZYnYaRxA",
    //    //      "activation_code": "duo://8LIRa5danrICkhHtkLxi-cKLu2DWzDYCmBwBHY2YzW5ZYnYaRxA",
    //    //      "expiration": 1357020061,
    //    //      "user_id": "DU94SWSN4ADHHJHF2HXT",
    //    //      "username": "49c6c3097adb386048c84354d82ea63d"
    //    //    }
    //    //  }
    
    //if they have a push phone, lets delete it and start over
    String duoPushPhoneId = duoPushPhoneId(duoUser);
    
    if (!StringUtils.isBlank(duoPushPhoneId)) {
      
      deleteDuoPhone(duoPushPhoneId);
      
    }
    
    JSONObject jsonPhone = createDuoPhone("phone_push", null, true);
    String phoneId = jsonPhone.getString("phone_id");
    
    twoFactorUser.setDuoPushPhoneId(phoneId);
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
    
    String userId = duoUser.getString("user_id");

    associateUserWithPhone(userId, phoneId);

    String url = enrollUserInPushByPhoneId(phoneId);

    duoReorderPhonesBySomeId(someId, false);
    
    return url;
    
  }

  /**
   * @param phoneId
   * @return the url of activation code
   */
  private static String enrollUserInPushByPhoneId(String phoneId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "enrollUserInPushByPhoneId");
    debugMap.put("phoneId", phoneId);
    long startTime = System.nanoTime();
    try {
    
      //generate an activation code
      // POST /admin/v1/phones/[phone_id]/activation_url
      String path = "/admin/v1/phones/" + phoneId + "/activation_url";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      // {
      //   "stat": "OK",
      //   "response": {
      //     "activation_barcode": "https://api-abcdef.duosecurity.com/frame/qr?value=duo%3A%2F%2Factivation-code",
      //     "valid_secs": 3600
      //   }
      // }
  
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
   
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
  
      jsonObject = (JSONObject)jsonObject.get("response");
  
      String barcode = jsonObject.getString("activation_barcode");
      
      debugMap.put("barcode", StringUtils.abbreviate(barcode, 20));

      return barcode;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
      
  }

  /**
   * @param someId
   * @return the response object
   */
  public static String duoBypassCodeBySomeId(String someId) {

    //user id or netId or whatever
    if (StringUtils.isBlank(someId)) {
      throw new RuntimeException("someId is required!");
    }

    String userId = retrieveDuoUserIdBySomeId(someId);
    return duoBypassCodeByUserId(userId);
  }

  /**
   * @param userId
   * @return the response object
   */
  public static String duoBypassCodeByUserId(String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "duoBypassCodeByUserId");
    debugMap.put("userId", userId);
    long startTime = System.nanoTime();
    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is required!");
      }
      
      int phoneCodeLastsMinutes = TwoFactorServerConfig.retrieveConfig()
          .propertyValueInt("twoFactorServer.phoneCodeLastsMinutes", 10);

      debugMap.put("phoneCodeLastsMinutes", phoneCodeLastsMinutes);

      //cant be 0
      if (phoneCodeLastsMinutes == 0) {
        phoneCodeLastsMinutes = 1;
      }
      
      //  POST /admin/v1/users/[user_id]/bypass_codes
      //  count Optional  Number of new bypass codes to create. At most 10 codes (the default) can be created at a time. Codes will be generated randomly.
      //  codes Optional  CSV string of codes to use. Mutually exclusive with count.
      //  valid_secs  Optional  The number of seconds generated bypass codes should be valid for. If 0 (the default) the codes will never expire.
      //  reuse_count Optional  The number of times generated bypass codes can be used. If 0, the codes will have an infinite reuse_count. Default: 1.
  
      String path = "/admin/v1/users/" + userId + "/bypass_codes";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("count", "1");
      request.addParam("valid_secs", Integer.toString(phoneCodeLastsMinutes + 60));
   
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  EXAMPLE RESPONSE
      //  {
      //    "stat": "OK",
      //    "response": [
      //      "407176182",
      //      "016931781",
      //      "338390347",
      //      "537828175",
      //      "006165274",
      //      "438680449",
      //      "877647224",
      //      "196167433",
      //      "719424708",
      //      "727559878"
      //    ]
      //  }
      //
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);

        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      JSONArray codes = (JSONArray)jsonObject.get("response");
  
      if (codes.size() != 1) {
        throw new RuntimeException("Why is codes size not 1??? " + codes.size());
      }
      
      String code = codes.getString(0);
      
      debugMap.put("code", StringUtils.repeat("*", TwoFactorServerUtils.stringLength(code)));
      
      return code;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  
  /**
   * @param phoneId
   * @param number if not null, this is the phone number
   * @param mobile
   * @return the response object
   */
  public static JSONObject editDuoPhone(String phoneId, String number, boolean mobile) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "editDuoPhone");
    debugMap.put("phoneId", phoneId);
    debugMap.put("number", number);
    debugMap.put("mobile", mobile);

    long startTime = System.nanoTime();
    try {
    
      //phone id
      if (StringUtils.isBlank(phoneId)) {
        throw new RuntimeException("phoneId is required!");
      }
          
      //edit phone
      String path = "/admin/v1/phones/" + phoneId;
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("type", mobile ? "mobile" : "landline");
   
      if (mobile) {
        request.addParam("platform", "generic smartphone");
      } else {
        request.addParam("platform", "unknown");
      }
      
      if (!StringUtils.isBlank(number)) {
        request.addParam("number", number);
      } else {
        request.addParam("number", null);
      }
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "stat": "OK",
      //    "response": {
      //      "phone_id": "DPFZRS9FB0D46QFTM899",
      //      "number": "+15555550100",
      //      "name": "",
      //      "extension": "",
      //      "postdelay": null,
      //      "predelay": null,
      //      "type": "Mobile",
      //      "capabilities": [
      //        "sms",
      //        "phone",
      //        "push"
      //      ],
      //      "platform": "Apple iOS",
      //      "activated": false,
      //      "sms_passcodes_sent": false,
      //      "users": [{
      //        "user_id": "DUJZ2U4L80HT45MQ4EOQ",
      //        "username": "jsmith",
      //        "realname": "Joe Smith",
      //        "email": "jsmith@example.com",
      //        "status": "active",
      //        "last_login": 1343921403,
      //        "notes": ""
      //      }]
      //    }
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
      
      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * @param name is name of phone, e.g. phone_push, phone_0, phone_1, phone_2
   * @param number if not null, this is the phone number
   * @param mobile
   * @return the response object
   */
  public static JSONObject createDuoPhone(String name, String number, boolean mobile) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createDuoPhone");
    debugMap.put("name", name);
    debugMap.put("number", number);
    debugMap.put("mobile", mobile);
    long startTime = System.nanoTime();
    try {
    
      //create phone
      String path = "/admin/v1/phones";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("name", name);
      request.addParam("type", mobile ? "mobile" : "landline");
      if (mobile) {
        request.addParam("platform", "generic smartphone");
      }
      
      if (!StringUtils.isBlank(number)) {
        request.addParam("number", number);
      }
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "stat": "OK",
      //    "response": {
      //      "phone_id": "DPFZRS9FB0D46QFTM899",
      //      "number": "+15555550100",
      //      "name": "",
      //      "extension": "",
      //      "postdelay": null,
      //      "predelay": null,
      //      "type": "Mobile",
      //      "capabilities": [
      //        "sms",
      //        "phone",
      //        "push"
      //      ],
      //      "platform": "Apple iOS",
      //      "activated": false,
      //      "sms_passcodes_sent": false,
      //      "users": [{
      //        "user_id": "DUJZ2U4L80HT45MQ4EOQ",
      //        "username": "jsmith",
      //        "realname": "Joe Smith",
      //        "email": "jsmith@example.com",
      //        "status": "active",
      //        "last_login": 1343921403,
      //        "notes": ""
      //      }]
      //    }
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
      
      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * 
   * @param duoUser
   * @return the phone Id or null if none
   */
  public static String duoPushPhoneId(JSONObject duoUser) {
    //see if there is a phone
    //  "phones": [{
    //    "phone_id": "DPFZRS9FB0D46QFTM899",
    //    "number": "+15555550100",
    //    "extension": "",
    //    "name": "", 
    //    "postdelay": null,
    //    "predelay": null,
    //    "type": "Mobile",
    //    "capabilities": [
    //      "sms",
    //      "phone",
    //      "push"
    //    ],
    //    "platform": "Apple iOS",
    //    "activated": false,
    //    "sms_passcodes_sent": false
    //  }],
    
    JSONArray phones = (JSONArray)duoUser.get("phones");
    
    if (phones == null || phones.size() == 0) {
      return null;
    }
    
    for (int i=0;i<phones.size();i++) {
      
      JSONObject phone = phones.getJSONObject(i);
      if (StringUtils.equals("phone_push", phone.getString("name"))) {
        return phone.getString("phone_id");
      }
      
    }
    return null;
  }

  /**
   * get phone by id
   * @param duoUser
   * @param phoneId
   * @return the phone Id or null if none
   */
  public static JSONObject duoPhoneById(JSONObject duoUser, String phoneId) {
    //see if there is a phone
    //  "phones": [{
    //    "phone_id": "DPFZRS9FB0D46QFTM899",
    //    "number": "+15555550100",
    //    "extension": "",
    //    "name": "", 
    //    "postdelay": null,
    //    "predelay": null,
    //    "type": "Mobile",
    //    "capabilities": [
    //      "sms",
    //      "phone",
    //      "push"
    //    ],
    //    "platform": "Apple iOS",
    //    "activated": false,
    //    "sms_passcodes_sent": false
    //  }],
    
    JSONArray phones = (JSONArray)duoUser.get("phones");
    
    if (phones == null || phones.size() == 0) {
      return null;
    }
    
    for (int i=0;i<phones.size();i++) {
      
      JSONObject phone = phones.getJSONObject(i);
      if (StringUtils.equals(phoneId, phone.getString("phone_id"))) {
        return phone;
      }
      
    }
    return null;
  }

  /**
   * see if enrolled in push
   * @param duoUser
   * @return enrolled in push
   */
  public static boolean enrolledInPush(JSONObject duoUser) {
    //see if there is a phone
    //  "phones": [{
    //    "phone_id": "DPFZRS9FB0D46QFTM899",
    //    "number": "+15555550100",
    //    "extension": "",
    //    "name": "", 
    //    "postdelay": null,
    //    "predelay": null,
    //    "type": "Mobile",
    //    "capabilities": [
    //      "sms",
    //      "phone",
    //      "push"
    //    ],
    //    "platform": "Apple iOS",
    //    "activated": false,
    //    "sms_passcodes_sent": false
    //  }],

    String pushPhoneId = duoPushPhoneId(duoUser);
    
    if (StringUtils.isBlank(pushPhoneId)) {
      return false;
    }
    
    JSONObject pushPhone = duoPhoneById(duoUser, pushPhoneId);
    
    //see if there is a push capability
    if (pushPhone.has("capabilities")) {
      JSONArray capabilities = pushPhone.getJSONArray("capabilities");
      if (capabilities != null) {
        for (int i=0;i<capabilities.size();i++) {
          String capability = capabilities.getString(i);
          if (StringUtils.equals(capability, "push")) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * sync up phones from open two factor to duo
   * @param someId
   * @param printResults if sys out print results
   */
  public static void migratePhonesToDuoBySomeId(String someId, boolean printResults) {

    //get two factor object
    String tfUserUuid = retrieveTfUserUuidBySomeId(someId, true);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), tfUserUuid);

    if (!twoFactorUser.isOptedIn()) {
      return;
    }

    //get the duo user
    JSONObject duoUser = retrieveDuoUserBySomeId(someId);
    
    if (duoUser == null) {
      throw new RuntimeException("Cant find duo user for someId: " + someId);
    }
    
    for (int i=0;i<3;i++) {
      
      try {
        String phoneNumber = null;
        boolean isText = false;
        
        //uh... translate to a loopable value
        if (i==0) {
          
          phoneNumber = twoFactorUser.getPhone0();
          isText = TwoFactorServerUtils.defaultIfNull(twoFactorUser.getPhoneIsText0(), false);
          
        } else if (i==1) {
  
          phoneNumber = twoFactorUser.getPhone1();
          isText = TwoFactorServerUtils.defaultIfNull(twoFactorUser.getPhoneIsText1(), false);
          
        } else if (i==2) {
          
          phoneNumber = twoFactorUser.getPhone2();
          isText = TwoFactorServerUtils.defaultIfNull(twoFactorUser.getPhoneIsText2(), false);
  
        }
  
        //get the phone from duo
        JSONObject duoPhone = duoPhoneByIndex(duoUser, i);
        
        //if they both arent there, thats good
        if (duoPhone == null && StringUtils.isBlank(phoneNumber)) {
          if (printResults) {
            System.out.println("Phone for user " + twoFactorUser.getLoginid() + " " + i + " was in sync");
          }
          continue;
        }
        
        //if there is no open two factor, but is a duo, then delete
        String phoneId = duoPhone == null ? null : duoPhone.getString("phone_id");
        if (duoPhone != null && StringUtils.isBlank(phoneNumber)) {
          if (printResults) {
            System.out.println("Phone for user " + twoFactorUser.getLoginid() + " " + i + " does not exist in open two factor and will be removed from Duo");
          }
          deleteDuoPhone(phoneId);
          continue;
        }
  
        //if there is no duo, then create
        if (duoPhone == null && !StringUtils.isBlank(phoneNumber)) {
          
          //see if we can look it up by number
          duoPhone = duoPhoneByIdOrNumber(phoneNumber, false);
          
          if (duoPhone != null) {
            //found the phone, but not associated with user yet
            associateUserWithPhone(duoUser.getString("user_id"), duoPhone.getString("phone_id"));
          }
        }
  
        //if there is no duo, then create
        if (duoPhone == null && !StringUtils.isBlank(phoneNumber)) {
          if (printResults) {
            System.out.println("Phone for user " + twoFactorUser.getLoginid() + " " + i + " did not exist in duo and will be created: " + phoneNumber + ", mobile? " + isText);
          }
          duoPhone = createDuoPhone("phone " + (i+1), phoneNumber, isText);
          phoneId = duoPhone.getString("phone_id");
          associateUserWithPhone(duoUser.getString("user_id"), phoneId);
          continue;
        }
        
        //if both, make sure ok
        if (duoPhone != null && !StringUtils.isBlank(phoneNumber)) {
          
          boolean phoneNumberInternational = phoneNumber.trim().startsWith("+");
          
          boolean duoIsMobile = StringUtils.equalsIgnoreCase(duoPhone.getString("type"), "mobile"); 
  
          //note, this will likely always get updated since duo stores as +11234567890 and open two factor is 123-456-7890
          String duoNumber = TwoFactorServerUtils.trimToEmpty(duoPhone.getString("number"));
          
          duoNumber = duoNumber.replaceAll("[^\\d]", "");

          if (duoNumber.startsWith("1")) {
            
            duoNumber = duoNumber.substring(1);
          }
          
          phoneNumber = TwoFactorServerUtils.trimToEmpty(phoneNumber).replaceAll("[^\\d]", "");
          boolean phoneNumberStartsWithOne = false;

          if (phoneNumber.startsWith("1")) {
            phoneNumberStartsWithOne = true;
            phoneNumber = phoneNumber.substring(1);
          }
          
          //phone number and if mobile needs to match
          if (!StringUtils.equals(duoNumber, phoneNumber)
              || (duoIsMobile != isText)) {
          
            if (printResults) {
              System.out.println("Phone for user " + twoFactorUser.getLoginid() + " " + i + " was out of sync, changing number to: " + phoneNumber + ", mobile? " + isText);
            }

            if (phoneNumberInternational) {
              if (phoneNumberStartsWithOne) {
                phoneNumber = "1" + phoneNumber;
              }
              phoneNumber = "+" + phoneNumber;
            }
            
            editDuoPhone(phoneId, phoneNumber, isText);
            
          }
          continue;
        }
      } catch (Exception e) {
        //catch this so it doesnt fail
        System.out.println("Error with user: " + twoFactorUser.getLoginid());
        e.printStackTrace();
      }
    }
    
    // reorder to get push up front
    duoReorderPhonesBySomeId(someId, printResults);
    
  }
  
  /**
   * reorder phones to get push up front
   * @param someId
   * @param printResults
   */
  public static void duoReorderPhonesBySomeId(String someId, boolean printResults) {

    //get two factor object
    String tfUserUuid = retrieveTfUserUuidBySomeId(someId, true);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), tfUserUuid);

    if (!twoFactorUser.isOptedIn()) {
      return;
    }

    //get the duo user
    JSONObject duoUser = retrieveDuoUserBySomeId(someId);
    
    if (duoUser == null) {
      throw new RuntimeException("Cant find duo user for someId: " + someId);
    }
    
    Boolean needsReordering = null;
    
    int numberOfPhones = 0;
    
    JSONArray phones = (JSONArray)duoUser.get("phones");
    
    if (phones != null && phones.size() > 0) {
      numberOfPhones = phones.size();
    }
    
    int pushPhoneIndex = -1;
    for (int i=0;i<numberOfPhones;i++) {
      
      
      //get the phone from duo
      JSONObject duoPhone = phones.getJSONObject(i);
      
      //if they both arent there, thats good
      if (duoPhone == null) {
        if (printResults) {
          System.out.println("Phone for user " + twoFactorUser.getLoginid() + " " + i + " was in sync");
        }
        continue;
      }

      Object capablities = duoPhone.get("capabilities");
      
      if (capablities == null || capablities == JSONNull.getInstance() || (!(capablities instanceof JSONArray))) {
        continue;
      }

      JSONArray capabilitiesArray = (JSONArray)capablities;
      
      Set<String> capabilitiesSet = new HashSet<String>();
      
      for (int j=0;j<capabilitiesArray.size();j++) {
        capabilitiesSet.add(capabilitiesArray.getString(j));
      }
      
      if (i==0 && capabilitiesSet.contains("push")) {
        if (printResults) {
          System.out.println("Push phone is in index 0, doesnt need reordering");
        }
        pushPhoneIndex = i;
        needsReordering = false;
        break;
      }
      
      if (i!=0 && capabilitiesSet.contains("push")) {
        if (printResults) {
          System.out.println("Push phone is not in index 0, its in index " + i + ", does need reordering");
        }
        pushPhoneIndex = i;
        needsReordering = true;
        break;
      }
      
    }

    String userId = duoUser.getString("user_id");
    
    if (needsReordering != null && needsReordering) {

      //remove all phones
      for (int i=0;i<numberOfPhones;i++) {
        
        //get the phone from duo
        JSONObject duoPhone = phones.getJSONObject(i);
        
        if (duoPhone == null) {
          continue;
        }

        String phoneId = duoPhone.getString("phone_id");
        
        disassociateUserWithPhone(userId, phoneId);
        
      }
      
      //add back the push device
      JSONObject duoPhone = phones.getJSONObject(pushPhoneIndex);
      String phoneId = duoPhone.getString("phone_id");
      associateUserWithPhone(userId, phoneId);
      
      //add back the other phones
      for (int i=0;i<numberOfPhones;i++) {
        
        if (i==pushPhoneIndex) {
          continue;
        }
        
        //get the phone from duo
        duoPhone = phones.getJSONObject(i);
        
        if (duoPhone == null) {
          continue;
        }

        phoneId = duoPhone.getString("phone_id");
        
        associateUserWithPhone(userId, phoneId);
        
      }
      
    }
    
  }
  
  /**
   * @param index is the index of the phone, 0, 1, 2
   * @param duoUser
   * @return the phone Id or null if none
   */
  public static JSONObject duoPhoneByIndex(JSONObject duoUser, int index) {
    //see if there is a phone
    //  "phones": [{
    //    "phone_id": "DPFZRS9FB0D46QFTM899",
    //    "number": "+15555550100",
    //    "extension": "",
    //    "name": "", 
    //    "postdelay": null,
    //    "predelay": null,
    //    "type": "Mobile",
    //    "capabilities": [
    //      "sms",
    //      "phone",
    //      "push"
    //    ],
    //    "platform": "Apple iOS",
    //    "activated": false,
    //    "sms_passcodes_sent": false
    //  }],
    
    JSONArray phones = (JSONArray)duoUser.get("phones");
    
    if (phones == null || phones.size() == 0) {
      return null;
    }
    
    for (int i=0;i<phones.size();i++) {
      
      JSONObject phone = phones.getJSONObject(i);
      if (StringUtils.equals("phone " + (index+1), phone.getString("name"))) {
        return phone;
      }
      
    }
    return null;
  }
  
  /**
   * 
   * @param someId
   * @param requireOptin
   */
  private static void migrateUserBySomeId(String someId, boolean requireOptin) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "migrateUserBySomeId");

    long startTime = System.nanoTime();

    try {
      
      debugMap.put("someId", someId);

      String userUuid = retrieveTfUserUuidBySomeId(someId, true);

      debugMap.put("userUuid", userUuid);

      debugMap.put("requireOptin", requireOptin);

      TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), userUuid);
  
      if (requireOptin && !twoFactorUser.isOptedIn()) {
        debugMap.put("optedIn", twoFactorUser.isOptedIn());
        return;
      }
      
      String netId = TfSourceUtils.convertSubjectIdToNetId(TfSourceUtils.mainSource(), twoFactorUser.getLoginid());
  
      debugMap.put("netId", netId);

      if (StringUtils.isBlank(netId)) {
        System.out.println("Cant find netId for user: " + someId);
        return;
      }
  
      JSONObject duoUser = retrieveDuoUserByIdOrUsername(netId, false);
  
      debugMap.put("duoUserNull", duoUser == null);

      if (duoUser == null) {
        duoUser = createDuoUserByUsername(TwoFactorDaoFactory.getFactory(), twoFactorUser, netId);
        
      } else {
        debugMap.put("duoUserExists", true);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * 
   */
  private static void syncDuoUserIds() {

    //lets get all the opted in users
    for (TwoFactorUserView twoFactorUserView : TwoFactorServerUtils.nonNull(
        TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveAllOptedInUsers())) {
      
      String twoFactorDuoUserId = twoFactorUserView.getDuoUserId();
      
      String duoUserId = retrieveDuoUserIdBySomeId(twoFactorUserView.getLoginid(), false);
      
      if (!StringUtils.equals(twoFactorDuoUserId, duoUserId)) {
        System.out.println("Updating user: " + twoFactorUserView.getLoginid() + " duo user id: " + duoUserId);
        TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), twoFactorUserView.getUuid());
        twoFactorUser.setDuoUserId(duoUserId);
        twoFactorUser.store(TwoFactorDaoFactory.getFactory());
      } else {
        System.out.println("User duo id ok: " + twoFactorUserView.getLoginid() + " duo user id: " + duoUserId);
        
      }
      
    }
  }
  
  /**
   * 
   */
  private static void migrateAllToDuo() {

    //lets get all the opted in users
    for (TwoFactorUserView twoFactorUserView : TwoFactorServerUtils.nonNull(
        TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveAllOptedInUsers())) {
      migrateUserBySomeId(twoFactorUserView.getUuid(), true);
    }
    
    migrateAllPhonesToDuo();
    
    migrateAllTokensToDuo();
  }

  /**
   * 
   */
  private static void migrateAllPhonesToDuo() {
    
    for (TwoFactorUserView twoFactorUserView : TwoFactorServerUtils.nonNull(
        TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveAllOptedInUsers())) {
      migratePhonesToDuoBySomeId(twoFactorUserView.getUuid(), true);
    }
    
  }

  /**
   * migrate all tokens to duo
   */
  private static void migrateAllTokensToDuo() {
    //lets get all the opted in users
    for (TwoFactorUserView twoFactorUserView : TwoFactorServerUtils.nonNull(
        TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveAllOptedInUsers())) {
      
      String loginid = twoFactorUserView.getLoginid();
      
      String netId = TfSourceUtils.convertSubjectIdToNetId(TfSourceUtils.mainSource(), loginid);
      
      if (StringUtils.isBlank(netId)) {
        System.out.println("Cant find netId for user: " + loginid);
        continue;
      }

      String duoUserId = retrieveDuoUserIdBySomeId(netId);

      if (!StringUtils.isBlank(duoUserId)) {
        try {
          migrateTokensBySomeId(netId, true);
        } catch (Exception e) {
          
          System.out.println("Problem with user: " + duoUserId);
          e.printStackTrace();
          
        }
      }
    }

  }

  /**
   * @param someId
   * @param tokenIndex (0 based)
   */
  private static void migrateHotpTokenBySomeId(String someId, Long tokenIndex) {
    
    String duoUserId = retrieveDuoUserIdBySomeId(someId);
  
    if (!StringUtils.isBlank(duoUserId)) {
      try {
      
        String tfUserId = retrieveTfUserUuidBySomeId(someId, true);
        TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), tfUserId);

        setupHotpToken(twoFactorUser, duoUserId, true, tokenIndex);

      } catch (Exception e) {
        
        System.out.println("Problem with user: duoUserId: " + duoUserId + ", someId: " + someId);
        e.printStackTrace();
        
      }
    }
  
  }  
  
  /**
   * setup an HOTP token for this
   * @param twoFactorUser
   * @param userId
   * @param resetCodesIfExists
   */
  public static void setupOneTimeCodes(TwoFactorUser twoFactorUser,
     String userId, boolean resetCodesIfExists) {
    
    Long sequentialPassIndexGivenToUser = twoFactorUser.getSeqPassIndexGivenToUser();
    
    Long sequentialPassIndex = twoFactorUser.getSequentialPassIndex();

    //if (sequentialPassIndex == null && sequentialPassIndexGivenToUser != null) {
    //  int numberOfOneTimePassesShownOnScreen = TwoFactorServerConfig.retrieveConfig()
    //      .propertyValueInt("twoFactorServer.hotpSecretsShownOnScreen", 20);
    //  sequentialPassIndex = 1 + (sequentialPassIndexGivenToUser - numberOfOneTimePassesShownOnScreen);
    //}

    String institutionEnvironmentPrefix = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("duo.tokenInstitutionEnvironmentString");

    String tokenSerial = institutionEnvironmentPrefix + "__" + twoFactorUser.getLoginid() + "__hotp__500000";
    String tokenType = "h6";
    
    JSONObject tokenJsonObject = retrieveDuoTokenBySerial(tokenType, tokenSerial);
    
    String tokenId = tokenJsonObject == null ? null : tokenJsonObject.getString("token_id");
    
    if (!resetCodesIfExists && !StringUtils.isBlank(tokenId)) {
      return;
    }
    
    if (sequentialPassIndex != null && sequentialPassIndexGivenToUser != null) {
      
      //see if exists
      if (!StringUtils.isBlank(tokenId)) {
        
        deleteDuoToken(tokenId);

      }

      Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

      debugMap.put("method", "setupOneTimeCodes");
      debugMap.put("resetCodesIfExists", resetCodesIfExists);
      debugMap.put("userId", userId);
      long startTime = System.nanoTime();
      try {
      
        String twoFactorSecretUnencryptedHex = twoFactorUser.getTwoFactorSecretUnencryptedHex();
        
        //create token
        //      POST /admin/v1/tokens
        //      Parameter  Required?   Description
        //      type  Required   The type of hardware token. See Retrieve Hardware Tokens for a list of possible values.
        //      serial  Required   The serial number of the token.
        //      secret   Optional  The HOTP secret. This parameter is required for HOTP-6 and HOTP-8 hardware tokens.
        //      counter  Optional  Initial value for the HOTP counter. Default: 0. This parameter is only valid for HOTP-6 and HOTP-8 hardware tokens.
        String path = "/admin/v1/tokens";
        debugMap.put("POST", path);
        Http request = httpAdmin("POST", path);
        request.addParam("type", tokenType);
        request.addParam("serial", tokenSerial);
        request.addParam("secret", twoFactorSecretUnencryptedHex);
        request.addParam("counter", sequentialPassIndex.toString());
        signHttpAdmin(request);
        
        String result = executeRequestRaw(request);
        
        //  {
        //    "response":{
        //      "serial":"10021368_hotp_500000",
        //      "token_id":"DHKHLLTZMDPZFLQWRT6Z",
        //      "totp_step":null,
        //      "type":"h6",
        //      "users":[
        //        
        //      ]
        //    },
        //    "stat":"OK"
        //  }      
  
        JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
   
        if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
          
          debugMap.put("error", true);
          debugMap.put("result", result);
          // {"code": 40002, "message": "Invalid request parameters", "message_detail": "secret", "stat": "FAIL"}
          
          throw new RuntimeException("Bad response from Duo: " + result);
        }
        
        jsonObject = (JSONObject)jsonObject.get("response");
        
        tokenId = jsonObject.getString("token_id");

        debugMap.put("tokenId", tokenId);

      } catch (RuntimeException re) {
        debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
        throw re;
      } finally {
        DuoLog.duoLog(debugMap, startTime);
      }
      
      associateUserWithToken(userId, tokenId);      
    }
  }

  /**
   * setup an HOTP token for this index 0
   * @param twoFactorUser
   * @param userId
   * @param resetCodesIfExists
   */
  private static void setupHotpToken(TwoFactorUser twoFactorUser,
     String userId, boolean resetCodesIfExists) {
    setupHotpToken(twoFactorUser, userId, resetCodesIfExists, null);
  }

  /**
   * setup an HOTP token for this index 0
   * @param twoFactorUser
   * @param userId
   * @param resetCodesIfExists
   * @param tokenIndex
   */
  private static void setupHotpToken(TwoFactorUser twoFactorUser,
     String userId, boolean resetCodesIfExists, Long tokenIndex) {
    
    if (tokenIndex == null) {
      tokenIndex = twoFactorUser.getTokenIndex();

      if (tokenIndex == null || tokenIndex == 0) {
        tokenIndex = 0L;
      }
    } else {
      try {
        twoFactorUser.setTokenIndex(tokenIndex);
        twoFactorUser.store(TwoFactorDaoFactory.getFactory());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    String institutionEnvironmentPrefix = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("duo.tokenInstitutionEnvironmentString");
    
    String tokenSerial = institutionEnvironmentPrefix + "__" + twoFactorUser.getLoginid() + "__hotp__0";
    String tokenType = "h6";
    
    JSONObject tokenJsonObject = retrieveDuoTokenBySerial(tokenType, tokenSerial);
    
    String tokenId = tokenJsonObject == null ? null : tokenJsonObject.getString("token_id");

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "setupHotpToken");

    debugMap.put("userId", userId);
    debugMap.put("resetCodesIfExists", resetCodesIfExists);

    if (!resetCodesIfExists && !StringUtils.isBlank(tokenId)) {
      
      //System.out.println("Token exists: " + tokenSerial);
      debugMap.put("tokenExists", true);
      return;
    }
    
    //see if exists
    if (!StringUtils.isBlank(tokenId)) {
      
      deleteDuoToken(tokenId);

    }
    
    long startTime = System.nanoTime();
    try {

      String twoFactorSecretUnencryptedHex = twoFactorUser.getTwoFactorSecretUnencryptedHex();

      //create token
      //      POST /admin/v1/tokens
      //      Parameter  Required?   Description
      //      type  Required   The type of hardware token. See Retrieve Hardware Tokens for a list of possible values.
      //      serial  Required   The serial number of the token.
      //      secret   Optional  The HOTP secret. This parameter is required for HOTP-6 and HOTP-8 hardware tokens.
      //      counter  Optional  Initial value for the HOTP counter. Default: 0. This parameter is only valid for HOTP-6 and HOTP-8 hardware tokens.
      String path = "/admin/v1/tokens";

      debugMap.put("POST", path);
      
      Http request = httpAdmin("POST", path);
      request.addParam("type", tokenType);
      request.addParam("serial", tokenSerial);
      request.addParam("secret", twoFactorSecretUnencryptedHex);
      request.addParam("counter", tokenIndex.toString());
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "response":{
      //      "serial":"10021368_hotp_500000",
      //      "token_id":"DHKHLLTZMDPZFLQWRT6Z",
      //      "totp_step":null,
      //      "type":"h6",
      //      "users":[
      //        
      //      ]
      //    },
      //    "stat":"OK"
      //  }      

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
 
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
      
      tokenId = jsonObject.getString("token_id");

      debugMap.put("returnedTokenId", tokenId);

    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }

    associateUserWithToken(userId, tokenId);
  }

  /**
   * setup an TOTP token
   * @param twoFactorUser
   * @param userId
   * @param resetCodesIfExists
   * @param periodSeconds 30 or 60
   */
  private static void setupTotp(TwoFactorUser twoFactorUser,
     String userId, boolean resetCodesIfExists, int periodSeconds) {
    
    Long tokenIndex = twoFactorUser.getTokenIndex();
    if (tokenIndex == 0) {
      tokenIndex = 0L;
    }

    if (periodSeconds != 30 && periodSeconds != 60) {
      throw new RuntimeException("Period must be 30 or 60");
    }
    
    String institutionEnvironmentPrefix = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("duo.tokenInstitutionEnvironmentString");

    String tokenSerial = institutionEnvironmentPrefix + "__" + twoFactorUser.getLoginid() + "__totp__" + periodSeconds;
    String tokenType = "t6";
    
    JSONObject tokenJsonObject = retrieveDuoTokenBySerial(tokenType, tokenSerial);
    
    String tokenId = tokenJsonObject == null ? null : tokenJsonObject.getString("token_id");
    
    if (!resetCodesIfExists && !StringUtils.isBlank(tokenId)) {
      return;
    }
    
    //see if exists
    if (!StringUtils.isBlank(tokenId)) {
      
      deleteDuoToken(tokenId);

    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "setupTotp");
    debugMap.put("userId", userId);
    long startTime = System.nanoTime();
    try {

      String twoFactorSecretUnencryptedHex = twoFactorUser.getTwoFactorSecretUnencryptedHex();

      //create token
      //      POST /admin/v1/tokens
      //      Parameter  Required?   Description
      //      type  Required   The type of hardware token. See Retrieve Hardware Tokens for a list of possible values.
      //      serial  Required   The serial number of the token.
      //      secret   Optional  The HOTP secret. This parameter is required for HOTP-6 and HOTP-8 hardware tokens.
      //      totp_step  Optional  The TOTP time step. Default: 30 seconds. This parameter is only valid for TOTP-6 and TOTP-8 hardware tokens.
      String path = "/admin/v1/tokens";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("type", tokenType);
      request.addParam("serial", tokenSerial);
      request.addParam("secret", twoFactorSecretUnencryptedHex);
      request.addParam("totp_step", Integer.toString(periodSeconds));
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "response":{
      //      "serial":"10021368_hotp_500000",
      //      "token_id":"DHKHLLTZMDPZFLQWRT6Z",
      //      "totp_step":null,
      //      "type":"h6",
      //      "users":[
      //        
      //      ]
      //    },
      //    "stat":"OK"
      //  }      

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      jsonObject = (JSONObject)jsonObject.get("response");

      tokenId = jsonObject.getString("token_id");
      debugMap.put("tokenId", tokenId);

    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }

    
    associateUserWithToken(userId, tokenId);
  }

  /**
   * @param userId
   * @param tokenId
   */
  private static void associateUserWithToken(String userId, String tokenId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "associateUserWithToken");
    debugMap.put("userId", userId);
    debugMap.put("tokenId", tokenId);
    long startTime = System.nanoTime();
    try {
    
      //associate token with user
      //  POST /admin/v1/users/[user_id]/tokens
      //  token_id
      String path = "/admin/v1/users/" + userId + "/tokens";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("token_id", tokenId);
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      // {"response": "", "stat": "OK"}
  
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
   
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * @param userId
   * @param phoneId
   */
  private static void associateUserWithPhone(String userId, String phoneId) {
    
    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("userId is null");
    }
    
    if (StringUtils.isBlank(phoneId)) {
      throw new RuntimeException("phoneId is null");
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "associateUserWithPhone");
    debugMap.put("userId", userId);
    debugMap.put("phoneId", phoneId);
    long startTime = System.nanoTime();
    try {
    
      //associate token with user
      //  POST /admin/v1/users/[user_id]/phones
      //  phone_id
      String path = "/admin/v1/users/" + userId + "/phones";
      
      debugMap.put("path", path);
      
      Http request = httpAdmin("POST", path);
      request.addParam("phone_id", phoneId);
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      // {"response": "", "stat": "OK"}
  
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
   
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * @param userId
   * @param phoneId
   */
  private static void disassociateUserWithPhone(String userId, String phoneId) {
    
    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("userId is null");
    }
    
    if (StringUtils.isBlank(phoneId)) {
      throw new RuntimeException("phoneId is null");
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "disassociateUserWithPhone");
    debugMap.put("userId", userId);
    debugMap.put("phoneId", phoneId);
    long startTime = System.nanoTime();
    try {
    
      //associate token with user
      //  DELETE /admin/v1/users/[user_id]/phones/[phone_id]
      //  phone_id
      String path = "/admin/v1/users/" + userId + "/phones/" + phoneId;
      
      debugMap.put("path", path);
      
      Http request = httpAdmin("DELETE", path);
      request.addParam("phone_id", phoneId);
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      // {"response": "", "stat": "OK"}
  
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
   
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * @param tokenId
   * @return json object
   */
  private static JSONObject deleteDuoToken(String tokenId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoToken");
    debugMap.put("tokenId", tokenId);
    long startTime = System.nanoTime();
    try {

      //lets delete
      //DELETE /admin/v1/tokens/[token_id]
      String path = "/admin/v1/tokens/" + tokenId;
      
      debugMap.put("DELETE", path);
      
      Http request = httpAdmin("DELETE", path);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      // {"response": "", "stat": "OK"}
  
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
      
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      return jsonObject;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * @param phoneId
   */
  public static void deleteDuoPhone(String phoneId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoPhone");
    debugMap.put("phoneId", phoneId);
    long startTime = System.nanoTime();
    try {
    
      //lets delete
      //DELETE /admin/v1/phones/[token_id]
      String path = "/admin/v1/phones/" + phoneId;
      debugMap.put("DELETE", path);
      Http request = httpAdmin("DELETE", path);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      // {"response": "", "stat": "OK"}
  
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
      
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);

        throw new RuntimeException("Bad response from Duo: " + result);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * retrieve duo user
   * @param type
   * @param serial
   * @return the json object
   */
  public static JSONObject retrieveDuoTokenBySerial(String type, String serial) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoTokenBySerial");
    debugMap.put("type", type);
    debugMap.put("serial", type);
    long startTime = System.nanoTime();
    try {
    
      if (StringUtils.isBlank(type)) {
        throw new RuntimeException("Why is type blank?");
      }
      
      if (StringUtils.isBlank(serial)) {
        throw new RuntimeException("Why is serial blank?");
      }
      
      //lookup the token
      //  GET /admin/v1/tokens
      //  type   Optional*  
      //  serial Optional
      String path = "/admin/v1/tokens";
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);
      request.addParam("type", type);
      request.addParam("serial", serial);
         
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "response":[
      //      {
      //        "serial":"10021368_hotp_500000",
      //        "token_id":"DHKHLLTZMDPZFLQWRT6Z",
      //        "totp_step":null,
      //        "type":"h6",
      //        "users":[
      //          
      //        ]
      //      }
      //    ],
      //    "stat":"OK"
      //  }    
      
      //or
      
      //{"response": [], "stat": "OK"}
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      JSONArray responseArray = (JSONArray)jsonObject.get("response");
      if (responseArray.size() > 0) {
        if (responseArray.size() > 1) {
          throw new RuntimeException("Why more than 1 user found? " + responseArray.size() + ", " + result);
        }
        return (JSONObject)responseArray.get(0);
      }
      return null;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * retrieve duo tokens by user
   * @param userId
   * @return the json object
   */
  public static JSONArray retrieveDuoTokensByUserId(String userId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoTokensByUserId");
    debugMap.put("userId", userId);
    long startTime = System.nanoTime();
    try {
    
  
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("Why is userId blank?");
      }
      
      //lookup the token
      //  GET /admin/v1/users/[user_id]/tokens
      String path = "/admin/v1/users/" + userId + "/tokens";

      debugMap.put("GET", path);

      Http request = httpAdmin("GET", path);
         
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //    {
      //      "stat": "OK",
      //      "response": [{
      //        "type": "d1",
      //        "serial": "0",
      //        "token_id": "DHEKH0JJIYC1LX3AZWO4"
      //      },
      //      {
      //        "type": "d1",
      //        "serial": "7",
      //        "token_id": "DHUNT3ZVS3ACF8AEV2WG",
      //        "totp_step": null
      //      }]
      //    }
          
      //or
      
      //{"response": [], "stat": "OK"}
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      JSONArray responseArray = (JSONArray)jsonObject.get("response");
      
      return responseArray;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
    
  }
  
  /**
   * delete duo user
   * @param theId 
   * @return the response
   */
  public static String deleteDuoUserBySomeId(String theId) {
    
    JSONObject user = retrieveDuoUserBySomeId(theId);
    
    if (user == null) {
      System.out.println("User not found");
    } else {
      String userUuid = user.getString("user_id");
      String result = deleteDuoUser(userUuid);
      System.out.println("User deleted: " + theId + ", " + userUuid + ", " + user.getString("username"));
      return result;
    }
    return null;
  }
    
  /**
   * delete duo user and tokens
   * @param userId 
   */
  public static void deleteDuoUserAndPhonesAndTokensByUserId(String userId) {
    
    deleteDuoPhonesByUserId(userId);
    deleteDuoTokensByUserId(userId);
    deleteDuoUser(userId);
  }

  /**
   * delete duo user and tokens
   * @param someId is a userId or loginId or subjectId or whatever 
   */
  public static void deleteDuoUserAndPhonesAndTokensBySomeId(String someId) {
   
    String duoUserId = retrieveDuoUserIdBySomeId(someId);
    
    deleteDuoUserAndPhonesAndTokensByUserId(duoUserId);

  }
    
  /**
   * delete duo tokens
   * @param someId is a userId or loginId or subjectId or whatever 
   */
  public static void deleteDuoTokensBySomeId(String someId) {
   
    String duoUserId = retrieveDuoUserIdBySomeId(someId);
    
    deleteDuoTokensByUserId(duoUserId);

  }
    
  /**
   * delete duo tokens
   * @param userId is a userId or loginId or subjectId or whatever 
   */
  public static void deleteDuoTokensByUserId(String userId) {
   
    JSONArray tokenArray = retrieveDuoTokensByUserId(userId);
    
    if (tokenArray != null) {
      for (int i=0;i<tokenArray.size();i++) {
        JSONObject token = (JSONObject)tokenArray.get(i);
        
          // {
          //      "serial": "0",
          //      "token_id": "DHIZ34ALBA2445ND4AI2",
          //      "type": "d1",
          //      "totp_step": null
          // }
        String tokenId = token.getString("token_id");
        deleteDuoToken(tokenId);
      }
    }
  }

  /**
   * delete duo user alias
   * @param userId 
   * @param alias 
   * @return if added or already there
   */
  public static boolean deleteDuoUserAlias(String userId, String alias) {

    // see if its not even there
    JSONObject user = retrieveDuoUserByIdOrUsername(userId, true);
    String deleteAttributeName = null;
    for (int i=1;i<=4;i++) {
      String aliasAttributeName = "alias" + i;
      if (deleteAttributeName == null && StringUtils.equals(alias, user.getString(aliasAttributeName))) {
        deleteAttributeName = aliasAttributeName;
      }
    }

    if (StringUtils.isBlank(deleteAttributeName)) {
      //wasnt there so ignore
      return false;
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoUserAlias");
    debugMap.put("userId", userId);
    long startTime = System.nanoTime();
    try {

      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is blank: " + userId);
      }

      String path = "/admin/v1/users/" + userId;
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam(deleteAttributeName, "");
      debugMap.put("aliasName", deleteAttributeName);

      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {"response": "", "stat": "OK"}
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
    
    return true;
  }


  /**
   * add duo user alias
   * @param userId 
   * @param alias 
   * @return if added or already there
   */
  public static boolean addDuoUserAlias(String userId, String alias) {

    // does this work?
    JSONObject user = retrieveDuoUserByIdOrUsername(userId, true);
    String availableAttributeName = null;
    
    Map<String, String> params = new LinkedHashMap<String, String>();
    
    for (int i=1;i<=4;i++) {
      String aliasAttributeName = "alias" + i;
      String aliasAttributeStringValue = user.getString(aliasAttributeName);
      Object aliasAttributeObjectValue = user.get(aliasAttributeName);
      boolean aliasAttributeValueIsNull = aliasAttributeObjectValue == null || aliasAttributeObjectValue == JSONNull.getInstance();
      
      //if its already set then all good
      if (StringUtils.equals(aliasAttributeStringValue, alias)) {
        return false;
      }
      
      if (!aliasAttributeValueIsNull) {
        //we need to send all aliases if we send any
        params.put(aliasAttributeName,aliasAttributeStringValue);
      } else {
        //keep track of where we put it
        if (availableAttributeName == null) {
          availableAttributeName = aliasAttributeName;
          params.put(aliasAttributeName,alias);
        }
      }
    }

    if (StringUtils.isBlank(availableAttributeName) || params.size() == 0) {
      //cant add an alias if not one available
      throw new RuntimeException("Cant add alias '" + alias + "' to user: " + userId + ", since all aliases are being used");
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "addDuoUserAlias");
    debugMap.put("userId", userId);
    long startTime = System.nanoTime();
    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is blank: " + userId);
      }
      
      String path = "/admin/v1/users/" + userId;
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      
      for (String aliasName : params.keySet()) {
        
        String aliasValue = params.get(aliasName);
        request.addParam(aliasName, aliasValue);
        debugMap.put(aliasName, aliasValue);

      }
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {"response": "", "stat": "OK"}
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
    
    return true;
  }

  
  /**
   * delete duo user
   * @param userId 
   * @return the response
   */
  public static String deleteDuoUser(String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoUser");
    debugMap.put("userId", userId);
    long startTime = System.nanoTime();
    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is blank: " + userId);
      }
      
      String path = "/admin/v1/users/" + userId;
      debugMap.put("DELETE", path);
      Http request = httpAdmin("DELETE", path);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {"response": "", "stat": "OK"}
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      return jsonObject.getString("response");
  
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * see if a code is valid
   * @param someId
   * @param code
   * @return if code is valid
   */
  public static boolean verifyDuoCodeBySomeId(String someId, String code) {

    String duoUserId = retrieveDuoUserIdBySomeId(someId);
    
    return verifyDuoCode(duoUserId, code);
    
  }
    
  /**
   * see if a code is valid
   * @param duoUserId
   * @param code
   * @return if code is valid
   */
  public static boolean verifyDuoCode(String duoUserId, String code) {
    
    //    Http request = httpAuth("POST", "/auth/v2/preauth");
    //    request.addParam("user_id", someUserId);
    //
    //    signHttpAuth(request);
    //    
    //    String result = executeRequestRaw(request);
    //
    //    //  {
    //    //    "result":"auth",
    //    //    "status_msg":"Account is active",
    //    //    "devices":[
    //    //      {
    //    //        "name":"123461",
    //    //        "device":"DH3O4UZ8LB33ILDKUP1C",
    //    //        "type":"token"
    //    //      },
    //    //      {
    //    //        "name":"123462",
    //    //        "device":"DH8RJM5XBE5JT1A4V69S",
    //    //        "type":"token"
    //    //      }
    //    //    ]
    //    //  }
    //
    //    System.out.println("preauth: " + result);
    //    
    //    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
    //
    //    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
    //      throw new RuntimeException("Bad response from Duo: " + result);
    //    }
    //    
    //    jsonObject = (JSONObject)jsonObject.get("response");

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "verifyDuoCode");
    debugMap.put("userId", duoUserId);
    debugMap.put("code", StringUtils.repeat("*", TwoFactorServerUtils.stringLength(code)));
    long startTime = System.nanoTime();
    try {
      String path = "/auth/v2/auth";
      debugMap.put("POST", path);
      Http request = httpAuth("POST", path);
      
      request.addParam("user_id", duoUserId);
      request.addParam("factor", "passcode");
      
      //code has to be 6 digits
      if (code.length() < 6) {
        code = StringUtils.leftPad(code, 6, '0');
      }
      
      
      request.addParam("passcode", code);

      signHttpAuth(request);
      
      String result = executeRequestRaw(request);

      //System.out.println("Took " + (nanos/1000000L) + "ms");
      
      //  {
      //    "response":{
      //      "result":"allow",
      //      "status":"allow",
      //      "status_msg":"Success. Logging you in..."
      //    },
      //    "stat":"OK"
      //  }

      //  {
      //    "response":{
      //      "result":"deny",
      //      "status":"deny",
      //      "status_msg":"This passcode has already been used. Please generate a new passcode and try again."
      //    },
      //    "stat":"OK"
      //  }
          
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {

        debugMap.put("error", true);
        debugMap.put("result", result);

        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");

      String resultString = jsonObject.getString("result");
      
      boolean allowed = StringUtils.equals("allow", resultString);
      debugMap.put("allowed", allowed);
      return allowed;
    
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }

    
  }

  /**
   * initiate a push
   * @param someId
   * @param exceptionIfNotPushable
   * @param message
   * @param timeoutSeconds
   * 
   * @return tx id
   */
  private static String duoInitiatePushBySomeId(String someId, boolean exceptionIfNotPushable, String message, Integer timeoutSeconds) {

    String userId = retrieveDuoUserIdBySomeId(someId);
    return duoInitiatePush(userId, exceptionIfNotPushable, message, timeoutSeconds);

  }
  
  /**
   * initiate a push
   * @param userId
   * @param exceptionIfNotPushable 
   * @param message shown before "Request", default is Login
   * @param timeoutSeconds
   * @return tx id or null if no capable device
   */
  private static String duoInitiatePush(String userId, boolean exceptionIfNotPushable, String message, Integer timeoutSeconds) {

    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("userId is required");
    }

    long start = System.nanoTime();
    
    JSONObject duoUser = retrieveDuoUserByIdOrUsername(userId, true, timeoutSeconds);

    long millisElapsed = (System.nanoTime() - start) / 1000000;
    
    if (timeoutSeconds != null) {
      timeoutSeconds = timeoutSeconds - (int)millisElapsed;
      if (timeoutSeconds < 0) {
        timeoutSeconds = 1;
      }
    }
    
    String duoPushPhoneId = duoPushPhoneId(duoUser);

    if (StringUtils.isBlank(duoPushPhoneId)) {
      if (exceptionIfNotPushable) {
        throw new RuntimeException("User is not pushable: " + (duoUser == null ? userId : duoUser.getString("username")));
      }
      return null;
    }
    
    return duoInitiatePushByPhoneId(userId, duoPushPhoneId, message, timeoutSeconds);
    
  }

  /**
   * @param duoUserId 
   * @param duoPushPhoneId
   * @param message
   * @param timeoutSeconds throws runtime httpclient exception if timeout, or null for none
   * @return tx id or null if no capable device
   */
  public static String duoInitiatePushByPhoneId(String duoUserId, String duoPushPhoneId,
      String message, Integer timeoutSeconds) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "duoInitiatePushByPhoneId");
    debugMap.put("message", StringUtils.abbreviate(message, 50));
    long startTime = System.nanoTime();
    try {

      String path = "/auth/v2/auth";
      debugMap.put("POST", path);
      Http request = httpAuth("POST", path, timeoutSeconds);

      request.addParam("user_id", duoUserId);
      debugMap.put("user_id", duoUserId);
      request.addParam("factor", "push");
      request.addParam("async", "1");
      request.addParam("device", duoPushPhoneId);
      debugMap.put("device", duoPushPhoneId);
  
      if (!StringUtils.isBlank(message)) {
        request.addParam("type", message);
      }
      
      signHttpAuth(request);
      
      String result = executeRequestRaw(request);
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        if (result.contains("no capable device")) {
          debugMap.put("noCapableDevice", true);
          return null;
        }
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
  
      String txid = jsonObject.getString("txid");

      debugMap.put("txid", txid);

      return txid;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * @param duoUserId 
   * @param duoPhoneId
   * @param timeoutSeconds throws runtime httpclient exception if timeout, or null for none
   * @return tx id or null if no capable device
   */
  public static String duoInitiatePhoneCallByPhoneId(String duoUserId, String duoPhoneId,
      Integer timeoutSeconds) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "duoInitiatePhoneCallByPhoneId");
    long startTime = System.nanoTime();
    try {

      String path = "/auth/v2/auth";
      debugMap.put("POST", path);
      Http request = httpAuth("POST", path, timeoutSeconds);

      request.addParam("user_id", duoUserId);
      debugMap.put("user_id", duoUserId);
      request.addParam("factor", "phone");
      request.addParam("async", "1");
      request.addParam("device", duoPhoneId);
      debugMap.put("device", duoPhoneId);
  
      signHttpAuth(request);
      
      String result = executeRequestRaw(request);
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        if (result.contains("no capable device")) {
          debugMap.put("noCapableDevice", true);
          return null;
        }
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
  
      String txid = jsonObject.getString("txid");

      debugMap.put("txid", txid);

      return txid;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * set the push by default flag 
   * @param someId
   * @param pushByDefault
   */
  public static void duoPushByDefaultForSomeId(String someId, boolean pushByDefault) {
    
    String tfUserId = retrieveTfUserUuidBySomeId(someId, true);
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), tfUserId);
    twoFactorUser.setDuoPushByDefault(pushByDefault);
    twoFactorUser.store(TwoFactorDaoFactory.getFactory());
        
  }
  
  /**
   * check on a push
   * @param txId 
   * @param timeoutSeconds
   * @return if success
   */
  public static boolean duoPushOrPhoneSuccess(String txId, Integer timeoutSeconds) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "duoPushOrPhoneSuccess");
    debugMap.put("txId", txId);
    long startTime = System.nanoTime();
    try {
    

      if (StringUtils.isBlank(txId)) {
        throw new RuntimeException("txId is required");
      }
      
      //  txid  Required  The transaction ID of the authentication attempt, as returned by the /auth endpoint.
      //  result  One of the following values:
      //  Value Meaning
      //  allow Authentication succeeded. Your application should grant access to the user.
      //  deny  Authentication denied. Your application should deny access.
      //  waiting
      
      String path = "/auth/v2/auth_status";
      debugMap.put("GET", path);
      Http request = httpAuth("GET", path, timeoutSeconds);
          
      request.addParam("txid", txId);
  
      signHttpAuth(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "stat": "OK",
      //    "response": {
      //      "result": "waiting",
      //      "status": "pushed",
      //      "status_msg": "Pushed a login request to your phone..."
      //    }
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
  
      String resultField = jsonObject.getString("result");
      
      boolean allowed = StringUtils.equals(resultField, "allow");
      debugMap.put("allowed", allowed);
      return allowed;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
    
  }
  
  /**
   * 
   * @param someId
   * @return the json object
   */
  public static JSONObject retrieveDuoUserBySomeId(String someId) {
    //lets see if it is a userUuid
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(),someId);
    
    if (twoFactorUser == null) {
      //try by id
      twoFactorUser = TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveByLoginid(someId);
      if (twoFactorUser != null) {
        twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), twoFactorUser.getUuid());
      }
    }
    
    if (twoFactorUser == null) {
      //try by netId
      String theId = TfSourceUtils.resolveSubjectId(TfSourceUtils.mainSource(), someId, true);
      if (!StringUtils.isBlank(theId)) {
        twoFactorUser = TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveByLoginid(theId);
        if (twoFactorUser != null) {
          twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), twoFactorUser.getUuid());
        }
      }
    }
    
    if (twoFactorUser == null) {
      //maybe its the duo id
      return retrieveDuoUserByIdOrUsername(someId, true);
    }
    
    //we have a twoFactorUser, and a netId, use that
    String netId = TfSourceUtils.convertSubjectIdToNetId(TfSourceUtils.mainSource(), twoFactorUser.getLoginid(), true);
    return retrieveDuoUserByIdOrUsername(netId, false);
  }

  /**
   * 
   * @param someId
   * @return the duo user id
   */
  public static String retrieveDuoUserIdBySomeId(String someId) {
    return retrieveDuoUserIdBySomeId(someId, true);
  }

  /**
   * 
   * @param someId
   * @param trustDbId
   * @return the duo user id
   */
  public static String retrieveDuoUserIdBySomeId(String someId, boolean trustDbId) {
    //lets see if it is a userUuid
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), someId);
    
    if (twoFactorUser == null) {
      //try by id
      twoFactorUser = TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveByLoginid(someId);
      if (twoFactorUser != null) {
        twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), twoFactorUser.getUuid());
      }
    }
    
    if (twoFactorUser == null) {
      //try by netId
      String theId = TfSourceUtils.resolveSubjectId(TfSourceUtils.mainSource(), someId, true);
      if (!StringUtils.isBlank(theId)) {
        twoFactorUser = TwoFactorDaoFactory.getFactory().getTwoFactorUser().retrieveByLoginid(theId);
        if (twoFactorUser != null) {
          twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), twoFactorUser.getUuid());
        }
      }
    }
    
    if (twoFactorUser == null) {
      //maybe its the duo id
      return someId;
    }

    String duoUserId = twoFactorUser.getDuoUserId();
    
    if (trustDbId && !StringUtils.isBlank(duoUserId)) {
      return duoUserId;
    }
    
    //we have a twoFactorUser, and a netId, use that to get the user and user_id
    String netId = TfSourceUtils.convertSubjectIdToNetId(TfSourceUtils.mainSource(), twoFactorUser.getLoginid(), true);
    JSONObject duoUserJsonObject = retrieveDuoUserByIdOrUsername(netId, false);
    
    //not there?
    if (duoUserJsonObject == null) {
      return null;
    }
    
    duoUserId = duoUserJsonObject.getString("user_id");
    
    if (!StringUtils.isBlank(duoUserId) && !StringUtils.equals(duoUserId, twoFactorUser.getDuoUserId())) {
      twoFactorUser.setDuoUserId(duoUserId);
      try {
        twoFactorUser.store(TwoFactorDaoFactory.getFactory());
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    
    return duoUserId;
  }
  
  /**
   * 
   * @param someId
   * @param exceptionIfNotFound 
   * @return the duo user id
   */
  public static String retrieveTfUserUuidBySomeId(String someId, boolean exceptionIfNotFound) {
    //lets see if it is a userUuid
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), someId);

    if (twoFactorUser == null) {
      JSONObject userJsonObject = retrieveDuoUserByIdOrUsername(someId, false);
      if (userJsonObject != null) {
        String username = userJsonObject.getString("username");
        someId = username;
      }
    }

    if (twoFactorUser == null) {
      //try by id
      twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), someId);
    }

    if (twoFactorUser == null) {
      //try by netId
      String theId = TfSourceUtils.resolveSubjectId(TfSourceUtils.mainSource(), someId, true);
      if (!StringUtils.isBlank(theId)) {
        twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), theId);
      }
    }
    
    if (twoFactorUser == null) {
      if (exceptionIfNotFound) {
        throw new RuntimeException("Cant find user id in TF: " + someId);
      }
      return null;
    }

    return twoFactorUser.getUuid();
  }

  /**
   * retrieve duo user
   * @param theId 
   * @param isDuoUuid true if id, false if username
   * @return the json object
   */
  public static JSONObject retrieveDuoUserByIdOrUsername(String theId, boolean isDuoUuid) {
    return retrieveDuoUserByIdOrUsername(theId, isDuoUuid, null);
  }

  /**
   * retrieve duo user
   * @param theId 
   * @param isDuoUuid true if id, false if username
   * @param timeoutSeconds null if no timeout
   * @return the json object
   */
  public static JSONObject retrieveDuoUserByIdOrUsername(String theId, boolean isDuoUuid, Integer timeoutSeconds) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoUserByIdOrUsername");
    if (isDuoUuid) {
      debugMap.put("userId", theId);
    } else {
      debugMap.put("username", theId);
    }
    long startTime = System.nanoTime();
    try {
    
      if (StringUtils.isBlank(theId)) {
        throw new RuntimeException("Why is id blank?");
      }
      
      //retrieve user
      String path = "/admin/v1/users" + (isDuoUuid ? ("/" + theId) : "");
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path, timeoutSeconds);
      
      if (!isDuoUuid) {
        request.addParam("username", theId);
      }
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "response":[
      //      {
      //        "desktoptokens":[
      //          
      //        ],
      //        "email":"",
      //        "groups":[
      //          
      //        ],
      //        "last_login":null,
      //        "notes":"",
      //        "phones":[
      //          
      //        ],
      //        "realname":"",
      //        "status":"active",
      //        "tokens":[
      //          
      //        ],
      //        "user_id":"DUXEK2QS0MSI7TV3TEN1",
      //        "username":"harveycg"
      //      }
      //    ],
      //    "stat":"OK"
      //  }    
      
      // {"code": 40401, "message": "Resource not found", "stat": "FAIL"}
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
      
      if (jsonObject.has("code") && jsonObject.getInt("code") == 40401) {
        debugMap.put("code", 40401);
        return null;
      }
      
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result + ", " + theId);
      }
      Object response = jsonObject.get("response");
      JSONObject duoUser = null;
      if (response instanceof JSONObject) {
        duoUser = (JSONObject)response;
      } else {
        JSONArray responseArray = (JSONArray)response;
        if (responseArray.size() > 0) {
          if (responseArray.size() > 1) {
            throw new RuntimeException("Why more than 1 user found? " + responseArray.size() + ", " + result);
          }
          duoUser = (JSONObject)responseArray.get(0);
        }
      }
      if (duoUser != null) {
        debugMap.put("returnedUserId", duoUser.getString("user_id"));
        debugMap.put("returnedUsername", duoUser.getString("username"));
      }
      return duoUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * create duo user
   * @param twoFactorDaoFactory (if null dont store)
   * @param twoFactorUser
   * @param username 
   * @return the json object
   */
  public static JSONObject createDuoUserByUsername(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorUser twoFactorUser, String username) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createDuoUserByUsername");
    debugMap.put("username", username);
    long startTime = System.nanoTime();
    try {
    
  
      if (StringUtils.isBlank(username)) {
        throw new RuntimeException("Why is username blank?");
      }
      
      //create user
      String path = "/admin/v1/users";
      debugMap.put("POST", path);
      Http request = httpAdmin("POST", path);
      request.addParam("username", username);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
          
      //  {
      //    "response":[
      //      {
      //        "desktoptokens":[
      //          
      //        ],
      //        "email":"",
      //        "groups":[
      //          
      //        ],
      //        "last_login":null,
      //        "notes":"",
      //        "phones":[
      //          
      //        ],
      //        "realname":"",
      //        "status":"active",
      //        "tokens":[
      //          
      //        ],
      //        "user_id":"DUXEK2QS0MSI7TV3TEN1",
      //        "username":"harveycg"
      //      }
      //    ],
      //    "stat":"OK"
      //  }    
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
        
      String userId = jsonObject.getString("user_id");

      debugMap.put("userId", userId);

      if (twoFactorUser != null) {
        twoFactorUser.setDuoUserId(userId);
        if (twoFactorDaoFactory != null) {
          twoFactorUser.store(twoFactorDaoFactory);
        }
      }
  
      return jsonObject;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
    
  }

  /**
   * retrieve all from duo
   * @return the array of users
   */
  private static JSONArray retrieveAllFromDuo() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAllFromDuo");
    long startTime = System.nanoTime();
    try {
    
      String path = "/admin/v1/users";
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "response":[
      //      {
      //        "desktoptokens":[
      //          
      //        ],
      //        "email":"",
      //        "groups":[
      //          
      //        ],
      //        "last_login":null,
      //        "notes":"",
      //        "phones":[
      //          
      //        ],
      //        "realname":"",
      //        "status":"active",
      //        "tokens":[
      //          
      //        ],
      //        "user_id":"DUXEK2QS0MSI7TV3TEN1",
      //        "username":"harveycg"
      //      }
      //    ],
      //    "stat":"OK"
      //  }    
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      JSONArray responseArray = (JSONArray)jsonObject.get("response");
      return responseArray;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }
  
  /**
   * retrieve all tokens from duo
   * @return the array of users
   */
  private static JSONArray retrieveAllTokensFromDuo() {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAllTokensFromDuo");

    long startTime = System.nanoTime();
    try {
    
      String path = "/admin/v1/tokens";
      debugMap.put("path", path);
      Http request = httpAdmin("GET", path);
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "stat": "OK",
      //    "response": [{
      //      "serial": "0",
      //      "token_id": "DHIZ34ALBA2445ND4AI2",
      //      "type": "d1",
      //      "totp_step": null,
      //      "users": [{
      //        "user_id": "DUJZ2U4L80HT45MQ4EOQ",
      //        "username": "jsmith",
      //        "realname": "Joe Smith",
      //        "email": "jsmith@example.com",
      //        "status": "active",
      //        "last_login": 1343921403,
      //        "notes": ""
      //      }]
      //    }]
      //  }
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
  
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      JSONArray responseArray = (JSONArray)jsonObject.get("response");
      return responseArray;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * 
   */
  private static void deleteAllFromDuo() {

    {
      //get all users in duo
      JSONArray allUsers = retrieveAllFromDuo();
      
      if (allUsers == null || allUsers.size() == 0) {
        System.out.println("No users in duo");
        return;
      }
      
      for (int i=0; i<allUsers.size(); i++) {
        JSONObject user = (JSONObject)allUsers.get(i);
        String userUuid = user.getString("user_id");
        deleteDuoUser(userUuid);
        System.out.println("User deleted: " + userUuid + ", " + user.getString("username"));
      }
    }
    
    deleteAllTokensFromDuo();
  }

  /**
   * retrieve the alias to the username
   * @return the map
   */
  public static Map<String, JSONObject> retrieveAllAliasesFromDuo() {

    //get all users in duo
    JSONArray allUsers = retrieveAllFromDuo();
    
    Map<String, JSONObject> aliasToUsernameMap = new HashMap<String, JSONObject>();
    
    if (allUsers == null || allUsers.size() == 0) {
      return aliasToUsernameMap;
    }

    for (int i=0; i<allUsers.size(); i++) {
      JSONObject user = (JSONObject)allUsers.get(i);
      for (int j=1;j<=4;j++) {
        // alias1...4
        String aliasName = "alias" + j;
        if (user.containsKey(aliasName) && !JSONNull.getInstance().equals(user.get(aliasName))) {
          String theAlias = user.getString(aliasName);
          if (!StringUtils.isBlank(theAlias)) {
            aliasToUsernameMap.put(theAlias, user);
          }
        }
      }
    }
    return aliasToUsernameMap;
  }

  /**
   * 
   */
  private static void deleteAllTokensFromDuo() {
  
    {
      JSONArray allTokens = retrieveAllTokensFromDuo();
      
      if (allTokens == null || allTokens.size() == 0) {
        System.out.println("No tokens in duo");
        return;
      }
      
      for (int i=0; i<allTokens.size(); i++) {
        JSONObject token = (JSONObject)allTokens.get(i);
        String tokenUuid = token.getString("token_id");
        deleteDuoToken(tokenUuid);
        System.out.println("Token deleted: " + tokenUuid + ", " + token.getString("serial"));
      }
    }    
  }

  /**
   * retrieve duo user
   * @param numberOrId
   * @param isId
   * @return the json object
   */
  public static JSONObject duoPhoneByIdOrNumber(String numberOrId, boolean isId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "duoPhoneByNumber");
    debugMap.put("isId", isId);
    debugMap.put("numberOrId", numberOrId);

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(numberOrId)) {
        throw new RuntimeException("Why is numberOrId blank?");
      }
      
      //retrieve user
      String path = "/admin/v1/phones" + (isId ? ("/" + numberOrId) : "");
      debugMap.put("GET", path);
      Http request = httpAdmin("GET", path);

      if (!isId) {
        request.addParam("number", numberOrId);
      }
      
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "stat": "OK",
      //    "response": [{
      //      "phone_id": "DPFZRS9FB0D46QFTM899",
      //      "number": "+15555550100",
      //      "name": "", 
      //      "extension": "",
      //      "postdelay": null,
      //      "predelay": null,
      //      "type": "Mobile",
      //      "capabilities": [
      //        "sms",
      //        "phone",
      //        "push"
      //      ],
      //      "platform": "Apple iOS",
      //      "activated": false,
      //      "sms_passcodes_sent": false,
      //      "users": [{
      //        "user_id": "DUJZ2U4L80HT45MQ4EOQ",
      //        "username": "jsmith",
      //        "realname": "Joe Smith",
      //        "email": "jsmith@example.com",
      //        "status": "active",
      //        "last_login": 1343921403,
      //        "notes": ""
      //      }]
      //    }]
      //  }
      
      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
      
      if (jsonObject.has("code") && jsonObject.getInt("code") == 40401) {
        debugMap.put("code", 40401);
        return null;
      }
      
      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result + ", " + numberOrId);
      }
      
      Object response = jsonObject.get("response");
      
      JSONObject duoPhone = null;
      if (response instanceof JSONObject) {
        duoPhone = (JSONObject)response;
      } else {
        JSONArray responseArray = (JSONArray)response;
        if (responseArray.size() > 0) {
          if (responseArray.size() > 1) {
            throw new RuntimeException("Why more than 1 phone found? " + responseArray.size() + ", " + result + ", " + numberOrId);
          }
          duoPhone = (JSONObject)responseArray.get(0);
        }
      }
      if (duoPhone != null) {
        debugMap.put("returnedPhoneId", duoPhone.getString("phone_id"));
        debugMap.put("returnedNumber", duoPhone.getString("number"));
      }
      return duoPhone;

    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
  }

  /**
   * delete duo phones
   * @param someId is a userId or loginId or subjectId or whatever 
   */
  public static void deleteDuoPhonesBySomeId(String someId) {
  
    String duoUserId = retrieveDuoUserIdBySomeId(someId);
    
    deleteDuoPhonesByUserId(duoUserId);
  
  }

  /**
   * delete duo phones
   * @param userId is a userId or loginId or subjectId or whatever 
   */
  public static void deleteDuoPhonesByUserId(String userId) {
  
    JSONArray phoneArray = retrieveDuoPhonesByUserId(userId);
    
    if (phoneArray != null) {
      for (int i=0;i<phoneArray.size();i++) {
        JSONObject phone = (JSONObject)phoneArray.get(i);

        //   {
        //      "activated": false, 
        //      "capabilities": [
        //        "sms",
        //        "phone",
        //        "push"
        //      ],
        //      "extension": "", 
        //      "name": "", 
        //      "number": "+15035550102", 
        //      "phone_id": "DPFZRS9FB0D46QFTM890",
        //      "platform": "Apple iOS", 
        //      "postdelay": null, 
        //      "predelay": null, 
        //      "sms_passcodes_sent": false, 
        //      "type": "Mobile"
        //      "users":[{"email":"","last_login":null,"notes":"","realname":"","status":"active","user_id":"DUEO9ESQG8LBZ0WM2X7P","username":"shuque"}]
        //    }        

        String phoneId = phone.getString("phone_id");
        
        //get the phone object again, in case users arent in there
        phone = duoPhoneByIdOrNumber(phoneId, true);
        
        //if the phone is being used by more than one user, then dont delete it
        if (phone.containsKey("users")) {
          JSONArray users = phone.getJSONArray("users");
          if (users.size() <= 1) {
            deleteDuoPhone(phoneId);
          }
        }
      }
    }
  }

  /**
   * retrieve duo phones by user
   * @param userId
   * @return the json object
   */
  public static JSONArray retrieveDuoPhonesByUserId(String userId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "retrieveDuoPhonesByUserId");
    debugMap.put("userId", userId);
    long startTime = System.nanoTime();
    try {
    
  
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("Why is userId blank?");
      }
      
      //lookup the token
      //  GET /admin/v1/users/[user_id]/phones
      String path = "/admin/v1/users/" + userId + "/phones";
  
      debugMap.put("GET", path);
  
      Http request = httpAdmin("GET", path);
         
      signHttpAdmin(request);
      
      String result = executeRequestRaw(request);
      
      //  {
      //    "stat": "OK",
      //    "response": [{
      //      "activated": false, 
      //      "capabilities": [
      //        "sms",
      //        "phone",
      //        "push"
      //      ],
      //      "extension": "", 
      //      "name": "", 
      //      "number": "+15035550102", 
      //      "phone_id": "DPFZRS9FB0D46QFTM890",
      //      "platform": "Apple iOS", 
      //      "postdelay": null, 
      //      "predelay": null, 
      //      "sms_passcodes_sent": false, 
      //      "type": "Mobile"
      //    }, 
      //    {
      //      "activated": false, 
      //      "capabilities": [
      //        "phone"
      //      ].
      //      "extension": "", 
      //      "name": "", 
      //      "number": "+15035550103", 
      //      "phone_id": "DPFZRS9FB0D46QFTM891",
      //      "platform": "Unknown", 
      //      "postdelay": null, 
      //      "predelay": null, 
      //      "sms_passcodes_sent": false, 
      //      "type": "Landline"
      //    }]
      //  }
      
      //or
      
      //{"response": [], "stat": "OK"}

      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
        debugMap.put("error", true);
        debugMap.put("result", result);
        throw new RuntimeException("Bad response from Duo: " + result);
      }

      JSONArray responseArray = (JSONArray)jsonObject.get("response");
      
      return responseArray;
    } catch (RuntimeException re) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DuoLog.duoLog(debugMap, startTime);
    }
    
  }

  /**
   * initiate a push
   * @param someId
   * @param phoneIdOrNumber
   * @param isIdOrNumber
   * @param timeoutSeconds
   * 
   * @return tx id
   */
  public static String duoInitiatePhoneCallBySomeId(String someId, String phoneIdOrNumber, boolean isIdOrNumber, Integer timeoutSeconds) {
  
    String userId = retrieveDuoUserIdBySomeId(someId);
    JSONObject duoPhone = duoPhoneByIdOrNumber(phoneIdOrNumber, isIdOrNumber);
    if (duoPhone == null) {
      throw new RuntimeException("Cant find duo phone: " + phoneIdOrNumber + ", (isId? " + isIdOrNumber + ")");
    }
    String phoneId = duoPhone.getString("phone_id");
    return duoInitiatePhoneCall(userId, phoneId, timeoutSeconds);

  }

  /**
   * initiate a push
   * @param userId
   * @param duoPhoneId
   * @param timeoutSeconds
   * @return tx id or null if no capable device
   */
  private static String duoInitiatePhoneCall(String userId, String duoPhoneId, Integer timeoutSeconds) {
  
    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("userId is required");
    }
  
    long start = System.nanoTime();
    
    long millisElapsed = (System.nanoTime() - start) / 1000000;
    
    if (timeoutSeconds != null) {
      timeoutSeconds = timeoutSeconds - (int)millisElapsed;
      if (timeoutSeconds < 0) {
        timeoutSeconds = 1;
      }
    }
    
    return duoInitiatePhoneCallByPhoneId(userId, duoPhoneId, timeoutSeconds);
    
  }
}
