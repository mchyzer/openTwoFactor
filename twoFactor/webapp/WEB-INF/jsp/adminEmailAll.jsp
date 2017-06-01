<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminEmailAllTitle']}</title>

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
  <h2>${textContainer.text['adminEmailAllSubheader']}</h2>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <br />
  <form action="UiMainAdmin.emailAllUsersSubmit" method="post">
    <div class="formBox" style="width: 50em">
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="userIdOperatingOn">${textContainer.text['adminEmailAllReallySend']}</label></b></div>
        <div class="formValue" style="width:34.5em">
        
          <input type="checkbox" name="checkedAdminAllReallySend" value="true" style="margin-top: 0.5em;" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="userIdOperatingOn">${textContainer.text['adminEmailAllEmailSubject']}</label></b></div>
        <div class="formValue" style="width:34.5em">
        
          <input type="text" name="emailSubject"  style="width:30em" />
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="userIdOperatingOn">${textContainer.text['adminEmailAllEmailBody']}</label></b></div>
        <div class="formValue" style="width:34.5em">
        
          <textarea rows="15" cols="60" name="emailBody" wrap="off"></textarea>
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"></div>
        <div class="formValue" style="width:34.5em"><input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
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

  &nbsp; &nbsp;
    
  <form action="../../twoFactorAdminUi/app/UiMainAdmin.adminIndex" method="get" style="display: inline; font-size: smaller">
    <input value="${textContainer.textEscapeDouble['buttonAdminHome']}" class="tfLinkButton"
    type="submit" />
  </form>
  
  
  <br /><br />
  
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

