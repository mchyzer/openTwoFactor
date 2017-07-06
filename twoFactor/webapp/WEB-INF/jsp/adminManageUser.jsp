<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminManageUserTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

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
    <div class="paragraphs">

    <h2>${textContainer.text['adminManageUserSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br /><br />
    ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.description) }
    <br /><br />
    
    <c:if test="${twoFactorRequestContainer.twoFactorConfigContainer.allowAdminsToGenerateCodesforUsers}">
      <form action="UiMainAdmin.generateCodeSubmit" method="post" style="display: inline">
        <input value="${textContainer.textEscapeDouble['adminGenerateCodeFor']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.name) }" 
          class="tfBlueButton"
           type="submit"
          onclick="return confirm('${textContainer.textEscapeSingleDouble['adminGenerateCodeConfirm']}');" />
        <input type="hidden" name="userIdOperatingOn" 
          value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
      </form>
      &nbsp;&nbsp;
    </c:if>
    <form action="UiMainAdmin.optOutSubmit" method="post" style="display: inline">
      <input value="${textContainer.textEscapeDouble['adminOptOutPerson']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.name) }" 
        class="tfBlueButton"
         type="submit"
        onclick="return confirm('${textContainer.textEscapeSingleDouble['adminOptOutConfirm']}');" />
      <input type="hidden" name="userIdOperatingOn" 
        value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
    </form>
    &nbsp;&nbsp;
    <c:choose>
      <c:when test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.trustedBrowserCount > 0}">
        <form action="UiMainAdmin.untrustBrowsers" method="post" style="display: inline">
          <input value="${textContainer.textEscapeDouble['adminUntrustBrowsersFor']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.name) }" 
          class="tfBlueButton"
           type="submit"
          onclick="return confirm('${textContainer.textEscapeSingleDouble['adminUntrustConfirm']}');" />
          <input type="hidden" name="userIdOperatingOn" 
            value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
        </form>
      </c:when>
      <c:otherwise>
          <input value="${textContainer.textEscapeDouble['adminUntrustBrowsersFor']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.name) }" 
            class="tfBlueButton"
             type="submit"
            onclick="alert('${textContainer.textEscapeSingleDouble['buttonUntrustBrowsersNone']}'); return false;" />
      </c:otherwise>
      
    </c:choose>

    <br /><br />
    <input type="hidden" name="userIdOperatingOn" 
      value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
    
    <br />
    <br />
    <form action="../../twoFactorAdminUi/app/UiMainAdmin.adminIndex" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['optinCancelButton']}" class="tfLinkButton"
       type="submit" />
    </form>

    <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
      <div class="logoutBottom">
        <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
        &nbsp; &nbsp;      
      </div>
    </c:if>

    <br />
    <br />
    <%@ include file="../assetsJsp/commonAbout.jsp"%>

  </div>

<c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.showSerialSection && twoFactorRequestContainer.twoFactorAdminContainer.allowSerialNumberRegistration}">
  <script type="text/javascript">
    $('#activeHardwareTokenButtonId').click();
    $('#serialRadioId').click();
  </script>
</c:if>

</body></html>

