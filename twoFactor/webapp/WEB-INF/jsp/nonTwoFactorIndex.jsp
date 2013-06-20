<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['indexTitle'] }</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

<!-- nonTwoFactorIndex.jsp -->
</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader'] }</h1>
  <br />
  <b>${textContainer.text['havingTroubleSubheader']}</b>
  <br /><br />
  ${textContainer.text['havingTroubleParagraph1']}
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
    <c:choose>
      <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
        <b>${textContainer.text['havingTroubleEnrolled']}</b>
        
        <br /><br />
        

        <h1>${textContainer.text['havingTroubleGetCodeByPhone']}</h1>
        <br />
        <c:choose>
          <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasPhoneNumbers}" >
            
            <c:set var="i" value="0" />
            <c:forEach items="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.phonesForScreen}" 
                var="twoFactorPhoneForScreen"  >
              
              <c:if test="${twoFactorPhoneForScreen.hasPhone}">

                <div class="formBox" style="width: 28em">
                  <div class="formRow" style="height: 2em">
                    <div class="formValue" style="width: 12em">
                      <c:choose>
                        
                        <c:when test="${twoFactorPhoneForScreen.voice}">
                          <form action="UiMainPublic.phoneCode" method="post" style="display: inline">
                            <input value="${textContainer.textEscapeDouble['havingTroubleVoicePrefix']} ${ fn:escapeXml(twoFactorPhoneForScreen.phoneForScreen ) }" class="tfBlueButton"
                              onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
                               />
                            <input type="hidden" name="phoneIndex" 
                              value="${i}" />
                            <input type="hidden" name="phoneType" 
                              value="voice" />
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
                            onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
                             />
                          <input type="hidden" name="phoneIndex" 
                            value="${i}" />
                          <input type="hidden" name="phoneType" 
                            value="text" />
                        </form>
                      </c:if>                  
                    </div>
                  </div>
                </div>


              </c:if>
              <br />            
              <c:set var="i" value="${i+1}" />
            </c:forEach>
                  
            
          </c:when>
          <c:otherwise>
            ${textContainer.text['havingTroubleNoPhoneNumbers']}
          </c:otherwise>
        </c:choose>
            
            
        <br /><br />

        <h1>${textContainer.text['havingTroubleGetHelpFromFriend']}</h1>
        <br />
        <c:choose>
          <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasColleagueLoginids}" >

            <c:choose>
              <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.invitedColleagues}" >
                ${textContainer.text['havingTroubleYouHaveFriendsSubtitle']}
              </c:when>
              <c:otherwise>
                <form action="../../twoFactorPublicUi/app/UiMainPublic.allowColleaguesToOptYouOut" method="post" style="display: inline">
                  <input value="${textContainer.textEscapeDouble['havingTroubleAllowFriendButton']}" class="tfBlueButton"
                    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
                </form> &nbsp;
                              
              </c:otherwise>
            </c:choose>
            <br /><br />
            ${textContainer.text['havingTroubleFriendAllowedToOptYouOut']}
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
            ${textContainer.text['havingTroubleAllowFriendsPrefix']} <c:if test="${!twoFactorRequestContainer.twoFactorHelpLoggingInContainer.invitedColleagues}" ><form
             action="../../twoFactorPublicUi/app/UiMainPublic.allowColleaguesToOptYouOut" method="post" style="display: inline"><input
                value="${textContainer.textEscapeDouble['havingTroubleAllowFriendsLink']}" class="tfBlueButton"
                  style="background: none; border: none; color: #7794C9; text-decoration: underline; cursor: pointer;"
                  type="submit" /></form></c:if> ${textContainer.text['havingTroubleAllowFriendsSuffix']}
            <br /><br />

          </c:when>
          <c:otherwise>
            <b>${textContainer.text['havingTroubleNoFriends']}</b>
          </c:otherwise>
        </c:choose>

      </c:when>
      <c:otherwise>
        ${textContainer.text['havingTroubleNotEnrolled']}
      </c:otherwise>  
    </c:choose>
    <br /><br />
    <form action="../../twoFactorUi/app/UiMain.index" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
    </form>
    

   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
