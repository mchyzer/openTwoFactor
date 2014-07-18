
/**
 * 
 */
package org.openTwoFactor.server.status;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openTwoFactor.server.cache.TwoFactorCache;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * see if the server can connect to the DB (cache results)
 * @author mchyzer
 *
 */
public class DiagnosticDaemonJobTest extends DiagnosticTask {

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DiagnosticDaemonJobTest) {
      DiagnosticDaemonJobTest other = (DiagnosticDaemonJobTest)obj;
      return new EqualsBuilder().append(this.daemonName, other.daemonName).isEquals();
    }
    return false;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.daemonName).toHashCode();
  }

  /** */
  private static final String INVALID_PROPERTIES_REGEX = "[^a-zA-Z0-9._-]";

  /** daemon name */
  private String daemonName;

  /**
   * construct with source id
   * @param theDaemonName
   */
  public DiagnosticDaemonJobTest(String theDaemonName) {
    this.daemonName = theDaemonName;
  }
  
  /**
   * cache the configs for 50 hours
   */
  private static TwoFactorCache<String, Long> daemonResultsCache = new TwoFactorCache<String, Long>(
      DiagnosticDaemonJobTest.class.getName() + ".daemonResultsDiagnostic", 10000, false, 60 * 60 * 50, 60 * 60 * 50, false);
  
  /**
   * @see org.openTwoFactor.server.status.DiagnosticsTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    //we will give it 52 hours... 48 (two days), plus 4 hours to run...
    int defaultMinutesSinceLastSuccess = TwoFactorServerConfig.retrieveConfig().propertyValueInt("status.daemon.defaultMinutesSinceLastSuccess", 60*52);
        
    //default of last success is usually 25 hours, but can be less for change log jobs
    int minutesSinceLastSuccess = -1;

    String diagnosticsName = this.retrieveName();

    //try with full job name
    if (minutesSinceLastSuccess == -1) {
      String configName = diagnosticsName.replaceAll(INVALID_PROPERTIES_REGEX, "_");

      minutesSinceLastSuccess = TwoFactorServerConfig.retrieveConfig().propertyValueInt("status.daemon.minutesSinceLastSuccess." + configName, defaultMinutesSinceLastSuccess);
    }
      
    
    Long lastSuccess = daemonResultsCache.get(this.daemonName);
    
    if (lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess ) {
      this.appendSuccessTextLine("Not checking, there was a success from before: " + TwoFactorServerUtils.dateStringValue(lastSuccess) 
          + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
    } else {
     
      lastSuccess = TwoFactorDaoFactory.getFactory().getTwoFactorDaemonLog().retrieveMostRecentSuccessTimestamp(this.daemonName);
            
      
      if (lastSuccess != null && (System.currentTimeMillis() - lastSuccess) / (1000 * 60) < minutesSinceLastSuccess ) {
        
        this.appendSuccessTextLine("Found the most recent success: " + TwoFactorServerUtils.dateStringValue(lastSuccess) 
            + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
        
      } else {
        daemonResultsCache.remove(this.daemonName);
        if (lastSuccess == null) {
          throw new RuntimeException("Cant find a success, expecting one in the last " + minutesSinceLastSuccess + " minutes");
        }
        throw new RuntimeException("Cant find a success since: " + TwoFactorServerUtils.dateStringValue(lastSuccess) 
            + ", expecting one in the last " + minutesSinceLastSuccess + " minutes");
      }
      
      daemonResultsCache.put(this.daemonName, lastSuccess);
    }
        
    return true;
  }

  /**
   * @see org.openTwoFactor.server.status.DiagnosticTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "daemon_" + this.daemonName;
  }

  /**
   * @see org.openTwoFactor.server.status.DiagnosticTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Daemon job " + this.daemonName;
  }

}
