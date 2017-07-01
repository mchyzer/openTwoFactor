/**
 * @author mchyzer
 * $Id: TwoFactorUiServlet.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.j2ee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.daemon.DaemonController;
import org.openTwoFactor.server.duo.DuoLog;
import org.openTwoFactor.server.exceptions.TfAjaxException;
import org.openTwoFactor.server.ui.beans.TextContainer;
import org.openTwoFactor.server.ui.beans.TwoFactorRequestContainer;
import org.openTwoFactor.server.ui.serviceLogic.UiMain;
import org.openTwoFactor.server.ui.serviceLogic.UiMainAdmin;
import org.openTwoFactor.server.ui.serviceLogic.UiMainPublic;
import org.openTwoFactor.server.ui.serviceLogic.UiMainUnprotected;
import org.openTwoFactor.server.util.TwoFactorServerUtils;




/**
 * Servlet for UI
 */
@SuppressWarnings("serial")
public class TwoFactorUiServlet extends HttpServlet {

  /**
   * @see javax.servlet.GenericServlet#init()
   */
  @Override
  public void init() throws ServletException {
    super.init();
    DaemonController.scheduleJobsOnce();

  }

  /** uris that it is ok to get (e.g. auto complete and other ajax components */
  private static Set<String> operationsOkGet = TwoFactorServerUtils.toSet(
      UiMainPublic.class.getSimpleName() + ".index",
      UiMainPublic.class.getSimpleName() + ".phoneIndex",
      UiMainUnprotected.class.getSimpleName() + ".index",
      UiMainUnprotected.class.getSimpleName() + ".logout",
      UiMain.class.getSimpleName() + ".qrCode",
      UiMain.class.getSimpleName() + ".qrCodeSecret",
      UiMain.class.getSimpleName() + ".optout",
      UiMain.class.getSimpleName() + ".changeDevice",
      UiMain.class.getSimpleName() + ".addPhoneOrDevice",
      UiMain.class.getSimpleName() + ".addPhone",
      UiMain.class.getSimpleName() + ".optin",
      UiMain.class.getSimpleName() + ".optinWizard",
      UiMain.class.getSimpleName() + ".userAudits",
      UiMain.class.getSimpleName() + ".untrustBrowsers",
      UiMain.class.getSimpleName() + ".showOneTimeCodes",
      UiMain.class.getSimpleName() + ".index",
      UiMain.class.getSimpleName() + ".personPicker",
      UiMain.class.getSimpleName() + ".profile",
      UiMain.class.getSimpleName() + ".profileView",
      UiMain.class.getSimpleName() + ".helpColleague",
      UiMain.class.getSimpleName() + ".uiTestIndex",
      UiMain.class.getSimpleName() + ".uiTestIndex",
      UiMain.class.getSimpleName() + ".uiTestPersonPickerCombo",
      UiMain.class.getSimpleName() + ".reports",
      UiMain.class.getSimpleName() + ".duoPush",
      UiMain.class.getSimpleName() + ".optinWizardTotpAppSetup",
      UiMainAdmin.class.getSimpleName() + ".adminIndex",
      UiMainAdmin.class.getSimpleName() + ".personPicker",
      UiMainAdmin.class.getSimpleName() + ".adminEmailAllPage",
      UiMainAdmin.class.getSimpleName() + ".adminImportSerialsPage",
      UiMainAdmin.class.getSimpleName() + ".reportsIndex",
      UiMainAdmin.class.getSimpleName() + ".reportsAdd",
      UiMainAdmin.class.getSimpleName() + ".reportsEdit",
      UiMainAdmin.class.getSimpleName() + ".reportsPrivilegesEdit",
      UiMainAdmin.class.getSimpleName() + ".reportsRollupsEdit"
  );

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorUiServlet.class);

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @SuppressWarnings({ "unchecked" })
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
  
    // if the UI should run in this env
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.runUi", true)) {
      throw new RuntimeException("UI doesnt run in this env per: twoFactorServer.runUi");
    }

    if (StringUtils.equals("true",request.getParameter("status"))) {
      return;
    }
    
    TwoFactorFilterJ2ee.assignHttpServlet(this);
    
    //set the text container
    TextContainer.retrieveFromRequest();
    
    List<String> urlStrings = extractUrlStrings(request);
    
    //lets do some defaults
    if (TwoFactorServerUtils.length(urlStrings) == 0) {
      //relative links break, send redirect
      response.sendRedirect(request.getContextPath() + "/twoFactorUnprotectedUi/app/UiMainUnprotected.index");
      return;
    }

    //lets do some defaults
    if (TwoFactorServerUtils.length(urlStrings) == 1 && StringUtils.equals("app", urlStrings.get(0))) {
      //relative links break, send redirect
      response.sendRedirect(request.getContextPath() + "/twoFactorUnprotectedUi/app/UiMainUnprotected.index");
      return;
    }

    //see what operation we are doing
    if (TwoFactorServerUtils.length(urlStrings) == 2 
        && StringUtils.equals("app", urlStrings.get(0))) {
  
      String classAndMethodName = urlStrings.get(1);
  
      if (classAndMethodName != null && classAndMethodName.endsWith(".gif")) {
        classAndMethodName = classAndMethodName.substring(0, classAndMethodName.length()-4);
      }
      
      //lets do some simple validation, text dot text
      if (!classAndMethodName.matches("^[a-zA-Z0-9]+\\.[a-zA-Z0-9]+$")) {
        throw new RuntimeException("Invalid class and method name: '" + classAndMethodName + "'");
      }
      
      String className = TwoFactorServerUtils.prefixOrSuffix(classAndMethodName, ".", true);
      
      //now lets call some simple reflection, must be public static void and take a request and response
      className = UiMain.class.getPackage().getName() + "." + className;
      
      Class logicClass = TwoFactorServerUtils.forName(className);

      //lets check security
      //    /twoFactorMchyzer/twoFactorUi/app/UiMain.userAudits
      {
        String requestUri = request.getRequestURI();
        String[] requestResources = StringUtils.split(requestUri, '/');
        if (requestResources.length < 2) {
          throw new RuntimeException("Why is requestUri so short?  What is the servlet?");
        }
        String servlet = requestResources[1];
        boolean ok = false;
        //lets check
        if (StringUtils.equals(servlet, "twoFactorUnprotectedUi")) {
          ok = UiMainUnprotected.class.equals(logicClass);
        }
        if (StringUtils.equals(servlet, "twoFactorPublicUi")) {
          ok = UiMainPublic.class.equals(logicClass) || UiMainUnprotected.class.equals(logicClass);
        }
        if (StringUtils.equals(servlet, "twoFactorUi")) {
          ok = UiMain.class.equals(logicClass) || UiMainUnprotected.class.equals(logicClass);
        }
        if (StringUtils.equals(servlet, "twoFactorAdminUi")) {
          ok = UiMainAdmin.class.equals(logicClass) || UiMainUnprotected.class.equals(logicClass);
        }
        if (!ok) {
          throw new RuntimeException("The servlet does not match the logic class: " + servlet + ", " + logicClass.getSimpleName());
        }
      }

      //I think we are all post all the time, right?  ok, most of the time
      if (!StringUtils.equalsIgnoreCase("post", request.getMethod() )) {
        if (!operationsOkGet.contains(classAndMethodName)) {
          String errorMessage = "Cant process method: " + request.getMethod() + " for operation: " + classAndMethodName + ", do not bookmark, hit the back button or refresh";
          LOG.info(errorMessage);
          TwoFactorRequestContainer twoFactorRequestContainer = TwoFactorRequestContainer.retrieveFromRequest();
          twoFactorRequestContainer.setError(errorMessage);
          
          new UiMainUnprotected().index(request, response);
          //go to the admin page in that realm
          //if (realmAdminUi) {
          //  new UiMainAdmin().adminIndex(request, response);
          //} else if (realmPublicUi) {
          //  new UiMainPublic().index(request, response);
          //} else if (realmUi) {
          //  new UiMain().index(request, response);
          //} else {
          //  new UiMainUnprotected().index(request, response);
          //}
          return;
        }
      }

      String methodName = null;
      
      try {
        
        String loggedInUser = TwoFactorFilterJ2ee.retrieveUserIdFromRequest(true, false);

        DuoLog.assignUsername(loggedInUser);
        
        methodName = TwoFactorServerUtils.prefixOrSuffix(classAndMethodName, ".", false);

        callMethod(request, response, logicClass, methodName);
  
      } catch (TfAjaxException tae) {
        throw tae;
      } catch (RuntimeException re) {
        String error = "Problem calling reflection from URL: " + className + "." + methodName + "\n\n" + ExceptionUtils.getFullStackTrace(re);
        LOG.error(error);
        TwoFactorServerUtils.printToScreen("Problem with request", null, true, true);
      } finally {
        DuoLog.assignUsername(null);
      }
    } else {
      String error = "Error: cant find logic to execute: " + request.getRequestURI();
      
      LOG.error(error);
      TwoFactorServerUtils.printToScreen(error, null, true, true);
    }
    
  }

  /**
   * 
   * @param request
   * @param response
   * @param logicClass
   * @param methodName
   */
  private static void callMethod(HttpServletRequest request, HttpServletResponse response,
      Class<?> logicClass, String methodName) {
    
    Object instance = TwoFactorServerUtils.newInstance(logicClass);
      
    TwoFactorServerUtils.callMethod(instance.getClass(), instance, methodName, 
        new Class<?>[]{HttpServletRequest.class, HttpServletResponse.class}, 
        new Object[]{request, response}, true, false);
  }
  
  /**
   * return the xml version tag and doctype tag
   * @return the code
   */
  public static String xmlVersionAndDoctype() {
    
    return "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
      + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 "
            + "Transitional//EN\" " +
                "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n";

  }

  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    this.doGet(request, response);
    
  }

  /**
   * TODO change to GrouperRestServlet in next release
   * take a request and get the list of url strings for the rest web service
   * @see #extractUrlStrings(String)
   * @param request is the request to get the url strings out of
   * @return the list of url strings
   */
  private static List<String> extractUrlStrings(HttpServletRequest request) {
    String requestResourceFull = request.getRequestURI();
    return extractUrlStrings(requestResourceFull);
  }

  /**
   * TODO change to GrouperRestServlet in next release
   * <pre>
   * take a request uri and break up the url strings not including the app name or servlet
   * this does not include the url params (if applicable)
   * if the input is: grouper-ws/servicesRest/xhtml/v1_3_000/groups/members
   * then the result is a list of size 2: {"group", "members"}
   * 
   * </pre>
   * @param requestResourceFull
   * @return the url strings
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = StringUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();
    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(TwoFactorServerUtils.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
