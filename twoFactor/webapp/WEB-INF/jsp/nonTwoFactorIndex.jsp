<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['indexTitle'] }</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

<!-- nonTwoFactorIndex.jsp -->
</head>
<body alink="#cc6600" bgcolor="#ffffff" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <div id="headerDiv">
    <div class="alignleft"><h1 style="display: inline;">${textContainer.text['pageHeader'] }</h1></div>
    <div class="alignright">
      <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
        <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
      </c:if>    
    </div> 
    <div class="clearboth"></div> 
  </div> 
  <h2>${textContainer.text['havingTroubleSubheader']}</h2>
  <div class="paragraphs">
    <br />
    <br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />

      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
          
          ${textContainer.text['havingTroubleEnrolled']}
          <br /><br />
          ${textContainer.text['havingTroubleParagraph1']}
          <br />
          <c:choose>
            <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasPhoneNumbers}" >
              
              <c:set var="i" value="0" />
              <c:forEach items="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.phonesForScreen}" 
                  var="twoFactorPhoneForScreen"  >
                <c:if test="${twoFactorPhoneForScreen.hasPhone}">
  
                  <div class="formBox" style="width: 28em; margin-bottom: 1em">
                    <div class="formRow" style="height: 2em">
                      <div class="formValue" style="width: 12em">
                        <c:choose>
                          
                          <c:when test="${twoFactorPhoneForScreen.voice}">
                            <form action="UiMainPublic.phoneCode" method="post" style="display: inline">
                              <input value="${textContainer.textEscapeDouble['havingTroubleVoicePrefix']} ${ fn:escapeXml(twoFactorPhoneForScreen.phoneForScreen ) }" class="tfBlueButton"
                                 type="submit"
                                 />
                               <input type="hidden" name="relay" 
                                value="${fn:escapeXml(twoFactorRequestContainer.twoFactorHelpLoggingInContainer.relay) }" />
                              <input type="hidden" name="phoneIndex" 
                                value="${i}" />
                              <input type="hidden" name="phoneType" 
                                value="voice" />
                              <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>  
                            </form>
                          </c:when>         
                          <c:otherwise>
                            &nbsp;
                          </c:otherwise>
                        </c:choose>
                      </div>
                      <div class="formValue" style="width: 12em">
                        <c:if test="${twoFactorPhoneForScreen.text}">
                          <form action="UiMainPublic.phoneCode" method="post" style="display: inline">
                            <input value="${textContainer.textEscapeDouble['havingTroubleTextPrefix']} ${ fn:escapeXml(twoFactorPhoneForScreen.phoneForScreen ) }" class="tfBlueButton"
                               type="submit"
                               />
                              <input type="hidden" name="relay" 
                                value="${fn:escapeXml(twoFactorRequestContainer.twoFactorHelpLoggingInContainer.relay) }" />
                            <input type="hidden" name="phoneIndex" 
                              value="${i}" />
                            <input type="hidden" name="phoneType" 
                              value="text" />
                            <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>
                          </form>
                        </c:if>                  
                      </div>
                    </div>
                  </div>
  
  
                </c:if>
                <c:set var="i" value="${i+1}" />
              </c:forEach>
                    
              
            </c:when>
            <c:otherwise>
              ${textContainer.text['havingTroubleNoPhoneNumbers']}
              <br /><br />
            </c:otherwise>
          </c:choose>
          
          ${textContainer.text['havingTroubleParagraph2']}    
          <br />
  
          <c:choose>
            <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasColleagueLoginids}" >
  
              <c:choose>
                <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.invitedColleagues}" >
                  ${textContainer.text['havingTroubleFriendsInvitedSubtitle']}
                </c:when>
                <c:otherwise>
                  ${textContainer.text['havingTroubleFriendsNotInvitedSubtitle']}
                </c:otherwise>
              </c:choose>
              <br /><br />
              
              <c:set var="i" value="0" />
              <c:forEach items="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.colleagueLoginidsForScreen}" 
                  var="colleagueLoginidForScreen"  >
  
                <c:if test="${colleagueLoginidForScreen != null}">
  
                  ${ fn:escapeXml(colleagueLoginidForScreen) }
                  <br />                
                </c:if> 
                 
                <c:set var="i" value="${i+1}" />
              </c:forEach>
              <br />
              <c:choose>
                <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.invitedColleagues}" >
                  ${textContainer.text['havingTroubleYouHaveAllowedFriendParagraph']}
                </c:when>
                <c:otherwise>
                  <form action="../../twoFactorPublicUi/app/UiMainPublic.allowColleaguesToOptYouOut" method="post" style="display: inline">
                    <input value="${textContainer.textEscapeDouble['havingTroubleAllowFriendButton']}" class="tfBlueButton"
                       type="submit" />
                    <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>
                  </form> &nbsp;
                  <br /><br />
                  ${textContainer.text['havingTroubleAllowFriendsPrefix']} <c:if test="${!twoFactorRequestContainer.twoFactorHelpLoggingInContainer.invitedColleagues}" ><form
                   action="../../twoFactorPublicUi/app/UiMainPublic.allowColleaguesToOptYouOut" method="post" style="display: inline"><input
                      value="${textContainer.textEscapeDouble['havingTroubleAllowFriendsLink']}" class="tfBlueButton"
                        style="background: none; border: none; color: #7794C9; text-decoration: underline; cursor: pointer;"
                        type="submit" /><input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/></form></c:if> ${textContainer.text['havingTroubleAllowFriendsSuffix']}

                </c:otherwise>
              </c:choose>
              <br /><br />

            </c:when>
            <c:otherwise>
              ${textContainer.text['havingTroubleNoFriends']}
              <br /><br />
              <c:choose>
                <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.invitedColleagues}" >
                  ${textContainer.text['havingTroubleYouHaveAllowedFriendNoFriendsParagraph']}
                </c:when>
                <c:otherwise>
                  <form action="../../twoFactorPublicUi/app/UiMainPublic.allowColleaguesToOptYouOut" method="post" style="display: inline">
                    <input value="${textContainer.textEscapeDouble['havingTroubleAllowFriendButton']}" class="tfBlueButton"
                       type="submit" />
                    <input type="hidden" name="<csrf:token-name/>" value="<csrf:token-value />"/>
                  </form>
                </c:otherwise>
              </c:choose>
              <br />
              <br />
            </c:otherwise>
          </c:choose>
          ${textContainer.text['havingTroubleParagraph2end']}    

        </c:when>
        <c:otherwise>
          ${textContainer.text['havingTroubleNotEnrolled']}
          <c:if test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.userRequiredToOptInButIsNot}">
            <br /><br />
            ${textContainer.text['havingTroubleRequiredToOptInButIsnt'] }

            <br /><br />

            <form action="../../twoFactorUi/app/UiMain.optin" method="get" style="display: inline">
              <input value="${textContainer.textEscapeDouble['buttonOptIn']}" class="tfBlueButton"
               type="submit" />
            </form>
            <%-- 
            &nbsp;
            <form action="../../twoFactorPublicUi/app/UiMainPublic.stopOptInRequirement" method="post" style="display: inline"><input
                value="${textContainer.textEscapeDouble['havingTroubleRequiredToOptInButIsntButton']}" class="tfBlueButton"
                
                type="submit" /></form>
             --%>
          </c:if>
        </c:otherwise>  
      </c:choose>

    </div>
    <br /><br />
    <br /><br />
    <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
      <div class="logoutBottom" style="font-size: smaller">
        <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
        &nbsp; &nbsp;      
      </div>
    </c:if>    
    <form action="../../twoFactorUi/app/UiMain.index" method="get" style="display: inline; font-size: smaller">
      <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfLinkButton"
      type="submit" />
    </form>
    

   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
