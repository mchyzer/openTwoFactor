/**
 * @author mchyzer
 * $Id: TwoFactorAuditView.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.ui.beans.TextContainer;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * view on audit records to make them more human readable
 */
public class TwoFactorAuditView extends TwoFactorHibernateBeanBase {

  /** constant for field name for: action */
  public static final String FIELD_ACTION = "action";

  /** constant for field name for: browserUuid */
  public static final String FIELD_BROWSER_UUID = "browserUuid";

  /** constant for field name for: ipAddressUuid */
  public static final String FIELD_IP_ADDRESS_UUID = "ipAddressUuid";

  /** constant for field name for: serviceProviderUuid */
  public static final String FIELD_SERVICE_PROVIDER_UUID = "serviceProviderUuid";

  /** constant for field name for: userAgentUuid */
  public static final String FIELD_USER_AGENT_UUID = "userAgentUuid";

  /** constant for field name for: userUuid */
  public static final String FIELD_USER_UUID = "userUuid";

  /** constant for field name for: userUuidUsingApp */
  public static final String FIELD_USER_UUID_USING_APP = "userUuidUsingApp";

  /** constant for field name for: userUsingLoginid */
  public static final String FIELD_USER_USING_LOGINID = "userUsingLoginid";

  /** constant for field name for: loginid */
  public static final String FIELD_LOGINID = "loginid";

  /** constant for field name for: trustedBrowser */
  public static final String FIELD_TRUSTED_BROWSER = "trustedBrowser";

  /** constant for field name for: ipAddress */
  public static final String FIELD_IP_ADDRESS = "ipAddress";

  /** constant for field name for: userAgentOperatingSystem */
  public static final String FIELD_USER_AGENT_OPERATING_SYSTEM = "userAgentOperatingSystem";

  /** constant for field name for: userAgentBrowser */
  public static final String FIELD_USER_AGENT_BROWSER = "userAgentBrowser";

  /** constant for field name for: userAgentMobile */
  public static final String FIELD_USER_AGENT_MOBILE = "userAgentMobile";

  /** constant for field name for: serviceProviderId */
  public static final String FIELD_SERVICE_PROVIDER_ID = "serviceProviderId";

  /** constant for field name for: serviceProviderName */
  public static final String FIELD_SERVICE_PROVIDER_NAME = "serviceProviderName";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: domainName */
  public static final String FIELD_DOMAIN_NAME = "domainName";

  /** constant for field name for: whenBrowserTrusted */
  public static final String FIELD_WHEN_BROWSER_TRUSTED = "whenBrowserTrusted";

  /** constant for field name for: userAgent */
  public static final String FIELD_USER_AGENT = "userAgent";

  /** constant for field name for: auditUuid */
  public static final String FIELD_AUDIT_UUID = "auditUuid";

  /** constant for field name for: theTimestamp */
  public static final String FIELD_THE_TIMESTAMP = "theTimestamp";

  /**
   * action enum
   */
  private String action;

  
  /**
   * action enum
   * @return the action
   */
  public String getAction() {
    return this.action;
  }

  /**
   * formatted enum
   * @return the action
   */
  public String getActionFormatted() {
    
    if (this.action == null) {
      return null;
    }
    
    try {
      TwoFactorAuditAction twoFactorAuditAction = TwoFactorAuditAction.valueOfIgnoreCase(this.action);
      return TextContainer.retrieveFromRequest().getText().get(twoFactorAuditAction.toStringForUi());
    } catch (Exception e) {
      LOG.info("Error decoding action: " + this.action);
    }
    
    return this.action;
  }

  
  /**
   * action enum
   * @param action1 the action to set
   */
  public void setAction(String action1) {
    this.action = action1;
  }
  
  /**
   * loginid of user who performed it
   */
  private String loginid;
  
  /**
   * loginid of user who performed it
   * @return the loginid
   */
  public String getLoginid() {
    return this.loginid;
  }
  
  /**
   * loginid of user who performed it
   * @param loginid1 the loginid to set
   */
  public void setLoginid(String loginid1) {
    this.loginid = loginid1;
  }
  
  /**
   * if this is a trusted browser (might not be in sync)
   */
  private Boolean trustedBrowser;

  
  /**
   * if this is a trusted browser (might not be in sync)
   * @return the trustedBrowser
   */
  public Boolean getTrustedBrowser() {
    return this.trustedBrowser;
  }

  
  /**
   * if this is a trusted browser (might not be in sync)
   * @param trustedBrowser1 the trustedBrowser to set
   */
  public void setTrustedBrowser(Boolean trustedBrowser1) {
    this.trustedBrowser = trustedBrowser1;
  }
  
