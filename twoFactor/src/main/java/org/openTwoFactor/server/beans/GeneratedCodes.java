/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.beans;

import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;


/**
 * generated:123456789,codes:[{sent:123456789,code:"1560396627950__cIh4Tm/yzzM+Z8M9Mi40S7rEwi1pjJOi98bJ0XMCYlc="}]
 */
public class GeneratedCodes {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    GeneratedCodes generatedCodes = new GeneratedCodes();
    generatedCodes.setGenerated(12345L);
    generatedCodes.setCodes(new ArrayList<GeneratedCode>());
    
    GeneratedCode generatedCode = new GeneratedCode();
    generatedCode.setCode("abc");
    generatedCode.setSent(123L);
    
    generatedCodes.getCodes().add(generatedCode);
    
    generatedCode = new GeneratedCode();
    generatedCode.setCode("bcd");
    generatedCode.setSent(234L);
    
    generatedCodes.getCodes().add(generatedCode);

//    JSONObject jsonObject = JSONObject.fromObject(generatedCodes);
//    String json = jsonObject.toString();
//    
//    JSONObject.fromObject(json);
//    System.out.println(json);
//    try {
//      jsonObject = new JSONObject(json);
//    } catch ()
    
  }
  
  /**
   * when these codes were generated (millis since 1970)
   */
  private long generated;
  
  /**
   * when these codes were generated (millis since 1970)
   * @return the generated
   */
  public long getGenerated() {
    return this.generated;
  }
  
  /**
   * when these codes were generated (millis since 1970)
   * @param generated1 the generated to set
   */
  public void setGenerated(long generated1) {
    this.generated = generated1;
  }

  /**
   * encrypted codes and if used yet
   */
  private List<GeneratedCode> codes;
  
  /**
   * encrypted codes and if used yet
   * @return the codes
   */
  public List<GeneratedCode> getCodes() {
    return this.codes;
  }
  
  /**
   * encrypted codes and if used yet
   * @param codes1 the codes to set
   */
  public void setCodes(List<GeneratedCode> codes1) {
    this.codes = codes1;
  }

  /**
   * 
   */
  public GeneratedCodes() {
  }

}
