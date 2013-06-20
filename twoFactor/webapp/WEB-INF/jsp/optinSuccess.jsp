<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>Two step verification opt-in success</title>
<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader']}</h1>
  <br />
  <b>${textContainer.text['optinSuccessSubheader']}</b>
  <br /><br />
  ${textContainer.text['optinSuccessMessage']}
  <br /><br />
  <div style="width: 25em">
  
  ${textContainer.text['optinSuccessParagraph1']}
  <br /><br />
  ${textContainer.text['optinSuccessParagraph2']}
  </div>
  <br /><br />
    <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
    <a href="../../twoFactorUnprotectedUi/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
    &nbsp; &nbsp;
    </c:if>
    <form action="UiMain.index" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
    </form>
    &nbsp; &nbsp;
  <form action="UiMain.showOneTimeCodes" method="get" style="display: inline">
    <input value="${textContainer.textEscapeDouble['buttonGenerateCodes']}" class="tfBlueButton"
    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
  </form>

  <br /><br />

  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

