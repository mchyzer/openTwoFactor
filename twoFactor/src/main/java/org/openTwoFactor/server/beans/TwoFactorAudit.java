<<<<<<< HEAD
/**
 * @author mchyzer
 * $Id: TwoFactorAudit.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * two factor audit object tied to audit db table
 */
public class TwoFactorAudit extends TwoFactorHibernateBeanBase {

  /**
   * truncate fields for db
   */
  public void truncate() {
    this.description = TwoFactorServerUtils.truncateAscii(this.description, 1000);
  }

  /**
   * create and store an audit
   * @param twoFactorDaoFactory 
   * @param twoFactorAuditAction 
   * @param ipAddress 
   * @param userAgent 
   * @param userUuidOperatingOn
   * @param userUuidLoggedIn 
   * @return the audit
   */
  public static TwoFactorAudit createAndStore(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent, String userUuidOperatingOn, 
      String userUuidLoggedIn) {
    return createAndStore(twoFactorDaoFactory, twoFactorAuditAction, ipAddress, 
        userAgent, userUuidOperatingOn, userUuidLoggedIn, null);
  }

  /**
   * create and store an audit
   * @param twoFactorDaoFactory 
   * @param twoFactorAuditAction 
   * @param ipAddress 
   * @param userAgent 
   * @param userUuidOperatingOn
   * @param userUuidLoggedIn 
   * @param description 
   * @return the audit
   */
  public static TwoFactorAudit createAndStore(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent, String userUuidOperatingOn, 
      String userUuidLoggedIn, String description) {
    
    TwoFactorAudit twoFactorAudit = create(twoFactorDaoFactory, twoFactorAuditAction,
        ipAddress, userAgent, userUuidOperatingOn, userUuidLoggedIn);
    twoFactorAudit.setDescription(description);
    twoFactorAudit.store(twoFactorDaoFactory);
    return twoFactorAudit;
  }


  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorAudit.class);

  
  /**
   * thread to store audits from WS
   */
  private static Thread storeThread = new Thread(new Runnable() {

    @Override
    public void run() {
      
      while (true) {
        try {
          
          //process items
          while (!twoFactorAuditFailsafes.isEmpty()) {

            TwoFactorAuditFailsafe twoFactorAuditFailsafe = twoFactorAuditFailsafes.element();
            //try to store
            TwoFactorAudit twoFactorAudit = create(twoFactorAuditFailsafe.getTwoFactorDaoFactory(), 
                twoFactorAuditFailsafe.getTwoFactorAuditAction(),
                twoFactorAuditFailsafe.getIpAddress(),
                twoFactorAuditFailsafe.getUserAgent(),
                twoFactorAuditFailsafe.getUserUuid(), 
                twoFactorAuditFailsafe.getUserUuidUsingApp());
            twoFactorAudit.assignServiceProvider(twoFactorAuditFailsafe.getTwoFactorDaoFactory(), 
                twoFactorAuditFailsafe.getServiceProviderId(), twoFactorAuditFailsafe.getServiceProviderName());
            twoFactorAudit.setDescription(twoFactorAuditFailsafe.getDescription());
            twoFactorAudit.store(twoFactorAuditFailsafe.getTwoFactorDaoFactory());
            
            //not sure why wouldnt be equal
            synchronized (TwoFactorAudit.class) {
              if (twoFactorAuditFailsafe != twoFactorAuditFailsafes.removeFirst()) {
                throw new RuntimeException("Why is this not equal??? ");
              }
            }            
          }
            
          try {
            synchronized (twoFactorAuditFailsafes) {
              //note, the 60000 isnt really required, but just to wake up and see if there is something to do
              twoFactorAuditFailsafes.wait(60000);
            }
          } catch (InterruptedException ignored) {
          }
          
        } catch (Throwable t) {
          LOG.error("Error in storeThread", t);
          //lets pause for a little while to not hammer the DB
          TwoFactorServerUtils.sleep(5000);
        }
      }
    }
    
  });
  
  /**
   * linked list of audits to save to DB
   */
  private static LinkedList<TwoFactorAuditFailsafe> twoFactorAuditFailsafes = new LinkedList<TwoFactorAuditFailsafe>();
  
  /**
   * bean to put on queue to store in another thread
   */
  private static class TwoFactorAuditFailsafe {
    
    /**
     * user uuid who is using the app
     */
    private String userUuidUsingApp;
    
    /**
     * user uuid who is using the app
     * @return the userUuidUsingApp
     */
    public String getUserUuidUsingApp() {
      return this.userUuidUsingApp;
    }
    
    /**
     * user uuid who is using the app
     * @param userUuidUsingApp1 the userUuidUsingApp to set
     */
    public void setUserUuidUsingApp(String userUuidUsingApp1) {
      this.userUuidUsingApp = userUuidUsingApp1;
    }

    /**
     * service provider id
     */
    private String serviceProviderId;
    
    /**
     * service provider name
     */
    private String serviceProviderName;
    
    
    /**
     * service provider id
     * @return the serviceProviderId
     */
    public String getServiceProviderId() {
      return this.serviceProviderId;
    }

    
    /**
     * service provider id
     * @param serviceProviderId1 the serviceProviderId to set
     */
    public void setServiceProviderId(String serviceProviderId1) {
      this.serviceProviderId = serviceProviderId1;
    }

    
    /**
     * service provider name
     * @return the serviceProviderName
     */
    public String getServiceProviderName() {
      return this.serviceProviderName;
    }

    
    /**
     * service provider name
     * @param serviceProviderName1 the serviceProviderName to set
     */
    public void setServiceProviderName(String serviceProviderName1) {
      this.serviceProviderName = serviceProviderName1;
    }

    /**
     * dao factory
     */
    private TwoFactorDaoFactory twoFactorDaoFactory;
    
    /**
     * dao factory
     * @return the twoFactorDaoFactory
     */
    public TwoFactorDaoFactory getTwoFactorDaoFactory() {
      return this.twoFactorDaoFactory;
    }
    
    /**
     * dao factory
     * @param twoFactorDaoFactory1 the twoFactorDaoFactory to set
     */
    public void setTwoFactorDaoFactory(TwoFactorDaoFactory twoFactorDaoFactory1) {
      this.twoFactorDaoFactory = twoFactorDaoFactory1;
    }
    
    /**
     * action
     */
    private TwoFactorAuditAction twoFactorAuditAction; 
    
    /**
     * action
     * @return the twoFactorAuditAction
     */
    public TwoFactorAuditAction getTwoFactorAuditAction() {
      return this.twoFactorAuditAction;
    }
    
    /**
     * action
     * @param twoFactorAuditAction1 the twoFactorAuditAction to set
     */
    public void setTwoFactorAuditAction(TwoFactorAuditAction twoFactorAuditAction1) {
      this.twoFactorAuditAction = twoFactorAuditAction1;
    }
    
    /**
     * ip address
     */
    private String ipAddress;
    
    /**
     * ip address
     * @return the ipAddress
     */
    public String getIpAddress() {
      return this.ipAddress;
    }

    /**
     * ip address
     * @param ipAddress1 the ipAddress to set
     */
    public void setIpAddress(String ipAddress1) {
      this.ipAddress = ipAddress1;
    }
    
    /**
     * user agent
     */
    private String userAgent; 
    
    /**
     * user agent
     * @return the userAgent
     */
    public String getUserAgent() {
      return this.userAgent;
    }
    
    /**
     * user agent
     * @param userAgent1 the userAgent to set
     */
    public void setUserAgent(String userAgent1) {
      this.userAgent = userAgent1;
    }
    
    /**
     * user uuid
     */
    private String userUuid;
    
    /**
     * user uuid
     * @return the userUuid
     */
    public String getUserUuid() {
      return this.userUuid;
    }

    /**
     * user uuid
     * @param userUuid1 the userUuid to set
     */
    public void setUserUuid(String userUuid1) {
      this.userUuid = userUuid1;
    }
    
    /**
     * description
     */
    private String description;
    
    /**
     * description
     * @return the description
     */
    public String getDescription() {
      return this.description;
    }
    
    /**
     * description
     * @param description1 the description to set
     */
    public void setDescription(String description1) {
      this.description = description1;
    }    
    
  }

  /**
   * store thread is started?
   */
  private static boolean storeThreadStarted = false;
  
  /**
   * start the store thread if not started
   */
  private static void storeThreadStartIfNotStarted() {
    if (!storeThreadStarted) {
      synchronized (TwoFactorAudit.class) {
        if (!storeThreadStarted) {
          storeThread.setDaemon(true);
          storeThread.start();
          storeThreadStarted = true;
        }
      }
    }
  }
  
  /**
   * create and store an audit in new thread without giving error (for web service), doesnt block
   * @param twoFactorDaoFactory 
   * @param twoFactorAuditAction 
   * @param ipAddress 
   * @param userAgent 
   * @param description
   * @param userUuidLoggedIn 
   * @param serviceProviderId 
   * @param serviceProviderName 
   * @param userUuidUsingApp
   */
  public static void createAndStoreFailsafe(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent, String userUuidLoggedIn, String description,
      String serviceProviderId, String serviceProviderName, String userUuidUsingApp
      ) {
    
    storeThreadStartIfNotStarted();
    
    TwoFactorAuditFailsafe twoFactorAuditFailsafe = new TwoFactorAuditFailsafe();
    twoFactorAuditFailsafe.setDescription(description);
    twoFactorAuditFailsafe.setIpAddress(ipAddress);
    twoFactorAuditFailsafe.setTwoFactorAuditAction(twoFactorAuditAction);
    twoFactorAuditFailsafe.setTwoFactorDaoFactory(twoFactorDaoFactory);
    twoFactorAuditFailsafe.setUserAgent(userAgent);
    twoFactorAuditFailsafe.setUserUuid(userUuidLoggedIn);
    twoFactorAuditFailsafe.setUserUuidUsingApp(userUuidUsingApp);
    twoFactorAuditFailsafe.setServiceProviderId(serviceProviderId);
    twoFactorAuditFailsafe.setServiceProviderName(serviceProviderName);
    synchronized (TwoFactorAudit.class) {
      twoFactorAuditFailsafes.add(twoFactorAuditFailsafe);
    }
    synchronized (twoFactorAuditFailsafes) {
      twoFactorAuditFailsafes.notify();
    }
  }


  /**
   * @param twoFactorDaoFactory
   * @param twoFactorAuditAction
   * @param ipAddress
   * @param userAgent
   * @param userUuidOperatingOn
   * @param userUuidLoggedIn 
   * @return the audit object to augment or store
   */
  public static TwoFactorAudit create(TwoFactorDaoFactory twoFactorDaoFactory,
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent,
      String userUuidOperatingOn,  String userUuidLoggedIn) {
    TwoFactorAudit twoFactorAudit = new TwoFactorAudit();
    twoFactorAudit.setUuid(TwoFactorServerUtils.uuid());
    twoFactorAudit.setAction(twoFactorAuditAction.name());
    twoFactorAudit.assignIpAddress(twoFactorDaoFactory, ipAddress);
    twoFactorAudit.assignUserAgent(twoFactorDaoFactory, userAgent);
    twoFactorAudit.setTheTimestamp(System.currentTimeMillis());
    twoFactorAudit.setUserUuid(userUuidOperatingOn);
    twoFactorAudit.setUserUuidUsingApp(userUuidLoggedIn);
    return twoFactorAudit;
  }
  
  /**
   * retrieve a list of audits for a particular user
   * @param twoFactorDaoFactory
   * @param userUuid
   * @param tfQueryOptions
   * @return the list
   */
  public static List<TwoFactorAuditView> retrieveByUser(final TwoFactorDaoFactory twoFactorDaoFactory, 
      String userUuid, TfQueryOptions tfQueryOptions) {
    return twoFactorDaoFactory.getTwoFactorAudit().retrieveByUser(userUuid, tfQueryOptions);
  }

  /**
   * retrieve an audit for a uuid
   * @param twoFactorDaoFactory
   * @param uuid
   * @return the list
   */
  public static TwoFactorAudit retrieveByUuid(final TwoFactorDaoFactory twoFactorDaoFactory, 
      String uuid) {
    return twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(uuid);
  }

  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;

  /**
   * delete this audit
   * @param twoFactorDaoFactory
   */
  @Override
  public void delete(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    twoFactorDaoFactory.getTwoFactorAudit().delete(this);
    testDeletes++;
  
  }

  /**
   * 
   * @param twoFactorDaoFactory
   */
  public void store(TwoFactorDaoFactory twoFactorDaoFactory) {

    if (StringUtils.isBlank(this.action)) {
      throw new RuntimeException("action is null");
    }
    
    if (this.action != null && this.action.length() > 30) {
      throw new RuntimeException("action is too long (30): '" + this.action + "'");
    }
    
    //max length if 1000, chop off some for invalid chars
    this.truncate();
    
    twoFactorDaoFactory.getTwoFactorAudit().store(this);
    testInsertsAndUpdates++;

  }
  
  /**
   * number of deletes
   */
  static int testDeletes = 0;


  
  /**
   * foreign key to the user agent
   * USER_AGENT_UUID        VARCHAR2(40 CHAR),
   */
  private String userAgentUuid;
  
  
  /**
   * foreign key to the user agent
   * USER_AGENT_UUID        VARCHAR2(40 CHAR),
   * @return the userAgentUuid
   */
  public String getUserAgentUuid() {
    return this.userAgentUuid;
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
   * foreign key to the service provider
   * SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
   */
  private String serviceProviderUuid;
  
  /**
   * foreign key to the service provider
   * SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
   * @return the serviceProviderUuid
   */
  public String getServiceProviderUuid() {
    return this.serviceProviderUuid;
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
   * foreign key to the ip address
   * IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
   */
  private String ipAddressUuid;
  
  
  /**
   * foreign key to the ip address
   * IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
   * @return the ipAddressUuid
   */
  public String getIpAddressUuid() {
    return this.ipAddressUuid;
  }

  /**
   * assign an ip addres to the audit record
   * @param twoFactorDaoFactory
   * @param ipAddress
   */
  public void assignIpAddress(TwoFactorDaoFactory twoFactorDaoFactory, String ipAddress) {
    TwoFactorIpAddress twoFactorIpAddress = StringUtils.isBlank(ipAddress) ? null : 
        TwoFactorIpAddress.retrieveByIpAddressOrCreate(
        twoFactorDaoFactory, ipAddress);
    if (twoFactorIpAddress == null) {
      this.ipAddressUuid = null;
    } else {
      this.ipAddressUuid = twoFactorIpAddress.getUuid();
    }
  }
  
  /**
   * assign a user agent to an audit record
   * @param twoFactorDaoFactory
   * @param userAgent
   */
  public void assignUserAgent(TwoFactorDaoFactory twoFactorDaoFactory, String userAgent) {
    TwoFactorUserAgent twoFactorUserAgent = StringUtils.isBlank(userAgent) ? null : 
        TwoFactorUserAgent.retrieveByUserAgentOrCreate(
        twoFactorDaoFactory, userAgent);
    if (twoFactorUserAgent == null) {
      this.userAgentUuid = null;
    } else {
      this.userAgentUuid = twoFactorUserAgent.getUuid();
    }
  }
  
  /**
   * assign a service provider to an audit record
   * @param twoFactorDaoFactory
   * @param serviceProviderId
   * @param serviceProviderName
   */
  public void assignServiceProvider(TwoFactorDaoFactory twoFactorDaoFactory, String serviceProviderId, String serviceProviderName) {
    TwoFactorServiceProvider twoFactorServiceProvider = StringUtils.isBlank(serviceProviderId) ? null : 
        TwoFactorServiceProvider.retrieveByServiceProviderIdOrCreate(
        twoFactorDaoFactory, serviceProviderId, serviceProviderName);
    if (twoFactorServiceProvider == null) {
      this.serviceProviderUuid = null;
    } else {
      this.serviceProviderUuid = twoFactorServiceProvider.getUuid();
    }
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
   * if a description is needed, this is more info
   * DESCRIPTION            VARCHAR2(100 CHAR),
   */
  private String description;
  
  
  /**
   * if a description is needed, this is more info
   * DESCRIPTION            VARCHAR2(100 CHAR),
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  
  /**
   * if a description is needed, this is more info
   * DESCRIPTION            VARCHAR2(100 CHAR),
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * user uuid using the application
   */
  private String userUuidUsingApp;
  
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
   * ACTION                 VARCHAR2(30 CHAR)      NOT NULL,
   * action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, 
   * NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, 
   * INVALIDATE_PASSWORDS, GENERATE_PASSWORDS
   */
  private String action;
  
  
  /**
   * action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, 
   * NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, 
   * INVALIDATE_PASSWORDS, GENERATE_PASSWORDS
   * 
   * ACTION                 VARCHAR2(30 CHAR)      NOT NULL,
   * @return the action
   */
  public String getAction() {
    return this.action;
  }

  
  /**
   * action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, 
   * NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, 
   * INVALIDATE_PASSWORDS, GENERATE_PASSWORDS
   * ACTION                 VARCHAR2(30 CHAR)      NOT NULL,
   * @param action1 the action to set
   */
  public void setAction(String action1) {
    this.action = action1;
  }

  /**
   * timestamp of this record
   * THE_TIMESTAMP          INTEGER                NOT NULL,
   */
  private long theTimestamp;
  
  
  /**
   * timestamp of this record
   * THE_TIMESTAMP          INTEGER                NOT NULL,
   * @return the theTimestamp
   */
  public long getTheTimestamp() {
    return this.theTimestamp;
  }

  
  /**
   * timestamp of this record
   * THE_TIMESTAMP          INTEGER                NOT NULL,
   * @param theTimestamp1 the theTimestamp to set
   */
  public void setTheTimestamp(long theTimestamp1) {
    this.theTimestamp = theTimestamp1;
  }

  /**
   * foreign key to the browser
   * BROWSER_UUID           VARCHAR2(40 CHAR),
   */
  private String browserUuid;
  
  
  
  
  /**
   * foreign key to the browser
   * BROWSER_UUID           VARCHAR2(40 CHAR),
   * @return the browserUuid
   */
  public String getBrowserUuid() {
    return this.browserUuid;
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
   * foreign key to the user
   * USER_UUID              VARCHAR2(40 CHAR),
   */
  private String userUuid;

  /** constant for field name for: action */
  public static final String FIELD_ACTION = "action";
  
  /** constant for field name for: browserUuid */
  public static final String FIELD_BROWSER_UUID = "browserUuid";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: ipAddressUuid */
  public static final String FIELD_IP_ADDRESS_UUID = "ipAddressUuid";

  /** constant for field name for: serviceProviderUuid */
  public static final String FIELD_SERVICE_PROVIDER_UUID = "serviceProviderUuid";

  /** constant for field name for: theTimestamp */
  public static final String FIELD_THE_TIMESTAMP = "theTimestamp";

  /** constant for field name for: userAgentUuid */
  public static final String FIELD_USER_AGENT_UUID = "userAgentUuid";

  /** constant for field name for: userUuid */
  public static final String FIELD_USER_UUID = "userUuid";

  /** constant for field name for: userUuidUsingApp */
  public static final String FIELD_USER_UUID_USING_APP = "userUuidUsingApp";
  

  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
      FIELD_ACTION,
      FIELD_BROWSER_UUID,
      FIELD_DELETED_ON,
      FIELD_DESCRIPTION,
      FIELD_IP_ADDRESS_UUID,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_SERVICE_PROVIDER_UUID,
      FIELD_THE_TIMESTAMP,
      FIELD_USER_AGENT_UUID,
      FIELD_USER_UUID,
      FIELD_USER_UUID_USING_APP,
      FIELD_VERSION_NUMBER));
  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_ACTION,
            FIELD_BROWSER_UUID,
            FIELD_DELETED_ON,
            FIELD_DESCRIPTION,
            FIELD_IP_ADDRESS_UUID,
            FIELD_LAST_UPDATED,
            FIELD_UUID,
            FIELD_SERVICE_PROVIDER_UUID,
            FIELD_THE_TIMESTAMP,
            FIELD_USER_AGENT_UUID,
            FIELD_USER_UUID,
            FIELD_USER_UUID_USING_APP,
            FIELD_VERSION_NUMBER));
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_ACTION,
            FIELD_BROWSER_UUID,
            FIELD_DELETED_ON,
            FIELD_DESCRIPTION,
            FIELD_IP_ADDRESS_UUID,
            FIELD_LAST_UPDATED,
            FIELD_UUID,
            FIELD_SERVICE_PROVIDER_UUID,
            FIELD_THE_TIMESTAMP,
            FIELD_USER_AGENT_UUID,
            FIELD_USER_UUID,
            FIELD_USER_UUID_USING_APP,
            FIELD_VERSION_NUMBER));
  
  /**
   * foreign key to the user
   * USER_UUID              VARCHAR2(40 CHAR),
   * @return the userUuid
   */
  public String getUserUuid() {
    return this.userUuid;
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
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    return TwoFactorServerUtils.clone(this, CLONE_FIELDS);
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorAudit dbTwoFactorAudit = (TwoFactorAudit)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(dbTwoFactorAudit, this, DB_VERSION_FIELDS);
    
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    return DB_NEEDS_UPDATE_FIELDS;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbVersionReset()
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.assignDbVersion(TwoFactorServerUtils.clone(this, DB_VERSION_FIELDS));
  
  }
}
=======
/**
 * @author mchyzer
 * $Id: TwoFactorAudit.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * two factor audit object tied to audit db table
 */
