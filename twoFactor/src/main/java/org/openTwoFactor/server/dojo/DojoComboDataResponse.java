/**
 * @author mchyzer
 * $Id: DojoComboDataResponse.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.dojo;

import java.util.Collection;

import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * response for dojo combo ajax request
 * <pre>
 * {
 *   "identifier": "abbreviation",
 *   "label": "name",
 *   "items": [
 *       { "abbreviation": "AL", "name": "Alabama" },
 *       { "abbreviation": "WY", "name": "Wyoming" }
 *   ]
 * }
 * </pre>
 */
public class DojoComboDataResponse {

  /**
   * 
   */
  public DojoComboDataResponse() {
    
  }

  /**
   * 
   * @param theItems
   */
  public DojoComboDataResponse(Collection<DojoComboDataResponseItem> theItems) {
    
    if (TwoFactorServerUtils.length(theItems) > 0) {
      
      this.items = TwoFactorServerUtils.toArray(theItems, DojoComboDataResponseItem.class);
      
    }
  }

  /**
   * 
   * @param theItems
   */
  public DojoComboDataResponse(DojoComboDataResponseItem[] theItems) {
    
    this.items = theItems;
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    DojoComboDataResponse dojoComboDataResponse = new DojoComboDataResponse(
        new DojoComboDataResponseItem[]{
           new DojoComboDataResponseItem("id0", "label0"),
           new DojoComboDataResponseItem("id1", "label1"),
           new DojoComboDataResponseItem("id2", "label2"),
           new DojoComboDataResponseItem("id3", "label3")
        });
    
    String json = TwoFactorServerUtils.jsonConvertTo(dojoComboDataResponse, false);
    
    System.out.println(json);
  }
  
  /**
   * items
   */
  private DojoComboDataResponseItem[] items;
  
  /**
   * @return the items
   */
  public DojoComboDataResponseItem[] getItems() {
    return this.items;
  }

  
  /**
   * @param items1 the items to set
   */
  public void setItems(DojoComboDataResponseItem[] items1) {
    this.items = items1;
  }
  
  /**
   * label attribute
   */
  private String label = "name";
  
  /**
   * identifier attribute
   */
  private String identifier = "id";

  
  /**
   * label attribute
   * @return the label
   */
  public String getLabel() {
    return this.label;
  }

  
  /**
   * label attribute
   * @param label1 the label to set
   */
  public void setLabel(String label1) {
    this.label = label1;
  }

  
  /**
   * identifier attribute
   * @return the identifier
   */
  public String getIdentifier() {
    return this.identifier;
  }

  
  /**
   * identifier attribute
   * @param identifier1 the identifier to set
   */
  public void setIdentifier(String identifier1) {
    this.identifier = identifier1;
  }
  
}
