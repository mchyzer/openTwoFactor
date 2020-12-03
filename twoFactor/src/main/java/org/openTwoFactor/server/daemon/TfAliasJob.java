/**
 * @author mchyzer
 * $Id: TfAuditClearingJob.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.daemon;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.codehaus.jackson.JsonNode;
import org.ldaptive.BindOperation;
import org.ldaptive.BindRequest;
import org.ldaptive.Connection;
import org.ldaptive.ConnectionConfig;
import org.ldaptive.Credential;
import org.ldaptive.DefaultConnectionFactory;
import org.ldaptive.LdapEntry;
import org.ldaptive.SearchOperation;
import org.ldaptive.SearchRequest;
import org.ldaptive.SearchResult;
import org.openTwoFactor.server.beans.TwoFactorDaemonLog;
import org.openTwoFactor.server.beans.TwoFactorDaemonLogStatus;
import org.openTwoFactor.server.beans.TwoFactorDaemonName;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.duo.DuoCommands;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;



/**
 * daemon queries for aliases and assigns them
 */
public class TfAliasJob implements Job {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    TwoFactorDaoFactory twoFactorDaoFactory = TwoFactorDaoFactory.getFactory();
    new TfAliasJob().aliasLogic(twoFactorDaoFactory);

//    for (String alias : retrieveAliases()) {
//      System.out.println(alias);
//    }
  }
  
  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfAliasJob.class);

  /**
   * @see org.quartz.Job#execute(org.quartz.JobExecutionContext)
   */
  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    aliasLogic(TwoFactorDaoFactory.getFactory());
  }
  
  /**
   * get the aliases
   * @return the aliases
   */
  private static List<String> retrieveAliases() {
    Connection connection = null;
    try {
      List<String> aliases = new ArrayList<String>();
      
      String ldapUrl = TwoFactorServerConfig.retrieveConfig().propertyValueString("aliasLdapUrl");
      
      ConnectionConfig connectionConfig = new ConnectionConfig(ldapUrl);
      
      connectionConfig.setUseSSL(true);

      DefaultConnectionFactory connectionFactory = new DefaultConnectionFactory(connectionConfig);
      
      connection = connectionFactory.getConnection();

      connection.open();
      
      BindOperation bind = new BindOperation(connection);

      String aliasLdapUser = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("aliasLdapUser");
      String aliasLdapPass = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("aliasLdapPass");
      
      bind.execute(new BindRequest(aliasLdapUser, new Credential(aliasLdapPass)));

      SearchOperation searchOperation = new SearchOperation(connection);

      String dn = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("aliasLdapAliasDn");

      String filter = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("aliasLdapAliasFilter");

      String attribute = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("aliasLdapAliasAttribute");
      
      SearchResult searchResult = searchOperation.execute(
        new SearchRequest(
            dn, filter, attribute)).getResult();
      
      for (LdapEntry entry : searchResult.getEntries()) {
        // if you're expecting multiple entries
        // do something useful with the entry
        aliases.add(entry.getAttribute().getStringValue());
      }      
          
      return aliases;
    } catch (Exception e) {
      throw new RuntimeException("Error in ldap", e);
    } finally {
      try {
        // close the connection to the ldap
        connection.close();
      } catch (Exception e) {
        //ignore
        LOG.info("error closing connection", e);
      }
    }
  }
  
  /**
   * clear out audit logs
   * @param twoFactorDaoFactory 
   */
  public void aliasLogic(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("aliasDaemonEnabled", false)) {
      return;
    }

    boolean hasError = false;

    long start = System.nanoTime();
    Date startDate = new Date();
    int elapsedTime = -1;
    
    Map<String, Object> debugLog = new LinkedHashMap<String, Object>();

    int count = 0;
    
    Throwable throwable = null;
    try {

      List<String> aliasesFromLdap = retrieveAliases();

      debugLog.put("aliasCountInLdap", TwoFactorServerUtils.length(aliasesFromLdap));
      
      Map<String, String> aliasToNetidFromLdap = new HashMap<String, String>();
      // System.out.println(TwoFactorServerUtils.setToString(new java.util.TreeSet<String>(aliasToNetidFromLdap.keySet())))
      Pattern pattern = Pattern.compile("^(.*)-.*$");

      for (String aliasFromLdap : aliasesFromLdap) {
        Matcher matcher = pattern.matcher(aliasFromLdap);
        if (matcher.matches()) {
          String netid = matcher.group(1);
          aliasToNetidFromLdap.put(aliasFromLdap, netid);
        }
      }

      debugLog.put("aliasCountInLdapPostRegex", TwoFactorServerUtils.length(aliasToNetidFromLdap));

      Map<String, JsonNode> duoAliasesToUserObject = DuoCommands.retrieveAllAliasesFromDuo();
      // System.out.println(TwoFactorServerUtils.mapToString(duoAliasesToUserObject))
      debugLog.put("aliasCountInDuo", TwoFactorServerUtils.length(duoAliasesToUserObject));
      // System.out.println(TwoFactorServerUtils.setToString(new java.util.TreeSet<String>(duoAliasesToUserObject.keySet())))
      Map<String, JsonNode> duoAliasesToDelete = new HashMap<String, JsonNode>(duoAliasesToUserObject);

      Set<String> aliasWhitelist = new HashSet<String>();

      String aliasWhitelistString = TwoFactorServerConfig.retrieveConfig().propertyValueString("aliasWhitelist");
      
      if (!StringUtils.isBlank(aliasWhitelistString)) {
        aliasWhitelist = TwoFactorServerUtils.splitTrimToSet(aliasWhitelistString, ",");
      }
      
      //remove ones that should be there
      //dont worry about aliases assigned to other users
      Iterator<String> iterator = duoAliasesToDelete.keySet().iterator();
      while (iterator.hasNext()) {
        String duoAlias = iterator.next();
        if (aliasesFromLdap.contains(duoAlias) || aliasWhitelist.contains(duoAlias)) {
          iterator.remove();
        }
      }

      debugLog.put("aliasCountInDuoToDelete", TwoFactorServerUtils.length(duoAliasesToDelete));

      //remove these aliases not used
      for (String duoAliasToDelete : duoAliasesToDelete.keySet()) {
        JsonNode user = duoAliasesToDelete.get(duoAliasToDelete);
        
        String duoUserId = DuoCommands.jsonJacksonGetString(user, "user_id");
        
        DuoCommands.deleteDuoUserAlias(duoUserId, duoAliasToDelete);
        
      }
      
      int aliasCountToAdd = 0;
      int aliasCountNoAccount = 0;
      
      //add aliases that arent there
      for (String aliasFromLdap : aliasToNetidFromLdap.keySet()) {
        if (duoAliasesToUserObject.containsKey(aliasFromLdap)) {
          continue;
        }
        
        JSONObject user = null;
        String userNetId = aliasToNetidFromLdap.get(aliasFromLdap);
        try {
          user = DuoCommands.retrieveDuoUserBySomeId(userNetId);
        } catch (Exception e) {
          LOG.info("Cant find user '" + userNetId + "'", e);
        }
        if (user == null) {
          //if theres no account we cant add an alias
          aliasCountNoAccount++;
        } else {
          String duoUserId = user.getString("user_id");
          
          try {
            DuoCommands.addDuoUserAlias(duoUserId, aliasFromLdap);
          } catch (Exception e) {
            //if someone has too many aliases (or alias is reused), dont kill the process
            LOG.error("cant add alias on user: " + userNetId + ", " + duoUserId + ", " + aliasFromLdap, e);
          }
          aliasCountToAdd++;
        }
      }
      debugLog.put("aliasCountInDuoToAdd", aliasCountToAdd);
      debugLog.put("aliasCountNoAccount", aliasCountNoAccount);
      
    } catch (Throwable t) {
      hasError = true;
      debugLog.put("exception", TwoFactorServerUtils.getFullStackTrace(t));
      LOG.error(TwoFactorServerUtils.mapToString(debugLog));
      LOG.error("Error in daemon", t);
      throwable = t;
    } finally {
      elapsedTime = (int)((System.nanoTime() - start) / 1000000);
      debugLog.put("tookMillis", elapsedTime);
      debugLog.put("total records", count);
      TfDaemonLog.daemonLog(debugLog);
    }
    
    try {
      //lets store to DB
      TwoFactorDaemonLog twoFactorDaemonLog = new TwoFactorDaemonLog();
      twoFactorDaemonLog.setUuid(TwoFactorServerUtils.uuid());
      twoFactorDaemonLog.setDaemonName(TwoFactorDaemonName.aliases.name());
      twoFactorDaemonLog.setDetails(TwoFactorServerUtils.mapToString(debugLog));
      //2012-06-05 17:09:19
      twoFactorDaemonLog.setStartedTimeDate(startDate);
      twoFactorDaemonLog.setEndedTimeDate(new Date());
      twoFactorDaemonLog.setMillis(new Long(elapsedTime));
      twoFactorDaemonLog.setRecordsProcessed(new Long(count));
      twoFactorDaemonLog.setServerName(TwoFactorServerUtils.hostname());
      twoFactorDaemonLog.setStatus(hasError ? TwoFactorDaemonLogStatus.error.toString() : TwoFactorDaemonLogStatus.success.toString());
      twoFactorDaemonLog.store(twoFactorDaoFactory);
    } catch (Throwable t) {
      LOG.error("Error storing log", t);
      if (t instanceof RuntimeException) {
        throw (RuntimeException)t;
      }
      throw new RuntimeException(t);
    }
    if (hasError) {
      throw new RuntimeException("Error in daemon!", throwable);
    }
  }

  
}

