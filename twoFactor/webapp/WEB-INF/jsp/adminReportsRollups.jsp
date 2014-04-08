<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminReportsRollupsTitle']}</title>

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
  <b>${textContainer.text['adminReportsRollupsSubheader']}</b>
  <br />
  ${textContainer.text['adminReportsRollupsInstructions']}

  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>

    <form action="UiMainAdmin.reportsRollupsSubmit" method="post">

      <div style="background-color: #ffffff; width: 20em;">
        <div class="formBox profileFormBox">
          <div class="formRow">
            <div class="formLabel"><b><label for="parentReportUuidId">${textContainer.text['adminReportsRollupsColParentReport'] }</label></b></div>
            <div class="formValue">
                
              <select name="parentReportUuid" id="parentReportUuidId" >
                <option value=""></option>
                <c:forEach items="${twoFactorRequestContainer.twoFactorAdminContainer.reportsRollup}" var="twoFactorReport">
                  <option value="${fn:escapeXml(twoFactorReport.uuid) }">${fn:escapeXml(twoFactorReport.reportNameFriendly)}</option>                
                </c:forEach>
              </select>
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
          <div class="formRow">
            <div class="formLabel"><b><label for="reportUserId">${textContainer.text['adminReportsRollupsColChildReport'] }</label></b></div>
            <div class="formValue">
                
              <select name="childReportUuid" id="childReportUuidId" >
                <option value=""></option>
                <c:forEach items="${twoFactorRequestContainer.twoFactorAdminContainer.reports}" var="twoFactorReport">
                  <option value="${fn:escapeXml(twoFactorReport.uuid) }">${fn:escapeXml(twoFactorReport.reportNameFriendly)}</option>                
                </c:forEach>
              </select>
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>


          <div class="formRow">
            <div class="formLabel"></div>
            <div class="formValue">
            
              <a href="../../twoFactorAdminUi/app/UiMainAdmin.reportsIndex">${textContainer.text['buttonCancel'] }</a>
              &nbsp; &nbsp;
              <input value="${textContainer.textEscapeDouble['buttonAdd']}" class="tfBlueButton"
                onmouseover="this.style.backgroundColor='#011D5C';" 
                onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </div>
      </div>
      <br /><br />
    </form>  

  <table class="tfDarkDataTable">
    
    <tr>
      <th></th>
      <th>${textContainer.text['adminReportsRollupsColParentReport'] }</th>
      <th>${textContainer.text['adminReportsRollupsColChildReport'] }</th>
    </tr>
    <c:forEach items="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorReportRollups}" 
        var="twoFactorReportRollup"  >
      <tr>
        <td>
        
          <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsRollupsDelete" method="post" style="display: inline; font-size: smaller">
            <input type="hidden" name="reportRollupUuid" value="${twoFactorReportRollup.uuid}" />
            <input value="${textContainer.textEscapeDouble['adminReportsEditDeleteButton']}" class="tfLinkButton"
            type="submit" onclick="return confirm('${textContainer.textEscapeSingleDouble['adminReportsRollupsDeleteConfirm']}');" />
          </form>
        
        </td>
        <td>${fn:escapeXml(twoFactorReportRollup.parentReport.reportNameFriendly)}</td>   
        <td>${fn:escapeXml(twoFactorReportRollup.childReport.reportNameFriendly)}</td>   
      </tr>  
    </c:forEach>
  </table>

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
  
    &nbsp; &nbsp;
  
  <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsIndex" method="get" style="display: inline; font-size: smaller">
    <input value="${textContainer.textEscapeDouble['buttonAdminReportAdmin']}" class="tfLinkButton"
    type="submit" />
  </form>
  
  
  <br /><br />
  
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

