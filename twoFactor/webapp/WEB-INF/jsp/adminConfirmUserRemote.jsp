<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminConfirmUserRemoteTitle']}</title>

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

    <h2>${textContainer.text['adminConfirmUserRemoteSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    ${textContainer.text['adminConfirmUserRemoteText']}
    <br /><br />
    ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.description) }
    <br /><br />
    <div class="formBox profileFormBox">
      <div class="formRow">
        <input type="checkbox" id="checkedNameId" name="checkedNameName" value="true" />
        &nbsp;
        <b><label for="checkedNameId">${textContainer.text['adminConfirmName']}</label></b>
      </div>
      <div class="formRow">
        <input type="checkbox" id="checkedNameId" name="checkedNameName" value="true" />
        &nbsp;
        <b><label for="checkedNameId">${textContainer.text['adminConfirmDepartmentTitle']}</label></b>
      </div>
      <div class="formRow">
        <div class="formLabel">
          <b><label for="userIdId">${textContainer.text['adminConfirmPennid']}</label></b>
        </div>
        <div class="formValue">
          <input type="text" name="userIdName" size="12" id="userIdId" class="textfield" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel">
          <b><label for="netIdId">${textContainer.text['adminConfirmPennkey']}</label></b>
        </div>
        <div class="formValue">
          <input type="text" name="netIdName" size="12" id="netIdId" class="textfield" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel">
          <b><label for="lastFourId">${textContainer.text['adminConfirmLastFour']}</label></b>
        </div>
        <div class="formValue">
          <input type="password" name="lastFourName" size="12" id="lastFourId" class="textfield" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel">
          <b><label for="lastFourId">${textContainer.text['adminConfirmBirthdate']}</label></b>
        </div>
        <div class="formValue">
          <%@ include file="../assetsJsp/birthdayForm.jsp"%>
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
    </div>



<%--
        <div class="formValue"><input type="text" name="phone0" size="12" 
          class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone0) }" />
          &nbsp; <input type="checkbox" name="phoneVoice0" value="true" 
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice0 == 'true' ? 'checked="checked"' : ''} />
          ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp;
          <input type="checkbox" name="phoneText0" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText0 == 'true' ? 'checked="checked"' : ''} />
          ${textContainer.text['profilePhoneTextOptionLabel']}  </div>
      </div>
    </div>


    <table>
      <tr style="vertical-align: top">
        <td style="padding-top: 0.25em; padding-right: 0.5em"></td>
        <td>
          <label for="optinTypeAppId">${textContainer.text['optinTypeAppLabel']}</label>
          <div class="formElementHelp">${textContainer.text['optinTypeAppHelp']}</div>
          <br />
        </td>
      </tr>
      <tr style="vertical-align: top">
        <td style="padding-top: 0.25em; padding-right: 0.5em"><input type="radio" id="optinTypePhoneId" name="optInTypeName" value="phone" /></td>
        <td>
          <label for="optinTypePhoneId">${textContainer.text['optinTypePhoneLabel']}</label>
          <div class="formElementHelp">${textContainer.text['optinTypePhoneHelp']}</div>
          <br />
        </td>
      </tr>
      <tr style="vertical-align: top">
        <td style="padding-top: 0.25em; padding-right: 0.5em"><input type="radio" id="optinTypeFobId" name="optInTypeName" value="fob" /></td>
        <td>
          <label for="optinTypeFobId">${textContainer.text['optinTypeFobLabel']}</label>
          <div class="formElementHelp">${textContainer.text['optinTypeFobHelp']}</div>
        </td>
      </tr>
    </table>
    
    --%>

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