  /**
   * ip address of user who performed it
   */
  private String ipAddress;
  
  /**
   * ip address of user who performed it
   * @return the ipAddress
   */
  public String getIpAddress() {
    return this.ipAddress;
  }
  
  /**
   * ip address of user who performed it
   * @param ipAddress1 the ipAddress to set
   */
  public void setIpAddress(String ipAddress1) {
    this.ipAddress = ipAddress1;
  }
  
  /**
   * user agent operating system of user who performed it (might not be accurate)
   */
  private String userAgentOperatingSystem;

  
  /**
   * user agent operating system of user who performed it (might not be accurate)
   * @return the userAgentOperatingSystem
   */
  public String getUserAgentOperatingSystem() {
    return this.userAgentOperatingSystem;
  }
  
  /**
   * user agent operating system of user who performed it (might not be accurate)
   * @param userAgentOperatingSystem1 the userAgentOperatingSystem to set
   */
  public void setUserAgentOperatingSystem(String userAgentOperatingSystem1) {
    this.userAgentOperatingSystem = userAgentOperatingSystem1;
  }
  
  /**
   * user agent browser of user who performed it (might not be accurate)
   */
  private String userAgentBrowser;
  
  /**
   * user agent browser of user who performed it (might not be accurate)
   * @return the userAgentBrowser
   */
  public String getUserAgentBrowser() {
    return this.userAgentBrowser;
  }
  
  /**
   * user agent browser of user who performed it (might not be accurate)
   * @param userAgentBrowser1 the userAgentBrowser to set
   */
  public void setUserAgentBrowser(String userAgentBrowser1) {
    this.userAgentBrowser = userAgentBrowser1;
  }
  
  /**
   * T or F if the browser of the user who performed it is mobile (might not be accurate)
   * @return the userAgentMobile
   */
  public Boolean getUserAgentMobile() {
    return this.userAgentMobile;
  }

  /**
   * T or F if the browser of the user who performed it is mobile (might not be accurate)
   * @param userAgentMobile1 the userAgentMobile to set
   */
  public void setUserAgentMobile(Boolean userAgentMobile1) {
    this.userAgentMobile = userAgentMobile1;
  }

  /**
   * T or F if the browser of the user who performed it is mobile (might not be accurate)
   */
  private Boolean userAgentMobile;

  /**
   * service provider id of the affected service provider
   */
  private String serviceProviderId;
  
  /**
   * user uuid using the application
   */
  private String userUuidUsingApp;
  
  /**
   * loginid of the user using the app
   */
  private String userUsingLoginid;
  
  
  /**
   * user uuid using the application
   * @return the userUuidUsingApp
   */
  public String getUserUuidUsingApp() {
    return this.userUuidUsingApp;
  }

  
  /**
   * user uuid using the application
   * @param userUuidUsingApp1 the userUuidUsingApp to set
   */
  public void setUserUuidUsingApp(String userUuidUsingApp1) {
    this.userUuidUsingApp = userUuidUsingApp1;
  }

  
  /**
   * loginid of the user using the app
   * @return the userUsingLoginid
   */
  public String getUserUsingLoginid() {
    return this.userUsingLoginid;
  }

  
  /**
   * loginid of the user using the app
   * @param userUsingLoginid1 the userUsingLoginid to set
   */
  public void setUserUsingLoginid(String userUsingLoginid1) {
    this.userUsingLoginid = userUsingLoginid1;
  }

  /**
   * service provider id of the affected service provider
   * @return the serviceProviderId
   */
  public String getServiceProviderId() {
    return this.serviceProviderId;
  }
  
  /**
   * service provider id of the affected service provider
   * @param serviceProviderId1 the serviceProviderId to set
   */
  public void setServiceProviderId(String serviceProviderId1) {
    this.serviceProviderId = serviceProviderId1;
  }

  /**
   * service provider name of the affected service provider
   */
  private String serviceProviderName;
  
  /**
   * service provider name of the affected service provider
   * @return the serviceProviderName
   */
  public String getServiceProviderName() {
    return this.serviceProviderName;
  }
  
  /**
   * service provider name of the affected service provider
   * @param serviceProviderName1 the serviceProviderName to set
   */
  public void setServiceProviderName(String serviceProviderName1) {
    this.serviceProviderName = serviceProviderName1;
  }
  
