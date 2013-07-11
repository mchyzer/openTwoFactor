
/**
 * 
 */
package org.openTwoFactor.server.status;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openTwoFactor.server.cache.TwoFactorCache;

import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.SubjectCheckConfig;
import edu.internet2.middleware.subject.SubjectUtils;
import edu.internet2.middleware.subject.provider.SourceManager;



/**
 * see if the server can connect to the DB (cache results)
 * @author mchyzer
 *
 */
public class DiagnosticSourceTest extends DiagnosticTask {

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof DiagnosticSourceTest) {
      DiagnosticSourceTest other = (DiagnosticSourceTest)obj;
      return new EqualsBuilder().append(this.sourceId, other.sourceId).isEquals();
    }
    return false;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().append(this.sourceId).toHashCode();
  }

  /** sourceId */
  private String sourceId;

  /**
   * construct with source id
   * @param theSourceId
   */
  public DiagnosticSourceTest(String theSourceId) {
    this.sourceId = theSourceId;
  }
  
  /**
   * cache the results
   */
  private static TwoFactorCache<String, Boolean> sourceCache = new TwoFactorCache<String, Boolean>(DiagnosticSourceTest.class.getName() + ".sourceDiagnostic", 100, false, 120, 120, false);
  
  /**
   * @see org.openTwoFactor.server.status.DiagnosticsTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    if (sourceCache.containsKey(this.sourceId)) {

      this.appendSuccessTextLine("Source checked successfully recently");

    } else {

      Source source = SourceManager.getInstance().getSource(this.sourceId);
      
      String findSubjectOnCheckConfigString = source.getInitParam(SubjectCheckConfig.FIND_SUBJECT_BY_ID_ON_CHECK_CONFIG);
      boolean findSubjectOnCheckConfig = SubjectUtils.booleanValue(findSubjectOnCheckConfigString, true);
      
      if (findSubjectOnCheckConfig) {
        String subjectToFindOnCheckConfig = source.getInitParam(SubjectCheckConfig.SUBJECT_ID_TO_FIND_ON_CHECK_CONFIG);
        subjectToFindOnCheckConfig = SubjectUtils.defaultIfBlank(subjectToFindOnCheckConfig, SubjectCheckConfig.GROUPER_TEST_SUBJECT_BY_ID);
        source.getSubject(subjectToFindOnCheckConfig, false);
        this.appendSuccessTextLine("Searched for subject by id: " + subjectToFindOnCheckConfig);
        sourceCache.put(this.sourceId, Boolean.TRUE);
      } else {
        this.appendSuccessTextLine("Not configured to check source by id");
      }

    }
    
    return true;
    
  }

  /**
   * @see org.openTwoFactor.server.status.DiagnosticsTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "source_" + this.sourceId;
  }

  /**
   * @see org.openTwoFactor.server.status.DiagnosticsTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Source " + this.sourceId;
  }

}
