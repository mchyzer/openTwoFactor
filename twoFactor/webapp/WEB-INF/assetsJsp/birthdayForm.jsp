<%@ include file="../assetsJsp/commonTaglib.jsp"%>
              <select name="birthMonth" id="birthMonthId">
                <option value="">${textContainer.text['optinStep3enterBdayMonth']}</option>
                <option value="1"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 1 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthJan']}</option>
                <option value="2"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 2 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthFeb']}</option>
                <option value="3"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 3 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthMar']}</option>
                <option value="4"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 4 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthApr']}</option>
                <option value="5"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 5 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthMay']}</option>
                <option value="6"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 6 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthJun']}</option>
                <option value="7"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 7 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthJul']}</option>
                <option value="8"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 8 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthAug']}</option>
                <option value="9"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 9 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthSep']}</option>
                <option value="10"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 10 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthOct']}</option>
                <option value="11"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 11 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthNov']}</option>
                <option value="12"
                  ${twoFactorRequestContainer.twoFactorOptinContainer.birthMonthSubmitted == 12 ? 'selected="selected"' : ''}
                >${textContainer.text['optinStep3enterBdayMonthDec']}</option>
              </select>
              <select name="birthDay">
                <option value="">${textContainer.text['optinStep3enterBdayDay']}</option>
                
                <c:forEach begin="1" end="31" var="currentDay">

                  <option value="${currentDay}"
                    ${twoFactorRequestContainer.twoFactorOptinContainer.birthDaySubmitted == currentDay ? 
                      'selected="selected"' :  '' }
                  >${currentDay}</option>
                
                </c:forEach>
                
              </select>
              <select name="birthYear">
                
                <c:forEach begin="0" end="125" var="currentYearIndex">
                  <c:set var="currentYear" value="${2025 - currentYearIndex }" />
                  <option value="${currentYear}"
                    ${twoFactorRequestContainer.twoFactorOptinContainer.birthYearSubmitted == currentYear ? 'selected="selected"' 
                      : '' }
                  >${currentYear}</option>
                  <c:if test="${ currentYear == 2000 }" >
                    <option value="" ${ twoFactorRequestContainer.twoFactorOptinContainer.birthYearSubmitted <= 0 ? 
                      'selected="selected"' : '' } >${textContainer.text['optinStep3enterBdayYear']}</option>
                  </c:if>
                
                
                </c:forEach>

              </select>

