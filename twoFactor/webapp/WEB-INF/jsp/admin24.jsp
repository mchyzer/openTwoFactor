<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['admin24Title']}</title>

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
  <h2>${textContainer.text['admin24Subheader']}</h2>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <br />
  <form action="UiMainAdmin.admin24Submit" method="post">
    <div class="formBox" style="width: 30em">
    
      <div class="formRow">
        <div class="formLabel">
          <b><label for="userIdId">${textContainer.text['admin24ConfirmPennid']}</label></b>
        </div>
        <div class="formValue" style="padding-top: 0.6em">
          <input type="text" name="userIdName" size="12" id="userIdId" class="textfield" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel">
          <b><label for="netIdId">${textContainer.text['admin24ConfirmPennkey']}</label></b>
        </div>
        <div class="formValue" style="padding-top: 0.6em">
          <input type="text" name="netIdName" size="12" id="netIdId" class="textfield" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel">
          <b><label for="lastFourId">${textContainer.text['admin24ConfirmLastFour']}</label></b>
        </div>
        <div class="formValue" style="padding-top: 0.6em">
          <input type="password" name="lastFourName" size="12" id="lastFourId" class="textfield" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel">
          <b><label for="lastFourId">${textContainer.text['admin24ConfirmBirthdate']}</label></b>
        </div>
        <div class="formValue" style="padding-top: 0.6em">
          <%@ include file="../assetsJsp/birthdayForm.jsp"%>
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
    
      <br /><br />
    
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"></div>
        <div class="formValue" style="width: 14.5em"><input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
       type="submit" /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
    </div>
    <br />
  </form>

    
  <br />
  <br /> 
  <br />
  <br /> 
  
    <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
      <div class="logoutBottom" style="font-size: smaller">
        <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
        &nbsp; &nbsp;      
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

