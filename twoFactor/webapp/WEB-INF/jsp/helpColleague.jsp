<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['helpFriendTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

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
  <div class="paragraphs">
  
    <br />
    <b>${textContainer.text['helpFriendSubheader'] }</b>
    <br /> <br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    ${textContainer.text['helpFriendParagraph1'] }
    <br /><br />
    ${textContainer.text['helpFriendParagraph2'] }
    <br /><br />

  </div>

      <c:choose>
        <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasColleaguesIdentifiedUser}" >
          
          <c:if test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasColleaguesAuthorizedUser}" >
            ${textContainer.text['helpFriendParagraph3'] }
            <br />
            <br />
            <c:forEach items="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.colleaguesIdentifiedUser}" 
                var="colleagueIdentifiedUser"  >
              <c:if test="${colleagueIdentifiedUser.invitedColleaguesWithinAllottedTime}">
                  
                <form action="UiMain.optOutColleague" method="post" style="display: inline">
                  <spap style="white-space: nowrap"><input type="checkbox" name="checkedApproval" value="true" /> &nbsp; ${textContainer.text['helpFriendCheckboxPrefix']}${fn:escapeXml(colleagueIdentifiedUser.name)}${textContainer.text['helpFriendCheckboxSuffix']}</spap>
                  <br /><input value="${textContainer.textEscapeDouble['helpFriendOptOutButtonPrefix'] } ${fn:escapeXml(colleagueIdentifiedUser.name) }" 
                    class="tfBlueButton" style="margin-top: 0.3em"
                    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
                    onclick="return confirm('${textContainer.textEscapeSingleDouble['helpFriendOptOutConfirmPrefix']} ${twoFactor:escapeSingleQuotesAndXml(colleagueIdentifiedUser.name)} ${textContainer.textEscapeSingleDouble['helpFriendOptOutConfirmSuffix']}');" />
                  <input type="hidden" name="userIdOperatingOn" 
                    value="${fn:escapeXml(colleagueIdentifiedUser.uuid) }" />
                </form>
                <br /><br />
               </c:if>
            </c:forEach>
            <br /><br />
          </c:if>
          <c:if test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasColleaguesNotAuthorizedUser}" >
            ${textContainer.text['helpFriendListNotAuthorized'] }
            <br /><br />
            <c:forEach items="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.colleaguesIdentifiedUser}" 
                var="colleagueIdentifiedUser"  >
              
              <c:if test="${!colleagueIdentifiedUser.invitedColleaguesWithinAllottedTime}">
                  
                  <b>${ fn:escapeXml(colleagueIdentifiedUser.name) }</b>
                  <br />            
                
              </c:if>
            </c:forEach>
          </c:if>
        </c:when>
        <c:otherwise>
          ${textContainer.text['helpFriendNoIdentifiedFriends'] }
        </c:otherwise>      
      </c:choose>

  
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
