/**
 * @author mchyzer
 * $Id: TfSourceUtils.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.cache.TwoFactorCache;
import org.openTwoFactor.server.config.TwoFactorServerConfig;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 *
 */
public class TfSourceUtils {

  /**
   * convert subject id to netid (or subject id if there is no netid)
   * @param subjectId
   * @return the netid or subject id
   */
  public static String convertSubjectIdToNetId(Source source, String subjectId) {
    return convertSubjectIdToNetId(source, subjectId, true);
  }
  
  /**
   * convert subject id to netid (or subject id if there is no netid)
   * @param subjectId
   * @return the netid or subject id
   */
  public static String convertSubjectIdToNetId(Source source, String subjectId, boolean exceptionOnProblem) {
    try {
      String netIdAttribute = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.subject.netIdAttribute");
      if (!StringUtils.isBlank(netIdAttribute)) {
        Subject subject = retrieveSubjectByIdOrIdentifier(source, subjectId, true, false, true);
        if (subject != null) {
          String netId = subject.getAttributeValue(netIdAttribute);
          if (!StringUtils.isBlank(netId)) {
            return netId;
          }
        }
      }
      return subjectId;
    } catch (RuntimeException re) {
      if (exceptionOnProblem) {
        throw re;
      }
    }
    return subjectId;
  }

  /**
   * does the set of subject ids or net ids contain one
   * @param subjectIdOrNetIdOfSubject
   * @return true if in the set
   */
  public static boolean subjectIdOrNetIdInSet(Source source, String subjectIdOrNetIdOfSubject, Set<String> subjectIdsOrNetIds) {

    if (StringUtils.isBlank(subjectIdOrNetIdOfSubject)) {
      return false;
    }
    
    if (TwoFactorServerUtils.length(subjectIdsOrNetIds) == 0) { 
      return false;
    }
    
    if (subjectIdsOrNetIds.contains(subjectIdOrNetIdOfSubject)) {
      return true;
    }
    
    String netIdAttribute = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.subject.netIdAttribute");
    if (!StringUtils.isBlank(netIdAttribute)) {
      
      Subject subject = retrieveSubjectByIdOrIdentifier(source, subjectIdOrNetIdOfSubject, true, false, true);
      if (subject != null) {
        
        if (subjectIdsOrNetIds.contains(subject.getId())) {
          return true;
        }
        
        String netId = subject.getAttributeValue(netIdAttribute);
        if (!StringUtils.isBlank(netId)) {
          if (subjectIdsOrNetIds.contains(netId)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  
  /**
   * get the main live source
   * @return the source
   */
  public static Source mainSource() {
    return SourceManager.getInstance().getSource(SOURCE_NAME);
  }
  
  /**
   * cache the realm, source, and subjectIdOrIdentifier, to the subject
   */
  private static TwoFactorCache<MultiKey, Subject> subjectIdOrIdentifierToSubject = new TwoFactorCache<MultiKey, Subject>(
       TfSourceUtils.class.getName() + ".subjectIdOrIdentifierToSubject");

  /**
   * resolve a subject by id or identifier
   * @param subject
   * @return the subject
   */
  public static String retrieveEmail(Subject subject) {
    
    String emailAttributeName = emailAttributeNameForSource(subject.getSource());
    if (StringUtils.isBlank(emailAttributeName)) {
      return null;
    }
    return subject == null ? null : subject.getAttributeValueSingleValued(emailAttributeName);
    
  }

  /**
   * search the subject source and page the results
   * @param source
   * @param searchString
   * @param isAdmin true if this is an admin query
   */
  public static Set<Subject> searchPage(Source source, String searchString, boolean isAdmin) {
    return TwoFactorServerUtils.nonNull(source.searchPage(searchString, subjectSourceRealm(isAdmin)).getResults());
  }

  /**
   * search the subject source, generally this is only for testing, must page the results in real life
   * @param source
   * @param searchString
   * @param isAdmin true if this is an admin query
   */
  public static Set<Subject> search(Source source, String searchString, boolean isAdmin) {
    return TwoFactorServerUtils.nonNull(source.search(searchString, subjectSourceRealm(isAdmin)));
  }

  /**
   * resolve a subject by id or identifier
   * @param source
   * @param subjectIdOrIdentifier
   * @param exceptionIfNotFound
   * @return the subject
   */
  public static Subject retrieveSubjectByIdOrIdentifier(Source source, String subjectIdOrIdentifier, 
      boolean okToReadFromCache, boolean exceptionIfNotFound, boolean isAdminView) {

    if (source == null) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("source is null when looking up: '" + subjectIdOrIdentifier + "', stack: " 
            + TwoFactorServerUtils.getFullStackTrace(new RuntimeException()));
      }
      return null;
    }
    
    MultiKey multiKey = new MultiKey(subjectSourceRealm(isAdminView), source.getId(), subjectIdOrIdentifier);
    
    Subject subject = okToReadFromCache ? subjectIdOrIdentifierToSubject.get(multiKey) : null;

    if (subject != null) {
      return subject;
    }
    subject = source.getSubjectByIdOrIdentifier(subjectIdOrIdentifier, exceptionIfNotFound, subjectSourceRealm(isAdminView));
        
    if (subject == null) {
      return null;
    }
    //even if you arent reading from cache, you can put it back to cache
    synchronized (TfSourceUtils.class) {
      subjectIdOrIdentifierToSubject.put(multiKey, subject);      
    }
    
    return subject;
    
  }

  /**
   * resolve a subject by id or identifier
   * @param source
   * @param subjectIdOrIdentifier
   * @param isAdminView
   * @return the subject
   */
  public static Subject retrieveSubjectByIdOrIdentifierFromCache(Source source, String subjectIdOrIdentifier, 
      boolean isAdminView) {

    if (source == null) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("source is null when looking up: '" + subjectIdOrIdentifier + "', stack: " 
            + TwoFactorServerUtils.getFullStackTrace(new RuntimeException()));
      }
      return null;
    }
    
    MultiKey multiKey = new MultiKey(subjectSourceRealm(isAdminView), source.getId(), subjectIdOrIdentifier);
    
    Subject subject = subjectIdOrIdentifierToSubject.get(multiKey);

    return subject;
    
  }


  /**
   * resolve subjects by ids or identifiers
   * @param source
   * @param subjectIdOrIdentifiers
   * @param isAdminView
   * @return the subject
   */
  public static Map<String, Subject> retrieveSubjectsByIdsOrIdentifiers(Source source, Collection<String> subjectIdsOrIdentifiers, 
      boolean isAdminView) {

    if (source == null) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("source is null when looking up: '" + TwoFactorServerUtils.toStringForLog(subjectIdsOrIdentifiers, 1000) + "', stack: " 
            + TwoFactorServerUtils.getFullStackTrace(new RuntimeException()));
      }
      return null;
    }
    
