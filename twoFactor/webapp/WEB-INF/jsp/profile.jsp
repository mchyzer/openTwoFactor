<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['profileTitle'] }</title>

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
  <br />
  <b>${textContainer.text['profileSubheader']}</b>
  <br /><br />
  <%@ include file="../assetsJsp/profileInstructions.jsp" %>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>

  <form action="UiMain.profileSubmit" method="post">

    <input type="hidden" name="profileForOptin" value="${twoFactorRequestContainer.twoFactorProfileContainer.profileForOptin}" />

    <div class="formBox profileFormBox">
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
                ${textContainer.text['profileEditEmailPrefix']}<a 
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
      <div class="formRow">
        <div class="formLabel"><span style="font-size: 0.8em">&nbsp;</span><br />
           <b><label for="colleagueLogin0">${textContainer.text['profileFriendLabel1']}</label></b></div>
        <div class="formValue">
           <span style="font-size: 0.8em">Enter a name to search for a friend</span><br />
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
                onmouseover="this.style.backgroundColor='#011D5C';" 
                onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
            
            </c:when>
            <c:otherwise>
              <input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
                onmouseover="this.style.backgroundColor='#011D5C';" 
                onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
            
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
  
  <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
    <div class="logoutBottom" style="font-size: smaller">
      <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
      &nbsp; &nbsp;      
    </div>
  </c:if>    

  <form action="UiMain.index" method="get" style="display: inline; font-size: smaller">
    <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfLinkButton"
    type="submit" />
  </form>

  <br /><br />
  
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

