/**
 * @author mchyzer
 * $Id: TfSubjectIdResolver.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.subject;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.cache.TwoFactorCache;
import org.openTwoFactor.server.util.TfSourceUtils;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


/**
 *
 */
public class TfSubjectIdResolver {

  /**
   * cache the logins in a hash cache
   */
  private static TwoFactorCache<String, String> principalToLoginId = new TwoFactorCache<String, String>(
      TfSubjectIdResolver.class.getName() + ".principalToLoginId");

  /**
   * resolve a subject id or netid into a subject id
   * @param idOrIdentifier the subject id or eppn
   * @return the resolved subject id
   */
  public static String resolveSubjectId(String idOrIdentifier) {

    String subjectId = principalToLoginId.get(idOrIdentifier);
    if (StringUtils.isBlank(subjectId)) {
      
      Subject subject = SourceManager.getInstance()
        .getSource(TfSourceUtils.SOURCE_NAME).getSubjectByIdOrIdentifier(idOrIdentifier, true);
      
      subjectId = subject.getId();

      if (StringUtils.isBlank(subjectId)) {
        throw new RuntimeException("subjectId is blank for id: '" + idOrIdentifier + "'");
      }
      
      principalToLoginId.put(idOrIdentifier, subjectId);
      
    }
    return subjectId;
  }
}
