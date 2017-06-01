<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['addPhoneTitle']}</title>

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
  
    <h2>${textContainer.text['addPhoneSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    ${textContainer.text['addPhoneStep1description']}
    <br /><br />
    <div class="substep">
      ${textContainer.text['addPhoneStep1substep']}
    </div><br />
    
    ${textContainer.text['addPhoneStep2description']}

    <br /><br />
    <div class="substep">
      <div id="activateAppContentDiv" >
        <div style="border: 1px solid #95956F; padding: 3px">
          <div class="alignleft"><b>${textContainer.text['addPhoneStep2activateApp']}</b></div>
          
          <br />
          ${textContainer.text['addPhoneStep2activateContent']}
          <br /><br />
          <b>${twoFactorRequestContainer.twoFactorUserLoggedIn.twoFactorSecretUnencryptedHexFormatted}</b><br />
          ${textContainer.text['addPhoneHexLabel']}
        </div>  
      </div>
      <br /><br />
    </div>
    <a id="step3"></a>
    ${textContainer.text['addPhoneStep3description']}
    <br /><br />
    <form action="UiMain.addPhoneTestSubmit" method="post">
      <div class="substep">
        ${textContainer.text['addPhoneStep3substep']} <br /><br />
          ${textContainer.text['addPhoneStep3codeLabel']} <input type="text" name="twoFactorCode" size="12" autocomplete="off" class="textfield" />
          <br /><br />
          <input value="${textContainer.textEscapeDouble['addPhoneStep3codeButton']}" class="tfBlueButton"
             
            type="submit"  />
          
      </div>
    </form>
    
  </div>
  <br />
  <a name="qrCode"></a>
     <%-- --%>
    <img src="UiMain.qrCodeSecret.gif?imageId=${twoFactorRequestContainer.twoFactorAddPhoneContainer.qrCodeUniqueId}" height="300" width="300" />
  <br />
  <br /> 
  <br />
  <br /> 
  <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['addPhoneCancelButton']}</a> &nbsp; &nbsp;
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

</body></html>

