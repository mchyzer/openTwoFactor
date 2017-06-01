<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>
<%-- dont be too descriptive here since this is on the printed form --%>
<title>${textContainer.text['codesTitle']}</title>
<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#ffffff" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
<div class="tfPrinterFriendlyNot">
  <div id="headerDiv">
    <div class="alignleft"><h1 style="display: inline;">${textContainer.text['pageHeader'] }</h1></div>
    <div class="alignright">
      <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
        <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
      </c:if>    
    </div> 
    <div class="clearboth"></div> 
  </div> 
  <h2>${textContainer.text['codesSubheader']}</h2>
  <br />
  <br />
  <div class="paragraphs">
  ${textContainer.text['codesParagraph1']}
  <br /><br />
  ${textContainer.text['codesParagraph2']}
  <br /><br />
  ${textContainer.text['codesParagraph3']}
  <br /><br />
  ${textContainer.text['codesLabel']}
  <br /><br />
  </div>
  
</div>
  <span class="tfPrinterFriendlyOnlyInline" style="font-size: 0.5625em">
  ${textContainer.text['codesPrintedInstructions']}
  </span>
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <pre class="codes" style="width: 30em" >
    <c:forEach items="${twoFactorRequestContainer.oneTimePassRows}" var="oneTimePassRow">
  ${oneTimePassRow.oneTimePassCol1}   ${oneTimePassRow.oneTimePassCol2}</c:forEach>
  </pre>  
  <br />
  <br />
  <br />
  <br />
  <div class="tfPrinterFriendlyNot">
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
  </div>
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

