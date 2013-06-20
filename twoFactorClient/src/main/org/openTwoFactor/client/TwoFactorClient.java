/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: TwoFactorClient.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openTwoFactor.client.api.TfValidatePassword;
import org.openTwoFactor.client.contentType.TfClientRestContentType;
import org.openTwoFactor.client.corebeans.TfCheckPasswordResponse;
import org.openTwoFactor.client.util.TwoFactorClientCommonUtils;
import org.openTwoFactor.client.util.TwoFactorClientConfig;
import org.openTwoFactor.client.util.TwoFactorClientLog;
import org.openTwoFactor.client.util.TwoFactorClientUtils;
import org.openTwoFactor.client.ws.TwoFactorClientWs;
import org.openTwoFactor.clientExt.edu.internet2.middleware.morphString.Crypto;
import org.openTwoFactor.clientExt.org.apache.commons.logging.Log;




/**
 * <pre>
 * main class for grouper client.  note, stdout is for output, stderr is for error messages (or logs)
 * 
 * --operation=testSuite --verbose=high
 * 
 * </pre>
 */
public class TwoFactorClient {

  /** timing gate */
  private static long startTime = System.currentTimeMillis();
  
  /**
   * 
   */
  private static Log log = TwoFactorClientUtils.retrieveLog(TwoFactorClient.class);

  /** should java exit on error? */
  public static boolean exitOnError = true;
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    
    String operation = null;
    try {
      if (TwoFactorClientUtils.length(args) == 0) {
        usage();
        return;
      }
      
      //map of all command line args
      Map<String, String> argMap = TwoFactorClientUtils.argMap(args);
      
      Map<String, String> argMapNotUsed = new LinkedHashMap<String, String>(argMap);

      boolean debugMode = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "debug", false, false);
      
      TwoFactorClientLog.assignDebugToConsole(debugMode);
      
      //init if not already
      TwoFactorClientConfig.retrieveConfig().properties();
      
      //see where log file came from
      StringBuilder callingLog = new StringBuilder();
      TwoFactorClientUtils.propertiesFromResourceName("twoFactor.client.properties", 
          false, true, TwoFactorClientCommonUtils.class, callingLog);
      
      //see if the message about where it came from is
      //log.debug(callingLog.toString());
      
