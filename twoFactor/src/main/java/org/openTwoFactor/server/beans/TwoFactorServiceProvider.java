/**
 * @author mchyzer
 * $Id: TwoFactorServiceProvider.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.beans;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
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
 * list of service providers (applications that are requiring the authentication)
 */
public class TwoFactorServiceProvider extends TwoFactorHibernateBeanBase {
  
  /**
   * SERVICE_PROVIDER_NAME  VARCHAR2(100 BYTE),
   * name of the service provider sent from the authn system
   */
  private String serviceProviderName;
  
  
  /**
   * SERVICE_PROVIDER_NAME  VARCHAR2(100 BYTE),
   * name of the service provider sent from the authn system
   * @return the serviceProviderName
   */
  public String getServiceProviderName() {
    return this.serviceProviderName;
  }

  
  /**
   * SERVICE_PROVIDER_NAME  VARCHAR2(100 BYTE),
   * name of the service provider sent from the authn system
   * @param serviceProviderName1 the serviceProviderName to set
   */
  public void setServiceProviderName(String serviceProviderName1) {
    this.serviceProviderName = serviceProviderName1;
  }

  /**
   * SERVICE_PROVIDER_ID    VARCHAR2(100 BYTE)     NOT NULL,
   * id of the service provider sent from authn system
   * The ID should not change
   */
  private String serviceProviderId;

  /**
   * SERVICE_PROVIDER_ID    VARCHAR2(100 BYTE)     NOT NULL,
   * id of the service provider sent from authn system
   * The ID should not change
   * @return the serviceProviderId
   */
  public String getServiceProviderId() {
    return this.serviceProviderId;
  }
  
  /**
   * SERVICE_PROVIDER_ID    VARCHAR2(100 BYTE)     NOT NULL,
   * id of the service provider sent from authn system
   * The ID should not change
   * @param serviceProviderId1 the serviceProviderId to set
   */
  public void setServiceProviderId(String serviceProviderId1) {
    this.serviceProviderId = serviceProviderId1;
  }

  /**
   * number of inserts and updates
   */
  static int testDeletes = 0;

  /** constant for field name for: serviceProviderId */
  public static final String FIELD_SERVICE_PROVIDER_ID = "serviceProviderId";

  /** constant for field name for: serviceProviderName */
  public static final String FIELD_SERVICE_PROVIDER_NAME = "serviceProviderName";
  
  /**
   * fields which are included in clone
   */
  private static final Set<String> CLONE_FIELDS = Collections.unmodifiableSet(
      TwoFactorServerUtils.toSet(
      FIELD_DELETED_ON,
      FIELD_SERVICE_PROVIDER_ID,
      FIELD_SERVICE_PROVIDER_NAME,
      FIELD_LAST_UPDATED,
      FIELD_UUID,
      FIELD_VERSION_NUMBER));

