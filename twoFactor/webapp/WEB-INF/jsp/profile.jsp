<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['profileTitle'] }</title>

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
  <h2>${textContainer.text['profileSubheader']}</h2>
  <br /><br />
  <%@ include file="../assetsJsp/profileInstructions.jsp" %>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <br />

  <form action="UiMain.profileSubmit" method="post">

    <input type="hidden" name="profileForOptin" value="${twoFactorRequestContainer.twoFactorProfileContainer.profileForOptin}" />

    <div class="formBox profileFormBox">
      <c:if test="${twoFactorRequestContainer.twoFactorConfigContainer.optSomeEnabledUi}">
        <div class="formRow">
          <div class="formLabel"><b>${textContainer.text['profileOptinType']}</b></div>
          <div class="formValue">
            <input type="radio" name="optinTypeName" value="optinForAll" 
              id="optinForAllRadioId"
              ${twoFactorRequestContainer.twoFactorProfileContainer.optinForAll == 'true' ? 'checked="checked"' : ''}
              /> ${textContainer.text['profileOptinRadioForAll'] } <br />
              
            <input type="radio" name="optinTypeName" value="optinForApplicationsWhichRequire" 
              id="optinForApplicationsWhichRequireRadioId"
              ${twoFactorRequestContainer.twoFactorProfileContainer.optinForApplicationsWhichRequire == 'true' ? 'checked="checked"' : ''}
              /> ${textContainer.text['profileOptinRadioIfRequired'] }
    
          </div>
          <div class="formFooter">&nbsp;</div>
        </div> 
      </c:if>
      <div class="formRow">
          <c:choose>
            <c:when test="twoFactorRequestContainer.editableEmail" >
              <div class="formLabel"><b><label for="email0">${textContainer.text['profileEmailLabel']}</label></b></div>
              <div class="formValue">
                <input type="text" name="email0" size="18" style="width: 16em;"
                  class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.email0) }" />
              </div>
            </c:when>
            <c:otherwise>
              <div class="formLabel"><b>${textContainer.text['profileEmailLabel']}</b></div>
              <div class="formValue">
                ${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.email0) } 
                ${textContainer.text['profileEditEmailPrefix']}<a onclick="alert('${textContainer.textEscapeSingleDouble['profileEditEmailLinkAlert']}'); return true;"
                href="${textContainer.textEscapeDouble['profileEditEmailLinkUrl']}" 
                target="_blank">${textContainer.textEscapeXml['profileEditEmailLinkText']}</a>${textContainer.text['profileEditEmailSuffix']}
              </div>
            </c:otherwise>
          </c:choose>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"><b><label for="phone0">${textContainer.text['profilePhoneNumberLabel1']}</label></b></div>
        <div class="formValue"><input type="text" name="phone0" size="12" 
          class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone0) }" />
          &nbsp; <input type="checkbox" name="phoneVoice0" value="true" 
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice0 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
          <input type="checkbox" name="phoneText0" value="true" 
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText0 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneTextOptionLabel']}  </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"><b><label for="phone1">${textContainer.text['profilePhoneNumberLabel2']}</label></b></div>
        <div class="formValue"><input type="text" name="phone1" size="12" 
          class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone1) }" />
          &nbsp; <input type="checkbox" name="phoneVoice1" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice1 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
          <input type="checkbox" name="phoneText1" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText1 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneTextOptionLabel']} </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"><b><label for="phone2">${textContainer.text['profilePhoneNumberLabel3']}</label></b></div>
        <div class="formValue"><input type="text" name="phone2" size="12" 
          class="textfield" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone2) }" />
          &nbsp; <input type="checkbox" name="phoneVoice2" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice2 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
          <input type="checkbox" name="phoneText2" value="true"
          ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText2 == 'true' ? 'checked="checked"' : ''} /> 
          ${textContainer.text['profilePhoneTextOptionLabel']} </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <%-- if we are auto calling / texting and if the user is opted in by phone - -%>
      <c:if test="${twoFactorRequestContainer.twoFactorConfigContainer.enableAutoCallText && twoFactorRequestContainer.twoFactorUserLoggedIn.phoneOptIn != null && twoFactorRequestContainer.twoFactorUserLoggedIn.phoneOptIn == true}">
        <div class="formRow">
          <div class="formLabel">
          <span style="font-size: 0.8em">&nbsp;</span><br />
          <b><label for="phone0">${textContainer.text['profilePhoneAutoVoiceText']}</label></b></div>
          <div class="formValue">
            <span style="font-size: 0.8em">${textContainer.text['profilePhoneAutoVoiceSubtext']}</span><br />
            <select name="phoneAutoVoiceText">
              <%- - this is 0v (first phone voice), 0t (first phone text), 1v (second phone voice), etc - -%>
              <option value="" ></option>
              <option value="0t"
                  ${twoFactorRequestContainer.twoFactorProfileContainer.phoneAutoCalltext == '0t' ? 'selected="selected"' : ''}
                >${textContainer.text['profilePhoneAutoText1']}</option>
              <option value="0v"
                  ${twoFactorRequestContainer.twoFactorProfileContainer.phoneAutoCalltext == '0v' ? 'selected="selected"' : ''}
                >${textContainer.text['profilePhoneAutoVoice1']}</option>
              <option value="1t"
                  ${twoFactorRequestContainer.twoFactorProfileContainer.phoneAutoCalltext == '1t' ? 'selected="selected"' : ''}
                >${textContainer.text['profilePhoneAutoText2']}</option>
              <option value="1v"
                  ${twoFactorRequestContainer.twoFactorProfileContainer.phoneAutoCalltext == '1v' ? 'selected="selected"' : ''}
                >${textContainer.text['profilePhoneAutoVoice2']}</option>
              <option value="2t"
                  ${twoFactorRequestContainer.twoFactorProfileContainer.phoneAutoCalltext == '2t' ? 'selected="selected"' : ''}
                >${textContainer.text['profilePhoneAutoText3']}</option>
              <option value="2v" 
                  ${twoFactorRequestContainer.twoFactorProfileContainer.phoneAutoCalltext == '2v' ? 'selected="selected"' : ''}
                >${textContainer.text['profilePhoneAutoVoice3']}</option>
            </select>
          </div>
          <div class="formFooter">&nbsp;</div>
        </div>
      </c:if>
      --%>
      <div class="formRow">
        <div class="formLabel"><span style="font-size: 0.8em">&nbsp;</span><br />
           <b><label for="colleagueLogin0">${textContainer.text['profileFriendLabel1']}</label></b></div>
        <div class="formValue">
           <span style="font-size: 0.8em">${textContainer.text['profileFriendComboSubtext']}</span><br />
           <twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker" 
             idBase="colleagueLogin0" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin0) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"><b><label for="colleagueLogin1">${textContainer.text['profileFriendLabel2']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker" 
             idBase="colleagueLogin1" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin1) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"><b><label for="colleagueLogin2">${textContainer.text['profileFriendLabel3']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker" 
             idBase="colleagueLogin2" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin2) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"><b><label for="colleagueLogin3">${textContainer.text['profileFriendLabel4']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker" 
             idBase="colleagueLogin3" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin3) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"><b><label for="colleagueLogin4">${textContainer.text['profileFriendLabel5']}</label></b></div>
        <div class="formValue"><twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.personPicker" 
             idBase="colleagueLogin4" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueLogin4) }"
             /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel"></div>
        <div class="formValue">
          <c:choose>
            <c:when test="${twoFactorRequestContainer.twoFactorProfileContainer.profileForOptin}">
              <input value="${textContainer.textEscapeDouble['buttonContinue']}" class="tfBlueButton"
                 
                 type="submit" />
            
            </c:when>
            <c:otherwise>
              <input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
                 
                 type="submit" />
            
            </c:otherwise>
          </c:choose>
        </div>
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

  <br /><br />
  
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