  /**
   * description of the audit record if applicable
   */
  private String description;
  
  /**
   * description of the audit record if applicable
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }
  
  /**
   * description of the audit record if applicable
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }
  
  /**
   * domain name of the ip address of the user who performed it
   */
  private String domainName;

  /**
   * domain name of the ip address of the user who performed it
   * @return the domainName
   */
  public String getDomainName() {
    return this.domainName;
  }
  
  /**
   * domain name of the ip address of the user who performed it
   * @param domainName1 the domainName to set
   */
  public void setDomainName(String domainName1) {
    this.domainName = domainName1;
  }
  
  /**
   * when the browser was last trusted
   */
  private Long whenBrowserTrusted;
  
  /**
   * when the browser was last trusted
   * @return the whenBrowserTrusted
   */
  public Long getWhenBrowserTrusted() {
    return this.whenBrowserTrusted;
  }

  /**
   * when the browser was last trusted
   * @param whenBrowserTrusted1 the whenBrowserTrusted to set
   */
  public void setWhenBrowserTrusted(Long whenBrowserTrusted1) {
    this.whenBrowserTrusted = whenBrowserTrusted1;
  }
  
  /**
   * the user agent that performed the action
   */
  private String userAgent;
  
  /**
   * the user agent that performed the action
   * @return the userAgent
   */
  public String getUserAgent() {
    return this.userAgent;
  }
  
  /**
   * the user agent that performed the action
   * @param userAgent1 the userAgent to set
   */
  public void setUserAgent(String userAgent1) {
    this.userAgent = userAgent1;
  }
  
  /**
   * date of when it happened in millis from 1970
   */
  private Long theTimestamp;

  /**
   * foreign key to the browser
   * BROWSER_UUID           VARCHAR2(40 CHAR),
   */
  private String browserUuid;

  /**
   * foreign key to the ip address
   * IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
   */
  private String ipAddressUuid;

  /**
   * foreign key to the service provider
   * SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
   */
  private String serviceProviderUuid;

  /**
   * foreign key to the user agent
   * USER_AGENT_UUID        VARCHAR2(40 CHAR),
   */
  private String userAgentUuid;

