<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>Two step verification opt-in success</title>
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
  <h2>${textContainer.text['optinSuccessSubheader']}</h2>
  <br /><br />
  <div class="paragraphs">
    ${textContainer.text['optinSuccessMessage']}
    <br /><br />
    
    ${textContainer.text['optinSuccessParagraph1']}
    <br /><br />
    ${textContainer.text['optinSuccessParagraph2']}
  </div>
  <br /><br />
  <br /><br />
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
  <form action="UiMain.showOneTimeCodes" method="get" style="display: inline">
    <input value="${textContainer.textEscapeDouble['buttonGenerateCodes']}" class="tfBlueButton"
     type="submit" />
  </form>

  <br /><br />

  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

