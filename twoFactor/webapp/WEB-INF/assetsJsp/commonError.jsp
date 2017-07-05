<%@ include file="../assetsJsp/commonTaglib.jsp"%>
  <c:if test="${fn:length(twoFactorRequestContainer.error) > 0}">
    <div class="error">
      ${twoFactorRequestContainer.error}
    </div>    
    <br />
    <br />
  </c:if>
