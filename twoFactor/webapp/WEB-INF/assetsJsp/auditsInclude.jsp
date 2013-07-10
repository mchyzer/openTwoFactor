  <c:choose>
    <c:when test="${twoFactorRequestContainer.twoFactorAuditContainer.twoFactorAuditViewsTotalCount == 0}">
      ${textContainer.text['auditsNoAudits']}
    </c:when>
      <c:otherwise>
      <table class="tfDarkDataTable">
        <tr class="fastTableHeader">
          <th>${textContainer.text['auditsHeaderDate']}</th>
          <th>${textContainer.text['auditsHeaderAction']}</th>
          <th>${textContainer.text['auditsHeaderIpAddress']}</th>
          <th>${textContainer.text['auditsHeaderDomainName']}</th>
          <th>${textContainer.text['auditsHeaderOperatingSystem']}</th>
          <th>${textContainer.text['auditsHeaderBrowser']}</th>
          <th>${textContainer.text['auditsHeaderTrustedBrowser']}</th>
          <th>${textContainer.text['auditsHeaderUserLoggedIn']}</th>
          <th>${textContainer.text['auditsHeaderDescription']}</th>
        </tr>
        <c:set var="i" value="0" />
        <c:forEach items="${twoFactorRequestContainer.twoFactorAuditContainer.twoFactorAuditViews}" 
           var="twoFactorAuditView"  >
          <tr class="${i%2==0 ? 'tfEven' : 'tfOdd' }">
            <td>${fn:escapeXml(twoFactorAuditView.theTimestampFormatted)}</td>
            <td>${fn:escapeXml(twoFactorAuditView.actionFormatted)}</td>
            <td>${fn:escapeXml(twoFactorAuditView.ipAddress)}</td>
            <td>${fn:escapeXml(twoFactorAuditView.domainName)}</td>
            <td>${fn:escapeXml(twoFactorAuditView.userAgentOperatingSystem)}</td>
            <td>${fn:escapeXml(twoFactorAuditView.userAgentBrowser)}</td>
            <td>${twoFactorAuditView.trustedBrowser == null ? textContainer.text['no'] : (twoFactorAuditView.trustedBrowser == 'T' ? textContainer.text['yes'] : textContainer.text['no'])}</td>
            <td>${fn:escapeXml(twoFactorAuditView.userUsingName)}</td>
            <td>${fn:escapeXml(twoFactorAuditView.description)}</td>
          </tr>
          <c:set var="i" value="${i+1}" />
        </c:forEach>      
      </table>
    </c:otherwise>
  </c:choose>
