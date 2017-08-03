/**
 * @author mchyzer
 * $Id: TwoFactorIpAddress.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.io.File;
import java.net.InetAddress;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.dao.TwoFactorIpAddressDao;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.hibernate.TwoFactorHibernateBeanBase;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * row for each source ip address used in two factor
 */
@SuppressWarnings("serial")
public class TwoFactorIpAddress extends TwoFactorHibernateBeanBase {

  /**
   * IP_ADDRESS             VARCHAR2(45 CHAR)      NOT NULL,
   * ip address (ipv4 or ipv6) of the source
   */
  private String ipAddress;

  
  /**
   * IP_ADDRESS             VARCHAR2(45 CHAR)      NOT NULL,
   * ip address (ipv4 or ipv6) of the source
   * @return the ipAddress
   */
  public String getIpAddress() {
    return this.ipAddress;
  }

  
  /**
   * IP_ADDRESS             VARCHAR2(45 CHAR)      NOT NULL,
   * ip address (ipv4 or ipv6) of the source
   * @param ipAddress1 the ipAddress to set
   */
  public void setIpAddress(String ipAddress1) {
    this.ipAddress = ipAddress1;
  }
  
  /**
   * DOMAIN_NAME            VARCHAR2(80 CHAR),
   * after doing a reverse lookup, domain name if one found
   */
  private String domainName;
  
  /**
   * DOMAIN_NAME            VARCHAR2(80 CHAR),
   * after doing a reverse lookup, domain name if one found
   * @return the domainName
   */
  public String getDomainName() {
    return this.domainName;
  }

  /**
   * DOMAIN_NAME            VARCHAR2(80 CHAR),
   * after doing a reverse lookup, domain name if one found
   * @param domainName1 the domainName to set
   */
  public void setDomainName(String domainName1) {
    this.domainName = domainName1;
  }
  
  /**
   * LOOKED_UP_DOMAIN_NAME  VARCHAR2(1 BYTE)       NOT NULL,
   * T or F if this IP address has been looked up
   */
  private boolean lookedUpDomainName;

  /**
   * number of inserts and updates
   */
  static int testDeletes = 0;

  /** constant for field name for: domainName */
  public static final String FIELD_DOMAIN_NAME = "domainName";

  /** constant for field name for: ipAddress */
  public static final String FIELD_IP_ADDRESS = "ipAddress";

