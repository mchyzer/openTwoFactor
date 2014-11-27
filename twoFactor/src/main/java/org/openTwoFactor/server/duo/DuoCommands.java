/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.duo;

import java.io.UnsupportedEncodingException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserView;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import com.duosecurity.client.Http;


/**
 * migrate users from open two factor to duo.  note, this can be run twice will sync things up
 */
public class DuoCommands {

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @return the http
   */
  private static Http httpAdmin(String method, String path) {
    
    String domain = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.adminDomainName");
    
    Http request = new Http(method, domain, path);
    
    return request;
  }

  /**
   * get the http for duo and set the url, 
   * @param method 
   * @param path 
   * @return the http
   */
  private static Http httpAuth(String method, String path) {
    
    String domain = TwoFactorServerConfig.retrieveConfig().propertyValueString("duo.authDomainName");
    
    Http request = new Http(method, domain, path);
    
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
    } else if (args.length == 1 && StringUtils.equals("migrateAllTokensToDuo", args[0])) {
      migrateAllTokensToDuo();
    } else if (args.length == 3 && StringUtils.equals("testCode", args[0])) {
      boolean validCode = verifyDuoCodeBySomeId(args[1], args[2]);
      System.out.println("Valid code? " + validCode);
    } else if (args.length == 1 && StringUtils.equals("deleteAllTokensFromDuo", args[0])) {
      deleteAllTokensFromDuo();
    } else if (args.length == 2 && StringUtils.equals("deleteTokensByUserFromDuo", args[0])) {
      deleteDuoTokensBySomeId(args[1]);
    } else if (args.length == 2 && StringUtils.equals("migrateTokensByUserToDuo", args[0])) {
      migrateTokensBySomeId(args[1]);
    } else if (args.length == 2 && StringUtils.equals("deleteDuoToken", args[0])) {
      deleteDuoToken(args[1]);
    } else if (args.length == 1 && StringUtils.equals("deleteAllFromDuo", args[0])) {
      deleteAllFromDuo();
    } else if (args.length == 2 && StringUtils.equals("migrateUserAndTokensToDuo", args[0])) {
      migrateUserAndTokensBySomeId(args[0]);
    } else if (args.length == 2 && StringUtils.equals("viewUser", args[0])) {
      String userLookupId = args[1];
      JSONObject duoUser = retrieveDuoUserBySomeId(userLookupId);
      if (duoUser != null) {
        System.out.println(TwoFactorServerUtils.indent(duoUser.toString(), true));
      } else {
        System.out.println("User not found");
      }
    } else if (args.length == 2 && StringUtils.equals("deleteUserAndTokens", args[0])) {
      String userLookupId = args[1];
      deleteDuoUserAndTokensBySomeId(userLookupId);
    } else if (args.length == 2 && StringUtils.equals("deleteUser", args[0])) {
      String userLookupId = args[1];
      deleteDuoUserBySomeId(userLookupId);
    } else if (args.length == 2 && StringUtils.equals("enrollUserInDuoPush", args[0])) {
      String userLookupId = args[1];
      JSONObject jsonObject = enrollUserInPushBySomeId(userLookupId);
      System.out.println(jsonObject.getString("activation_barcode"));
      
    } else if (args.length == 2 && StringUtils.equals("migratePhonesToDuo", args[0])) {
      String userLookupId = args[1];
      migratePhonesToDuoBySomeId(userLookupId, true);

    } else if (args.length == 2 && StringUtils.equals("duoBypassCode", args[0])) {
      String userLookupId = args[1];
      String code = duoBypassCodeBySomeId(userLookupId);
      System.out.println(code);

    } else if (args.length == 2 && StringUtils.equals("duoInitiatePush", args[0])) {
      String userLookupId = args[1];
      String txid = duoInitiatePushBySomeId(userLookupId, true);
      System.out.println(txid);

    } else if (args.length == 2 && StringUtils.equals("duoPushSuccess", args[0])) {
      String txId = args[1];
      boolean allowed = duoPushSuccess(txId);
      System.out.println("Allowed? " + allowed);

    } else if (args.length == 3 && StringUtils.equals("duoPushByDefault", args[0])) {
      String userId = args[1];
      boolean allowed = TwoFactorServerUtils.booleanValue(args[2]);

      duoPushByDefaultForSomeId(userId, allowed);

    } else if (args.length == 1 && StringUtils.equals("syncDuoUserIds", args[0])) {
      syncDuoUserIds();
      
    } else {

      System.out.println("Enter arg: migrateAllToDuo to migrate all users to duo");
      System.out.println("Enter arg: migrateAllTokensToDuo to migrate all users to duo");
      System.out.println("Enter arg: deleteAllTokensFromDuo to delete all tokens from duo");
      System.out.println("Enter arg: deleteAllFromDuo to delete all data from duo");
      System.out.println("Enter arg: deleteDuoToken <tokenId> to delete a token");
      System.out.println("Enter arg: viewUser <someUserId> to view a user");
      System.out.println("Enter arg: deleteUserAndTokens <someUserId> to delete a user and their tokens");
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
      
      System.exit(1);
    }
    
  }

  /**
   * 
   * @param someId
   */
  private static void migrateTokensBySomeId(String someId) {
    
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
      
      System.out.println("Created tokens for: " + twoFactorUser.getLoginid() + ", " + netId);
    } catch (Exception e) {
      System.out.println("Error: " + twoFactorUser.getLoginid() + ", " + netId);
      e.printStackTrace();
    }

  }
  
  /**
   * 
   * @param someId
   */
  public static void migrateUserAndTokensBySomeId(String someId) {
    
    migrateUserBySomeId(someId);
    
    migrateTokensBySomeId(someId);
  }
  
  /**
   * 
   * @param someId
   * @return json object
   */
  private static JSONObject enrollUserInPushBySomeId(String someId) {
    
    String userUuid = retrieveTfUserUuidBySomeId(someId, true);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), userUuid);

    if (!twoFactorUser.isOptedIn()) {
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
      
    String userId = duoUser.getString("user_id");

    associateUserWithPhone(userId, phoneId);

    //generate an activation code
    // POST /admin/v1/phones/[phone_id]/activation_url
    Http request = httpAdmin("POST", "/admin/v1/phones/" + phoneId + "/activation_url");
    signHttpAdmin(request);
    
    String result = executeRequestRaw(request);
    
    // {
    //   "stat": "OK",
    //   "response": {
    //     "activation_barcode": "https://api-abcdef.duosecurity.com/frame/qr?value=duo%3A%2F%2Factivation-code",
    //     "valid_secs": 3600
    //   }
    // }

    System.out.println(result);
    
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
 
    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result);
    }

    jsonObject = (JSONObject)jsonObject.get("response");

    return jsonObject;

    
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
    
    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("userId is required!");
    }
    
    int phoneCodeLastsMinutes = TwoFactorServerConfig.retrieveConfig()
        .propertyValueInt("twoFactorServer.phoneCodeLastsMinutes", 10);

    //cant be 0
    if (phoneCodeLastsMinutes == 0) {
      phoneCodeLastsMinutes = 1;
    }
    
    //  POST /admin/v1/users/[user_id]/bypass_codes
    //  count Optional  Number of new bypass codes to create. At most 10 codes (the default) can be created at a time. Codes will be generated randomly.
    //  codes Optional  CSV string of codes to use. Mutually exclusive with count.
    //  valid_secs  Optional  The number of seconds generated bypass codes should be valid for. If 0 (the default) the codes will never expire.
    //  reuse_count Optional  The number of times generated bypass codes can be used. If 0, the codes will have an infinite reuse_count. Default: 1.

    Http request = httpAdmin("POST", "/admin/v1/users/" + userId + "/bypass_codes");
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    JSONArray codes = (JSONArray)jsonObject.get("response");

    if (codes.size() != 1) {
      throw new RuntimeException("Why is codes size not 1??? " + codes.size());
    }
    
    return codes.getString(0);
  }

  
  /**
   * @param phoneId
   * @param number if not null, this is the phone number
   * @param mobile
   * @return the response object
   */
  public static JSONObject editDuoPhone(String phoneId, String number, boolean mobile) {

    //phone id
    if (StringUtils.isBlank(phoneId)) {
      throw new RuntimeException("phoneId is required!");
    }
        
    //edit phone
    Http request = httpAdmin("POST", "/admin/v1/phones/" + phoneId);
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    jsonObject = (JSONObject)jsonObject.get("response");
    
    return jsonObject;
  }
  
  /**
   * @param name is name of phone, e.g. phone_push, phone_0, phone_1, phone_2
   * @param number if not null, this is the phone number
   * @param mobile
   * @return the response object
   */
  public static JSONObject createDuoPhone(String name, String number, boolean mobile) {
    //create phone
    Http request = httpAdmin("POST", "/admin/v1/phones");
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    jsonObject = (JSONObject)jsonObject.get("response");
    
    return jsonObject;
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
   * sync up phones from open two factor to duo
   * @param someId
   */
  public static void migratePhonesToDuoBySomeId(String someId) {
    migratePhonesToDuoBySomeId(someId, false);
  }
  
  /**
   * sync up phones from open two factor to duo
   * @param someId
   * @param printResults if sys out print results
   */
  public static void migratePhonesToDuoBySomeId(String someId, boolean printResults) {

    //get two factor object
    String userUuid = retrieveTfUserUuidBySomeId(someId, true);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), userUuid);

    if (!twoFactorUser.isOptedIn()) {
      return;
    }

    //get the duo user
    JSONObject duoUser = retrieveDuoUserBySomeId(someId);
    
    if (duoUser == null) {
      throw new RuntimeException("Cant find duo user for someId: " + someId);
    }
    
    for (int i=0;i<3;i++) {
      
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
        
        boolean duoIsMobile = StringUtils.equalsIgnoreCase(duoPhone.getString("type"), "mobile"); 

        //note, this will likely always get updated since duo stores as +11234567890 and open two factor is 123-456-7890
        String duoNumber = TwoFactorServerUtils.trimToEmpty(duoPhone.getString("number")).replaceAll("[^\\d]", "");
        if (duoNumber.startsWith("1")) {
          duoNumber = duoNumber.substring(1);
        }
        
        phoneNumber = TwoFactorServerUtils.trimToEmpty(phoneNumber).replaceAll("[^\\d]", "");
        if (phoneNumber.startsWith("1")) {
          phoneNumber = phoneNumber.substring(1);
        }
        
        //phone number and if mobile needs to match
        if (!StringUtils.equals(duoNumber, phoneNumber)
            || (duoIsMobile != isText)) {
        
          if (printResults) {
            System.out.println("Phone for user " + twoFactorUser.getLoginid() + " " + i + " was out of sync, changing number to: " + phoneNumber + ", mobile? " + isText);
          }

          editDuoPhone(phoneId, phoneNumber, isText);
          
        }
        continue;
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
   */
  private static void migrateUserBySomeId(String someId) {
    
    String userUuid = retrieveTfUserUuidBySomeId(someId, true);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), userUuid);

    if (!twoFactorUser.isOptedIn()) {
      return;
    }
    
    String netId = TfSourceUtils.convertSubjectIdToNetId(TfSourceUtils.mainSource(), twoFactorUser.getLoginid());

    if (StringUtils.isBlank(netId)) {
      System.out.println("Cant find netId for user: " + someId);
      return;
    }

    JSONObject duoUser = retrieveDuoUserByIdOrUsername(netId, false);

    if (duoUser == null) {
      duoUser = createDuoUserByUsername(TwoFactorDaoFactory.getFactory(), twoFactorUser, netId);
      
    } else {
      System.out.println("User: " + netId + ", already exists");
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
      migrateUserBySomeId(twoFactorUserView.getUuid());
    }
    
    for (TwoFactorUserView twoFactorUserView : TwoFactorServerUtils.nonNull(
        TwoFactorDaoFactory.getFactory().getTwoFactorUserView().retrieveAllOptedInUsers())) {
      migratePhonesToDuoBySomeId(twoFactorUserView.getUuid(), true);
    }
    
    migrateAllTokensToDuo();
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
          migrateTokensBySomeId(netId);
        } catch (Exception e) {
          
          System.out.println("Problem with user: " + duoUserId);
          e.printStackTrace();
          
        }
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
      
      {
        String twoFactorSecretUnencryptedHex = twoFactorUser.getTwoFactorSecretUnencryptedHex();
  
        //create token
        //      POST /admin/v1/tokens
        //      Parameter  Required?   Description
        //      type  Required   The type of hardware token. See Retrieve Hardware Tokens for a list of possible values.
        //      serial  Required   The serial number of the token.
        //      secret   Optional  The HOTP secret. This parameter is required for HOTP-6 and HOTP-8 hardware tokens.
        //      counter  Optional  Initial value for the HOTP counter. Default: 0. This parameter is only valid for HOTP-6 and HOTP-8 hardware tokens.
        Http request = httpAdmin("POST", "/admin/v1/tokens");
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
          
          // {"code": 40002, "message": "Invalid request parameters", "message_detail": "secret", "stat": "FAIL"}
          
          throw new RuntimeException("Bad response from Duo: " + result);
        }
        
        jsonObject = (JSONObject)jsonObject.get("response");
        
        tokenId = jsonObject.getString("token_id");
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
    
    Long tokenIndex = twoFactorUser.getTokenIndex();
    if (tokenIndex == 0) {
      tokenIndex = 0L;
    }

    String institutionEnvironmentPrefix = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("duo.tokenInstitutionEnvironmentString");
    
    String tokenSerial = institutionEnvironmentPrefix + "__" + twoFactorUser.getLoginid() + "__hotp__0";
    String tokenType = "h6";
    
    JSONObject tokenJsonObject = retrieveDuoTokenBySerial(tokenType, tokenSerial);
    
    String tokenId = tokenJsonObject == null ? null : tokenJsonObject.getString("token_id");
    
    if (!resetCodesIfExists && !StringUtils.isBlank(tokenId)) {
      
      System.out.println("Token exists: " + tokenSerial);
      
      return;
    }
    
    //see if exists
    if (!StringUtils.isBlank(tokenId)) {
      
      deleteDuoToken(tokenId);

    }
    
    {
      String twoFactorSecretUnencryptedHex = twoFactorUser.getTwoFactorSecretUnencryptedHex();

      //create token
      //      POST /admin/v1/tokens
      //      Parameter  Required?   Description
      //      type  Required   The type of hardware token. See Retrieve Hardware Tokens for a list of possible values.
      //      serial  Required   The serial number of the token.
      //      secret   Optional  The HOTP secret. This parameter is required for HOTP-6 and HOTP-8 hardware tokens.
      //      counter  Optional  Initial value for the HOTP counter. Default: 0. This parameter is only valid for HOTP-6 and HOTP-8 hardware tokens.
      Http request = httpAdmin("POST", "/admin/v1/tokens");
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
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
      
      tokenId = jsonObject.getString("token_id");
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
    
    {
      String twoFactorSecretUnencryptedHex = twoFactorUser.getTwoFactorSecretUnencryptedHex();

      //create token
      //      POST /admin/v1/tokens
      //      Parameter  Required?   Description
      //      type  Required   The type of hardware token. See Retrieve Hardware Tokens for a list of possible values.
      //      serial  Required   The serial number of the token.
      //      secret   Optional  The HOTP secret. This parameter is required for HOTP-6 and HOTP-8 hardware tokens.
      //      totp_step  Optional  The TOTP time step. Default: 30 seconds. This parameter is only valid for TOTP-6 and TOTP-8 hardware tokens.
      Http request = httpAdmin("POST", "/admin/v1/tokens");
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
        throw new RuntimeException("Bad response from Duo: " + result);
      }
      
      jsonObject = (JSONObject)jsonObject.get("response");
      
      tokenId = jsonObject.getString("token_id");
    }
    
    associateUserWithToken(userId, tokenId);
  }

  /**
   * @param userId
   * @param tokenId
   */
  private static void associateUserWithToken(String userId, String tokenId) {
    //associate token with user
    //  POST /admin/v1/users/[user_id]/tokens
    //  token_id
    Http request = httpAdmin("POST", "/admin/v1/users/" + userId + "/tokens");
    request.addParam("token_id", tokenId);
    signHttpAdmin(request);
    
    String result = executeRequestRaw(request);
    
    // {"response": "", "stat": "OK"}

    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
 
    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result);
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
    
    //associate token with user
    //  POST /admin/v1/users/[user_id]/phones
    //  phone_id
    Http request = httpAdmin("POST", "/admin/v1/users/" + userId + "/phones");
    request.addParam("phone_id", phoneId);
    signHttpAdmin(request);
    
    String result = executeRequestRaw(request);
    
    // {"response": "", "stat": "OK"}

    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
 
    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result);
    }
  }
  
  /**
   * @param tokenId
   * @return json object
   */
  private static JSONObject deleteDuoToken(String tokenId) {
    
    //lets delete
    //DELETE /admin/v1/tokens/[token_id]
    Http request = httpAdmin("DELETE", "/admin/v1/tokens/" + tokenId);
    
    signHttpAdmin(request);
    
    String result = executeRequestRaw(request);
    
    // {"response": "", "stat": "OK"}

    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
    
    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    return jsonObject;
  }
  
  /**
   * @param phoneId
   * @return json object
   */
  private static JSONObject deleteDuoPhone(String phoneId) {
    
    //lets delete
    //DELETE /admin/v1/phones/[token_id]
    Http request = httpAdmin("DELETE", "/admin/v1/phones/" + phoneId);
    
    signHttpAdmin(request);
    
    String result = executeRequestRaw(request);
    
    // {"response": "", "stat": "OK"}

    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
    
    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    return jsonObject;
  }
  
  /**
   * retrieve duo user
   * @param type
   * @param serial
   * @return the json object
   */
  public static JSONObject retrieveDuoTokenBySerial(String type, String serial) {
    
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
    Http request = httpAdmin("GET", "/admin/v1/tokens");
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
  }

  /**
   * retrieve duo tokens by user
   * @param userId
   * @return the json object
   */
  public static JSONArray retrieveDuoTokensByUserId(String userId) {
    
    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("Why is userId blank?");
    }
    
    //lookup the token
    //  GET /admin/v1/users/[user_id]/tokens
    Http request = httpAdmin("GET", "/admin/v1/users/" + userId + "/tokens");
       
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    JSONArray responseArray = (JSONArray)jsonObject.get("response");
    
    return responseArray;
    
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
  public static void deleteDuoUserAndTokensByUserId(String userId) {
    
    deleteDuoTokensByUserId(userId);
    deleteDuoUser(userId);
  }

  /**
   * delete duo user and tokens
   * @param someId is a userId or loginId or subjectId or whatever 
   */
  public static void deleteDuoUserAndTokensBySomeId(String someId) {
   
    String duoUserId = retrieveDuoUserIdBySomeId(someId);
    
    deleteDuoUserAndTokensByUserId(duoUserId);

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
   * delete duo user
   * @param userId 
   * @return the response
   */
  public static String deleteDuoUser(String userId) {

    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("userId is blank: " + userId);
    }
    
    Http request = httpAdmin("DELETE", "/admin/v1/users/" + userId);
    
    signHttpAdmin(request);
    
    String result = executeRequestRaw(request);
    
    //  {"response": "", "stat": "OK"}
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    return jsonObject.getString("response");

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

    Http request = httpAuth("POST", "/auth/v2/auth");
        
    request.addParam("user_id", duoUserId);
    request.addParam("factor", "passcode");
    
    //code has to be 6 digits
    if (code.length() < 6) {
      code = StringUtils.leftPad(code, 6, '0');
    }
    
    
    request.addParam("passcode", code);

    signHttpAuth(request);
    
    long now = System.nanoTime();
    String result = executeRequestRaw(request);
    @SuppressWarnings("unused")
    long nanos = System.nanoTime() - now;
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    jsonObject = (JSONObject)jsonObject.get("response");

    String resultString = jsonObject.getString("result");
    
    return StringUtils.equals("allow", resultString);
    
  }

  /**
   * initiate a push
   * @param someId
   * @param exceptionIfNotPushable
   * @return tx id
   */
  public static String duoInitiatePushBySomeId(String someId, boolean exceptionIfNotPushable) {

    String userId = retrieveDuoUserIdBySomeId(someId);
    return duoInitiatePush(userId, exceptionIfNotPushable);

  }
  
  /**
   * initiate a push
   * @param userId
   * @param exceptionIfNotPushable 
   * @return tx id
   */
  public static String duoInitiatePush(String userId, boolean exceptionIfNotPushable) {
    
    if (StringUtils.isBlank(userId)) {
      throw new RuntimeException("userId is required");
    }
    
    JSONObject duoUser = retrieveDuoUserByIdOrUsername(userId, true);

    String duoPushPhoneId = duoPushPhoneId(duoUser);
    
    if (StringUtils.isBlank(duoPushPhoneId)) {
      if (exceptionIfNotPushable) {
        throw new RuntimeException("User is not pushable: " + (duoUser == null ? userId : duoUser.getString("username")));
      }
      return null;
    }
    
    Http request = httpAuth("POST", "/auth/v2/auth");
        
    request.addParam("user_id", userId);
    request.addParam("factor", "push");
    request.addParam("async", "1");
    request.addParam("device", duoPushPhoneId);
    

    signHttpAuth(request);
    
    String result = executeRequestRaw(request);
    
    JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     

    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    jsonObject = (JSONObject)jsonObject.get("response");

    String txid = jsonObject.getString("txid");
    
    return txid;
    
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
   * @return if success
   */
  public static boolean duoPushSuccess(String txId) {
    
    if (StringUtils.isBlank(txId)) {
      throw new RuntimeException("txId is required");
    }
    
    //  txid  Required  The transaction ID of the authentication attempt, as returned by the /auth endpoint.
    //  result  One of the following values:
    //  Value Meaning
    //  allow Authentication succeeded. Your application should grant access to the user.
    //  deny  Authentication denied. Your application should deny access.
    //  waiting
    
    Http request = httpAuth("GET", "/auth/v2/auth_status");
        
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    jsonObject = (JSONObject)jsonObject.get("response");

    String resultField = jsonObject.getString("result");
    
    return StringUtils.equals(resultField, "allow");
    
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
    
    if (StringUtils.isBlank(theId)) {
      throw new RuntimeException("Why is netId blank?");
    }
    
    //retrieve user
    Http request = httpAdmin("GET", "/admin/v1/users" + (isDuoUuid ? ("/" + theId) : ""));
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
      return null;
    }
    
    if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
      throw new RuntimeException("Bad response from Duo: " + result + ", " + theId);
    }
    Object response = jsonObject.get("response");
    if (response instanceof JSONObject) {
      return (JSONObject)response;
    }
    JSONArray responseArray = (JSONArray)response;
    if (responseArray.size() > 0) {
      if (responseArray.size() > 1) {
        throw new RuntimeException("Why more than 1 user found? " + responseArray.size() + ", " + result);
      }
      return (JSONObject)responseArray.get(0);
    }
    return null;
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
    
    if (StringUtils.isBlank(username)) {
      throw new RuntimeException("Why is username blank?");
    }
    
    //create user
    Http request = httpAdmin("POST", "/admin/v1/users");
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    jsonObject = (JSONObject)jsonObject.get("response");
      
    String userId = jsonObject.getString("user_id");
    
    if (twoFactorUser != null) {
      twoFactorUser.setDuoUserId(userId);
      if (twoFactorDaoFactory != null) {
        twoFactorUser.store(twoFactorDaoFactory);
      }
    }

    return jsonObject;
    
  }

  /**
   * retrieve all from duo
   * @return the array of users
   */
  private static JSONArray retrieveAllFromDuo() {
    Http request = httpAdmin("GET", "/admin/v1/users");
    
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    JSONArray responseArray = (JSONArray)jsonObject.get("response");
    return responseArray;
  }
  
  /**
   * retrieve all tokens from duo
   * @return the array of users
   */
  private static JSONArray retrieveAllTokensFromDuo() {
    Http request = httpAdmin("GET", "/admin/v1/tokens");
    
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
      throw new RuntimeException("Bad response from Duo: " + result);
    }
    
    JSONArray responseArray = (JSONArray)jsonObject.get("response");
    return responseArray;
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
}