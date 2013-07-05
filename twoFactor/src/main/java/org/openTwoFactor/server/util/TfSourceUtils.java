/**
 * @author mchyzer
 * $Id: TfSourceUtils.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;

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
   * cache the source, and subjectIdOrIdentifier, to the subject
   */
  private static TwoFactorCache<MultiKey, Subject> subjectIdOrIdentifierToSubject = new TwoFactorCache<MultiKey, Subject>(
       TfSourceUtils.class.getName() + ".subjectIdOrIdentifierToSubject");

  /**
   * resolve a subject by id or identifier
   * @param subject
   * @return the subject
   */
  public static String retrieveEmail(Subject subject) {
    
    return subject == null ? null : subject.getAttributeValueSingleValued("email");
    
  }
    

  /**
   * resolve a subject by id or identifier
   * @param source
   * @param subjectIdOrIdentifier
   * @param exceptionIfNotFound
   * @return the subject
   */
  public static Subject retrieveSubjectByIdOrIdentifier(Source source, String subjectIdOrIdentifier, 
      boolean okToReadFromCache, boolean exceptionIfNotFound) {

    if (source == null) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("source is null when looking up: '" + subjectIdOrIdentifier + "', stack: " 
            + TwoFactorServerUtils.getFullStackTrace(new RuntimeException()));
      }
      return null;
    }
    
    MultiKey multiKey = new MultiKey(source.getId(), subjectIdOrIdentifier);
    
    Subject subject = okToReadFromCache ? subjectIdOrIdentifierToSubject.get(multiKey) : null;

    if (subject != null) {
      return subject;
    }
    subject = source.getSubjectByIdOrIdentifier(subjectIdOrIdentifier, exceptionIfNotFound);
        
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
   * cache the logins in a hash cache
   */
  private static TwoFactorCache<String, String> principalToLoginId = new TwoFactorCache<String, String>(
      TfSourceUtils.class.getName() + ".principalToLoginId");

  /**
   * resolve a subject id or netid into a subject id
   * @param idOrIdentifier the subject id or eppn
   * @return the resolved subject id
   */
  public static String resolveSubjectId(String idOrIdentifier) {

    String subjectId = principalToLoginId.get(idOrIdentifier);
    if (StringUtils.isBlank(subjectId)) {
      
      Subject subject = TfSourceUtils.retrieveSubjectByIdOrIdentifier(SourceManager.getInstance()
          .getSource(TfSourceUtils.SOURCE_NAME), idOrIdentifier, true, true);
      
      subjectId = subject.getId();

      if (StringUtils.isBlank(subjectId)) {
        throw new RuntimeException("subjectId is blank for id: '" + idOrIdentifier + "'");
      }
      
      principalToLoginId.put(idOrIdentifier, subjectId);
      
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
      description = subject.getName();
    }
    if (StringUtils.isBlank(description)) {
      description = subject.getId();
    }
    return description;
  }

  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    String emailAttributeName = TfSourceUtils.emailAttributeNameForSource();
    
    for (Subject subject : SourceManager.getInstance().getSource(TfSourceUtils.SOURCE_NAME).searchPage("harvey active=all").getResults()) {
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
  public static String emailAttributeNameForSource() {
    Source source = SourceManager.getInstance().getSource(SOURCE_NAME);
    return source.getInitParam("emailAttributeName");
  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfSourceUtils.class);

  
}
