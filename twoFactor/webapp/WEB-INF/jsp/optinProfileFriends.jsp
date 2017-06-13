<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinProfileFriendTitle']}</title>

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
  <form action="UiMain.optinWizardSubmitFriends" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['optinProfileFriendSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    
    ${textContainer.text['optinProfileFriendText']}
    <br /><br />
    
    <div class="formBox profileFormBox profileFormBoxNarrow">
    
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><span style="font-size: 0.8em">&nbsp;</span><br />
           <b><label for="colleagueLogin0">${textContainer.text['profileFriendLabel1']}</label></b></div>
        <div class="formValue">
           <span style="font-size: 0.8em">${textContainer.text['profileFriendComboSubtext']}</span><br />
           <twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker"  style="width: 45em; font-size: .9em"
             idBase="colleagueLogin0" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin0) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><b><label for="colleagueLogin1">${textContainer.text['profileFriendLabel2']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker"   style="width: 45em; font-size: .9em"
             idBase="colleagueLogin1" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin1) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><b><label for="colleagueLogin2">${textContainer.text['profileFriendLabel3']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker"   style="width: 45em; font-size: .9em"
             idBase="colleagueLogin2" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin2) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><b><label for="colleagueLogin3">${textContainer.text['profileFriendLabel4']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker"   style="width: 45em; font-size: .9em"
             idBase="colleagueLogin3" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin3) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabelNarrow"><b><label for="colleagueLogin4">${textContainer.text['profileFriendLabel5']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker"   style="width: 45em; font-size: .9em"
             idBase="colleagueLogin4" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin4) }"
             /></div>
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

