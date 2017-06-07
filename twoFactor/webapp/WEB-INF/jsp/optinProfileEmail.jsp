<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinProfileEmailTitle']}</title>

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
  <form action="UiMain.optinWizardSubmitEmail" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['optinProfileEmailSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />

    ${textContainer.text['optinProfileEmailText']}
    <br /><br />

    <div class="formBox profileFormBoxNarrow profileFormBox">
      <div class="formRow">
          <c:choose>
            <c:when test="twoFactorRequestContainer.editableEmail" >
              <div class="formLabelNarrow formLabel"><b><label for="email0">${textContainer.text['profileEmailLabel']}</label></b></div>
              <div class="formValue">
                <input type="text" name="email0" size="18" style="width: 16em;"
                  class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.email0) }" />
              </div>
            </c:when>
            <c:otherwise>
              <div class="formLabelNarrow formLabel"><b>${textContainer.text['profileEmailLabel']}</b></div>
              <div class="formValue">
                ${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.email0) } 
                ${textContainer.text['profileEditEmailPrefix']}<a onclick="alert('${textContainer.textEscapeSingleDouble['profileEditEmailLinkAlert']}'); return true;"
                href="${textContainer.textEscapeDouble['profileEditEmailLinkUrl']}" 
                target="_blank">${textContainer.textEscapeXml['profileEditEmailLinkText']}</a>${textContainer.text['profileEditEmailSuffix']}
              </div>
            </c:otherwise>
          </c:choose>
        <div class="formFooter">&nbsp;</div>
      </div>
    </div>
    
    <input type="hidden" name="birthdayTextfield" 
      value="${twoFactorRequestContainer.twoFactorUserLoggedIn.birthDayUuid}" />
    
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

