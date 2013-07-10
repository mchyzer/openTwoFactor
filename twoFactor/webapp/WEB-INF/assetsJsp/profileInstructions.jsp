<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  <div class="paragraphs">
    <c:if test="${!twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
      ${textContainer.text['profileInstructionsOptedInTop']}
    </c:if>
    ${textContainer.text['profileInstructionsMain']}
  </div>
