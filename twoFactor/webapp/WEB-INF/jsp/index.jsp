<%@ include file="../assetsJsp/commonTop.jsp"%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>

<title>${textContainer.text['indexTitle'] }</title>

<%@ include file="../assetsJsp/commonHead.jsp"%>

<!-- index.jsp -->

</head>
<body alink="#cc6600" bgcolor="#f0f0ea" link="#011d5c" text="#000000" vlink="#011d5c">

<%@ include file="../assetsJsp/commonBanner.jsp"%>

<div id="theForm">
  <h1>${textContainer.text['pageHeader'] }</h1>
  <br />
  <b>${textContainer.text['indexNotOptedInSubheader']}</b>
  <br /><br />
  ${textContainer.text['indexNotOptedInSubtext1']}
  <br />
  <br />
  <%@ include file="../assetsJsp/commonError.jsp"%>
  <b>
  </b>
    <br /><br />

    <form action="../../twoFactorUi/app/UiMain.index" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonManageSettings']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
    </form>
    
    &nbsp; &nbsp;
    
    <form action="../../twoFactorPublicUi/app/UiMainPublic.index" method="get" style="display: inline">
      <input value="${textContainer.textEscapeDouble['buttonHavingTrouble']}" class="tfBlueButton"
      onmouseover="this.style.backgroundColor='#011D5C';" onmouseout="this.style.backgroundColor='#7794C9';" type="submit" />
    </form>
    
   <br /><br />
  <%@ include file="../assetsJsp/commonAbout.jsp"%>
</div>

</body></html>