  /**
   * foreign key to the user
   * USER_UUID              VARCHAR2(40 CHAR),
   */
  private String userUuid;

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorAuditView.class);

  /**
   * fields which are included in clone
   */
  @SuppressWarnings("unused")
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
    FIELD_DELETED_ON,
    FIELD_BROWSER_UUID,
    FIELD_IP_ADDRESS_UUID,
    FIELD_SERVICE_PROVIDER_UUID,
    FIELD_USER_AGENT_UUID,
    FIELD_USER_UUID,
    FIELD_LAST_UPDATED,
    FIELD_UUID,
    FIELD_ACTION,
    FIELD_UUID,
    FIELD_DESCRIPTION,
    FIELD_DOMAIN_NAME,
    FIELD_IP_ADDRESS,
    FIELD_LOGINID,
    FIELD_SERVICE_PROVIDER_ID,
    FIELD_SERVICE_PROVIDER_NAME,
    FIELD_THE_TIMESTAMP,
    FIELD_TRUSTED_BROWSER,
    FIELD_USER_AGENT,
    FIELD_USER_AGENT_BROWSER,
    FIELD_USER_AGENT_MOBILE,
    FIELD_USER_AGENT_OPERATING_SYSTEM,
    FIELD_USER_UUID_USING_APP,
    FIELD_USER_USING_LOGINID,
    FIELD_WHEN_BROWSER_TRUSTED,
    FIELD_VERSION_NUMBER
      ));

  /**
   * fields which need db update
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_BROWSER_UUID,
      FIELD_IP_ADDRESS_UUID,
      FIELD_SERVICE_PROVIDER_UUID,
      FIELD_USER_AGENT_UUID,
      FIELD_USER_UUID,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_ACTION,
      FIELD_DESCRIPTION,
      FIELD_DOMAIN_NAME,
      FIELD_IP_ADDRESS,
      FIELD_LOGINID,
      FIELD_SERVICE_PROVIDER_ID,
      FIELD_SERVICE_PROVIDER_NAME,
      FIELD_THE_TIMESTAMP,
      FIELD_TRUSTED_BROWSER,
      FIELD_USER_AGENT,
      FIELD_USER_AGENT_BROWSER,
      FIELD_USER_AGENT_MOBILE,
      FIELD_USER_AGENT_OPERATING_SYSTEM,
      FIELD_USER_UUID_USING_APP,
      FIELD_USER_USING_LOGINID,
      FIELD_WHEN_BROWSER_TRUSTED,
      FIELD_VERSION_NUMBER
      ));

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_BROWSER_UUID,
      FIELD_IP_ADDRESS_UUID,
      FIELD_SERVICE_PROVIDER_UUID,
      FIELD_USER_AGENT_UUID,
      FIELD_USER_UUID,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_ACTION,
      FIELD_DESCRIPTION,
      FIELD_DOMAIN_NAME,
      FIELD_IP_ADDRESS,
      FIELD_LOGINID,
      FIELD_SERVICE_PROVIDER_ID,
      FIELD_SERVICE_PROVIDER_NAME,
      FIELD_THE_TIMESTAMP,
      FIELD_TRUSTED_BROWSER,
      FIELD_USER_AGENT,
      FIELD_USER_AGENT_BROWSER,
      FIELD_USER_AGENT_MOBILE,
      FIELD_USER_AGENT_OPERATING_SYSTEM,
      FIELD_USER_UUID_USING_APP,
      FIELD_USER_USING_LOGINID,
      FIELD_WHEN_BROWSER_TRUSTED,
      FIELD_VERSION_NUMBER
      ));
  
  /**
   * date of when it happened in millis from 1970
   * @return the theTimestamp
   */
  public Long getTheTimestamp() {
    return this.theTimestamp;
  }
  
  /**
   * 
   * @return the string
   */
  public String getTheTimestampFormatted() {
    
    return TwoFactorServerUtils.dateToString(this.theTimestamp);
  }
  
  /**
   * date of when it happened in millis from 1970
   * @param theTimestamp1 the theTimestamp to set
   */
  public void setTheTimestamp(Long theTimestamp1) {
    this.theTimestamp = theTimestamp1;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * foreign key to the browser
   * BROWSER_UUID           VARCHAR2(40 CHAR),
   * @return the browserUuid
   */
  public String getBrowserUuid() {
    return this.browserUuid;
  }

  /**
   * foreign key to the ip address
   * IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
   * @return the ipAddressUuid
   */
  public String getIpAddressUuid() {
    return this.ipAddressUuid;
  }

  /**
   * foreign key to the service provider
   * SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
   * @return the serviceProviderUuid
   */
  public String getServiceProviderUuid() {
    return this.serviceProviderUuid;
  }

  /**
   * foreign key to the user agent
   * USER_AGENT_UUID        VARCHAR2(40 CHAR),
   * @return the userAgentUuid
   */
  public String getUserAgentUuid() {
    return this.userAgentUuid;
  }

  /**
   * foreign key to the user
   * USER_UUID              VARCHAR2(40 CHAR),
   * @return the userUuid
   */
  public String getUserUuid() {
    return this.userUuid;
  }

  /**
   * foreign key to the browser
   * BROWSER_UUID           VARCHAR2(40 CHAR),
   * @param browserUuid1 the browserUuid to set
   */
  public void setBrowserUuid(String browserUuid1) {
    this.browserUuid = browserUuid1;
  }

  /**
   * foreign key to the ip address
   * IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
   * @param ipAddressUuid1 the ipAddressUuid to set
   */
  public void setIpAddressUuid(String ipAddressUuid1) {
    this.ipAddressUuid = ipAddressUuid1;
  }

  /**
   * foreign key to the service provider
   * SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
   * @param serviceProviderUuid1 the serviceProviderUuid to set
   */
  public void setServiceProviderUuid(String serviceProviderUuid1) {
    this.serviceProviderUuid = serviceProviderUuid1;
  }

  /**
   * foreign key to the user agent
   * USER_AGENT_UUID        VARCHAR2(40 CHAR),
   * @param userAgentUuid1 the userAgentUuid to set
   */
  public void setUserAgentUuid(String userAgentUuid1) {
    this.userAgentUuid = userAgentUuid1;
  }

  /**
   * foreign key to the user
   * USER_UUID              VARCHAR2(40 CHAR),
   * @param userUuid1 the userUuid to set
   */
  public void setUserUuid(String userUuid1) {
    this.userUuid = userUuid1;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#delete(org.openTwoFactor.server.hibernate.TwoFactorDaoFactory)
   */
  @Override
  public void delete(TwoFactorDaoFactory twoFactorDaoFactory) {
    throw new RuntimeException("Cant delete a view!!!!");
  }
  
}
