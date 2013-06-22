<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader']}</h1>
  <br />
  <b>${textContainer.text['optinSubheader']}</b>
  <br /><br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  ${textContainer.text['optinStep1description']}
  <br /><br />
  <div class="substep">
    ${textContainer.text['optinStep1substep']}
  </div><br />
  ${textContainer.text['optinStep2description']}
  <br /><br />
  <div class="substep">
    <b>${twoFactorRequestContainer.twoFactorUserLoggedIn.twoFactorSecretTempUnencryptedFormatted}</b> 
    &nbsp; &nbsp;  ${textContainer.text['optinStep2advancedPrefix']}
    <span id="otherOptionsLink"><a href="#" onclick="document.getElementById('otherOptions').style.display = 'block'; document.getElementById('otherOptionsLink').style.display = 'none';">${textContainer.text['optinStep2advancedLink']}</a>${textContainer.text['optinStep2advancedSuffix']}</span>
    <br /><br />
    <div id="otherOptions" style="display: none;">
      <b>${twoFactorRequestContainer.twoFactorUserLoggedIn.twoFactorSecretTempUnencryptedHexFormatted}</b>
      &nbsp; &nbsp; ${textContainer.text['optinHexLabel']}
      <br /><br />
      <div style="border: 1px solid #95956F; padding: 3px">
        ${textContainer.text['optinStep2customSecretLabel']} &nbsp;
          <form action="../../twoFactorUi/app/UiMain.optinCustom" method="post" style="display: inline; white-space: nowrap;">
            <input id="twoFactorCustomCodeId" type="text" name="twoFactorCustomCode" size="30" autocomplete="off" class="textfield" /> &nbsp;
            <input value="${textContainer.textEscapeDouble['optinStep2submitCustomSecretButton']}" class="tfBlueButton" 
            style="background: none; border: none; color: #7794C9; text-decoration: underline; cursor: pointer;"
            type="submit" />
          </form>
        <br /> 
        ${textContainer.text['optinStep2advancedCustomSecretDescription']}
      </div>
      <br /><br />
    </div>
    ${textContainer.text['optinStep2bottom']}
  </div><br />
  ${textContainer.text['optinStep3description']}
  <br /><br />
  <form action="UiMain.optinTestSubmit" method="post">
  <div class="substep">
    ${textContainer.text['optinStep3substep']} <br /><br />
      ${textContainer.text['optinStep3codeLabel']} <input type="text" name="twoFactorCode" size="12" autocomplete="off" class="textfield" />
      <br /><br />
        <input value="${textContainer.textEscapeDouble['optinStep3codeButton']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" 
      type="submit" onclick="if (document.getElementById('twoFactorCustomCodeId').value != null && document.getElementById('twoFactorCustomCodeId').value != '') {alert('If you enter a custom secret, you must click the button: Submit custom secret'); return false; }" />
      
    <br /><br />       
    ${textContainer.text['optinStep3bottom']}
      
  </div>
  <br />
  <hr />
  <a name="qrCode"></a>
    <img src="UiMain.qrCode.gif" height="300" width="300" />
  <br />
  <br /> 
  <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['optinCancelButton']}</a> &nbsp;
  
  </form>
  
  <br /> 
  <br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