    Map<String, Subject> subjectMap = source.getSubjectsByIdsOrIdentifiers(subjectIdsOrIdentifiers, subjectSourceRealm(isAdminView));

    return subjectMap;
    
  }

  /**
   * get the subject source 
   * @param isAdmin
   * @return the realm to use
   */
  public static String subjectSourceRealm(boolean isAdmin) {
    //if isAdmin and using realms
    if (isAdmin && TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.useAdminRealm", false)) {
      return "admin";
    }
    return null;
  }
  
  /**
   * cache the logins in a hash cache.  Key is the realm, and principal
   */
  private static TwoFactorCache<MultiKey, String> principalToLoginId = new TwoFactorCache<MultiKey, String>(
      TfSourceUtils.class.getName() + ".principalToLoginId");

  /**
   * resolve a subject id or netid into a subject id
   * @param idOrIdentifier the subject id or eppn
   * @return the resolved subject id
   */
  public static String resolveSubjectId(Source subjectSource, String idOrIdentifier, boolean isAdmin) {

    String realm = subjectSourceRealm(isAdmin);
    
    MultiKey key = new MultiKey(realm, idOrIdentifier);
    
    String subjectId = principalToLoginId.get(key);
    if (StringUtils.isBlank(subjectId)) {
      
      Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(subjectSource, 
          idOrIdentifier, true, false, isAdmin);
      
      //not caching null...
      if (subject == null) {
        LOG.info("Unresolveable ID: " + idOrIdentifier);
        return idOrIdentifier;
      }
      
      subjectId = subject.getId();

      if (StringUtils.isBlank(subjectId)) {
        throw new RuntimeException("subjectId is blank for id: '" + idOrIdentifier + "'");
      }
      
      principalToLoginId.put(key, subjectId);
      
    }
    return subjectId;
  }

  
  /**
   * convert from subject to description
   * @param subject
   * @param subjectId is what was used to lookup if there is something, could be null if not applicable
   * @return the description, or name, or id
   */
  public static String subjectDescription(Subject subject, String subjectId) {
    
    if (subject == null) {
      return subjectId;
    }
    
    //description could be null?
    String description = subject.getDescription();
    if (StringUtils.isBlank(description)) {
      description = subjectName(subject, subjectId);
    }
    return description;
  }

  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    String emailAttributeName = TfSourceUtils.emailAttributeNameForSource(TfSourceUtils.mainSource());
    
    for (Subject subject : TfSourceUtils.mainSource().searchPage("harvey active=all").getResults()) {
      System.out.println(subject.getName() + ", " + subject.getAttributeValue(emailAttributeName) 
          + ", " + subject.getId() + ", " + subject.getAttributeValue("pennname")
          + ", " + subject.getDescription() + ", active=" + subject.getAttributeValue("active"));
    }
    
