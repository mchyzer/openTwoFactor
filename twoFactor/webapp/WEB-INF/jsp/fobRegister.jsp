<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinFobRegisterTitle']}</title>

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
  <form action="UiMain.keychainFobAddSubmit" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['fobRegisterSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    ${textContainer.text['optinFobRegisterText']}
    <br /><br />

    <div class="formBox profileFormBox profileFormBox">
      <div class="formRow">
        <div class="formLabel formLabel"><b><label for="serialNumberId">${textContainer.text['optinFobRegisterLabel']}</label></b></div>
        <div class="formValue"><input type="text" name="serialNumber" size="12" autocomplete="off" id="serialNumberId" class="textfield" /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabel"><b><label for="twoFactorCode0id">${textContainer.text['optinFobCode1Label']}</label></b></div>
        <div class="formValue"><input type="text" name="twoFactorCode" size="12" autocomplete="off" id="twoFactorCode0id" class="textfield" /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel formLabel"><b><label for="twoFactorCode1id">${textContainer.text['optinFobCode2Label']}</label></b></div>
        <div class="formValue"><input type="text" name="twoFactorCode2" size="12" autocomplete="off" id="twoFactorCode1id" class="textfield" /></div>
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

</body></html>

