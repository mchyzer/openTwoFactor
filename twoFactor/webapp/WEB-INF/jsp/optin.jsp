<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

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
      <div id="activateTokenLinkDiv">
        <a href="#"  id="activeHardwareTokenButtonId"
          onclick="$('#activateTokenContentDiv').show(); $('#activateTokenLinkDiv').hide(); return false;">${textContainer.text['optinStep2activateToken']}</a>
      </div>
      <div id="activateTokenContentDiv" style="display: none">
        <div id="otherOptions">
          <div style="border: 1px solid #95956F; padding: 3px">

            <div class="alignleft"><b>${textContainer.text['optinStep2activateToken']}</b></div>
            <div class="alignright">
              
                <a href="#" onclick="$('#activateTokenContentDiv').hide(); $('#activateTokenLinkDiv').show(); $('#secretRadioId').click(); return false;">${textContainer.text['optinStepClose']}</a>
                  
            </div> 
            <div class="clearboth"></div> 
            <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.allowSerialNumberRegistration}">
              ${textContainer.text['optinStep2howToRegisterToken']}<br />
              <input type="radio" name="howRegisterToken" value="serial" id="serialRadioId"
                onchange="$('.serialClass').show('slow'); $('.secretClass').hide('slow'); return true;"
                /> ${textContainer.text['optinStep2registerWithSerialNumber'] } <br />
              <input type="radio" name="howRegisterToken" value="secret"  id="secretRadioId"
                onchange="$('.serialClass').hide('slow'); $('.secretClass').show('slow'); return true;"
                /> ${textContainer.text['optinStep2registerWithSecret'] }<br /><br />
            </c:if>
            <div id="enterSerialDivId" class="serialClass"  style="display: none">
              <hr />
              <form action="UiMain.optinBySerialAndTest" method="post">
                <table>
                  <tr valign="top">
                    <td style="padding: 0.5em; padding-right: 2em"><b><label for="serialNumber">${textContainer.text['optinStep2labelSerialNumber'] }</label></b></td>
                    <td style="padding: 0.5em;"><input type="text" name="serialNumber" size="12" autocomplete="off" class="textfield" /><br />
                      <span style="font-size: small;">${textContainer.text['optinStep2helpSerial'] }</span>
                    </td>
                  </tr>
                  <tr valign="top">
                    <td style="padding: 0.5em; padding-right: 2em"><b><label for="twoFactorCode">${textContainer.text['optinStep2labelCode'] }</label></b></td>
                    <td style="padding: 0.5em;"><input type="text" name="twoFactorCode" size="12" autocomplete="off" class="textfield" /><br />
                    <span style="font-size: small;">${textContainer.text['optinStep2helpCode'] }</span></td>
                  </tr>
                  <tr>
                    <td></td>
                    <td style="padding: 0.5em; padding-top: 1em"><input value="${textContainer.textEscapeDouble['optinStep2serialNumberButton'] }" class="tfBlueButton"
                    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" 
                    type="submit" /></td>
                  </tr>
                </table>
                <br />
                <br />
              </form>
            </div>
            <div id="enterSecretDivId" class="secretClass" style="${twoFactorRequestContainer.twoFactorAdminContainer.allowSerialNumberRegistration ? 'display: none' : ''}">
              <hr />
              <br />
              ${textContainer.text['optinStep2customSecretLabel']} <br /><br />
                <form action="../../twoFactorUi/app/UiMain.optinCustom" method="post" style="display: inline; white-space: nowrap;">
                  <input id="twoFactorCustomCodeId" type="text" name="twoFactorCustomCode" size="45" autocomplete="off" class="textfield" /> &nbsp;
                  <input value="${textContainer.textEscapeDouble['optinStep2submitCustomSecretButton']}" class="tfBlueButton" 
                  style="background: none; border: none; color: #7794C9; text-decoration: underline; cursor: pointer;"
                  type="submit" />
                </form>
              <br /><br />
              ${textContainer.text['optinStep2advancedCustomSecretDescription']}
              <br /><br />
              <b>${twoFactorRequestContainer.twoFactorUserLoggedIn.twoFactorSecretTempUnencryptedHexFormatted}</b><br />
              ${textContainer.text['optinHexLabel']}
              <br />
            </div>
          </div>
        </div>
      </div>
      <br />

      <div class="secretClass">
        <div id="activateAppLinkDiv">
          <a href="#" onclick="$('#activateAppContentDiv').show(); $('#activateAppLinkDiv').hide(); return false;">${textContainer.text['optinStep2activateApp']}</a>
        </div>
        <div id="activateAppContentDiv" style="display: none" >
          <div style="border: 1px solid #95956F; padding: 3px">
            <div class="alignleft"><b>${textContainer.text['optinStep2activateApp']}</b></div>
            <div class="alignright">
              
                <a href="#" onclick="$('#activateAppContentDiv').hide(); $('#activateAppLinkDiv').show(); return false;">${textContainer.text['optinStepClose']}</a>
                  
            </div> 
            <div class="clearboth"></div> 
            
            <br />
            ${textContainer.text['optinStep2activateContent']}
          </div>  
        </div>
        <br /><br />
      </div>
    </div>
    <div  class="secretClass">
      <a id="step3"></a>
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
      </form>
    </div>    
  </div>
  <div class="secretClass">
    <br />
    <a name="qrCode"></a>
       <%-- --%>
      <img src="UiMain.qrCode.gif?key=${twoFactorRequestContainer.twoFactorUserLoggedIn.lastUpdated}" height="300" width="300" />
      
  </div>
  <br />
  <br /> 
  <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['optinCancelButton']}</a> &nbsp; &nbsp;
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

<c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.showSerialSection && twoFactorRequestContainer.twoFactorAdminContainer.allowSerialNumberRegistration}">
  <script type="text/javascript">
    $('#activeHardwareTokenButtonId').click();
    $('#serialRadioId').click();
  </script>
</c:if>

</body></html>

