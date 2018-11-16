<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['indexTitle'] }</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

<!-- index.jsp -->

</head>
<body alink="#cc6600" bgcolor="#ffffff" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader'] }</h1>
  <br />
  <h2 style="margin-top: 0em">${textContainer.text['indexNotOptedInSubheader']}</h2>
  <br /><br />
  <div class="paragraphs">
  
    ${textContainer.text['indexNotOptedInSubtext1']}
    
  </div>
    
  <%@ include file="../assetsJsp/commonError.jsp"%>
    <br />
    <br />

    <form action="../../twoFactorUi/app/UiMain.optinWizard" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonOptIn']}" class="tfBlueButton"
       type="submit" />
    </form>

    &nbsp; &nbsp;

    <form action="../../twoFactorUi/app/UiMain.index" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfBlueButton"
       type="submit" />
    </form>
    
    &nbsp; &nbsp;
    
    <form action="../../twoFactorPublicUi/app/UiMainPublic.index" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonHavingTrouble']}" class="tfBlueButton"
       type="submit" />
    </form>
    
   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
