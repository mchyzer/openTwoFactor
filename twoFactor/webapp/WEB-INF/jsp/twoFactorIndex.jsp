<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['indexTitle'] }</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

<!-- twoFactorIndex.jsp -->

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
  <%@ include file="../assetsJsp/commonError.jsp"%>
  
    <div class="paragraphs">
      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
          <b>${textContainer.text['indexOptedInSubheader'] }</b>
          <br />
          <br />
          
          ${textContainer.text['indexOptedInSubtext'] }
          
        </c:when>
      <c:otherwise>
  
          <b>${textContainer.text['indexNotOptedInSubheader']}</b>
          <br />
          <br />
          ${textContainer.text['indexNotOptedInSubtext1']}
          <br />
          <br />
  
          <a href="../../twoFactorUi/app/UiMain.optin">${textContainer.text['indexNotOptedInLoginLink']}</a>
  
          <br />
          ${textContainer.text['indexNotOptedInSubtext2']}
         
        </c:otherwise>  
      </c:choose>

    </div>

    <br /><br />
    <br /><br />
    
    <c:if test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
    
      <form action="../../twoFactorUi/app/UiMain.optout" method="get" style="display: inline">
        <input value="${textContainer.textEscapeDouble['buttonOptOut']}" class="tfBlueButton"
        onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
        onclick="return confirm('${textContainer.textEscapeSingleDouble['buttonOptOutConfirm']}');" />
      </form>
      &nbsp;
    
      <form action="../../twoFactorUi/app/UiMain.changeDevice" method="get" style="display: inline">
        <input value="${textContainer.textEscapeDouble['buttonChangeDevice']}" class="tfBlueButton"
        onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
        onclick="return confirm('${textContainer.textEscapeSingleDouble['buttonChangeDeviceConfirm']}');" />
      </form>
      &nbsp;
    
    </c:if>
    <c:if test="${!twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
      <form action="../../twoFactorUi/app/UiMain.optin" method="get" style="display: inline">
        <input value="${textContainer.textEscapeDouble['buttonOptIn']}" class="tfBlueButton"
        onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
      </form>
      &nbsp;
    </c:if>
    
    <form action="../../twoFactorUi/app/UiMain.profileView" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonProfile']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
    </form>
    &nbsp;

      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
          <form action="../../twoFactorUi/app/UiMain.showOneTimeCodes" method="get" style="display: inline">
            <input value="${textContainer.textEscapeDouble['buttonGenerateCodes']}" class="tfBlueButton"
            onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
          </form>
          &nbsp;

          <c:choose>
            <c:when test="${twoFactorRequestContainer.twoFactorUserLoggedIn.trustedBrowserCount > 0}">
              <form action="../../twoFactorUi/app/UiMain.untrustBrowsers" method="get" style="display: inline">
                <input value="${textContainer.textEscapeDouble['buttonUntrustBrowsers']}" class="tfBlueButton"
                onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
                onclick="return confirm('${textContainer.textEscapeSingleDouble['buttonUntrustBrowsersConfirm']}');" />
              </form>
              &nbsp;
            </c:when>
            <c:otherwise>
              <input value="${textContainer.textEscapeDouble['buttonUntrustBrowsers']}" class="tfBlueButton"
                onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit"
                onclick="alert('${textContainer.textEscapeSingleDouble['buttonUntrustBrowsersNone']}'); return false;" />
            &nbsp;
            
            </c:otherwise>
          </c:choose>

        </c:when>
      </c:choose>
      <form action="../../twoFactorUi/app/UiMain.userAudits" method="get" style="display: inline">
        <input value="${textContainer.textEscapeDouble['buttonActivity']}" class="tfBlueButton"
        onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
      </form>
      &nbsp;
      <form action="../../twoFactorUi/app/UiMain.helpColleague" method="get" style="display: inline">
        <input value="${textContainer.textEscapeDouble['buttonHelpFriend']}" class="tfBlueButton"
        onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
      </form>
      &nbsp;

      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.admin}">
          <form action="../../twoFactorAdminUi/app/UiMainAdmin.adminIndex" method="get" style="display: inline">
            <input value="${textContainer.textEscapeDouble['buttonAdminConsole']}" class="tfBlueButton"
            onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
          </form>
          &nbsp;

        </c:when>
      </c:choose>
      <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
        <div class="logoutBottom">
          <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
          &nbsp;
        </div>
      </c:if>    
   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
