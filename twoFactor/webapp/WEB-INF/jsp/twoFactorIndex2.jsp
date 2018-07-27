<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['indexTitle'] }</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

<!-- twoFactorIndex.jsp -->

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
      <h2>${textContainer.text['indexNotOptedInSubheader']}</h2>
      <br />
      <%@ include file="../assetsJsp/commonError.jsp"%>
      <br />
      
      ${textContainer.text['indexNotOptedInSubtext1']}

      <br />
      ${textContainer.text['index2HowWorks']}

      <h4 style="color:black">${textContainer.text['index2EnrollmentHeader']}</h4>
      
      
      <div class="formBoxNarrow profileFormBoxNarrow">
        <div class="formRow">
          <div class="formLabel">${textContainer.text['index2EnrolledLabel']}</div>
          <div class="formValue">
            <c:choose>
              <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
                ${textContainer.text['index2EnrollmentEnrolled']}<br /><a href="../../twoFactorUi/app/UiMain.optout">${textContainer.text['index2EnrollmentUnEnroll']}</a>
              </c:when>
              <c:otherwise>  
                ${textContainer.text['index2EnrollmentNotEnrolled']}<br /><a href="../../twoFactorUi/app/UiMain.optinWizard">${textContainer.text['index2EnrollmentEnroll']}</a>
              </c:otherwise>
            </c:choose>
          
          </div>
          <div class="formFooter">&nbsp;</div>
        </div>
        <div class="formRow">
          <div class="formLabel">${textContainer.text['index2RequiredLabel']}</div>
          <div class="formValue">
            <c:choose>
              <c:when test="${twoFactorRequestContainer.twoFactorUserLoggedIn.requiredToOptin}">
                ${textContainer.text['index2RequiredToOptin']}
              </c:when>
              <c:otherwise>  
                ${textContainer.text['index2NotRequiredToOptin']}
              </c:otherwise>
            </c:choose>
          
          </div>
          <div class="formFooter">&nbsp;</div>
        </div>
      </div>
      
      <h4 style="color:black">${textContainer.text['index2ProfileHeader']} &nbsp; <a href="../../twoFactorUi/app/UiMain.profile" style="font-size: 80%">${textContainer.text['index2ProfileEdit']}</a>
      &nbsp; <a href="#" onclick="$('#profileInstructions').toggle('slow'); return false;"><img src="../../assets/orange-question-mark-icon-png-clip-art-30a.png" width="20" /></a>
      </h4>
      <div id="profileInstructions" style="display: none;">${textContainer.text['profileInstructionsMain']}</div>
      <div class="formBoxNarrow profileFormBoxNarrow">
        <div class="formRow">
          <div class="formLabel">${textContainer.text['profileEmailLabel']}</div>
          <div class="formValue" style="white-space: nowrap">
            ${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.email0) } 
            (<a href="https://directory.apps.upenn.edu/directory/jsp/fast.do?fastStart=profile"
              onclick="alert('${textContainer.textEscapeSingleDouble['profileEditEmailLinkAlert']}'); return true;"
              >edit email address</a>)
          </div>
          <div class="formFooter">&nbsp;</div>
        </div>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasPhone0}">
          <div class="formRow">
            <div class="formLabel"><label for="phone0">${textContainer.text['index2PhoneNumberLabel1']}</label></div>
            <div class="formValue">${twoFactor:formatPhone(fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone0)) }
              &nbsp; <input type="checkbox" name="phoneVoice0" value="true" disabled="disabled"
              ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice0 == 'true' ? 'checked="checked"' : ''} /> 
              ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
              <input type="checkbox" name="phoneText0" value="true" disabled="disabled" 
              ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText0 == 'true' ? 'checked="checked"' : ''} /> 
              ${textContainer.text['profilePhoneTextOptionLabel']}</div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasPhone1}">
          <div class="formRow">
            <div class="formLabel"><label for="phone1">${textContainer.text['index2PhoneNumberLabel2']}</label></div>
            <div class="formValue">${twoFactor:formatPhone(fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone1)) }
              &nbsp; <input type="checkbox" name="phoneVoice1" value="true" disabled="disabled"
              ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice1 == 'true' ? 'checked="checked"' : ''} /> 
              ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
              <input type="checkbox" name="phoneText1" value="true" disabled="disabled"
              ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText1 == 'true' ? 'checked="checked"' : ''} /> 
              ${textContainer.text['profilePhoneTextOptionLabel']}</div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasPhone2}">
          <div class="formRow">
            <div class="formLabel"><label for="phone2">${textContainer.text['index2PhoneNumberLabel3']}</label></div>
            <div class="formValue">${twoFactor:formatPhone(fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.phone2)) }
              &nbsp; <input type="checkbox" name="phoneVoice2" value="true" disabled="disabled"
              ${twoFactorRequestContainer.twoFactorProfileContainer.phoneVoice2 == 'true' ? 'checked="checked"' : ''} /> 
              ${textContainer.text['profilePhoneVoiceOptionLabel']} &nbsp;&nbsp; 
              <input type="checkbox" name="phoneText2" value="true" disabled="disabled"
              ${twoFactorRequestContainer.twoFactorProfileContainer.phoneText2 == 'true' ? 'checked="checked"' : ''} /> 
              ${textContainer.text['profilePhoneTextOptionLabel']} </div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague0}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin0">${textContainer.text['index2FriendLabel1']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName0) }</div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague1}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin1">${textContainer.text['index2FriendLabel2']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName1) }</div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague2}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin2">${textContainer.text['index2FriendLabel3']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName2) }</div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague3}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin3">${textContainer.text['index2FriendLabel4']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName3) }</div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague4}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin4">${textContainer.text['index2FriendLabel5']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName4) }</div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        
      </div>
      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">

          <h4 style="color:black">${textContainer.text['index2Devices']}
          &nbsp; <a href="#" onclick="$('#devicesInstructions').toggle('slow'); return false;"><img src="../../assets/orange-question-mark-icon-png-clip-art-30a.png" width="20" /></a></h4>
          <div class="formBoxNarrow profileFormBoxNarrow">
            <div class="formRow">
              <div class="formLabel">${textContainer.text['index2DuoPushLabel']}</div>
              <div class="formValue" style="white-space: nowrap">
                <c:choose>
                
                  <c:when  test="${twoFactorRequestContainer.twoFactorDuoPushContainer.enrolledInDuoPush}">
                    ${textContainer.text['index2DuoPushEnrolled']}
                    <br />
                    <form action="../../twoFactorUi/app/UiMain.duoPushUnenroll2" method="post" style="display: inline;">
                      <input value="${textContainer.textEscapeDouble['index2DuoPushUnenrollButton']}" class="indexLinkButton"
                        type="submit" />
                    </form>
                    &nbsp;
                    <form action="../../twoFactorUi/app/UiMain.testDuoPush2" method="post" style="display: inline;">
                      <input value="${textContainer.textEscapeDouble['index2DuoPushTestButton']}" class="indexLinkButton"
                        type="submit" />
                    </form>
                  </c:when>
                  <c:otherwise>
                    ${textContainer.text['index2DuoPushNotEnrolled']}
                    <br />
                    <form action="../../twoFactorUi/app/UiMain.duoPushEnroll2" method="post" style="display: inline;">
                      <input value="${textContainer.textEscapeDouble['index2DuoPushEnrollButton']}" class="indexLinkButton"
                        type="submit" />
                    </form>
                  </c:otherwise>
                </c:choose>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            <c:choose>
              <c:when  test="${twoFactorRequestContainer.twoFactorDuoPushContainer.enrolledInDuoPush}">
                <div class="formRow">
                  <div class="formLabel">${textContainer.text['index2DuoPushByDefaultLabel']}</div>
                  <div class="formValue" style="white-space: nowrap">
                    <c:choose>
                    
                      <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.duoPushByDefaultBoolean}">
                        ${textContainer.text['index2PushByDefault']}
                        <br />
                        <form action="../../twoFactorUi/app/UiMain.duoPushUnenrollWeb2" method="post" style="display: inline;">
                          <input value="${textContainer.textEscapeDouble['index2DontDuoPushDefaultButton']}" class="indexLinkButton"
                            type="submit" />
                        </form>
                      </c:when>
                      <c:otherwise>
                        ${textContainer.text['index2DontPushByDefault']}
                        <br />
                        <form action="../../twoFactorUi/app/UiMain.duoPushEnrollWeb2" method="post" style="display: inline;">
                          <input value="${textContainer.textEscapeDouble['index2DuoPushDefaultButton']}" class="indexLinkButton"
                            type="submit" />
                        </form>
                      </c:otherwise>
                    </c:choose>
                  </div>
                  <div class="formFooter">&nbsp;</div>
                </div>
                
              </c:when>
            </c:choose>
            
            <div class="formRow">
              <div class="formLabel">Default phone</div>
              <div class="formValue" style="white-space: nowrap">
                No, you are using default Duo Push
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            
            <div class="formRow">
              <div class="formLabel">Keychain fob</div>
              <div class="formValue" style="white-space: nowrap">
                <form action="../../twoFactorUi/app/UiMain." method="post" style="display: inline;">
                  <input value="Add" class="indexLinkButton"
                    type="submit" />
                </form>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            <div class="formRow">
              <div class="formLabel">Google</div>
              <div class="formValue" style="white-space: nowrap">
                <form action="../../twoFactorUi/app/UiMain." method="post" style="display: inline;">
                  <input value="Add phone" class="indexLinkButton"
                    type="submit" />
                </form>
                &nbsp;
                <form action="../../twoFactorUi/app/UiMain." method="post" style="display: inline;">
                  <input value="Change secret" class="indexLinkButton"
                    type="submit" />
                </form>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            <div class="formRow">
              <div class="formLabel">Browser trust</div>
              <div class="formValue" style="white-space: nowrap">
                You have 7 trusted browsers<br />
                <form action="../../twoFactorUi/app/UiMain." method="post" style="display: inline;">
                  <input value="Untrust browsers" class="indexLinkButton"
                    type="submit" />
                </form>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            <div class="formRow">
              <div class="formLabel">Printed codes</div>
              <div class="formValue" style="white-space: nowrap">
                Current index is 37<br />
                <form action="../../twoFactorUi/app/UiMain." method="post" style="display: inline;">
                  <input value="Generate new codes" class="indexLinkButton"
                    type="submit" />
                </form>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            
          </div>
        </c:when>
      </c:choose>
      
    </div>

    <br /><br />
    <br /><br />
    
      <form action="../../twoFactorUi/app/UiMain.userAudits" method="get" style="display: inline">
        <input value="${textContainer.textEscapeDouble['buttonActivity']}" class="tfBlueButton"
         type="submit" />
      </form>
      &nbsp;
      <form action="../../twoFactorUi/app/UiMain.helpColleague" method="get" style="display: inline">
        <input value="${textContainer.textEscapeDouble['buttonHelpFriend']}" class="tfBlueButton"
         type="submit" />
      </form>
      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.admin}">
          <form action="../../twoFactorAdminUi/app/UiMainAdmin.adminIndex" method="get" style="display: inline">
            <input value="${textContainer.textEscapeDouble['buttonAdminConsole']}" class="tfBlueButton"
             type="submit" />
          </form>
          &nbsp;

        </c:when>
      </c:choose>
      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.hasReportPrivilege}">
          <form action="../../twoFactorUi/app/UiMain.reports" method="get" style="display: inline">
            <input value="${textContainer.textEscapeDouble['buttonViewReports']}" class="tfBlueButton"
             type="submit" />
          </form>
          &nbsp;

        </c:when>
      </c:choose>
      <c:if test="${twoFactorRequestContainer.hasLogoutUrl}">
        <div class="logoutBottom">
          <a href="../../twoFactorUnprotectedUi/app/UiMainUnprotected.logout">${textContainer.textEscapeXml['buttonLogOut']}</a>
          &nbsp;
        </div>
      </c:if>    
   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
