<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  <div style="width:45em">
    <c:if test="${!twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
      ${textContainer.text['profileInstructionsOptedInTop']}
    </c:if>
    ${textContainer.text['profileInstructionsMain']}
  </div>
