<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminTitle']}</title>

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
  <h2>${textContainer.text['adminSubheader']}</h2>
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <br />
  <form action="UiMainAdmin.userIdSubmit" method="post">
    <div class="formBox" style="width: 30em">
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="userIdOperatingOn">${textContainer.text['adminPersonToManage']}</label></b></div>
        <div class="formValue"  style="width: 14.5em">
        
           <twoFactor:combobox filterOperation="UiMainAdmin.personPicker" 
              idBase="userIdOperatingOn" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }"
            />
        
        </div>
        <div class="formFooter">&nbsp;</div>
      </div>
      <div class="formRow">
        <div class="formLabel" style="white-space: nowrap; text-align: right"></div>
        <div class="formValue" style="width: 14.5em"><input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
       type="submit" /></div>
        <div class="formFooter">&nbsp;</div>
      </div>
    </div>
    <br />
  </form>

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.canLoggedInUserBackdoor}">
    <br />
    <form action="UiMainAdmin.adminIndex" method="get">
      <div class="formBox" style="width: 30em">
        <div class="formRow">
          <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="tfBackdoorLoginId">${textContainer.text['adminPersonToBackdoorAs']}</label></b></div>
          <div class="formValue" style="width: 14.5em">
            <twoFactor:combobox filterOperation="UiMainAdmin.personPicker" 
              idBase="tfBackdoorLogin" value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.actingAsLoginid) }"
            />
          </div>
          <div class="formFooter">&nbsp;</div>
        </div>
        <div class="formRow">
          <div class="formLabel" style="white-space: nowrap; text-align: right"></div>
          <div class="formValue" style="width: 14.5em"><input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
         type="submit" /></div>
          <div class="formFooter">&nbsp;</div>
        </div>
      </div>
      <br />
    </form>
  
  </c:if>

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn != null}">
    <c:choose>
      <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.optedIn}">
        ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.name) } 
        
        ${textContainer.text['adminIsAlreadyEnrolledInThisService']}
        <br />
        
        ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.numberOfColleagues}
        ${textContainer.text['adminNumberOfColleagues']}
        <br />
        
        ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.numberOfPhones}
        ${textContainer.text['adminNumberOfPhones']}
        <br />
        
        <c:choose>
          <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.invitedColleaguesWithinAllottedTime}">
            ${textContainer.text['adminHasInvitedFriends']}
          </c:when>
          <c:otherwise>
            ${textContainer.text['adminHasntInvitedFriends']}
          </c:otherwise>
        </c:choose>
        <br />

        <c:choose>
          <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.duoPush}">
            ${textContainer.text['adminPush']}
            <br />
            <c:choose>
              <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.duoPushByDefaultBoolean}">

                ${textContainer.text['adminPushByDefault']}
              </c:when>
              <c:otherwise>
                ${textContainer.text['adminDontPushByDefault']}
              </c:otherwise>
            </c:choose>
            <br />
            
          </c:when>
          <c:otherwise>
            ${textContainer.text['adminDontPush']}
            <br />
          </c:otherwise>
        </c:choose>

        <c:choose>
          <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.phoneOptInBoolean}">
            ${textContainer.text['adminPhoneOptIn']}
            <br />
            <c:choose>
              <c:when test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.phoneAutoCallText}">
                ${textContainer.text['adminAutoPhoneToNumber']}
                <br />
                ${textContainer.text['adminPhoneAutoCalltextsInMonth']}
                ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.phoneAutoCalltextsInMonth}
                <br />
                
              </c:when>
              <c:otherwise>
                ${textContainer.text['adminNotAutoPhoneToNumber']}
                <br />
              </c:otherwise>
            </c:choose>

          </c:when>
          <c:otherwise>
            ${textContainer.text['adminNotPhoneOptIn']}
            <br />
          </c:otherwise>
        </c:choose>

        <c:choose>
          <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.optInOnlyIfRequiredBoolean}">
            ${textContainer.text['adminOptinOnlyIfRequired']}
          </c:when>
          <c:otherwise>
            ${textContainer.text['adminOptinForAll']}
          </c:otherwise>
        </c:choose>
        <br />
        
      </c:when>
      <c:otherwise>
        ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.name) } ${textContainer.text['adminNotEnrolled']}<br />
        
      </c:otherwise>  
    </c:choose>
    ${textContainer.text['adminEmailAddressIs']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.email0)}
    <br />
    <c:choose>
      <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.optedIn}">
        <br /><br />
        <form action="UiMainAdmin.manageUser" method="post" style="display: inline">
          <input value="${textContainer.textEscapeDouble['adminManageUser']} ${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.twoFactorUserOperatingOn.name) }" 
            class="tfBlueButton"
             type="submit" />
          <input type="hidden" name="userIdOperatingOn" 
            value="${fn:escapeXml(twoFactorRequestContainer.twoFactorAdminContainer.userIdOperatingOn) }" />
        </form>
      </c:when>
    </c:choose>    
    <br /><br />
        
    <%@ include file="../assetsJsp/auditsInclude.jsp"%>
        
    
  </c:if>
  <br />
  <br />
  ${textContainer.text['adminOptedInUsersPrefix'] } ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorAdminReportBean.optedInUsers}
  <br />
  ${textContainer.text['adminOptedOutUsersPrefix'] } ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorAdminReportBean.optedOutUsers}
  
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

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.canLoggedInUserEmailAll}">
    
    &nbsp; &nbsp;
    
    <form action="../../twoFactorAdminUi/app/UiMainAdmin.adminEmailAllPage" method="get" style="display: inline; font-size: smaller">
      <input value="${textContainer.textEscapeDouble['buttonAdminEmailAll']}" class="tfLinkButton"
      type="submit" />
    </form>
    
  </c:if>  

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.canImportSerials}">
    
    &nbsp; &nbsp;
    
    <form action="../../twoFactorAdminUi/app/UiMainAdmin.adminImportSerialsPage" method="get" style="display: inline; font-size: smaller">
      <input value="${textContainer.textEscapeDouble['buttonAdminImportSerials']}" class="tfLinkButton"
      type="submit" />
    </form>
    
  </c:if>  

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.canAdminReports}">
    
    &nbsp; &nbsp;
    
    <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsIndex" method="get" style="display: inline; font-size: smaller">
      <input value="${textContainer.textEscapeDouble['buttonAdminReportAdmin']}" class="tfLinkButton"
      type="submit" />
    </form>
    
  </c:if>  

  <br /><br />
  
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