      operation = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "operation", true);
      
      //where results should go if file
      String saveResultsToFile = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "saveResultsToFile", false);
      boolean shouldSaveResultsToFile = !TwoFactorClientUtils.isBlank(saveResultsToFile);
      
      if (shouldSaveResultsToFile) {
        log.debug("Will save results to file: " + TwoFactorClientUtils.fileCanonicalPath(new File(saveResultsToFile)));
      }
      
      String result = null;
      
      if (customOperations().containsKey(operation)) {
        
        Class<ClientOperation> operationClass = customOperations().get(operation);
        ClientOperation clientOperation = TwoFactorClientUtils.newInstance(operationClass);
        
        OperationParams operationParams = new OperationParams();
        operationParams.setArgMap(argMap);
        operationParams.setArgMapNotUsed(argMapNotUsed);
        operationParams.setShouldSaveResultsToFile(shouldSaveResultsToFile);
        
        result = clientOperation.operate(operationParams);
        
      } else if (TwoFactorClientUtils.equals(operation, "encryptPassword")) {
        
        result = encryptText(argMap, argMapNotUsed, shouldSaveResultsToFile);
        
      } else if (TwoFactorClientUtils.equals(operation, "validatePasswordWs")) {
        result = validatePasswordWs(argMap, argMapNotUsed);

      } else {
        System.err.println("Error: invalid operation: '" + operation + "', for usage help, run: java -jar twoFactorClient.jar" );
        if (exitOnError) {
          System.exit(1);
        }
        throw new RuntimeException("Invalid usage");
      }
      
      //this already has a newline on it
      if (shouldSaveResultsToFile) {
        TwoFactorClientUtils.saveStringIntoFile(new File(saveResultsToFile), result);
      } else {
        System.out.print(result);
      }

      failOnArgsNotUsed(argMapNotUsed);
      
    } catch (Exception e) {
      System.err.println("Error with two factor client, check the logs: " + e.getMessage());
      log.fatal(e.getMessage(), e);
      if (exitOnError) {
        System.exit(1);
      }
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e.getMessage(), e);
    } finally {
      try {
        log.debug("Elapsed time: " + (System.currentTimeMillis() - startTime) + "ms");
      } catch (Exception e) {}
      TwoFactorClientLog.assignDebugToConsole(false);
    }
    
  }

  /**
   * @param argMapNotUsed
   */
  public static void failOnArgsNotUsed(Map<String, String> argMapNotUsed) {
    if (argMapNotUsed.size() > 0) {
      boolean failOnExtraParams = TwoFactorClientConfig.retrieveConfig().propertyValueBooleanRequired(
          "twoFactorClient.failOnExtraCommandLineArgs");
      String error = "Invalid command line arguments: " + argMapNotUsed.keySet();
      if (failOnExtraParams) {
        throw new RuntimeException(error);
      }
      log.error(error);
    }
  }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @param shouldSaveResultsToFile
   * @return result
   */
  private static String encryptText(Map<String, String> argMap,
      Map<String, String> argMapNotUsed,
      boolean shouldSaveResultsToFile) {
    boolean dontMask = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "dontMask", false, false);
    
    String encryptKey = TwoFactorClientUtils.encryptKey();
    
    //lets get the password from stdin
    String password = TwoFactorClientUtils.retrievePasswordFromStdin(dontMask, 
        "Type the string to encrypt (note: pasting might echo it back): ");
    
    String encrypted = new Crypto(encryptKey).encrypt(password);
    
    if (shouldSaveResultsToFile) {
      return encrypted;
    }
    return "Encrypted password: " + encrypted;
  }


    /**
     * @param argMap
     * @param argMapNotUsed
     * @return result
     */
    private static String validatePasswordWs(Map<String, String> argMap,
        Map<String, String> argMapNotUsed) {
      
      TfValidatePassword tfValidatePassword = new TfValidatePassword();        
      
      boolean indent = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, false);
      
      tfValidatePassword.assignIndent(indent);

      //register that we will use this
      TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", false);
      
      {
        String browserUserAgent = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "browserUserAgent", false);
        tfValidatePassword.assignBrowserUserAgent(browserUserAgent);
      }

      {
        String format = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "format", false);
        tfValidatePassword.assignFormat(format);
      }
      
      {
        Boolean debug = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "debug");
        tfValidatePassword.assignDebug(debug);
      }

      {
        Boolean requireReauth = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "requireReauth");
        tfValidatePassword.assignRequireReauth(requireReauth);
      }

      {
        Boolean requireTwoFactor = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "requireTwoFactor");
        tfValidatePassword.assignRequireTwoFactor(requireTwoFactor);
      }

      {
        String serviceId = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "serviceId", false);
        tfValidatePassword.assignServiceId(serviceId);
      }

      {
        String serviceName = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "serviceName", false);
        tfValidatePassword.assignServiceName(serviceName);
      }

      {
        String spRequiredFactors = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "spRequiredFactors", false);
        tfValidatePassword.assignSpRequiredFactors(spRequiredFactors);
      }

      {
        Boolean trustedBrowser = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "trustedBrowser");
        tfValidatePassword.assignTrustedBrowser(trustedBrowser);
      }

      {
        String twoFactorPass = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "twoFactorPass", false);
        tfValidatePassword.assignTwoFactorPass(twoFactorPass);
      }

      {
        String userBrowserUuid = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "userBrowserUuid", false);
        tfValidatePassword.assignUserBrowserUuid(userBrowserUuid);
      }

      {
        String userIpAddress = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "userIpAddress", false);
        tfValidatePassword.assignUserIpAddress(userIpAddress);
      }

      {
        String username = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "username", true);
        tfValidatePassword.assignUsername(username);
      }

      failOnArgsNotUsed(argMapNotUsed);
      
      TfCheckPasswordResponse tfCheckPasswordResponse = tfValidatePassword.execute();
      
      StringBuilder result = new StringBuilder();
      
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
  
      substituteMap.put("tfCheckPasswordResponse", tfCheckPasswordResponse);
  
      String outputTemplate = null;
  
      if (argMap.containsKey("outputTemplate")) {
        outputTemplate = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "outputTemplate", true);
        outputTemplate = TwoFactorClientUtils.substituteCommonVars(outputTemplate);
      } else {
        outputTemplate = TwoFactorClientConfig.retrieveConfig().propertyValueStringRequired("webService.validatePassword.output");
      }
      log.debug("Output template: " + TwoFactorClientUtils.trim(outputTemplate) + ", available variables: tfCheckPasswordResponse");

      
      String output = TwoFactorClientUtils.substituteExpressionLanguage(outputTemplate, substituteMap);
      result.append(output);
      
      return result.toString();
    }

  /**
   * @param argMap
   * @param argMapNotUsed
   * @return result
   */
  @SuppressWarnings("unused")
  private static String sendFile(Map<String, String> argMap,
      Map<String, String> argMapNotUsed) {
    
    String clientVersion = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "clientVersion", false);
    
    String fileContents = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "fileContents", false);
    
    String theFileName = "[contents on command line]";
    if (TwoFactorClientUtils.isBlank(fileContents)) {
      String fileName = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "fileName", true);

      fileContents = TwoFactorClientUtils.readFileIntoString(new File(fileName));
      
      theFileName = TwoFactorClientUtils.fileCanonicalPath(new File(fileName));
    }
    
    if (fileContents.startsWith("POST") || fileContents.startsWith("GET")
        || fileContents.startsWith("PUT") || fileContents.startsWith("DELETE")
        || fileContents.startsWith("Connection:")) {
      throw new RuntimeException("The file is detected as containing HTTP headers, it should only contain the payload (e.g. the XML): " + theFileName);
    }
    
    String urlSuffix = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "urlSuffix", true);

    //this is part of the log file if logging output
    String labelForLog = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "labelForLog", false);
    
    labelForLog = TwoFactorClientUtils.defaultIfBlank(labelForLog, "sendFile");
    
    boolean indentOutput = TwoFactorClientUtils.argMapBoolean(argMap, argMapNotUsed, "indent", false, true);
    
    String contentType = TwoFactorClientUtils.argMapString(argMap, argMapNotUsed, "contentType", false);

    failOnArgsNotUsed(argMapNotUsed);
    
    TwoFactorClientWs<TfCheckPasswordResponse> twoFactorClientWs 
      = new TwoFactorClientWs<TfCheckPasswordResponse>();
    
    if (TwoFactorClientUtils.isNotBlank(contentType)) {
      
    }
    
    try {
      //assume the url suffix is already escaped...
      String results = (String)(Object)twoFactorClientWs.executeService(urlSuffix, 
          fileContents, labelForLog, clientVersion, TfClientRestContentType.json, null, null, null);

      if (indentOutput) {
        results = TwoFactorClientUtils.indent(results, false);
      }
      
      return results;
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * print usage and exit
   */
  public static void usage() {
    //read in the usage file
    String usage = TwoFactorClientUtils.readResourceIntoString("twoFactor.client.usage.txt", TwoFactorClientCommonUtils.class);
    System.err.println(usage);
  }


  /**
   * get custom operation classes configured in the twoFactor.client.properties
   * @return the map of operations
   */
  @SuppressWarnings({ "unchecked", "cast" })
  private static Map<String, Class<ClientOperation>> customOperations() {
    
    if (customOperations == null) {
      
      customOperations = new LinkedHashMap<String, Class<ClientOperation>>();
      
      int i=0;
      String operationName = null;
      while (true) {
        operationName = null;
        operationName = TwoFactorClientConfig.retrieveConfig().propertyValueString("customOperation.name." + i);
        if (TwoFactorClientUtils.isBlank(operationName)) {
          break;
        }
        if (customOperations.containsKey(operationName)) {
          throw new RuntimeException("There is a custom operation defined twice in twoFactor.client.properties: '" + operationName + "'");
        }
        try {
  
          String operationClassName = TwoFactorClientConfig.retrieveConfig().propertyValueStringRequired("customOperation.class." + i);
          Class<ClientOperation> operationClass = (Class<ClientOperation>)TwoFactorClientUtils.forName(operationClassName);
          customOperations.put(operationName, operationClass);
  
        } catch (RuntimeException re) {
          throw new RuntimeException("Problem with custom operation: " + operationName, re);
        }
        i++;
      }
    }
    
    return customOperations;
    
  }


  /** custom operations from config file */
  private static Map<String, Class<ClientOperation>> customOperations = null;

}
