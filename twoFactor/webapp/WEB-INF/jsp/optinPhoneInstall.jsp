<%@ include file="../assetsJsp/commonTop.jsp"%>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en"><head>

<title>${textContainer.text['optinPhoneInstallTitle']}</title>

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
    <div class="paragraphs">

    <h2>${textContainer.text['optinPhoneInstallSubheader']}</h2>
    <br /><br />
    <%@ include file="../assetsJsp/commonError.jsp"%>
    <br /><br />
    
              <c:choose>
                <%-- see if the user has any phones --%> 
                <c:when test="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.hasPhoneNumbers}">
                  ${textContainer.text['optinPhoneInstallText']}
                  <br /><br />
                  <c:set var="i" value="0" />
                  <c:forEach items="${twoFactorRequestContainer.twoFactorHelpLoggingInContainer.phonesForScreen}" 
                      var="twoFactorPhoneForScreen"  >
                    <c:if test="${twoFactorPhoneForScreen.hasPhone}">
      
                      <div class="formBox" style="width: 28em; margin-bottom: 1em; border: none;">
                        <div class="formRow" style="height: 2em">
                          <div class="formValue" style="width: 12em">
                            <c:choose>
                              
                              <c:when test="${twoFactorPhoneForScreen.voice}">
                                <form action="UiMain.optinWizardPhoneCodeSent" method="post" style="display: inline">
                                  <input type="hidden" name="birthdayTextfield" 
                                    value="${twoFactorRequestContainer.twoFactorUserLoggedIn.birthDayUuid}" />
                                  <input value="${textContainer.textEscapeDouble['havingTroubleVoicePrefix']} ${ fn:escapeXml(twoFactorPhoneForScreen.phoneForScreen ) }" class="tfBlueButton"
                                     type="submit"
                                     />
                                  <input type="hidden" name="phoneIndex" 
                                    value="${i}" />
                                  <input type="hidden" name="phoneType" 
                                    value="voice" />
                                </form>
                              </c:when>         
                              <c:otherwise>
                                &nbsp;
                              </c:otherwise>
                            </c:choose>
                          </div>
                          <div class="formValue" style="width: 12em">
                            <c:if test="${twoFactorPhoneForScreen.text}">
                              <form action="UiMain.optinWizardPhoneCodeSent" method="post" style="display: inline">
                                <input type="hidden" name="birthdayTextfield" 
                                  value="${twoFactorRequestContainer.twoFactorUserLoggedIn.birthDayUuid}" />
                                <input value="${textContainer.textEscapeDouble['havingTroubleTextPrefix']} ${ fn:escapeXml(twoFactorPhoneForScreen.phoneForScreen ) }" class="tfBlueButton"
                                   type="submit"
                                   />
                                <input type="hidden" name="phoneIndex" 
                                  value="${i}" />
                                <input type="hidden" name="phoneType" 
                                  value="text" />
                              </form>
                            </c:if>                  
                          </div>
                        </div>
                      </div>
      
      
                    </c:if>
                    <c:set var="i" value="${i+1}" />
                  </c:forEach>
                      
                  
                  
                </c:when>            
                <c:otherwise>
                  ${textContainer.text['optinStep2phoneNoPhone']}          
                </c:otherwise>
              </c:choose>
    
    
    <br />
    <br />
    <a href="../../twoFactorUi/app/UiMain.index">${textContainer.text['optinCancelButton']}</a> &nbsp; &nbsp; &nbsp; &nbsp;
    
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


</body></html>

