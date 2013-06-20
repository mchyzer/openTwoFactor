<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminTitle']}</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader'] }</h1>
  <br />
  <b>${textContainer.text['adminSubheader']}</b>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>

  <form action="UiMainAdmin.userIdSubmit" method="post">
    <div class="formBox">
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="userIdOperatingOn">${textContainer.text['adminPersonToManage']}</label></b></div>
        <div class="formValue">
        
           <twoFactor:combobox filterOperation="UiMainAdmin.personPicker" 
              idBase="userIdOperatingOn" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }"
            />
        
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"></div>
        <div class="formValue"><input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
    </div>
    <br />
  </form>

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.canLoggedInUserBackdoor}">
    <br />
    <form action="UiMainAdmin.adminIndex" method="get">
      <div class="formBox">
        <div class="formRow">
          <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="tfBackdoorLoginId">${textContainer.text['adminPersonToBackdoorAs']}</label></b></div>
          <div class="formValue">
            <twoFactor:combobox filterOperation="UiMainAdmin.personPicker" 
              idBase="tfBackdoorLogin" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.actingAsLoginid) }"
            />
          </div>
          <div class="formFooter">&nbsp;</div>
        </div>
        <div class="formRow">
          <div class="formLabel" style="white-space: nowrap; text-align: right"></div>
          <div class="formValue"><input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
        onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" /></div>
          <div class="formFooter">&nbsp;</div>
        </div>
      </div>
      <br />
    </form>
  
  </c:if>

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn != null}">
    <c:choose>
      <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.optedIn}">
        ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) } ${textContainer.text['adminIsAlreadyEnrolledInThisService']}
        
        <br /><br />
        
        <form action="UiMainAdmin.optOutSubmit" method="post" style="display: inline">
          <input value="${textContainer.textEscapeDouble['adminOptOutPerson']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" 
            class="tfBlueButton"
            onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
            onclick="return confirm('${textContainer.textEscapeSingleDouble['adminOptOutConfirm']}');" />
          <input type="hidden" name="userIdOperatingOn" 
            value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
        </form>

        <c:choose>
          <c:when test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.trustedBrowserCount > 0}">
            <form action="UiMainAdmin.untrustBrowsers" method="get" style="display: inline">
              <input value="${textContainer.textEscapeDouble['adminUntrustBrowsersFor']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" 
              class="tfBlueButton"
              onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
              onclick="return confirm('${textContainer.textEscapeSingleDouble['adminUntrustConfirm']}');" />
              <input type="hidden" name="userIdOperatingOn" 
                value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
            </form>
          </c:when>
        </c:choose>
        
        <br /><br />
        
        <%@ include file="../assetsJsp/auditsInclude.jsp"%>
        
      </c:when>
      <c:otherwise>
        ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) } ${textContainer.text['adminNotEnrolled']}
      </c:otherwise>  
    </c:choose>
  </c:if>

  <br />
  <br /> 
  <form action="../../twoFactorUi/app/UiMain.index" method="get" style="display: inline">
    <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
  </form>

  &nbsp; &nbsp;
    
  <form action="../../twoFactorAdminUi/app/UiMainAdmin.adminIndex" method="get" style="display: inline">
    <input value="${textContainer.textEscapeDouble['buttonAdminHome']}" class="tfBlueButton"
    onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
  </form>
  

  <br />
  <br />
  
  <br /><br />
  
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