public class TwoFactorAudit extends TwoFactorHibernateBeanBase {

  /**
   * truncate fields for db
   */
  public void truncate() {
    this.description = TwoFactorServerUtils.truncateAscii(this.description, 1000);
  }

  /**
   * create and store an audit
   * @param twoFactorDaoFactory 
   * @param twoFactorAuditAction 
   * @param ipAddress 
   * @param userAgent 
   * @param userUuidOperatingOn
   * @param userUuidLoggedIn 
   * @return the audit
   */
  public static TwoFactorAudit createAndStore(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent, String userUuidOperatingOn, 
      String userUuidLoggedIn) {
    return createAndStore(twoFactorDaoFactory, twoFactorAuditAction, ipAddress, 
        userAgent, userUuidOperatingOn, userUuidLoggedIn, null);
  }

  /**
   * create and store an audit
   * @param twoFactorDaoFactory 
   * @param twoFactorAuditAction 
   * @param ipAddress 
   * @param userAgent 
   * @param userUuidOperatingOn
   * @param userUuidLoggedIn 
   * @param description 
   * @return the audit
   */
  public static TwoFactorAudit createAndStore(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent, String userUuidOperatingOn, 
      String userUuidLoggedIn, String description) {
    
    TwoFactorAudit twoFactorAudit = create(twoFactorDaoFactory, twoFactorAuditAction,
        ipAddress, userAgent, userUuidOperatingOn, userUuidLoggedIn);
    twoFactorAudit.setDescription(description);
    twoFactorAudit.store(twoFactorDaoFactory);
    return twoFactorAudit;
  }


  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorAudit.class);

  
  /**
   * thread to store audits from WS
   */
  private static Thread storeThread = new Thread(new Runnable() {

    @Override
    public void run() {
      
      while (true) {
        try {
          
          //process items
          while (!twoFactorAuditFailsafes.isEmpty()) {

            TwoFactorAuditFailsafe twoFactorAuditFailsafe = twoFactorAuditFailsafes.element();
            //try to store
            TwoFactorAudit twoFactorAudit = create(twoFactorAuditFailsafe.getTwoFactorDaoFactory(), 
                twoFactorAuditFailsafe.getTwoFactorAuditAction(),
                twoFactorAuditFailsafe.getIpAddress(),
                twoFactorAuditFailsafe.getUserAgent(),
                twoFactorAuditFailsafe.getUserUuid(), 
                twoFactorAuditFailsafe.getUserUuidUsingApp());
            twoFactorAudit.assignServiceProvider(twoFactorAuditFailsafe.getTwoFactorDaoFactory(), 
                twoFactorAuditFailsafe.getServiceProviderId(), twoFactorAuditFailsafe.getServiceProviderName());
            twoFactorAudit.setDescription(twoFactorAuditFailsafe.getDescription());
            twoFactorAudit.store(twoFactorAuditFailsafe.getTwoFactorDaoFactory());
            
            //not sure why wouldnt be equal
            synchronized (TwoFactorAudit.class) {
              if (twoFactorAuditFailsafe != twoFactorAuditFailsafes.removeFirst()) {
                throw new RuntimeException("Why is this not equal??? ");
              }
            }            
          }
            
          try {
            synchronized (twoFactorAuditFailsafes) {
              //note, the 60000 isnt really required, but just to wake up and see if there is something to do
              twoFactorAuditFailsafes.wait(60000);
            }
          } catch (InterruptedException ignored) {
          }
          
        } catch (Throwable t) {
          LOG.error("Error in storeThread", t);
          //lets pause for a little while to not hammer the DB
          TwoFactorServerUtils.sleep(5000);
        }
      }
    }
    
  });
  
  /**
   * linked list of audits to save to DB
   */
  private static LinkedList<TwoFactorAuditFailsafe> twoFactorAuditFailsafes = new LinkedList<TwoFactorAuditFailsafe>();
  
  /**
   * bean to put on queue to store in another thread
   */
  private static class TwoFactorAuditFailsafe {
    
    /**
     * user uuid who is using the app
     */
    private String userUuidUsingApp;
    
    /**
     * user uuid who is using the app
     * @return the userUuidUsingApp
     */
    public String getUserUuidUsingApp() {
      return this.userUuidUsingApp;
    }
    
    /**
     * user uuid who is using the app
     * @param userUuidUsingApp1 the userUuidUsingApp to set
     */
    public void setUserUuidUsingApp(String userUuidUsingApp1) {
      this.userUuidUsingApp = userUuidUsingApp1;
    }

    /**
     * service provider id
     */
    private String serviceProviderId;
    
    /**
     * service provider name
     */
    private String serviceProviderName;
    
    
    /**
     * service provider id
     * @return the serviceProviderId
     */
    public String getServiceProviderId() {
      return this.serviceProviderId;
    }

    
    /**
     * service provider id
     * @param serviceProviderId1 the serviceProviderId to set
     */
    public void setServiceProviderId(String serviceProviderId1) {
      this.serviceProviderId = serviceProviderId1;
    }

    
    /**
     * service provider name
     * @return the serviceProviderName
     */
    public String getServiceProviderName() {
      return this.serviceProviderName;
    }

    
    /**
     * service provider name
     * @param serviceProviderName1 the serviceProviderName to set
     */
    public void setServiceProviderName(String serviceProviderName1) {
      this.serviceProviderName = serviceProviderName1;
    }

    /**
     * dao factory
     */
    private TwoFactorDaoFactory twoFactorDaoFactory;
    
    /**
     * dao factory
     * @return the twoFactorDaoFactory
     */
    public TwoFactorDaoFactory getTwoFactorDaoFactory() {
      return this.twoFactorDaoFactory;
    }
    
    /**
     * dao factory
     * @param twoFactorDaoFactory1 the twoFactorDaoFactory to set
     */
    public void setTwoFactorDaoFactory(TwoFactorDaoFactory twoFactorDaoFactory1) {
      this.twoFactorDaoFactory = twoFactorDaoFactory1;
    }
    
    /**
     * action
     */
    private TwoFactorAuditAction twoFactorAuditAction; 
    
    /**
     * action
     * @return the twoFactorAuditAction
     */
    public TwoFactorAuditAction getTwoFactorAuditAction() {
      return this.twoFactorAuditAction;
    }
    
    /**
     * action
     * @param twoFactorAuditAction1 the twoFactorAuditAction to set
     */
    public void setTwoFactorAuditAction(TwoFactorAuditAction twoFactorAuditAction1) {
      this.twoFactorAuditAction = twoFactorAuditAction1;
    }
    
    /**
     * ip address
     */
    private String ipAddress;
    
    /**
     * ip address
     * @return the ipAddress
     */
    public String getIpAddress() {
      return this.ipAddress;
    }

    /**
     * ip address
     * @param ipAddress1 the ipAddress to set
     */
    public void setIpAddress(String ipAddress1) {
      this.ipAddress = ipAddress1;
    }
    
    /**
     * user agent
     */
    private String userAgent; 
    
    /**
     * user agent
     * @return the userAgent
     */
    public String getUserAgent() {
      return this.userAgent;
    }
    
    /**
     * user agent
     * @param userAgent1 the userAgent to set
     */
    public void setUserAgent(String userAgent1) {
      this.userAgent = userAgent1;
    }
    
    /**
     * user uuid
     */
    private String userUuid;
    
    /**
     * user uuid
     * @return the userUuid
     */
    public String getUserUuid() {
      return this.userUuid;
    }

    /**
     * user uuid
     * @param userUuid1 the userUuid to set
     */
    public void setUserUuid(String userUuid1) {
      this.userUuid = userUuid1;
    }
    
    /**
     * description
     */
    private String description;
    
    /**
     * description
     * @return the description
     */
    public String getDescription() {
      return this.description;
    }
    
    /**
     * description
     * @param description1 the description to set
     */
    public void setDescription(String description1) {
      this.description = description1;
    }    
    
  }

  /**
   * store thread is started?
   */
  private static boolean storeThreadStarted = false;
  
  /**
   * start the store thread if not started
   */
  private static void storeThreadStartIfNotStarted() {
    if (!storeThreadStarted) {
      synchronized (TwoFactorAudit.class) {
        if (!storeThreadStarted) {
          storeThread.setDaemon(true);
          storeThread.start();
          storeThreadStarted = true;
        }
      }
    }
  }
  
  /**
   * create and store an audit in new thread without giving error (for web service), doesnt block
   * @param twoFactorDaoFactory 
   * @param twoFactorAuditAction 
   * @param ipAddress 
   * @param userAgent 
   * @param description
   * @param userUuidLoggedIn 
   * @param serviceProviderId 
   * @param serviceProviderName 
   * @param userUuidUsingApp
   */
  public static void createAndStoreFailsafe(TwoFactorDaoFactory twoFactorDaoFactory, 
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent, String userUuidLoggedIn, String description,
      String serviceProviderId, String serviceProviderName, String userUuidUsingApp
      ) {
    
    storeThreadStartIfNotStarted();
    
    TwoFactorAuditFailsafe twoFactorAuditFailsafe = new TwoFactorAuditFailsafe();
    twoFactorAuditFailsafe.setDescription(description);
    twoFactorAuditFailsafe.setIpAddress(ipAddress);
    twoFactorAuditFailsafe.setTwoFactorAuditAction(twoFactorAuditAction);
    twoFactorAuditFailsafe.setTwoFactorDaoFactory(twoFactorDaoFactory);
    twoFactorAuditFailsafe.setUserAgent(userAgent);
    twoFactorAuditFailsafe.setUserUuid(userUuidLoggedIn);
    twoFactorAuditFailsafe.setUserUuidUsingApp(userUuidUsingApp);
    twoFactorAuditFailsafe.setServiceProviderId(serviceProviderId);
    twoFactorAuditFailsafe.setServiceProviderName(serviceProviderName);
    synchronized (TwoFactorAudit.class) {
      twoFactorAuditFailsafes.add(twoFactorAuditFailsafe);
    }
    synchronized (twoFactorAuditFailsafes) {
      twoFactorAuditFailsafes.notify();
    }
  }


  /**
   * @param twoFactorDaoFactory
   * @param twoFactorAuditAction
   * @param ipAddress
   * @param userAgent
   * @param userUuidOperatingOn
   * @param userUuidLoggedIn 
   * @return the audit object to augment or store
   */
  public static TwoFactorAudit create(TwoFactorDaoFactory twoFactorDaoFactory,
      TwoFactorAuditAction twoFactorAuditAction, String ipAddress, String userAgent,
      String userUuidOperatingOn,  String userUuidLoggedIn) {
    TwoFactorAudit twoFactorAudit = new TwoFactorAudit();
    twoFactorAudit.setUuid(TwoFactorServerUtils.uuid());
    twoFactorAudit.setAction(twoFactorAuditAction.name());
    twoFactorAudit.assignIpAddress(twoFactorDaoFactory, ipAddress);
    twoFactorAudit.assignUserAgent(twoFactorDaoFactory, userAgent);
    twoFactorAudit.setTheTimestamp(System.currentTimeMillis());
    twoFactorAudit.setUserUuid(userUuidOperatingOn);
    twoFactorAudit.setUserUuidUsingApp(userUuidLoggedIn);
    return twoFactorAudit;
  }
  
  /**
   * retrieve a list of audits for a particular user
   * @param twoFactorDaoFactory
   * @param userUuid
   * @param tfQueryOptions
   * @return the list
   */
  public static List<TwoFactorAuditView> retrieveByUser(final TwoFactorDaoFactory twoFactorDaoFactory, 
      String userUuid, TfQueryOptions tfQueryOptions) {
    return twoFactorDaoFactory.getTwoFactorAudit().retrieveByUser(userUuid, tfQueryOptions);
  }

  /**
   * retrieve an audit for a uuid
   * @param twoFactorDaoFactory
   * @param uuid
   * @return the list
   */
  public static TwoFactorAudit retrieveByUuid(final TwoFactorDaoFactory twoFactorDaoFactory, 
      String uuid) {
    return twoFactorDaoFactory.getTwoFactorAudit().retrieveByUuid(uuid);
  }

  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;

  /**
   * delete this audit
   * @param twoFactorDaoFactory
   */
  @Override
  public void delete(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    twoFactorDaoFactory.getTwoFactorAudit().delete(this);
    testDeletes++;
  
  }

  /**
   * 
   * @param twoFactorDaoFactory
   */
  public void store(TwoFactorDaoFactory twoFactorDaoFactory) {

    if (StringUtils.isBlank(this.action)) {
      throw new RuntimeException("action is null");
    }
    
    if (this.action != null && this.action.length() > 30) {
      throw new RuntimeException("action is too long (30): '" + this.action + "'");
    }
    
    //max length if 1000, chop off some for invalid chars
    this.truncate();
    
    twoFactorDaoFactory.getTwoFactorAudit().store(this);
    testInsertsAndUpdates++;

  }
  
  /**
   * number of deletes
   */
  static int testDeletes = 0;


  
  /**
   * foreign key to the user agent
   * USER_AGENT_UUID        VARCHAR2(40 CHAR),
   */
  private String userAgentUuid;
  
  
  /**
   * foreign key to the user agent
   * USER_AGENT_UUID        VARCHAR2(40 CHAR),
   * @return the userAgentUuid
   */
  public String getUserAgentUuid() {
    return this.userAgentUuid;
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
   * foreign key to the service provider
   * SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
   */
  private String serviceProviderUuid;
  
  /**
   * foreign key to the service provider
   * SERVICE_PROVIDER_UUID  VARCHAR2(40 CHAR),
   * @return the serviceProviderUuid
   */
  public String getServiceProviderUuid() {
    return this.serviceProviderUuid;
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
   * foreign key to the ip address
   * IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
   */
  private String ipAddressUuid;
  
  
  /**
   * foreign key to the ip address
   * IP_ADDRESS_UUID        VARCHAR2(40 CHAR),
   * @return the ipAddressUuid
   */
  public String getIpAddressUuid() {
    return this.ipAddressUuid;
  }

  /**
   * assign an ip addres to the audit record
   * @param twoFactorDaoFactory
   * @param ipAddress
   */
  public void assignIpAddress(TwoFactorDaoFactory twoFactorDaoFactory, String ipAddress) {
    TwoFactorIpAddress twoFactorIpAddress = StringUtils.isBlank(ipAddress) ? null : 
        TwoFactorIpAddress.retrieveByIpAddressOrCreate(
        twoFactorDaoFactory, ipAddress);
    if (twoFactorIpAddress == null) {
      this.ipAddressUuid = null;
    } else {
      this.ipAddressUuid = twoFactorIpAddress.getUuid();
    }
  }
  
  /**
   * assign a user agent to an audit record
   * @param twoFactorDaoFactory
   * @param userAgent
   */
  public void assignUserAgent(TwoFactorDaoFactory twoFactorDaoFactory, String userAgent) {
    TwoFactorUserAgent twoFactorUserAgent = StringUtils.isBlank(userAgent) ? null : 
        TwoFactorUserAgent.retrieveByUserAgentOrCreate(
        twoFactorDaoFactory, userAgent);
    if (twoFactorUserAgent == null) {
      this.userAgentUuid = null;
    } else {
      this.userAgentUuid = twoFactorUserAgent.getUuid();
    }
  }
  
  /**
   * assign a service provider to an audit record
   * @param twoFactorDaoFactory
   * @param serviceProviderId
   * @param serviceProviderName
   */
  public void assignServiceProvider(TwoFactorDaoFactory twoFactorDaoFactory, String serviceProviderId, String serviceProviderName) {
    TwoFactorServiceProvider twoFactorServiceProvider = StringUtils.isBlank(serviceProviderId) ? null : 
        TwoFactorServiceProvider.retrieveByServiceProviderIdOrCreate(
        twoFactorDaoFactory, serviceProviderId, serviceProviderName);
    if (twoFactorServiceProvider == null) {
      this.serviceProviderUuid = null;
    } else {
      this.serviceProviderUuid = twoFactorServiceProvider.getUuid();
    }
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
   * if a description is needed, this is more info
   * DESCRIPTION            VARCHAR2(100 CHAR),
   */
  private String description;
  
  
  /**
   * if a description is needed, this is more info
   * DESCRIPTION            VARCHAR2(100 CHAR),
   * @return the description
   */
  public String getDescription() {
    return this.description;
  }

  
  /**
   * if a description is needed, this is more info
   * DESCRIPTION            VARCHAR2(100 CHAR),
   * @param description1 the description to set
   */
  public void setDescription(String description1) {
    this.description = description1;
  }

  /**
   * user uuid using the application
   */
  private String userUuidUsingApp;
  
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
   * ACTION                 VARCHAR2(30 CHAR)      NOT NULL,
   * action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, 
   * NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, 
   * INVALIDATE_PASSWORDS, GENERATE_PASSWORDS
   */
  private String action;
  
  
  /**
   * action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, 
   * NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, 
   * INVALIDATE_PASSWORDS, GENERATE_PASSWORDS
   * 
   * ACTION                 VARCHAR2(30 CHAR)      NOT NULL,
   * @return the action
   */
  public String getAction() {
    return this.action;
  }

  
  /**
   * action that occurred: AUTHN_TWO_FACTOR, AUTHN_TRUSTED_BROWSER, 
   * NOT_OPTED_IN, OPTIN_TWO_FACTOR, OPTOUT_TWO_FACTOR, WRONG_PASSWORD, 
   * INVALIDATE_PASSWORDS, GENERATE_PASSWORDS
   * ACTION                 VARCHAR2(30 CHAR)      NOT NULL,
   * @param action1 the action to set
   */
  public void setAction(String action1) {
    this.action = action1;
  }

  /**
   * timestamp of this record
   * THE_TIMESTAMP          INTEGER                NOT NULL,
   */
  private long theTimestamp;
  
  
  /**
   * timestamp of this record
   * THE_TIMESTAMP          INTEGER                NOT NULL,
   * @return the theTimestamp
   */
  public long getTheTimestamp() {
    return this.theTimestamp;
  }

  
  /**
   * timestamp of this record
   * THE_TIMESTAMP          INTEGER                NOT NULL,
   * @param theTimestamp1 the theTimestamp to set
   */
  public void setTheTimestamp(long theTimestamp1) {
    this.theTimestamp = theTimestamp1;
  }

  /**
   * foreign key to the browser
   * BROWSER_UUID           VARCHAR2(40 CHAR),
   */
  private String browserUuid;
  
  
  
  
  /**
   * foreign key to the browser
   * BROWSER_UUID           VARCHAR2(40 CHAR),
   * @return the browserUuid
   */
  public String getBrowserUuid() {
    return this.browserUuid;
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
   * foreign key to the user
   * USER_UUID              VARCHAR2(40 CHAR),
   */
  private String userUuid;

  /** constant for field name for: action */
  public static final String FIELD_ACTION = "action";
  
  /** constant for field name for: browserUuid */
  public static final String FIELD_BROWSER_UUID = "browserUuid";

  /** constant for field name for: description */
  public static final String FIELD_DESCRIPTION = "description";

  /** constant for field name for: ipAddressUuid */
  public static final String FIELD_IP_ADDRESS_UUID = "ipAddressUuid";

  /** constant for field name for: serviceProviderUuid */
  public static final String FIELD_SERVICE_PROVIDER_UUID = "serviceProviderUuid";

  /** constant for field name for: theTimestamp */
  public static final String FIELD_THE_TIMESTAMP = "theTimestamp";

  /** constant for field name for: userAgentUuid */
  public static final String FIELD_USER_AGENT_UUID = "userAgentUuid";

  /** constant for field name for: userUuid */
  public static final String FIELD_USER_UUID = "userUuid";

  /** constant for field name for: userUuidUsingApp */
  public static final String FIELD_USER_UUID_USING_APP = "userUuidUsingApp";
  

  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
      FIELD_ACTION,
      FIELD_BROWSER_UUID,
      FIELD_DELETED_ON,
      FIELD_DESCRIPTION,
      FIELD_IP_ADDRESS_UUID,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_SERVICE_PROVIDER_UUID,
      FIELD_THE_TIMESTAMP,
      FIELD_USER_AGENT_UUID,
      FIELD_USER_UUID,
      FIELD_USER_UUID_USING_APP,
      FIELD_VERSION_NUMBER));
  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_ACTION,
            FIELD_BROWSER_UUID,
            FIELD_DELETED_ON,
            FIELD_DESCRIPTION,
            FIELD_IP_ADDRESS_UUID,
            FIELD_LAST_UPDATED,
            FIELD_UUID,
            FIELD_SERVICE_PROVIDER_UUID,
            FIELD_THE_TIMESTAMP,
            FIELD_USER_AGENT_UUID,
            FIELD_USER_UUID,
            FIELD_USER_UUID_USING_APP,
            FIELD_VERSION_NUMBER));
  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_ACTION,
            FIELD_BROWSER_UUID,
            FIELD_DELETED_ON,
            FIELD_DESCRIPTION,
            FIELD_IP_ADDRESS_UUID,
            FIELD_LAST_UPDATED,
            FIELD_UUID,
            FIELD_SERVICE_PROVIDER_UUID,
            FIELD_THE_TIMESTAMP,
            FIELD_USER_AGENT_UUID,
            FIELD_USER_UUID,
            FIELD_USER_UUID_USING_APP,
            FIELD_VERSION_NUMBER));
  
  /**
   * foreign key to the user
   * USER_UUID              VARCHAR2(40 CHAR),
   * @return the userUuid
   */
  public String getUserUuid() {
    return this.userUuid;
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
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#clone()
   */
  @Override
  public TwoFactorHibernateBeanBase clone() {
    return TwoFactorServerUtils.clone(this, CLONE_FIELDS);
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdate()
   */
  @Override
  public boolean dbNeedsUpdate() {
    
    TwoFactorAudit dbTwoFactorAudit = (TwoFactorAudit)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(dbTwoFactorAudit, this, DB_VERSION_FIELDS);
    
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbNeedsUpdateFields()
   */
  @Override
  public Set<String> dbNeedsUpdateFields() {
    return DB_NEEDS_UPDATE_FIELDS;
  }

  /**
   * @see org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase#dbVersionReset()
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.assignDbVersion(TwoFactorServerUtils.clone(this, DB_VERSION_FIELDS));
  
  }
}
>>>>>>> e9b8fcf505079d579d6fccabfbddabd8ecb043f8
