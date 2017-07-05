<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminConfirmUserTitle']}</title>

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
  <form action="UiMain.optinWizardSubmitPhoneCode" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['adminConfirmUserSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    ${textContainer.text['adminConfirmUserText']}
    <br /><br />
    ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.description) }
    <br /><br />
    <div class="formBox profileFormBox profileFormBoxWide">
    
      <div class="formRow">
        <div class="formLabel" style="text-align: right">
          <b><label for="checkedIdNoId">${textContainer.text['adminConfirmCheckId']}</label></b>
        </div>
        <div class="formValue">
          <input type="radio" id="checkedIdNoId" name="checkedIdName" value="false" /> ${textContainer.text['adminConfirmNo']} &nbsp;&nbsp;
          <input type="radio" id="checkedIdYesId" name="checkedIdName" value="false" />  ${textContainer.text['adminConfirmYes']}
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
    </div>
    <br /><br />
    <input type="hidden" name="userIdOperatingOn" 
      value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
    
    <br />
    <br />
    <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['optinCancelButton']}</a> &nbsp; &nbsp; &nbsp; &nbsp;
    <input value="${textContainer.textEscapeDouble['buttonNextStep']}" class="tfBlueButton" 
       type="submit" />

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
</form>

<c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.showSerialSection && twoFactorRequestContainer.twoFactorAdminContainer.allowSerialNumberRegistration}">
  <script type="text/javascript">
    $('#activeHardwareTokenButtonId').click();
    $('#serialRadioId').click();
  </script>
</c:if>

</body></html>

