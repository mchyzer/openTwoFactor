<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminImportSerialsTitle']}</title>

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
  <h2>${textContainer.text['adminImportSerialsSubheader']}</h2>
  <br />
  ${textContainer.text['adminImportSerialsInstructions']}
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <br />
  <form action="UiMainAdmin.importSerialsSubmit" method="post">
    <div class="formBox" style="width: 50em">
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="serialNumbers">${textContainer.text['adminImportSerialsLabelSerials']}</label></b></div>
        <div class="formValue" style="width:34.5em">
        
          <textarea rows="20" cols="30" name="serialNumbers" wrap="off">serialNumber,secret
12345,ABCDEFGHIJKLMNOP
23456,PONMLKJIIHGFEDBA</textarea>
        </div>
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

