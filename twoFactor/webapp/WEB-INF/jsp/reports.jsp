<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['viewReportsTitle']}</title>

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
  <b>${textContainer.text['viewReportsSubheader']}</b>
  <br />
  ${textContainer.text['viewReportsInstructions']}

  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>

    <form action="UiMain.reports" method="get">

      <div style="background-color: #ffffff; width: 20em;">
        <div class="formBox profileFormBox">
          <div class="formRow">
            <div class="formLabel"><b><label for="reportNameId">${textContainer.text['viewReportsLabelReportName'] }</label></b></div>
            <div class="formValue">
              <c:set var="selectedReportUuid" value="${twoFactorRequestContainer.twoFactorViewReportContainer.mainReportStat.twoFactorReport.uuid}" />
              <select name="reportUuid" id="reportNameId" >
                <option value=""></option>
                <c:forEach items="${twoFactorRequestContainer.twoFactorViewReportContainer.reportsAllowedToView}" var="twoFactorReport">
                  <option value="${fn:escapeXml(twoFactorReport.uuid) }"
                    ${twoFactorReport.uuid == selectedReportUuid ? 'selected="selected"' : '' }
                    >${fn:escapeXml(twoFactorReport.reportNameFriendly)}</option>                
                </c:forEach>
              </select>
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>


          <div class="formRow">
            <div class="formLabel"></div>
            <div class="formValue">
            
              <input value="${textContainer.textEscapeDouble['viewReportsButtonViewReport']}" class="tfBlueButton"
                onmouseover="this.style.backgroundColor='#011D5C';" 
                onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </div>
      </div>
      <br />
    </form>  
  
    <c:if test="${twoFactorRequestContainer.twoFactorViewReportContainer.showReport}">
    
      <br />
      <b>${textContainer.text['viewReportsLabelReportName']}</b> ${twoFactorRequestContainer.twoFactorViewReportContainer.mainReportStat.twoFactorReport.reportNameFriendly}<br />
      <b>${textContainer.text['viewReportsLabelTotal']}</b> ${twoFactorRequestContainer.twoFactorViewReportContainer.mainReportStat.total}<br />
      <b>${textContainer.text['viewReportsLabelNumberNotOptedIn']}</b> ${twoFactorRequestContainer.twoFactorViewReportContainer.mainReportStat.totalNotOptedIn}<br />
      <b>${textContainer.text['viewReportsLabelPercentageOptedIn']}</b> ${twoFactorRequestContainer.twoFactorViewReportContainer.mainReportStat.percentOptedIn}<br />
      <br /><br />
        <c:if test="${twoFactorRequestContainer.twoFactorViewReportContainer.hasChildReports}">
          <table class="tfDarkDataTable">
            
            
            <tr>
              <th>${textContainer.text['viewReportsLabelSubreportName'] }</th>
              <th>${textContainer.text['viewReportsLabelTotal'] }</th>
              <th>${textContainer.text['viewReportsLabelNumberNotOptedIn'] }</th>
              <th>${textContainer.text['viewReportsLabelPercentageOptedIn'] }</th>
            </tr>
            <c:forEach items="${twoFactorRequestContainer.twoFactorViewReportContainer.childReportStats}" 
                var="twoFactorReportStat"  >
              <tr>
                <td>${fn:escapeXml(twoFactorReportStat.twoFactorReport.reportNameFriendly)}</td>   
                <td>${twoFactorReportStat.total}</td>   
                <td>${twoFactorReportStat.totalNotOptedIn}</td>   
                <td>${twoFactorReportStat.percentOptedIn}</td>   
              </tr>  
            </c:forEach>
          </table>
        
        
        
          <br /><br />
        </c:if>


      <table class="tfDarkDataTable">
        
        <tr>
          <th>${textContainer.text['viewReportsLabelSubjectsNotOptedIn'] }</th>
        </tr>
        <c:forEach items="${twoFactorRequestContainer.twoFactorViewReportContainer.subjectDescriptionsNotOptedIn}" 
            var="subjectDescriptionNotOptedIn"  >
          <tr>
            <td>${fn:escapeXml(subjectDescriptionNotOptedIn)}</td>   
          </tr>  
        </c:forEach>
      </table>
    
    </c:if>

    <br />
    <br />
    ${textContainer.text['viewReportsOptedInUsersPrefix'] } ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorAdminReportBean.optedInUsers}
    <br />
    ${textContainer.text['viewReportsOptedOutUsersPrefix'] } ${twoFactorRequestContainer.twoFactorAdminContainer.twoFactorAdminReportBean.optedOutUsers}

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

