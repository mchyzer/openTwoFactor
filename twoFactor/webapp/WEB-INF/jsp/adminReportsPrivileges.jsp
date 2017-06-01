<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['adminReportsPrivilegesTitle']}</title>

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
  <h2>${textContainer.text['adminReportsPrivilegesSubheader']}</h2>
  <br />
  ${textContainer.text['adminReportsPrivilegesInstructions']}

  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <br />

    <form action="UiMainAdmin.reportsPrivilegesSubmit" method="post">

      <div style="background-color: #ffffff; width: 20em;">
        <div class="formBox profileFormBox">
          <div class="formRow">
            <div class="formLabel"><b><label for="reportNameDisplayId">${textContainer.text['adminReportsPrivilegesColReport'] }</label></b></div>
            <div class="formValue">
                
              <select name="reportUuid" id="reportNameDisplayId" >
                <option value=""></option>
                <c:forEach items="${twoFactorRequestContainer.twoFactorAdminContainer.reports}" var="twoFactorReport">
                  <option value="${fn:escapeXml(twoFactorReport.uuid) }">${fn:escapeXml(twoFactorReport.reportNameFriendly)}</option>                
                </c:forEach>
              </select>
                
                
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
          <div class="formRow">
            <div class="formLabel"><b><label for="reportUserId">${textContainer.text['adminReportsPrivilegesColUser'] }</label></b></div>
            <div class="formValue">
                
             <twoFactor:combobox filterOperation="UiMainAdmin.personPicker" 
                idBase="userIdOperatingOn" 
              />
                
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>


          <div class="formRow">
            <div class="formLabel"></div>
            <div class="formValue">
            
              <a href="../../twoFactorAdminUi/app/UiMainAdmin.reportsIndex">${textContainer.text['buttonCancel'] }</a>
              &nbsp; &nbsp;
              <input value="${textContainer.textEscapeDouble['buttonAdd']}" class="tfBlueButton"
                 
                 type="submit" />
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
      <th>${textContainer.text['adminReportsPrivilegesColReport'] }</th>
      <th>${textContainer.text['adminReportsPrivilegesColUser'] }</th>
    </tr>
    <c:forEach items="${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorReportPrivileges}" 
        var="twoFactorReportPrivilege"  >
      <tr>
        <td>
        
          <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsPrivilegesDelete" method="post" style="display: inline; font-size: smaller">
            <input type="hidden" name="reportPrivilegeUuid" value="${twoFactorReportPrivilege.uuid}" />
            <input value="${textContainer.textEscapeDouble['adminReportsEditDeleteButton']}" class="tfLinkButton"
            type="submit" onclick="return confirm('${textContainer.textEscapeSingleDouble['adminReportsPrivilegeDeleteConfirm']}');" />
          </form>
        
        </td>
        <td>${fn:escapeXml(twoFactorReportPrivilege.twoFactorReport.reportNameFriendly)}</td>   
        <td>${fn:escapeXml(twoFactorReportPrivilege.twoFactorUser.descriptionAdmin)}</td>   
      </tr>  
    </c:forEach>
  </table>

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
  
    &nbsp; &nbsp;
  
  <form action="../../twoFactorAdminUi/app/UiMainAdmin.reportsIndex" method="get" style="display: inline; font-size: smaller">
    <input value="${textContainer.textEscapeDouble['buttonAdminReportAdmin']}" class="tfLinkButton"
    type="submit" />
  </form>
  
  
  <br /><br />
  
  <%@ include file="../assetsJsp/commonAbout.jsp"%>

</div>

</body></html>

