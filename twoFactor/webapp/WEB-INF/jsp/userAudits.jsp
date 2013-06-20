<%@ include file="../assetsJsp/commonTop.jsp"%>
<%@ taglib uri="/WEB-INF/tld/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/tld/fn.tld" prefix="fn"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['auditsTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>Penn WebLogin</h1>
  <br />
  <b>${textContainer.text['auditsSubheader']}</b>
  <br /><br />
  ${textContainer.text['auditsNamePrefix']}
  ${fn:escapeXml(twoFactorRequestContainer.twoFactorAuditContainer.userString)}${textContainer.text['auditsNameSuffix']}
  <br />
  ${textContainer.text['auditsNameParagraph2']}
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>

  <%@ include file="../assetsJsp/auditsInclude.jsp"%>
  
  <br /><br />
  
  <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
    <a href="../../twoFactorUnprotectedUi/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
    &nbsp; &nbsp;
  </c:if>
  
  <form action="UiMain.index" method="get" style="display: inline">
    <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfBlueButton"
    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
  </form>
  
  <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