  /**
   * fields which need db update
   */
  private static final Set<String> DB_NEEDS_UPDATE_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_DELETED_ON,
            FIELD_LAST_UPDATED,
            FIELD_SERVICE_PROVIDER_ID,
            FIELD_SERVICE_PROVIDER_NAME,
            FIELD_UUID,
            FIELD_VERSION_NUMBER));

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = Collections.unmodifiableSet(
        TwoFactorServerUtils.toSet(
            FIELD_DELETED_ON,
            FIELD_LAST_UPDATED,
            FIELD_SERVICE_PROVIDER_ID,
            FIELD_SERVICE_PROVIDER_NAME,
            FIELD_UUID,
            FIELD_VERSION_NUMBER));

  /**
   * number of inserts and updates
   */
  static int testInsertsAndUpdates = 0;
  
  /**
   * retrieve an ip address record by ip address or create, retry if problem
   * @param twoFactorDaoFactory
   * @param serviceProviderId
   * @param serviceProviderName 
   * @return the ip address
   */
  public static TwoFactorServiceProvider retrieveByServiceProviderIdOrCreate(final TwoFactorDaoFactory twoFactorDaoFactory, final String serviceProviderId, final String serviceProviderName) {
    try {
      return retrieveByServiceProviderIdOrCreateHelper(twoFactorDaoFactory, serviceProviderId, serviceProviderName);
    } catch (Exception e) {
      LOG.debug("Non-fatal error getting serviceProvider: " + serviceProviderId, e);
      //hmm, error
    }
    //wait some time, maybe someone else created it
    TwoFactorServerUtils.sleep(250 + new SecureRandom().nextInt(250));
    //try again, throw exception if happens
    return retrieveByServiceProviderIdOrCreateHelper(twoFactorDaoFactory, serviceProviderId, serviceProviderName);
  }

  /**
   * make sure the name matches up
   * @param twoFactorDaoFactory 
   * @param newServiceProviderName
   * @return this or the new object if changed
   */
  private TwoFactorServiceProvider fixServiceProviderName(final TwoFactorDaoFactory twoFactorDaoFactory, final String newServiceProviderName) {
    if (StringUtils.equals(this.serviceProviderName, newServiceProviderName)) {
      return this;
    }
    final String theTwoFactorServiceProviderId = this.getServiceProviderId();
    final int loops = 5;
    for (int i=0;i<loops;i++) {
      try {
        final int I = i;
        return (TwoFactorServiceProvider)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, 
            TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
          
          @Override
          public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
            
            TwoFactorServiceProvider twoFactorServiceProvider = null;
            //just use this object if first loop
            if (I != 0) {
              twoFactorServiceProvider = TwoFactorServiceProvider.this;
            } else {
              twoFactorServiceProvider = retrieveByServiceProviderId(twoFactorDaoFactory, theTwoFactorServiceProviderId);
            }
            twoFactorServiceProvider.setServiceProviderName(newServiceProviderName);
            twoFactorServiceProvider.store(twoFactorDaoFactory);
            return twoFactorServiceProvider;
          }
        });
        
      } catch (Exception e) {
        String error = "Error fixing service provider: " + this.getServiceProviderId() + ", " + this.getServiceProviderName();
        if (i != loops-1) {
          //generally ignore
          LOG.info(error, e);
          
        } else {
          throw new RuntimeException(error, e);
        }
      }
      //sleep a random amount o ftime from 250ms to 500ms
      TwoFactorServerUtils.sleep(250 + new SecureRandom().nextInt(250));
    }
    throw new RuntimeException("Should never get here: " + this.getServiceProviderId());
  }
  
  /**
   * retrieve an ip address record by ip address
   * @param twoFactorDaoFactory
   * @param serviceProviderId
   * @param serviceProviderName
   * @return the ip address or null if not found
   */
  private static TwoFactorServiceProvider retrieveByServiceProviderIdOrCreateHelper(final TwoFactorDaoFactory twoFactorDaoFactory, 
      final String serviceProviderId, final String serviceProviderName) {

    TwoFactorServiceProvider twoFactorServiceProvider = retrieveByServiceProviderId(twoFactorDaoFactory, serviceProviderId);

    if (twoFactorServiceProvider != null) {
      twoFactorServiceProvider = twoFactorServiceProvider.fixServiceProviderName(twoFactorDaoFactory, serviceProviderName);
      return twoFactorServiceProvider;
    }

    return (TwoFactorServiceProvider)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_NEW, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorServiceProvider localTwoFactorServiceProvider = new TwoFactorServiceProvider();
        localTwoFactorServiceProvider.setServiceProviderId(serviceProviderId);
        localTwoFactorServiceProvider.setUuid(TwoFactorServerUtils.uuid());
        localTwoFactorServiceProvider.setServiceProviderName(serviceProviderName);
        localTwoFactorServiceProvider.store(twoFactorDaoFactory);
        
        return localTwoFactorServiceProvider;
      }
    });
    
  }


  /**
   * retrieve an ip address record by ip address
   * @param twoFactorDaoFactory
   * @param serviceProviderId
   * @return the ip address or null if not found
   */
  public static TwoFactorServiceProvider retrieveByServiceProviderId(final TwoFactorDaoFactory twoFactorDaoFactory, final String serviceProviderId) {
  
    if (TwoFactorServerUtils.isBlank(serviceProviderId)) {
      throw new RuntimeException("Why is serviceProviderId blank? ");
    }
    
    TwoFactorServiceProvider result = (TwoFactorServiceProvider)HibernateSession.callbackHibernateSession(
        TwoFactorTransactionType.READONLY_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        
        TwoFactorServiceProvider twoFactorIpAddress = twoFactorDaoFactory.getTwoFactorServiceProvider().retrieveByServiceProviderId(serviceProviderId);
        
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
    
    TwoFactorServiceProvider twoFactorIpAddress = (TwoFactorServiceProvider)this.dbVersion();
    
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
  
        twoFactorDaoFactory.getTwoFactorServiceProvider().delete(TwoFactorServiceProvider.this);
        testDeletes++;
        return null;
      }
    });
  
  }


  /**
   * store this object and audit
   * @param twoFactorDaoFactory 
   * @return if changed
   */
  public boolean store(final TwoFactorDaoFactory twoFactorDaoFactory) {
    
    if (StringUtils.isBlank(this.serviceProviderId)) {
      throw new RuntimeException("serviceProviderId is null");
    }
    
    if (StringUtils.isBlank(this.serviceProviderName)) {
      this.serviceProviderName = this.serviceProviderId;
    }

    //note, actual length if 500, with extra space for special chars
    if (this.serviceProviderId != null && this.serviceProviderId.length() > 450) {
      throw new RuntimeException("serviceProviderId is too long (450): '" + this.serviceProviderId + "'");
    }

    //note, actual length if 500, with extra space for special chars
    if (this.serviceProviderName != null && this.serviceProviderName.length() > 450) {
      throw new RuntimeException("serviceProviderName is too long (450): '" + this.serviceProviderName + "'");
    }
    
    
    
    return (Boolean)HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_AUDIT, new HibernateHandler() {
      
      @Override
      public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
        TwoFactorServiceProvider dbVersion = (TwoFactorServiceProvider)TwoFactorServiceProvider.this.dbVersion();

        if (TwoFactorServerUtils.dbVersionDifferent(dbVersion, TwoFactorServiceProvider.this)) {
          twoFactorDaoFactory.getTwoFactorServiceProvider().store(TwoFactorServiceProvider.this);
        
          testInsertsAndUpdates++;
          hibernateSession.misc().flush();
          TwoFactorServiceProvider.this.dbVersionReset();
          return true;
        }

        return false;
      }
    });

  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorServiceProvider.class);
  
}
