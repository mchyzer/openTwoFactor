<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinProfilePhoneTitle']}</title>

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
  <form action="UiMain.optinWizardSubmitPhones" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['optinProfilePhoneSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    
    ${textContainer.text['optinProfilePhoneText']}
    <br /><br />
    
    <div class="formBox profileFormBox profileFormBoxNarrow">
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><b><label for="phone0">${textContainer.text['profilePhoneNumberLabel1']}</label></b></div>
        <div class="formValue"><input type="text" name="phone0" size="12" 
          class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone0) }" />
          &nbsp; <input type="checkbox" name="phoneVoice0" value="true" 
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice0 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
          <input type="checkbox" name="phoneText0" value="true" 
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText0 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneTextOptionLabel']}  </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><b><label for="phone1">${textContainer.text['profilePhoneNumberLabel2']}</label></b></div>
        <div class="formValue"><input type="text" name="phone1" size="12" 
          class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone1) }" />
          &nbsp; <input type="checkbox" name="phoneVoice1" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice1 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
          <input type="checkbox" name="phoneText1" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText1 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneTextOptionLabel']} </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><b><label for="phone2">${textContainer.text['profilePhoneNumberLabel3']}</label></b></div>
        <div class="formValue"><input type="text" name="phone2" size="12" 
          class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone2) }" />
          &nbsp; <input type="checkbox" name="phoneVoice2" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice2 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
          <input type="checkbox" name="phoneText2" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText2 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneTextOptionLabel']} </div>
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