//    String emailAttributeName = TfSourceUtils.emailAttributeNameForSource();
//    
//    Subject subject = SourceManager.getInstance()
//      .getSource(TfSourceUtils.SOURCE_NAME).getSubjectByIdOrIdentifier("10021368", false);
//
//    System.out.println(subject.getName() + ", " + subject.getAttributeValue(emailAttributeName) 
//        + ", " + subject.getId() + ", " + subject.getAttributeValue("pennname")
//        + ", " + subject.getDescription() + ", active=" + subject.getAttributeValue("active"));

  }
  
  /**
   * see if subject is active
   * @param subject
   * @return true if subject is active
   */
  public static boolean subjectIsActive(Subject subject) {
    if (!TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("twoFactorServer.subject.filteringInactives", false)) {
      return true;
    }

    String activeStatus = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.subject.activeStatus");
    String statusSubjectAttribute = TwoFactorServerConfig.retrieveConfig().propertyValueStringRequired("twoFactorServer.subject.statusSubjectAttribute");
    
    return StringUtils.equals(activeStatus, subject.getAttributeValue(statusSubjectAttribute));
  }
  
  /**
   * source name for two factor users
   */
  public static final String SOURCE_NAME = "twoFactor";
  
  /**
   * get the subject attribute name for two factor source
   * @return the attribute name
   */
  public static String emailAttributeNameForSource(Source subjectSource) {
    return subjectSource.getInitParam("emailAttributeName");
  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfSourceUtils.class);

  /**
   * convert from subject to name
   * @param subject
   * @return the description, or name, or id
   */
  public static String subjectName(Subject subject) {
    String netIdAttributeName = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.subject.netIdAttribute");
    String netId = null;
    if (!StringUtils.isBlank(netIdAttributeName)) {
      netId = subject.getAttributeValue(netIdAttributeName);
    }
    netId = StringUtils.defaultIfEmpty(netId, subject.getId());
    return subjectName(subject, netId);
  }
  
  /**
   * convert from subject to description
   * @param subject
   * @return the description, or name, or id
   */
  public static String subjectDescription(Subject subject) {
    String netIdAttributeName = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.subject.netIdAttribute");
    String netId = null;
    if (!StringUtils.isBlank(netIdAttributeName)) {
      netId = subject.getAttributeValue(netIdAttributeName);
    }
    netId = StringUtils.defaultIfEmpty(netId, subject.getId());
    return subjectDescription(subject, netId);
  }
  
  /**
   * convert from subject to name
   * @param subject
   * @param subjectId is what was used to lookup if there is something, could be null if not applicable
   * @return the description, or name, or id
   */
  public static String subjectName(Subject subject, String subjectId) {
    
    if (subject == null) {
      return subjectId;
    }
    
    //description could be null?
    String name = subject.getName();
    if (StringUtils.isBlank(name)) {
      String netIdAttribute = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.subject.netIdAttribute");
      if (!StringUtils.isBlank(netIdAttribute)) {
        name = subject.getAttributeValue(netIdAttribute);
      }
    }
    if (StringUtils.isBlank(name)) {
      name = subject.getId();
    }
    return name;
  }

  
}
