/**
 * @author mchyzer
 * $Id: TfSourceUtils.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.config.TwoFactorServerConfig;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 *
 */
public class TfSourceUtils {

  
  
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
          + ", " + subject.getId() + ", " + subject.getAttributeValue("pennkey")
          + ", " + subject.getDescription() + ", active=" + subject.getAttributeValue("active"));
    }
    
//    String emailAttributeName = TfSourceUtils.emailAttributeNameForSource();
//    
//    Subject subject = SourceManager.getInstance()
//      .getSource(TfSourceUtils.SOURCE_NAME).getSubjectByIdOrIdentifier("10021368", false);
//
//    System.out.println(subject.getName() + ", " + subject.getAttributeValue(emailAttributeName) 
//        + ", " + subject.getId() + ", " + subject.getAttributeValue("pennkey")
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

  
}
