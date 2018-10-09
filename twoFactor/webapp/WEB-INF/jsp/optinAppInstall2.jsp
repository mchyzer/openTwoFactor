<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinAppInstallTitle']}</title>

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
    <form action="UiMain.optinWizardSetupAppDone2" method="post">

      <h2>${textContainer.text['optinAppInstallSubheader2']}</h2>
      <br /><br />
      <%@ include file="../assetsJsp/commonError.jsp"%>
      <br />
      ${textContainer.text['optinAppInstallText']}
      
      <br />
      <br />
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
    </form>

  </div>

</body></html>

