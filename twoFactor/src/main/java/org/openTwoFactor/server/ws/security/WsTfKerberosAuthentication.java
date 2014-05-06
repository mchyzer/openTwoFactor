
/*
 * @author mchyzer
 * $Id: WsTfKerberosAuthentication.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.ws.security;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.cache.TwoFactorCache;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.logging.LogFactory;


/**
 * <pre>
 * basic kerberos authentication for grouper, settings are specified in grouper-ws.properties
 * note: this can be used for rest and soap, though it is not a bastion of security:
 *  1. for soap, ws-security would be better since a ticket is passed instead of user/pass
 *  2. for rest, Im not sure there is another option
 *  3. the user/pass is transmitted in basic auth, so make sure SSL is on
 *  4. passing the user/pass is not how kerberos should work since kerberos passes tickets and not passes
 *  5. the user is authenticated to the kdc, but an ssl service is not invoked, which would be the next
 *  level of verification since it might be possible for the kdc to be spoofed to the grouper-ws
 * 
 * </pre>
 */
public class WsTfKerberosAuthentication implements WsTfCustomAuthentication {

  /**
   * 
   */
  private static final String USER_PASS_SEPARATOR = "|-|-|-|-|-|-|";

  /**
   * 
   * @param args
   * @throws Exception 
   */
  public static void main(String[] args) throws Exception {
//    GrouperActivemqUtils.waitForInput();
//    for (int i=0; i<2; i++) {
//      for (int j=0;j<100;j++) {
//        if (!authenticateKerberos("", 
//            Morph.decryptIfFile(""))) {
//          throw new RuntimeException("Problem!");
//        }
//        System.gc();
//        System.out.println(j + ":" + i + ", " 
//            + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/(double)(1024*1024)) + " megs used");
//        Thread.sleep(100);
//      }
//      GrouperActivemqUtils.waitForInput();
//    }
    
    for (int i=0;i<10;i++) {
      System.out.println(authenticateKerberosHelper(
          "penngroups_activemq_test/medley.isc-seo.upenn.edu", "xxxxxx"));
    }
    
  }

  /** logger */
  private static final Log LOG = LogFactory.getLog(WsTfKerberosAuthentication.class);

  /**
   * cache the logins in a hash cache
   */
  private static ExpirableCache<String, Boolean> loginSuccessCache = new ExpirableCache<String, Boolean>(
    TwoFactorServerConfig.retrieveConfig().propertyValueInt("kerberos.login.cache.success.minutes", 5));

  /**
   * cache the logins in a hash cache
   */
  private static ExpirableCache<String, Boolean> loginFailedCache = new ExpirableCache<String, Boolean>(
      TwoFactorServerConfig.retrieveConfig().propertyValueInt("kerberos.login.cache.failed.minutes", 1));


