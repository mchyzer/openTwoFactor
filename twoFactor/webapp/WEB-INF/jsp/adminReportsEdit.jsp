<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminReportsEditTitle']}</title>

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
  <b>${textContainer.text['adminReportsEditSubheader']}</b>
  <br />
  ${textContainer.text['adminReportsEditInstructions']}
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>

  <c:if test="${twoFactorRequestContainer.twoFactorAdminContainer.reportAdd }">
  
    <form action="UiMainAdmin.reportsAddEditSubmit" method="post">

      <input type="hidden" name="reportUuid" value="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorReport.uuid}" />
      <div style="background-color: #ffffff; width: 20em;">
        <div class="formBox profileFormBox">
          <div class="formRow">
            <div class="formLabel"><b><label for="reportNameDisplayId">${textContainer.text['adminReportsEditColNameFriendly']}</label></b></div>
            <div class="formValue">
              <input type="text" name="reportNameDisplay" id="reportNameDisplayId" size="18" style="width: 16em;" class="textfield"
                value="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorReport.reportNameFriendly }" />
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
          <div class="formRow">
            <div class="formLabel"><b><label for="reportNameSystemId">${textContainer.text['adminReportsEditColNameSystem']}</label></b></div>
            <div class="formValue">
              <select name="reportNameSystemSelect">
                <option value=""></option>
                <c:forEach items="${twoFactorRequestContainer.twoFactorAdminContainer.reportNameSystems}" var="reportNameSystem">
                  <option value="${fn:escapeXml(reportNameSystem) }">${fn:escapeXml(reportNameSystem)}</option>                
                </c:forEach>
              </select>
              &nbsp; ${textContainer.text['adminReportsEditOrManualNameSystem'] } &nbsp;
              <input type="text" name="reportNameSystem" id="reportNameSystemId" size="18" style="width: 8em;" class="textfield"
                value="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorReport.reportNameSystem }" />
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
          <div class="formRow">
            <div class="formLabel"><b><label for="reportTypeId">${textContainer.text['adminReportsEditColType']}</label></b></div>
            <div class="formValue">
              <select name="reportType" id="reportTypeId">
                <option value=""></option>
                <option value="group" ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorReport.reportType == 'group' ? 'selected="selected"' : ''} 
                   >${textContainer.textEscapeXml['adminReportsEditTypeGroup'] }</option>
                <option value="rollup" ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorReport.reportType == 'rollup' ? 'selected="selected"' : ''}
                   >${textContainer.textEscapeXml['adminReportsEditTypeRollup'] }</option>            
              </select>
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
          <div class="formRow">
            <div class="formLabel"></div>
            <div class="formValue">
            
              <a href="../../twoFactorAdminUi/app/UiMainAdmin.reportsEdit">${textContainer.text['buttonCancel'] }</a>
              &nbsp; &nbsp;
              <input value="${textContainer.textEscapeDouble['buttonSubmit']}" class="tfBlueButton"
                onmouseover="this.style.backgroundColor='#011D5C';" 
                onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </div>
      </div>
      <br /><br />
    </form>  
  </c:if>


  <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsAdd" method="get" style="display: inline; font-size: smaller">
    <input value="${textContainer.textEscapeDouble['adminReportsEditAddNewReport']}" class="tfLinkButton"
    type="submit" />
  </form>

  <table class="tfDarkDataTable">
    
    <tr>
      <th></th>
      <th>${textContainer.text['adminReportsEditColNameFriendly'] }</th>
      <th>${textContainer.text['adminReportsEditColNameSystem'] }</th>
      <th>${textContainer.text['adminReportsEditColType'] }</th>
    </tr>
    <c:forEach items="${twoFactorRequestContainer.twoFactorAdminContainer.reports}" 
        var="twoFactorReport"  >
      <tr>
        <td>
        
          <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsEditReport" method="post" style="display: inline; font-size: smaller">
            <input type="hidden" name="reportUuid" value="${twoFactorReport.uuid}" />
            <input value="${textContainer.textEscapeDouble['adminReportsEditEditButton']}" class="tfLinkButton"
            type="submit" />
          </form>

          <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsDelete" method="post" style="display: inline; font-size: smaller">
            <input type="hidden" name="reportUuid" value="${twoFactorReport.uuid}" />
            <input value="${textContainer.textEscapeDouble['adminReportsEditDeleteButton']}" class="tfLinkButton"
            type="submit" onclick="return confirm('${textContainer.textEscapeSingleDouble['adminReportsEditDeleteConfirm']}');" />
          </form>
        
        </td>
        <td>${fn:escapeXml(twoFactorReport.reportNameFriendly)}</td>   
        <td>${fn:escapeXml(twoFactorReport.reportNameSystem)}</td>   
        <td>${fn:escapeXml(twoFactorReport.reportType)}</td>   
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

