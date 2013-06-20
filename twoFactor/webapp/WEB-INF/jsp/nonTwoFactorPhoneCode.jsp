<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>Two Factor Phone Code</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader'] }</h1>
  <br />
  <b>${textContainer.text['phoneCodeSubheader']}</b>
  <br />
  <br />
  
  ${textContainer.text['phoneCodeParagraph1']}
  
  <%@ include file="../assetsJsp/commonError.jsp"%>
<%--
    <c:choose>
      <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">

        Note: Only the last verification code sent to you will work.  And this it will only work for a few minutes.
        <br /><br />
        <form action="UiMainPublic.phoneCodeSubmit" method="post">
          <div class="formBox">
            <div class="formRow">
              <div class="formLabel" style="white-space: nowrap; text-align: right"><b><label for="twoFactorCode">Code sent to phone</label></b></div>
              <div class="formValue"><input type="text" name="twoFactorCode" size="12" autocomplete="off" class="textfield" /></div>
              <div class="formFooter">&nbsp;</div>
            </div>
          </div>
          <br />
          <input value="Submit" class="tfBlueButton"
            onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" 
            type="submit" onclick="if (document.getElementById('twoFactorCustomCodeId').value != null && document.getElementById('twoFactorCustomCodeId').value != '') {alert('If you enter a custom secret, you must click the button: Submit custom secret'); return false; }" />
        </form>

      </c:when>
      <c:otherwise>
        You are not currently enrolled in this service.
      </c:otherwise>  
    </c:choose> --%>
    <br /><br /><br />
    <form action="../../twoFactorUi/app/UiMain.index" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
    </form>
    

   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