  /** constant for field name for: lookedUpDomainName */
  public static final String FIELD_LOOKED_UP_DOMAIN_NAME = "lookedUpDomainName";
  
  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_DOMAIN_NAME,
      FIELD_IP_ADDRESS,
      FIELD_LAST_UPDATED,
      FIELD_LOOKED_UP_DOMAIN_NAME,
      FIELD_UUID,
      FIELD_VERSION_NUMBER));

  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_DELETED_ON,
            FIELD_DOMAIN_NAME,
            FIELD_IP_ADDRESS,
            FIELD_LAST_UPDATED,
            FIELD_LOOKED_UP_DOMAIN_NAME,
            FIELD_UUID,
            FIELD_VERSION_NUMBER));

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_DELETED_ON,
            FIELD_DOMAIN_NAME,
            FIELD_IP_ADDRESS,
            FIELD_LAST_UPDATED,
            FIELD_LOOKED_UP_DOMAIN_NAME,
            FIELD_UUID,
            FIELD_VERSION_NUMBER));
  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * LOOKED_UP_DOMAIN_NAME  VARCHAR2(1 BYTE)       NOT NULL,
   * T or F if this IP address has been looked up
   * @return the lookedUpDomainName
   */
  public boolean isLookedUpDomainName() {
    return this.lookedUpDomainName;
  }
  
  /**
   * LOOKED_UP_DOMAIN_NAME  VARCHAR2(1 BYTE)       NOT NULL,
   * T or F if this IP address has been looked up
   * @param lookedUpDomainName1 the lookedUpDomainName to set
   */
  public void setLookedUpDomainName(boolean lookedUpDomainName1) {
    this.lookedUpDomainName = lookedUpDomainName1;
  }


  /**
   * retrieve an ip address record by ip address or create, retry if problem
   * @param twoFactorDaoFactory
   * @param ipAddress
   * @return the ip address
   */
  public static TwoFactorIpAddress retrieveByIpAddressOrCreate(final TwoFactorDaoFactory twoFactorDaoFactory, final String ipAddress) {

    final int LOOP_COUNT = 5;
    
    //if two threads create it at the same time, then retrieve again
    for (int i=0;i<LOOP_COUNT;i++) {

      try {
        //exception on error if last one
        TwoFactorIpAddress twoFactorIpAddress = retrieveByIpAddressOrCreateHelper(twoFactorDaoFactory, ipAddress, i == LOOP_COUNT-1 ? true : false);
        if (twoFactorIpAddress != null) {
          return twoFactorIpAddress;
        }
      } catch (RuntimeException e) {
        
        if (LOG.isDebugEnabled()) {
          LOG.debug("Error in IP address: " + ipAddress, e);
        }
        
        if (i==LOOP_COUNT-1) {
          throw e;
        }
        
      }
      //wait some time, maybe someone else created it
      TwoFactorServerUtils.sleep((i+1) * 3000);
      
    }
    throw new RuntimeException("Why are we here?");

  }

  /**
   * retrieve an ip address record by ip address
   * @param twoFactorDaoFactory
   * @param ipAddress
   * @param exceptionOnError
   * @return the ip address or null if not found
   */
  private static TwoFactorIpAddress retrieveByIpAddressOrCreateHelper(final TwoFactorDaoFactory twoFactorDaoFactory, final String ipAddress, final boolean exceptionOnError) {

    TwoFactorIpAddress twoFactorIpAddress = retrieveByIpAddress(twoFactorDaoFactory, ipAddress);
    
    if (twoFactorIpAddress != null) {
      return twoFactorIpAddress;
    }
    
    TwoFactorIpAddress localTwoFactorIpAddress = new TwoFactorIpAddress();
    localTwoFactorIpAddress.setIpAddress(ipAddress);
    localTwoFactorIpAddress.setUuid(TwoFactorServerUtils.uuid());
    localTwoFactorIpAddress.calculateDomainName();
    Boolean changed = localTwoFactorIpAddress.store(twoFactorDaoFactory, exceptionOnError);
    
    //if not successful, null
    if (changed == null) {
      return null;
    }
    
    
    return localTwoFactorIpAddress;
  }


  /**
   * retrieve an ip address record by ip address
   * @param twoFactorDaoFactory
   * @param ipAddress
   * @return the ip address or null if not found
   */
  public static TwoFactorIpAddress retrieveByIpAddress(final TwoFactorDaoFactory twoFactorDaoFactory, final String ipAddress) {
  
    if (TwoFactorServerUtils.isBlank(ipAddress)) {
      throw new RuntimeException("Why is ipAddress blank? ");
    }
    
    TwoFactorIpAddress result = (TwoFactorIpAddress)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorIpAddressDao twoFactorIpAddressDao = twoFactorDaoFactory.getTwoFactorIpAddress();
        TwoFactorIpAddress twoFactorIpAddress = twoFactorIpAddressDao.retrieveByIpAddress(ipAddress);
        
        return twoFactorIpAddress;
      }
    });
    
    return result;
    
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
    
    TwoFactorIpAddress twoFactorIpAddress = (TwoFactorIpAddress)this.dbVersion();
    
    return TwoFactorServerUtils.dbVersionDifferent(twoFactorIpAddress, this, DB_VERSION_FIELDS);
    
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


  /**
   * delete this record
   * @param twoFactorDaoFactory is the factor to use
   */
  @Override
  public void delete(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
        if (!HibernateSession.isReadonlyMode()) {
          twoFactorDaoFactory.getTwoFactorIpAddress().delete(TwoFactorIpAddress.this);
        }
        testDeletes++;
        return null;
      }
    });
  
  }


  /**
   * store this object and audit
   * @param twoFactorDaoFactory
   * @param exceptionOnError
   * @return if changed, or null on error
   */
  public Boolean store(final TwoFactorDaoFactory twoFactorDaoFactory, final boolean exceptionOnError) {
    
    if (StringUtils.isBlank(this.ipAddress)) {
      throw new RuntimeException("ipAddress is null");
    }
    
    if (this.ipAddress != null && this.ipAddress.length() > 60) {
      throw new RuntimeException("ipAddress is too long (60): '" + this.ipAddress + "'");
    }
    
    //note, this is size 200, but with utf8 chars, maybe it is bigger in the db?
    this.domainName = StringUtils.abbreviate(this.domainName, 175);
    
    TwoFactorIpAddress dbVersion = (TwoFactorIpAddress)this.dbVersion();

    if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, this)) {
      boolean success = true;

      if (!HibernateSession.isReadonlyMode()) {

        success = twoFactorDaoFactory.getTwoFactorIpAddress().store(this, exceptionOnError);
        
      }
      
      if (!success) {
        return null;
      }
      testInsertsAndUpdates++;
      this.dbVersionReset();
      return true;
    }

    return false;

  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorIpAddress.class);

  /**
   * [appadmin@theprince WEB-INF]$ java -cp ./classes:./lib/*:/opt/appserv/tomcat6_18base/lib/* org.openTwoFactor.server.beans.TwoFactorIpAddress
   * @param args
   */
  public static void main(String[] args) {
    
    String ipAddress = "96.245.109.183";

    if (args.length == 1) {
      ipAddress = args[0];
    }
//    
//    TwoFactorIpAddress twoFactorIpAddress = new TwoFactorIpAddress();
//    twoFactorIpAddress.setIpAddress(ipAddress);
//    twoFactorIpAddress.calculateDomainName();
//    System.out.println(twoFactorIpAddress.getDomainName());
    
    //twoFactorServer.windows.nslookup = C:/Windows/System32/nslookup.exe
    //twoFactorServer.windows.nslookupRegex = Name:\\s+([^\\s]+)
    //twoFactorServer.nonwindows.nslookup = /usr/bin/nslookup
    //twoFactorServer.nonwindows.nslookupRegex = in-addr\.arpa\s+name\s+=\s+([^\s]+)

//    String output = "Server:         172.20.5.53\n"
//      + "Address:        172.20.5.53#53\n"
//      + "\n"
//      + "Non-authoritative answer:\n"
//      + "213.224.91.128.in-addr.arpa     name = flash.isc-seo.upenn.edu.\n"
//      + "\n"
//      + "Authoritative answers can be found from:";
    
    System.out.println("1. " + nslookupExecute(ipAddress, null));
    
    TwoFactorIpAddress twoFactorIpAddress = new TwoFactorIpAddress();
    twoFactorIpAddress.setIpAddress(ipAddress);
    twoFactorIpAddress.calculateDomainName();
    System.out.println("2. " + twoFactorIpAddress.getDomainName());
    
    
  }

  /**
   * ip address pattern for valid ipv6 or ipv4 address
   */
  private static Pattern ipAddressPattern = Pattern.compile("[0-9\\.:a-fA-F]+");
  
  /**
   * calculate the domain name from ip address
   */
  public void calculateDomainName() {
    
    Map<String, Object> debugMap = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    
    try {
      
      this.lookedUpDomainName = true;
      
      //lets validate the ip address
      this.ipAddress = TwoFactorServerUtils.trimToNull(this.ipAddress);

      if (LOG.isDebugEnabled()) {
        debugMap.put("ipAddress", this.ipAddress);
      }
      
      //validate IP address
      boolean validIpAddress = !StringUtils.isBlank(this.ipAddress) && ipAddressPattern.matcher(this.ipAddress).matches();

      if (LOG.isDebugEnabled()) {
        debugMap.put("validIpAddress", validIpAddress);
      }
      
      if (validIpAddress) {
        
        InetAddress ia = null; //InetAddress.getByAddress(new byte[] {(byte)130,91,(byte)219,(byte)176});
        //ia = InetAddress.getByAddress(new byte[] {(byte)176, (byte)219, 91, (byte)130});
        // or 
        //do it this way so it works with ipv6
        ia = InetAddress.getByName(this.ipAddress);
        if (ia != null) {
          this.domainName = ia.getCanonicalHostName();

          if (LOG.isDebugEnabled()) {
            debugMap.put("canonicalHostName", this.domainName);
          }

          if (StringUtils.equals(this.domainName, this.ipAddress)) {
            this.domainName = null;
          }

          if (StringUtils.isBlank(this.domainName)) {
            this.domainName = ia.getHostName();

            if (LOG.isDebugEnabled()) {
              debugMap.put("hostName", this.domainName);
            }
          }

          if (StringUtils.equals(this.domainName, this.ipAddress)) {
            this.domainName = null;
          }
        }
        if (StringUtils.isBlank(this.domainName)) {
          
          this.domainName = nslookupExecute(this.ipAddress, debugMap);

          if (LOG.isDebugEnabled()) {
            debugMap.put("nsLookupExecute", this.domainName);
          }

        }
        if (StringUtils.equals(this.domainName, this.ipAddress)) {
          this.domainName = null;
        }
        //strip the last dot if it is there
        if (!StringUtils.isBlank(this.domainName) && this.domainName.endsWith(".")) {
          this.domainName = this.domainName.substring(0, this.domainName.length()-1);
        }
        //System.out.println(ia.getCanonicalHostName());
      }
      
    } catch (Exception e) {
      //just ignore
      LOG.warn("Error looking up ip address: " + this.ipAddress, e);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(TwoFactorServerUtils.mapToString(debugMap));
      }
    }
  }

  /**
   * do nslookup on an ip address
   * @param ipAddress
   * @return the domain name
   * @param debugMap if debugging
   */
  public static String nslookupExecute(String ipAddress, Map<String, Object> debugMap) {
    
    //avoid command line injection
    if (!ipAddressPattern.matcher(ipAddress).matches()) {
      
      if (debugMap != null) {
        debugMap.put("invalidIpAddress", ipAddress);
      }
      
      return null;
    }
    
    boolean isWindows = TwoFactorServerUtils.isWindows();

    if (debugMap != null) {
      debugMap.put("isWindows", isWindows);
    }
    
    //twoFactorServer.windows.nslookup = C:/Windows/System32/nslookup.exe
    //twoFactorServer.windows.nslookupRegex = Name:\\s+([^\\s]+)
    //twoFactorServer.nonwindows.nslookup = /usr/bin/nslookup
    //twoFactorServer.nonwindows.nslookupRegex = in-addr\.arpa\s+name\s+=\s+([^\s]+)

    String commands = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer." + (isWindows ? "" : "non") + "windows.nslookup");
    
    if (!StringUtils.isBlank(commands)) {

      String regex = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer." + (isWindows ? "" : "non") + "windows.nslookupRegex");

      for (String command : TwoFactorServerUtils.splitTrim(commands, ",")) {
        
        if (new File(command).isFile()) {
          
          if (debugMap != null) {
            debugMap.put("command", command);
          }
          
          try {
            String result = TwoFactorServerUtils.executeProgram(new String[]{command, ipAddress});
  
            //Windows is:
            //Server:  safedns1-svc.security.isc.UPENN.EDU
            //Address:  128.91.19.240
            //
            //Name:    pool-96-245-109-183.phlapa.fios.verizon.net
            //Address:  96.245.109.183
  
            //Non-windows is:
            //Server:         172.20.5.53
            //Address:        172.20.5.53#53
            //
            //Non-authoritative answer:
            //213.224.91.128.in-addr.arpa     name = flash.isc-seo.upenn.edu.
            //
            //Authoritative answers can be found from:
  
            
            Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(result);
            
            boolean regexMatches = matcher.matches();
            
            if (debugMap != null) {
              debugMap.put("regexMatches", regexMatches);
            }
            
            if (regexMatches) {
              String matcherGroup1 = matcher.group(1);
              if (debugMap != null) {
                debugMap.put("matcherGroup1", matcherGroup1);
              }
  
              return matcherGroup1;
            }
            
            LOG.info("Why does regex not match? ipAddress: " + ipAddress 
                + ", regex: '" + regex + "' result: " + result);
          } catch (RuntimeException re) {
            LOG.info("Error running nslookup on '" + ipAddress + "'", re);
          }
          break;
        }
      }
    }
    if (debugMap != null) {
      debugMap.put("returningNull", true);
    }
    return null;
  }
  
}
