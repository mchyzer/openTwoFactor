<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['helpFriendTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader'] }</h1>
  <br />
<b>${textContainer.text['helpFriendSubheader'] }</b>
<br /> <br />
${textContainer.text['helpFriendParagraph1'] }
<br /><br />
${textContainer.text['helpFriendParagraph2'] }
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  
      <c:choose>
        <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasColleaguesIdentifiedUser}" >
  
          <c:forEach items="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.colleaguesIdentifiedUser}" 
              var="colleagueIdentifiedUser"  >
            
            <c:choose>
              <c:when test="${colleagueIdentifiedUser.invitedColleaguesWithinAllottedTime}">
                
                <form action="UiMain.optOutColleague" method="post" style="display: inline">
                  <input value="${textContainer.textEscapeDouble['helpFriendOptOutButtonPrefix'] } ${fn:escapeXml(colleagueIdentifiedUser.loginid) }" class="tfBlueButton"
                    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
                    onclick="return confirm('${textContainer.textEscapeSingleDouble['helpFriendOptOutConfirm']}');" />
                  <input type="hidden" name="userIdOperatingOn" 
                    value="${fn:escapeXml(colleagueIdentifiedUser.uuid) }" />
                </form>
              
              </c:when>
              <c:otherwise>
                 <b>${ fn:escapeXml(colleagueIdentifiedUser.name) }</b> ${textContainer.text['helpFriendNotRequestedSuffix']}
              </c:otherwise>
            </c:choose>
            <br />            
          </c:forEach>
        </c:when>
        <c:otherwise>
          ${textContainer.text['helpFriendNoFriends'] }
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
