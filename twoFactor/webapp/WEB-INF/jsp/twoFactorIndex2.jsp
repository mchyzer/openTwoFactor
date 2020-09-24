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

      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">
          <h4 style="color:black">Quick links</h4>

          <div class="formBoxNarrow profileFormBoxNarrow">
            <div class="formRow" style="white-space: nowrap;">
              <!--  div class="formLabel">&nbsp;</div -->
              <!-- div class="formValue" --> 
                <c:choose>
                  <c:when  test="${twoFactorRequestContainer.twoFactorDuoPushContainer.enrolledInDuoPush}">
                    <form action="../../twoFactorUi/app/UiMain.duoPushUnenroll2" method="post" style="display: inline;">
                      <input value="${textContainer.textEscapeDouble['index2DuoPushUnenrollButton']}" class="tfBlueButton"
                        type="submit" />
                    </form>
                    &nbsp;
                    <form action="../../twoFactorUi/app/UiMain.duoPushChangePhone" method="post" style="display: inline;">
                      <input value="${textContainer.textEscapeDouble['index2ChangePhonesButton']}" class="tfBlueButton"
                        type="submit" />
                    </form>
                    &nbsp;
                  </c:when>
                  <c:otherwise>
                    <form action="../../twoFactorUi/app/UiMain.duoPushEnroll2" method="post" style="display: inline;">
                      <input value="${textContainer.textEscapeDouble['index2DuoPushEnrollButton']}" class="tfBlueButton"
                        type="submit" />
                    </form>
                    &nbsp;
                  </c:otherwise>
                </c:choose>
                <form action="../../twoFactorUi/app/UiMain.profile" method="get" style="display: inline;">
                  <input value="Edit phone numbers" class="tfBlueButton"
                    type="submit" />
                </form>
                &nbsp;
                <form action="../../twoFactorUi/app/UiMain.showOneTimeCodes2" method="post" style="display: inline">
                  <input value="Print one-time codes" class="tfBlueButton"
                   type="submit" />
                </form>
              <!--  /div -->
            </div>
          </div>
        </c:when>
      </c:choose>


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
            
            <c:choose>
              <c:when  test="${ (!twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn) && twoFactorRequestContainer.twoFactorUserLoggedIn.requiredToOptin}">
                ${textContainer.text['profileEditEmailPrefix']}<a onclick="alert('${textContainer.textEscapeSingleDouble['profileEditEmailLinkAlertCant']}'); return false"
                href="#" 
              </c:when>
              <c:otherwise>
                ${textContainer.text['profileEditEmailPrefix']}<a onclick="alert('${textContainer.textEscapeSingleDouble['profileEditEmailLinkAlert']}'); return true;"
                href="${textContainer.textEscapeDouble['profileEditEmailLinkUrl']}" target="_blank"
              </c:otherwise>
            </c:choose>
            >${textContainer.textEscapeXml['profileEditEmailLinkText']}</a>${textContainer.text['profileEditEmailSuffix']}
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
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName0) }
                          &nbsp;
                  
              <form action="../../twoFactorUi/app/UiMain.removeFriend" method="post" style="display: inline; font-size: smaller">
                <input value="${textContainer.textEscapeDouble['adminColleaguesRemoveFriend']}" class="tfLinkButton" style="color: #800020" type="submit" />
                <input type="hidden" name="toUserUuid" value="${twoFactorRequestContainer.twoFactorUserLoggedIn.colleagueUserUuid0 }" />
              </form>
            
              <br /><a href="../../twoFactorUi/app/UiMain.helpColleague">${textContainer.text['buttonHelpFriend']}</a>
              
            </div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague1}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin1">${textContainer.text['index2FriendLabel2']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName1) }
            
                                      &nbsp;
                  
              <form action="../../twoFactorUi/app/UiMain.removeFriend" method="post" style="display: inline; font-size: smaller">
                <input value="${textContainer.textEscapeDouble['adminColleaguesRemoveFriend']}" class="tfLinkButton" style="color: #800020" type="submit" />
                <input type="hidden" name="toUserUuid" value="${twoFactorRequestContainer.twoFactorUserLoggedIn.colleagueUserUuid1 }" />
              </form>
            
              <br /><a href="../../twoFactorUi/app/UiMain.helpColleague">${textContainer.text['buttonHelpFriend']}</a></div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague2}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin2">${textContainer.text['index2FriendLabel3']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName2) }
                                      &nbsp;
                  
              <form action="../../twoFactorUi/app/UiMain.removeFriend" method="post" style="display: inline; font-size: smaller">
                <input value="${textContainer.textEscapeDouble['adminColleaguesRemoveFriend']}" class="tfLinkButton" style="color: #800020" type="submit" />
                <input type="hidden" name="toUserUuid" value="${twoFactorRequestContainer.twoFactorUserLoggedIn.colleagueUserUuid2 }" />
              </form>
            
              <br /><a href="../../twoFactorUi/app/UiMain.helpColleague">${textContainer.text['buttonHelpFriend']}</a></div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague3}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin3">${textContainer.text['index2FriendLabel4']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName3) }
                                      &nbsp;
                  
              <form action="../../twoFactorUi/app/UiMain.removeFriend" method="post" style="display: inline; font-size: smaller">
                <input value="${textContainer.textEscapeDouble['adminColleaguesRemoveFriend']}" class="tfLinkButton" style="color: #800020" type="submit" />
                <input type="hidden" name="toUserUuid" value="${twoFactorRequestContainer.twoFactorUserLoggedIn.colleagueUserUuid3 }" />
              </form>
            
              <br /><a href="../../twoFactorUi/app/UiMain.helpColleague">${textContainer.text['buttonHelpFriend']}</a></div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        <c:if test="${twoFactorRequestContainer.twoFactorProfileContainer.hasColleague4}">
          <div class="formRow">
            <div class="formLabel"><label for="colleagueLogin4">${textContainer.text['index2FriendLabel5']}</label></div>
            <div class="formValue">${fn:escapeXml(twoFactorRequestContainer.twoFactorProfileContainer.colleagueName4) }
                                      &nbsp;
                  
              <form action="../../twoFactorUi/app/UiMain.removeFriend" method="post" style="display: inline; font-size: smaller">
                <input value="${textContainer.textEscapeDouble['adminColleaguesRemoveFriend']}" class="tfLinkButton" style="color: #800020" type="submit" />
                <input type="hidden" name="toUserUuid" value="${twoFactorRequestContainer.twoFactorUserLoggedIn.colleagueUserUuid4 }" />
              </form>
            
              <br /><a href="../../twoFactorUi/app/UiMain.helpColleague">${textContainer.text['buttonHelpFriend']}</a></div>
            <div class="formFooter">&nbsp;</div>
          </div>
        </c:if>
        
      </div>
      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.optedIn}">

          <h4 style="color:black">${textContainer.text['index2Devices']} &nbsp; <a href="../../twoFactorUi/app/UiMain.testCode" style="font-size: 80%">${textContainer.text['index2testCodeButton']}</a>
          &nbsp; <a href="#" onclick="$('#devicesInstructions').toggle('slow'); return false;"><img src="../../assets/orange-question-mark-icon-png-clip-art-30a.png" width="20" /></a></h4>
          
          <div id="devicesInstructions" style="display: none;">${textContainer.text['deviceInstructionsMain']}</div>
          
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
                    <form action="../../twoFactorUi/app/UiMain.duoPushTestOnly" method="post" style="display: inline;">
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
                &nbsp; <form action="../../twoFactorUi/app/UiMain.duoSync" method="post" style="display: inline;">
                  <input value="${textContainer.textEscapeDouble['index2DuoSyncButton']}" class="indexLinkButton"
                    type="submit" />
                </form>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>

            <c:choose>
              <c:when  test="${twoFactorRequestContainer.twoFactorDuoPushContainer.enrolledInDuoPush}">
                        
              <div class="formRow">
                <div class="formLabel">${textContainer.text['index2DuoPushPhoneLabel']}</div>
                <div class="formValue" style="white-space: nowrap">
                  ${twoFactorRequestContainer.twoFactorDuoPushContainer.pushPhoneLabel}
                  <br />
                  <form action="../../twoFactorUi/app/UiMain.duoPushChangePhone" method="post" style="display: inline;">
                    <input value="${textContainer.textEscapeDouble['index2ChangePhonesButton']}" class="indexLinkButton"
                      type="submit" />
                  </form>
                  
                </div>
                <div class="formFooter">&nbsp;</div>
              </div>
            </c:when>
          </c:choose>

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
              <div class="formLabel">${textContainer.text['index2VoiceTextByDefaultLabel']}</div>
              <div class="formValue" style="white-space: nowrap">
                <c:choose>
              
                  <c:when  test="${twoFactorRequestContainer.twoFactorDuoPushContainer.enrolledInDuoPush}">
                    ${textContainer.text['index2VoiceTextByDefaultPush']}
                  
                  
                  </c:when>
                  
                  <c:when test="${twoFactorRequestContainer.twoFactorUserLoggedIn.phoneAutoCallText 
                      && twoFactorRequestContainer.twoFactorUserLoggedIn.phoneAutoCallTextType == 'voice'}">
                    ${textContainer.text['index2VoiceByDefault']}
                  </c:when>
                  
                  <c:when test="${twoFactorRequestContainer.twoFactorUserLoggedIn.phoneAutoCallText 
                      && twoFactorRequestContainer.twoFactorUserLoggedIn.phoneAutoCallTextType == 'text'}">
                    ${textContainer.text['index2TextByDefault']}
                  </c:when>
                  
                  <c:otherwise>
                    ${textContainer.text['index2VoiceTextByDefaultNo']}
                  </c:otherwise>

                </c:choose>
                
                <c:if test="${! twoFactorRequestContainer.twoFactorDuoPushContainer.enrolledInDuoPush}"> 
                  <br />
                  <c:choose>
                    <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.phoneAutoCallText}">
                      <form action="../../twoFactorUi/app/UiMain.defaultCallTextUnenroll" method="post" style="display: inline;">
                        <input value="${textContainer.textEscapeDouble['index2UnenrollPhoneTextDefaultButton']}" class="indexLinkButton"
                          type="submit" />
                      </form><br />
                      <form action="../../twoFactorUi/app/UiMain.defaultCallTextChange" method="post" style="display: inline;">
                        <input value="${textContainer.textEscapeDouble['index2ChangePhoneTextDefaultButton']}" class="indexLinkButton"
                          type="submit" />
                      </form> &nbsp;
                      
                    </c:when>
                    <c:otherwise>
                      <form action="../../twoFactorUi/app/UiMain.defaultCallTextEnroll" method="post" style="display: inline;">
                        <input value="${textContainer.textEscapeDouble['index2EnrollPhoneTextDefaultButton']}" class="indexLinkButton"
                          type="submit" />
                      </form>
                    </c:otherwise>
                  </c:choose>
                </c:if>
                
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            
            <div class="formRow">
              <div class="formLabel">${textContainer.text['keychainFobLabel']}</div>
              <div class="formValue" style="white-space: nowrap">
                <c:choose>
                  <c:when test="${twoFactorRequestContainer.twoFactorUserLoggedIn.hasFob}">
                    ${textContainer.text['keychainFobHasFob']}
                  </c:when>
                  <c:otherwise>
                    <form action="../../twoFactorUi/app/UiMain.keychainFobAdd" method="post" style="display: inline;">
                      <input value="${textContainer.textEscapeDouble['keychainFobAddButtonLabel']}" class="indexLinkButton"
                        type="submit" />
                    </form>
                  </c:otherwise>
                </c:choose>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            <div class="formRow">
              <div class="formLabel">${textContainer.text['index2hotpLabel']}</div>
              <div class="formValue" style="white-space: nowrap">
                <form action="../../twoFactorUi/app/UiMain.totpAdd" method="post" style="display: inline;">
                  <input value="${textContainer.textEscapeDouble['index2addHotpAppButton']}" class="indexLinkButton"
                    type="submit" />
                </form>
                &nbsp;
                <form action="../../twoFactorUi/app/UiMain.totpChangeSecret" method="post" style="display: inline;">
                  <input value="${textContainer.textEscapeDouble['index2hotpChangeSecret']}" class="indexLinkButton"
                        onclick="return confirm('${textContainer.textEscapeSingleDouble['index2hotpConfirmChangeSecret']}');"
                    type="submit" />
                </form>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            <div class="formRow">
              <div class="formLabel">${textContainer.text['trustedBrowserCountLabel']}</div>
              <div class="formValue" style="white-space: nowrap">
                
                ${textContainer.text['trustedBrowserCountText']}
                <c:if test="${twoFactorRequestContainer.twoFactorUntrustBrowserContainer.numberOfBrowsersWithoutUntrusting > 0} ">
                  <br />
                  <form action="../../twoFactorUi/app/UiMain.untrustBrowsers" method="post" style="display: inline;">
                    <input value="${textContainer.textEscapeDouble['untrustBrowserButtonText']}" class="indexLinkButton"
                      type="submit" />
                  </form>
                </c:if>
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            <div class="formRow">
              <div class="formLabel">${textContainer.textEscapeDouble['printedCodesLabel']}</div>
              <div class="formValue" style="white-space: nowrap">
                
                ${textContainer.text['printedCodesCurrentIndex']}
                
                <br />
                
                <form action="../../twoFactorUi/app/UiMain.showOneTimeCodes2" method="post" style="display: inline">
                  <input value="${textContainer.textEscapeDouble['buttonGenerateCodes']}" class="indexLinkButton"
                   type="submit" />
                </form>
                
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
            
            
          </div>
        </c:when>
      </c:choose>
      
      <h4 style="color:black">${textContainer.text['index2usersSelectedMe']}
      &nbsp; <a href="#" onclick="$('#usersSelectedMeInstructions').toggle('slow'); return false;"><img src="../../assets/orange-question-mark-icon-png-clip-art-30a.png" width="20" /></a></h4>
      
      <div id="usersSelectedMeInstructions" style="display: none;">${textContainer.text['usersSelectedMeInstructionsMain']}</div>
      
      <div class="formBoxNarrow profileFormBoxNarrow">
        <c:choose>
          <c:when test="${twoFactorRequestContainer.twoFactorUserLoggedIn.numberOfUsersWhoPickedThisUserToOptThemOut == 0}">
          
            <div class="formRow">
              <div class="formLabel">${textContainer.text['index2UserSelectedMeNone']}</div>
              <div class="formValue" style="white-space: nowrap">
              </div>
              <div class="formFooter">&nbsp;</div>
            </div>
          
          </c:when>
          
          <c:otherwise>

            <c:forEach items="${twoFactorRequestContainer.twoFactorUserLoggedIn.usersWhoPickedThisUserToOptThemOut}" var="colleague">
          
              <div class="formRow">
                <div class="formLabel">${textContainer.text['index2UserSelectedMe']}</div>
                <div class="formValue" style="white-space: nowrap">
                  ${colleague.netId}: ${colleague.name}
                  &nbsp;
                  
                  <form action="../../twoFactorUi/app/UiMain.removeFriend" method="post" style="display: inline; font-size: smaller">
                    <input value="${textContainer.textEscapeDouble['index2ColleaguesRemoveFriend']}" class="tfLinkButton" style="color: #800020" type="submit" />
                    <input type="hidden" name="fromUserUuid" value="${colleague.uuid }" />
                  </form>
                </div>
                <div class="formFooter">&nbsp;</div>
              </div>
            </c:forEach>
          
          </c:otherwise>
        
        </c:choose>
    </div>


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
      &nbsp;
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
        <c:when  test="${twoFactorRequestContainer.twoFactorUserLoggedIn.admin24}">
          <form action="../../twoFactorAdminUi/app/UiMainAdmin.admin24Index" method="get" style="display: inline">
            <input value="${textContainer.textEscapeDouble['buttonAdmin24Console']}" class="tfBlueButton"
             type="submit" />
          </form>
          &nbsp;

        </c:when>
      </c:choose>
      <c:choose>
        <c:when  test="${twoFactorRequestContainer.twoFactorAdminContainer.canLiteAdmin}">
          <form action="../../twoFactorUi/app/UiMain.liteAdmin" method="post" style="display: inline">
            <input value="${textContainer.textEscapeDouble['buttonAdminLiteConsole']}" class="tfBlueButton"
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
    </div>
    
   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
