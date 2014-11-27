<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>
<%-- dont be too descriptive here since this is on the printed form --%>
<title>${textContainer.text['duoPushTitle']}</title>
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
  <br />
  <b>${textContainer.text['duoPushSubheader']}</b>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  ${textContainer.text['duoPushExplanation']}
  <br /><br />
    <c:choose>
      <c:when test="${twoFactorRequestContainer.twoFactorDuoPushContainer.enrolling}">

        ${textContainer.text['duoPushEnrollInstructions']}

        <br /><br /><br />

        <img src="${twoFactorRequestContainer.twoFactorDuoPushContainer.barcodeUrl}" />

        <br /><br /><br />
        <form action="../../twoFactorUi/app/UiMain.duoPushTest" method="post" style="display: inline; font-size: smaller">
          <input value="${textContainer.textEscapeDouble['buttonDuoPushTest']}" class="tfLinkButton"
            type="submit" />
        </form>
      
      </c:when>
      <c:otherwise>
      
        <c:choose>
          <c:when test="${twoFactorRequestContainer.twoFactorDuoPushContainer.enrolledInDuoPush}">
            ${textContainer.text['duoPushEnrolledText']}
            
            <br /><br />
            <form action="../../twoFactorUi/app/UiMain.duoPushTest" method="post" style="display: inline; font-size: smaller">
              <input value="${textContainer.textEscapeDouble['buttonDuoPushTest']}" class="tfLinkButton"
                type="submit" />
            </form>
    
            <br /><br />
            <form action="../../twoFactorUi/app/UiMain.duoPushUnenroll" method="post" style="display: inline; font-size: smaller">
              <input value="${textContainer.textEscapeDouble['buttonDuoPushUnenroll']}" class="tfLinkButton"
                type="submit" />
            </form>

            <br /><br />
            <c:choose>
              <c:when test="${twoFactorRequestContainer.twoFactorDuoPushContainer.pushForWeb}">
                ${textContainer.text['duoPushEnrolledInPushForWeb']}
                <br /><br />
                <form action="../../twoFactorUi/app/UiMain.duoPushUnenrollWeb" method="post" style="display: inline; font-size: smaller">
                  <input value="${textContainer.textEscapeDouble['buttonDuoPushUnenrollWeb']}" class="tfLinkButton"
                    type="submit" />
                </form>
                
              </c:when>
              <c:otherwise>
                ${textContainer.text['duoPushNotEnrolledInPushForWeb']}
                <br /><br />
                <form action="../../twoFactorUi/app/UiMain.duoPushEnrollWeb" method="post" style="display: inline; font-size: smaller">
                  <input value="${textContainer.textEscapeDouble['buttonDuoPushEnrollWeb']}" class="tfLinkButton"
                    type="submit" />
                </form>
              </c:otherwise>
            </c:choose>
            
          </c:when>
          <c:otherwise>
            ${textContainer.text['duoPushNotEnrolledText']}
    
            <br /><br />
            <form action="../../twoFactorUi/app/UiMain.duoPushEnroll" method="post" style="display: inline; font-size: smaller">
              <input value="${textContainer.textEscapeDouble['buttonDuoPushEnroll']}" class="tfLinkButton"
                type="submit" />
            </form>
    
    
          </c:otherwise>
        </c:choose>
      
      </c:otherwise>
    </c:choose>
    

    <br /><br />
    <br /><br />
    <br /><br />
    <br /><br />

      <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
        <div class="logoutBottom">
          <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
          &nbsp;
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

