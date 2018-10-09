/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.ui.beans;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * container for stuff about duo push
 */
public class TwoFactorDuoPushContainer {

  /**
   * if duo enabled
   * @return if duo enabled
   */
  public boolean isDuoEnabled() {
    boolean duoRegisterUsers = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duo.registerUsers", true);
    return duoRegisterUsers;
  }

  /**
   * if duo web option enabled
   * @return if duo web option enabled
   */
  public boolean isAllowUsersToControlDuoWeb() {
    boolean allowUsersToControlDuoWeb = TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("duo.allowUsersToControlDuoWeb", true);
    return allowUsersToControlDuoWeb;
  }
  
  /**
   * duo user
   */
  private JSONObject duoUser;
  
  /**
   * duo user
   * @return the duoUser
   */
  public JSONObject getDuoUser() {
    return this.duoUser;
  }
  
  /**
   * duo user
   * @param duoUser1 the duoUser to set
   */
  public void setDuoUser(JSONObject duoUser1) {
    this.duoUser = duoUser1;
  }

  /**
   * barcode url for enrolling in push
   */
  private String barcodeUrl;
  
  /**
   * barcode url for enrolling in push
   * @return the barcodeUrl
   */
  public String getBarcodeUrl() {
    return this.barcodeUrl;
  }

  /**
   * duo qr code url text
   */
  private String duoQrUrlText;
  
  /**
   * @return the duoQrUrl
   */
  public String getDuoQrUrlText() {
    return this.duoQrUrlText;
  }
  
  /**
   * @param duoQrUrlText1 the duoQrUrl to set
   */
  public void setDuoQrUrlText(String duoQrUrlText1) {
    this.duoQrUrlText = duoQrUrlText1;
  }

  /**
   * barcode url for enrolling in push
   * @param barcodeUrl1 the barcodeUrl to set
   */
  public void setBarcodeUrl(String barcodeUrl1) {
    this.barcodeUrl = barcodeUrl1;
  }

  /**
   * if in process of enrolling in push
   */
  private boolean enrolling = false;
  
  /**
   * if in process of enrolling in push
   * @return the enrolling
   */
  public boolean isEnrolling() {
    return this.enrolling;
  }

  
  /**
   * if in process of enrolling in push
   * @param enrolling1 the enrolling to set
   */
  public void setEnrolling(boolean enrolling1) {
    this.enrolling = enrolling1;
  }

  
  
  /**
   * init the container
   * @param twoFactorUser
   */
  public void init(TwoFactorUser twoFactorUser) {

    if (this.duoUser == null) {
      
      String duoUserId = DuoCommands.retrieveDuoUserIdBySomeId(twoFactorUser.getLoginid(), false);
      
      if (!StringUtils.isBlank(duoUserId)) {
        this.duoUser = DuoCommands.retrieveDuoUserByIdOrUsername(duoUserId, true);
                
        this.enrolledInDuoPush = this.duoUser == null ? false : DuoCommands.enrolledInPush(this.duoUser);
        
        this.pushForWeb = TwoFactorServerUtils.booleanValue(twoFactorUser.getDuoPushByDefault(), false);
      }
    }
  }
  
  /**
   * null safe way to get push phone label
   * @return the push phone label
   */
  public String getPushPhoneLabel() {
    JSONObject jsonObject = this.duoUser;
    if (jsonObject == null) {
      return null;
    }
    
//    {  
//      "response":{  
//         "phones":[  
//            {  
//               "activated":true,
//               "capabilities":[  
//                  "auto",
//                  "push",
//                  "mobile_otp"
//               ],
//               "extension":"",
//               "last_seen":"2018-09-26T15:58:33",
//               "name":"phone_push",
//               "number":"",
//               "phone_id":"DPVNA9Q331ENU27RKA6Y",
//               "platform":"Apple iOS",
//               "postdelay":"",
//               "predelay":"",
//               "sms_passcodes_sent":false,
//               "type":"Mobile"
    
    JSONObject responseJsonObject = null;
    if (!jsonObject.has("response")) {
      responseJsonObject = jsonObject;
    } else {
      responseJsonObject = jsonObject.getJSONObject("response");

    }
    
    if (!responseJsonObject.has("phones")) {
      return null;
    }
    
    JSONArray phoneJsonArray = responseJsonObject.getJSONArray("phones");
    
    for (int i=0;i<phoneJsonArray.size();i++) {
      JSONObject phoneObject = phoneJsonArray.getJSONObject(i);
      if (phoneObject.has("name")) {
        if (StringUtils.equals(phoneObject.getString("name"), "phone_push")) {
          StringBuilder response = new StringBuilder();
          if (phoneObject.has("type")) {
            response.append("Type: ").append(phoneObject.getString("type"));
          }
          if (phoneObject.has("platform")) {
            if (response.length() > 0) {
              response.append(", ");
            }
            response.append("platform: ").append(phoneObject.getString("platform"));
          }
          if (phoneObject.has("phone_id")) {
            if (response.length() > 0) {
              response.append(", ");
            }
            response.append("id: ").append(StringUtils.abbreviate(phoneObject.getString("phone_id"), 6));
          }
          return response.toString();
        }
      }
    }
    
    return null;
  }
  
  /**
   * if the user is enrolled in duo push
   */
  private boolean enrolledInDuoPush = false;
  
  /**
   * if the user is enrolled in duo push
   * @return if enrolled in duo push
   */
  public boolean isEnrolledInDuoPush() {
    return this.enrolledInDuoPush;
  }
  
  /**
   * if the user is enrolled in duo push
   * @param enrolledInDuoPush1 the enrolledInDuoPush to set
   */
  public void setEnrolledInDuoPush(boolean enrolledInDuoPush1) {
    this.enrolledInDuoPush = enrolledInDuoPush1;
  }

  /**
   * if should do push for web
   */
  private boolean pushForWeb;

  
  /**
   * if should do push for web
   * @return the pushForWeb
   */
  public boolean isPushForWeb() {
    return this.pushForWeb;
  }

  
  /**
   * if should do push for web
   * @param pushForWeb1 the pushForWeb to set
   */
  public void setPushForWeb(boolean pushForWeb1) {
    this.pushForWeb = pushForWeb1;
  }

  
}
