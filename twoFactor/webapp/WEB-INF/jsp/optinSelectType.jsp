<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinSelectTypeTitle']}</title>

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
  <form action="UiMain.optinWizardSubmitType" method="post">
    <div class="paragraphs">

    <h2>${textContainer.text['optinSelectTypeSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    <table>
      <tr style="vertical-align: top">
        <td style="padding-top: 0.25em; padding-right: 0.5em"><input type="radio" id="optinTypeAppId" name="optInTypeName" value="app" checked="checked" /></td>
        <td>
          <label for="optinTypeAppId">${textContainer.text['optinTypeAppLabel']}</label>
          <div class="formElementHelp">${textContainer.text['optinTypeAppHelp']}</div>
          <br />
        </td>
      </tr>
      <tr style="vertical-align: top">
        <td style="padding-top: 0.25em; padding-right: 0.5em"><input type="radio" id="optinTypePhoneId" name="optInTypeName" value="phone" /></td>
        <td>
          <label for="optinTypePhoneId">${textContainer.text['optinTypePhoneLabel']}</label>
          <div class="formElementHelp">${textContainer.text['optinTypePhoneHelp']}</div>
          <br />
        </td>
      </tr>
      <tr style="vertical-align: top">
        <td style="padding-top: 0.25em; padding-right: 0.5em"><input type="radio" id="optinTypeFobId" name="optInTypeName" value="fob" /></td>
        <td>
          <label for="optinTypeFobId">${textContainer.text['optinTypeFobLabel']}</label>
          <div class="formElementHelp">${textContainer.text['optinTypeFobHelp']}</div>
        </td>
      </tr>
    </table>
    
    <input type="hidden" name="birthdayTextfield" 
      value="${twoFactorRequestContainer.twoFactorUserLoggedIn.birthDayUuid}" />
    
    <br />
    <br />
    <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['optinCancelButton']}</a> &nbsp; &nbsp; &nbsp; &nbsp;
    <input value="${textContainer.textEscapeDouble['buttonNextStep']}" class="tfBlueButton" 
       type="submit" />
    
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
</form>

</body></html>

