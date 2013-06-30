package org.openTwoFactor.server.poc;

import java.util.HashMap;
import java.util.Map;

import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.provider.SourceManager;


public class SubjectExpressionLanguagePoc {

  /**
   * @param args
   */
  public static void main(String[] args) {
    
    String el = "${subject.getAttributeValue('pennname')}@test.upenn.edu";
    
    Subject subject = SourceManager.getInstance()
        .getSource(TfSourceUtils.SOURCE_NAME).getSubjectByIdOrIdentifier("mchyzer", false);

    Map<String, Object> substituteMap = new HashMap<String, Object>();
    substituteMap.put("subject", subject);

    
    
    String result = TwoFactorServerUtils.substituteExpressionLanguage(el, substituteMap, true, true, true);
    
    System.out.println(result);
  }

}