  /**
   * see if a user and pass are correct with berberos
   * @param principal
   * @param password
   * @return true for ok, false for not
   */
  public static boolean authenticateKerberos(String principal, String password) {
    //hash the authHeader
    String authHeaderHash = TwoFactorServerUtils.encryptSha(principal + USER_PASS_SEPARATOR + password);

    {
      Boolean cachedFailedLogin = loginFailedCache.get(authHeaderHash);
      if (cachedFailedLogin != null) {
        LOG.debug("Retrieved cached failed login");
        return false;
      }
    }
    
    {
      Boolean cachedSuccessLogin = loginSuccessCache.get(authHeaderHash);
      if (cachedSuccessLogin != null) {
        LOG.debug("Retrieved cached success login");
        return true;
      }
    }
    
    LOG.debug("Login not in cache");

    //do the kerberos
    boolean result = authenticateKerberosHelper(principal, password);
    
    //add to cache
    if (result) {
      loginSuccessCache.put(authHeaderHash, true);
    } else {
      loginFailedCache.put(authHeaderHash, true);
    }
    
    return result;
  }
  

  
  /**
   * see if a user and pass are correct with kerberos.  This is code downloaded from the internet
   * @param principal
   * @param password
   * @return true for ok, false for not
   */
  private static boolean authenticateKerberosHelper(String principal, String password) {

    // Obtain a LoginContext, needed for authentication. Tell it 
    // to use the LoginModule implementation specified by the 
    // entry named "JaasSample" in the JAAS login configuration 
    // file and to also use the specified CallbackHandler.
    
    {
      File jaasConf = GrouperClientUtils.fileFromResourceName("jaas.conf");
  
      if (jaasConf == null) {
        throw new RuntimeException("Cant find jaas.conf!");
      }
      System.setProperty("java.security.auth.login.config", jaasConf.getAbsolutePath());
    }
    
    {
      File krb5confFile = GrouperClientUtils.fileFromResourceName("krb5.conf");
  
      if (krb5confFile == null) {
        throw new RuntimeException("Cant find krb5.conf!");
      }
  
      System.setProperty("java.security.krb5.conf", krb5confFile.getAbsolutePath());
    }
    
    //System.setProperty("sun.security.krb5.debug", "true");
    
    LoginContext lc = null;
    try {
      lc = new LoginContext("JaasSample", new TwoFactorWsKerberosHandler(principal, password));
      

    } catch (LoginException le) {
      LOG.error("Cannot create LoginContext. ", le);
      return false;
    } catch (SecurityException se) {
      LOG.error("Cannot create LoginContext. " , se);
      return false;
    }

    try {

      // attempt authentication
      lc.login();

      try {
        lc.logout();
      } catch (Exception e) {
        LOG.warn(e);
      }
      return true;
    } catch (LoginException le) {
      
      LOG.warn(le);
    }

    return false;
    
  }

  /**
   * cache the logins in a hash cache
   */
  private static TwoFactorCache<String, String> loginCache = new TwoFactorCache<String, String>(
      WsTfKerberosAuthentication.class.getName() + ".userCache", 10000, false, 60*1, 60*1, false);

  /**
   * this should be something like: Basic QWxhZGRabcdefGVuIHNlc2FtZQ==
   * ^\\s*Basic\\s+([^\\s]+)$
   * "^\\s*       Start of string and optional whitespace
   * Basic\\s+    Must be basic auth, and have some whitespace after this
   * ([^\\s]+)$   Capture the next non-whitespace string, then end of input
   */
  private static Pattern regexPattern = Pattern.compile("^\\s*Basic\\s+([^\\s]+)$");



  /**
   * @param httpServletRequest 
   * @return the loginid
   * @throws RuntimeException 
   * 
   */
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws RuntimeException {
    
    String authHeader = httpServletRequest.getHeader("Authorization");
  
    //if not header, we cant go to kerberos
    if (StringUtils.isBlank(authHeader)) {
      LOG.error("No authorization header in HTTP");
      return null;
    }
    
    //hash the authHeader
    String authHeaderHash = TwoFactorServerUtils.encryptSha(authHeader);
    
    String cachedLogin = loginCache.get(authHeaderHash);
    if (!StringUtils.isBlank(cachedLogin)) {
      LOG.debug("Retrieved cached login");
      return cachedLogin;
    }
    LOG.debug("Login not in cache");
    
    Matcher matcher = regexPattern.matcher(authHeader);
    String authHeaderBase64Part = null;
    if (matcher.matches()) {
      authHeaderBase64Part = matcher.group(1);
    }
    
    //either not basic or something else wrong
    if (StringUtils.isBlank(authHeaderBase64Part)) {
      //dont log password
      LOG.error("Cant find base64 part in auth header");
      return null;
    }
    
    //unencrypt this
    byte[] base64Bytes = authHeaderBase64Part.getBytes();
    byte[] unencodedBytes = Base64.decodeBase64(base64Bytes);
    
    String unencodedString = null;
    
    try {
      unencodedString = new String(unencodedBytes, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException(uee);
    }
    
    //split based on user/pass
    String user = TwoFactorServerUtils.prefixOrSuffix(unencodedString, ":", true);
    String pass = TwoFactorServerUtils.prefixOrSuffix(unencodedString, ":", false);
    
    if (authenticateKerberos(user, pass)) {
      
      loginCache.put(authHeaderHash, user);
      
      return user;
    }
    
    LOG.error("Error authenticating user: " + user);
    return null;
  }
  
}
