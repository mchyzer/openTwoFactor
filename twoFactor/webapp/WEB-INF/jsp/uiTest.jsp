<%@ taglib uri="/WEB-INF/tld/twoFactor.tld" prefix="twoFactor"%>
<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>Two-step verification UI test</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

</head>
<body alink="#cc6600" bgcolor="#ffffff" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>Two-step verification UI test</h1>
  <br />
  Test out the various two step components
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <b>
  </b>
    <br /><br />

    <form action="../../twoFactorUi/app/UiMain.uiTestIndex" method="post">
    
      <twoFactor:combobox filterOperation="../../twoFactorUi/app/UiMain.uiTestPersonPickerCombo" 
         idBase="personPicker" value="mchyzer"  />
    
    
      <input value="Submit" class="tfBlueButton"
       type="submit" />
    </form>
    
    &nbsp; &nbsp;
    
   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
