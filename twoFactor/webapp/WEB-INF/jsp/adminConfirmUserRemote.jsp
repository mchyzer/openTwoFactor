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
  <form action="UiMainAdmin.confirmUserRemote" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['adminConfirmUserRemoteSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    ${textContainer.text['adminConfirmUserRemoteText']}
    <br /><br />
    ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.description) }
    <br /><br />
    <div class="formBox profileFormBox profileFormBoxWide">
      <div class="formRow">
        <input type="checkbox" id="checkedNameId" name="checkedNameName" value="true" />
        &nbsp;
        <b><label for="checkedNameId">${textContainer.text['adminConfirmName']}</label></b>
      </div>
      <div class="formRow">
        <input type="checkbox" id="checkedDeptId" name="checkedDeptName" value="true" />
        &nbsp;
        <b><label for="checkedDeptId">${textContainer.text['adminConfirmDepartmentTitle']}</label></b>
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

</body></html>

