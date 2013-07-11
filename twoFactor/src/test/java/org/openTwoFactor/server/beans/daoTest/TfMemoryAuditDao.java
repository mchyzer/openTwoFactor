/**
 * @author mchyzer
 * $Id: TfMemoryAuditDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorAudit;
import org.openTwoFactor.server.beans.TwoFactorAuditView;
import org.openTwoFactor.server.beans.TwoFactorBrowser;
import org.openTwoFactor.server.beans.TwoFactorIpAddress;
import org.openTwoFactor.server.beans.TwoFactorServiceProvider;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.beans.TwoFactorUserAgent;
import org.openTwoFactor.server.dao.TwoFactorAuditDao;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TfQueryPaging;
import org.openTwoFactor.server.hibernate.TfQuerySort;



/**
 *
 */
public class TfMemoryAuditDao implements TwoFactorAuditDao {

  /**
   * audits
   */
  public static List<TwoFactorAudit> audits = Collections.synchronizedList(new ArrayList<TwoFactorAudit>());
  
  /**
   * 
   * @param twoFactorBrowser
   * @return true if an audit uses a browser
   */
  public static boolean auditUsesBrowser(TwoFactorBrowser twoFactorBrowser) {
    
    for (TwoFactorAudit twoFactorAudit : audits) {
      if (!StringUtils.isBlank(twoFactorBrowser.getUuid()) 
          && StringUtils.equals(twoFactorAudit.getBrowserUuid(), twoFactorBrowser.getUuid())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * 
   * @param twoFactorUserAgent
   * @return true if an audit uses a user agent
   */
  public static boolean auditUsesUserAgent(TwoFactorUserAgent twoFactorUserAgent) {
    
    for (TwoFactorAudit twoFactorAudit : audits) {
      if (!StringUtils.isBlank(twoFactorUserAgent.getUuid()) 
          && StringUtils.equals(twoFactorAudit.getUserAgentUuid(), twoFactorUserAgent.getUuid())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * 
   * @param twoFactorUser
   * @return true if an audit uses a user
   */
  public static boolean auditUsesUser(TwoFactorUser twoFactorUser) {
    
    for (TwoFactorAudit twoFactorAudit : audits) {
      if (!StringUtils.isBlank(twoFactorUser.getUuid()) 
          && StringUtils.equals(twoFactorAudit.getUserUuid(), twoFactorUser.getUuid())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * 
   * @param twoFactorServiceProvider
   * @return true if an audit uses a service provider
   */
  public static boolean auditUsesServiceProvider(TwoFactorServiceProvider twoFactorServiceProvider) {
    
    for (TwoFactorAudit twoFactorAudit : audits) {
      if (!StringUtils.isBlank(twoFactorServiceProvider.getUuid()) 
          && StringUtils.equals(twoFactorAudit.getServiceProviderUuid(), twoFactorServiceProvider.getUuid())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * 
   * @param twoFactorIpAddress
   * @return true if an audit uses an ip address
   */
  public static boolean auditUsesIpAddress(TwoFactorIpAddress twoFactorIpAddress) {
    
    for (TwoFactorAudit twoFactorAudit : audits) {
      if (!StringUtils.isBlank(twoFactorIpAddress.getUuid()) 
          && StringUtils.equals(twoFactorAudit.getIpAddressUuid(), twoFactorIpAddress.getUuid())) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#delete(org.openTwoFactor.server.beans.TwoFactorAudit)
   */
  @Override
  public void delete(TwoFactorAudit twoFactorAudit) {
    
    Iterator<TwoFactorAudit> iterator = audits.iterator();
    while (iterator.hasNext()) {
      TwoFactorAudit current = iterator.next();
      if (current == twoFactorAudit || StringUtils.equals(twoFactorAudit.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
    
  }

  /**
   * create an audit view based on audit
   * @param twoFactorAudit is the input
   * @return the audit view result
   */
  public static TwoFactorAuditView createTwoFactorAuditView(TwoFactorAudit twoFactorAudit) {
    
    TwoFactorAuditView twoFactorAuditView = new TwoFactorAuditView();

    TwoFactorBrowser twoFactorBrowser = StringUtils.isBlank(twoFactorAudit.getBrowserUuid()) ? null : new TfMemoryBrowserDao().retrieveByUuid(twoFactorAudit.getBrowserUuid());
    TwoFactorIpAddress twoFactorIpAddress = StringUtils.isBlank(twoFactorAudit.getIpAddressUuid()) ? null : new TfMemoryIpAddressDao().retrieveByUuid(twoFactorAudit.getIpAddressUuid());
    TwoFactorServiceProvider twoFactorServiceProvider = StringUtils.isBlank(twoFactorAudit.getServiceProviderUuid()) ? null : new TfMemoryServiceProviderDao().retrieveByUuid(twoFactorAudit.getServiceProviderUuid());
    TwoFactorUser twoFactorUser = StringUtils.isBlank(twoFactorAudit.getUserUuid()) ? null : new TfMemoryUserDao().retrieveByUuid(twoFactorAudit.getUserUuid());
    TwoFactorUserAgent twoFactorUserAgent = StringUtils.isBlank(twoFactorAudit.getUserAgentUuid()) ? null : new TfMemoryUserAgentDao().retrieveByUuid(twoFactorAudit.getUserAgentUuid());
    
    twoFactorAuditView.setAction(twoFactorAudit.getAction());
    twoFactorAuditView.setBrowserUuid(twoFactorBrowser == null ? null : twoFactorBrowser.getUuid());
    twoFactorAuditView.setDeletedOn(twoFactorAudit.getDeletedOn());
    twoFactorAuditView.setDescription(twoFactorAudit.getDescription());
    twoFactorAuditView.setDomainName(twoFactorIpAddress == null ? null : twoFactorIpAddress.getDomainName());
    twoFactorAuditView.setIpAddress(twoFactorIpAddress == null ? null : twoFactorIpAddress.getIpAddress());
    twoFactorAuditView.setIpAddressUuid(twoFactorIpAddress == null ? null : twoFactorIpAddress.getUuid());
    twoFactorAuditView.setLastUpdated(twoFactorAudit.getLastUpdated());
    twoFactorAuditView.setLoginid(twoFactorUser == null ? null : twoFactorUser.getLoginid());
    twoFactorAuditView.setServiceProviderId(twoFactorServiceProvider == null ? null : twoFactorServiceProvider.getServiceProviderId());
    twoFactorAuditView.setServiceProviderName(twoFactorServiceProvider == null ? null : twoFactorServiceProvider.getServiceProviderName());
    twoFactorAuditView.setServiceProviderUuid(twoFactorServiceProvider == null ? null : twoFactorServiceProvider.getUuid());
    twoFactorAuditView.setTheTimestamp(twoFactorAudit.getTheTimestamp());
    twoFactorAuditView.setTrustedBrowser(twoFactorBrowser == null ? null : twoFactorBrowser.isTrustedBrowser());
    twoFactorAuditView.setUserAgent(twoFactorUserAgent == null ? null : twoFactorUserAgent.getUserAgent());
    twoFactorAuditView.setUserAgentBrowser(twoFactorUserAgent == null ? null : twoFactorUserAgent.getBrowser());
    twoFactorAuditView.setUserAgentMobile(twoFactorUserAgent == null ? null : twoFactorUserAgent.getMobile());
    twoFactorAuditView.setUserAgentOperatingSystem(twoFactorUserAgent == null ? null : twoFactorUserAgent.getOperatingSystem());
    twoFactorAuditView.setUserAgentUuid(twoFactorUserAgent == null ? null : twoFactorUserAgent.getUuid());
    twoFactorAuditView.setUserUuid(twoFactorUser == null ? null : twoFactorUser.getUuid());
    twoFactorAuditView.setUuid(twoFactorAudit.getUuid());

    return twoFactorAuditView;
    
  }
  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveByUser(java.lang.String, org.openTwoFactor.server.hibernate.TfQueryOptions)
   */
  @Override
  public List<TwoFactorAuditView> retrieveByUser(String userUuid,
      TfQueryOptions tfQueryOptions) {

    List<TwoFactorAuditView> result = new ArrayList<TwoFactorAuditView>();
    
    for (TwoFactorAudit current : audits) {
      if (StringUtils.equals(current.getUserUuid(), userUuid)) {
        result.add(createTwoFactorAuditView(current));
      }
    }
    
    //sort by date
    if (tfQueryOptions != null && tfQueryOptions.getQuerySort() != null) {
      
      TfQuerySort tfQuerySort = tfQueryOptions.getQuerySort();
      
      if (tfQuerySort.isSorting()) {
        if (tfQuerySort.getQuerySortFields().size() != 1) {
          throw new RuntimeException("Cant sort by more than one field");
        }
        if (tfQuerySort.getQuerySortFields().get(0).isAscending()) {
          throw new RuntimeException("Query sort must be descending");
        }
        if (!StringUtils.equals(tfQuerySort.getQuerySortFields().get(0).getColumn(), "the_timestamp")) {
          throw new RuntimeException("Query sort must be on the_timestamp");
        }
        
        Collections.sort(result, new Comparator<TwoFactorAuditView>() {

          /**
           * 
           * @param twoFactorAuditView1
           * @param twoFactorAuditView2
           * @return 1 if more, 0 if equal, -1 if less
           */
          @Override
          public int compare(TwoFactorAuditView twoFactorAuditView1, TwoFactorAuditView twoFactorAuditView2) {
            return twoFactorAuditView1.getTheTimestamp().compareTo(twoFactorAuditView2.getTheTimestamp());
          }
          
        });
        
      }
    }
    if (tfQueryOptions != null && tfQueryOptions.getQueryPaging() != null) {
      
      TfQueryPaging tfQueryPaging = tfQueryOptions.getQueryPaging();
      
      if (tfQueryPaging.isDoTotalCount()) {
        tfQueryPaging.setTotalRecordCount(result.size());
        tfQueryOptions.setCount((long)result.size());
      }
      
      if (tfQueryPaging.getPageNumber() != 1) {
        throw new RuntimeException("Only configured to get first page");
      }
      
      if (tfQueryPaging.getPageSize() < result.size()) {
        for (int i=tfQueryPaging.getPageSize(); i<result.size(); i++) {
          //remove the last one
          result.remove(result.size()-1);
        }
      }
    }
    
    return result;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#store(org.openTwoFactor.server.beans.TwoFactorAudit)
   */
  @Override
  public void store(TwoFactorAudit twoFactorAudit) {
    if (StringUtils.isBlank(twoFactorAudit.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorAudit);

    audits.add(twoFactorAudit);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorAudit retrieveByUuid(String uuid) {
    for (TwoFactorAudit current : audits) {
      if (StringUtils.equals(current.getUuid(), uuid)) {
        return current;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorAudit> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorAudit> result = new ArrayList<TwoFactorAudit>();
    for (TwoFactorAudit current : audits) {
      if (current.isDeleted() && current.getDeletedOn() < selectBeforeThisMilli) {
        result.add(current);
      }
      if (result.size() == 1000) {
        break;
      }
    }
    return result;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorAuditDao#retrieveOlderThanAgeAndActions(java.util.Set, long)
   */
  @Override
  public List<TwoFactorAudit> retrieveOlderThanAgeAndActions(Set<String> actions,
      long selectBeforeThisMilli) {
    List<TwoFactorAudit> result = new ArrayList<TwoFactorAudit>();
    for (TwoFactorAudit current : audits) {
      if (!current.isDeleted() && current.getTheTimestamp() < selectBeforeThisMilli 
          && actions.contains(current.getAction())) {
        result.add(current);
      }
      if (result.size() == 1000) {
        break;
      }
    }
    return result;

  }

  /**
   * @see TwoFactorAuditDao#retrieveCountOptinOptouts(String)
   */
  @Override
  public int retrieveCountOptinOptouts(String userUuid) {
    int count = 0;
    for (TwoFactorAudit current : audits) {
      if ((StringUtils.equals(current.getAction(), "OPTOUT_TWO_FACTOR") || StringUtils.equals(current.getAction(), "OPTIN_TWO_FACTOR"))
          && StringUtils.equals(current.getUserUuid(), userUuid)) { 
        count++;
      }
    }
    return count;
  }

}
