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

  <div class="formBox profileFormBox">
    <div class="formRow">
      <div class="formLabel"><b>${textContainer.text['profileEmailLabel']}</b></div>
      <div class="formValue">
        ${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.email0) } 
        (<a href="https://medley.isc-seo.upenn.edu/directory/jsp/fast.do?fastStart=profile"
          onclick="alert('${textContainer.textEscapeSingleDouble['profileEditEmailLinkAlert']}'); return true;"
          >edit email address</a>)
      </div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"><b><label for="phone0">${textContainer.text['profilePhoneNumberLabel1']}</label></b></div>
      <div class="formValue"><c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasPhone0}">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone0) }
        &nbsp; <input type="checkbox" name="phoneVoice0" value="true" disabled="disabled"
        ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice0 == 'true' ? 'checked="checked"' : ''} /> 
        ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
        <input type="checkbox" name="phoneText0" value="true" disabled="disabled" 
        ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText0 == 'true' ? 'checked="checked"' : ''} /> 
        ${textContainer.text['profilePhoneTextOptionLabel']}</c:if></div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"><b><label for="phone1">${textContainer.text['profilePhoneNumberLabel2']}</label></b></div>
      <div class="formValue"><c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasPhone1}">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone1) }
        &nbsp; <input type="checkbox" name="phoneVoice1" value="true" disabled="disabled"
        ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice1 == 'true' ? 'checked="checked"' : ''} /> 
        ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
        <input type="checkbox" name="phoneText1" value="true" disabled="disabled"
        ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText1 == 'true' ? 'checked="checked"' : ''} /> 
        ${textContainer.text['profilePhoneTextOptionLabel']}</c:if></div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"><b><label for="phone2">${textContainer.text['profilePhoneNumberLabel3']}</label></b></div>
      <div class="formValue"><c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasPhone2}">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone2) }
        &nbsp; <input type="checkbox" name="phoneVoice2" value="true" disabled="disabled"
        ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice2 == 'true' ? 'checked="checked"' : ''} /> 
        ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
        <input type="checkbox" name="phoneText2" value="true" disabled="disabled"
        ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText2 == 'true' ? 'checked="checked"' : ''} /> 
        ${textContainer.text['profilePhoneTextOptionLabel']} </c:if></div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel">
         <b><label for="colleagueLogin0">${textContainer.text['profileFriendLabel1']}</label></b></div>
      <div class="formValue">
         ${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName0) }</div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"><b><label for="colleagueLogin1">${textContainer.text['profileFriendLabel2']}</label></b></div>
      <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName1) }</div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"><b><label for="colleagueLogin2">${textContainer.text['profileFriendLabel3']}</label></b></div>
      <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName2) }</div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"><b><label for="colleagueLogin3">${textContainer.text['profileFriendLabel4']}</label></b></div>
      <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName3) }</div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"><b><label for="colleagueLogin4">${textContainer.text['profileFriendLabel5']}</label></b></div>
      <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName4) }</div>
      <div class="formFooter">&nbsp;</div>
    </div>
    <div class="formRow">
      <div class="formLabel"></div>
      <div class="formValue">
        <form action="../../twoFactorUi/app/UiMain.profile" method="get" style="display: inline">
      
          <input value="${textContainer.textEscapeDouble['buttonEdit']}" class="tfBlueButton"
    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
        </form>
      </div>
      <div class="formFooter">&nbsp;</div>
    </div>
  </div>
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

