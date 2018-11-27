<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['liteAdminTitle']}</title>

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
  
    <h2>${textContainer.text['liteAdminSubheader'] }</h2>
    <br /> <br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    ${textContainer.text['liteAdminParagraph1'] }

  </div>
<br />
  <form action="UiMain.liteAdminSubmit" method="post">

    <div class="formBox profileFormBoxNarrow profileFormBox">
      <div class="formRow">
        <div class="formLabelNarrow formLabel"><b><label for="netIdId">${textContainer.text['netId']}</label></b></div>
        <div class="formValue">
          <input type="text" name="netIdName" size="18" style="width: 16em;" id="netIdId"
            class="textfield" />
        </div> 
        <div class="formFooter">&nbsp;</div>
      </div>
      <br />
      <div class="formRow">
        <input type="checkbox" id="checkedIdId" name="checkedIdName" value="true" />
        &nbsp;
        <b><label for="checkedIdId">${textContainer.text['adminLiteConfirmId']}</label></b>
      </div>
      <div class="formRow">
        <input type="checkbox" id="disclaimerId" name="disclaimerName" value="true" />
        &nbsp;
        <b><label for="disclaimerId">${textContainer.text['adminLiteDisclaimer']}</label></b>
      </div>
    </div>
  
    <br />
    <br />
    <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['optinCancelButton']}</a> &nbsp; &nbsp; &nbsp; &nbsp;
    <input value="${textContainer.textEscapeDouble['liteAdminSubmitButton']}" class="tfBlueButton" 
       type="submit" />
  </form>
    
  <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
    <div class="logoutBottom">
      <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
      &nbsp; &nbsp;      
    </div>
  </c:if>
    

   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
