<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['testCodeTitle']}</title>

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
  <form action="UiMain.testCodeSubmit" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['testCodeSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    ${textContainer.text['testConfirmCodeText']}
    <input type="text" name="twoFactorCode" size="12" autocomplete="off" class="textfield" />

    <br />
    
    <br />
    ${textContainer.text['testFobCodeText']}
    <input type="text" name="twoFactorFobCode" size="12" autocomplete="off" class="textfield" />

    <br />
    <br />
    
    <br />
    <br />
    <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['testCodeCancelButton']}</a> &nbsp; &nbsp; &nbsp; &nbsp;
    <input value="${textContainer.textEscapeDouble['testCodeSubmitButton']}" class="tfBlueButton" 
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

