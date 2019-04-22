/**
 * @author mchyzer
 * $Id: TfCombobox.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.customTags;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.apache.commons.lang.StringUtils;



/**
 * custom tag for the dojo combobox.  Shared code with internet2 grouper
 */
public class TfCombobox extends SimpleTagSupport {

  /**
   * id and class of elements, and name of combobox. Make this unique in page.
   * e.g. personPicker.  The id of the tag will be personPickerId, name will be
   * personPickerName.  Will generate a QueryReadStore too... 
   */
  private String idBase;

  /**
   * style, could include the width of the textfield
   */
  private String style;

  /**
   * class to use when drawing the control.  default is claro.  should be a dojo class theme, e.g.  claro, tundra, nihilo and soria
   */
  private String classCss;

  /**
   * if there should be a down arrow to click.  Default to false.  Generally this is useful only for 
   * combos with less then a few hundred options
   */
  private Boolean hasDownArrow = null;

  /**
   * search delay in ms defaults to 500
   */
  private Integer searchDelay = 500;

  /**
   * the operation to call when filtering, relative to this page url to call
   */
  private String filterOperation;

  /**
   * the default value (will be submitted) which should appear in the combo box when drawn.  Will lookup the label via ajax
   */
  private String value;

  /**
   * send more form element names to the filter operation, comma separated
   */
  private String additionalFormElementNames;

  
  /**
   * id and class of elements, and name of combobox. Make this unique in page.
   * e.g. personPicker.  The id of the tag will be personPickerId, name will be
   * personPickerName.  Will generate a QueryReadStore too... 
   * @param idBase1 the idBase to set
   */
  public void setIdBase(String idBase1) {
    this.idBase = idBase1;
  }

  
  /**
   * style, could include the width of the textfield
   * @param style1 the style to set
   */
  public void setStyle(String style1) {
    this.style = style1;
  }

  
  /**
   * class to use when drawing the control.  default is claro.  should be a dojo class theme, e.g.  claro, tundra, nihilo and soria
   * @param classCss1 the classCss to set
   */
  public void setClassCss(String classCss1) {
    this.classCss = classCss1;
  }

  
  /**
   * if there should be a down arrow to click.  Default to false.  Generally this is useful only for 
   * combos with less then a few hundred options
   * @param hasDownArrow1 the hasDownArrow to set
   */
  public void setHasDownArrow(Boolean hasDownArrow1) {
    this.hasDownArrow = hasDownArrow1;
  }

  
  /**
   * search delay in ms defaults to 500
   * @param searchDelay1 the searchDelay to set
   */
  public void setSearchDelay(Integer searchDelay1) {
    this.searchDelay = searchDelay1;
  }

  /**
   * 
   * @return pro
   */
  public int searchDelayProcessed() {
    if (this.searchDelay == null || this.searchDelay < 0) {
      return 500;
    }
    return this.searchDelay;
  }
  
  /**
   * default downarrow to false
   * @return if has down arrow
   */
  public boolean hasDownArrowProcessed() {
    if (this.hasDownArrow == null) {
      return false;
    }
    return this.hasDownArrow;
  }
  
  /**
   * the operation to call when filtering, relative to this page url to call
   * @param filterOperation1 the filterOperation to set
   */
  public void setFilterOperation(String filterOperation1) {
    this.filterOperation = filterOperation1;
  }

  
  /**
   * the default value (will be submitted) which should appear in the combo box when drawn.  Will lookup the label via ajax
   * @param value1 the value to set
   */
  public void setValue(String value1) {
    this.value = value1;
  }

  
  /**
   * send more form element names to the filter operation, comma separated
   * @param additionalFormElementNames1 the additionalFormElementNames to set
   */
  public void setAdditionalFormElementNames(String additionalFormElementNames1) {
    this.additionalFormElementNames = additionalFormElementNames1;
  }
  

  /**
   * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
   */
  @Override
  public void doTag() throws JspException, IOException {

    StringBuilder result = new StringBuilder();
    
    //<div data-dojo-type="dojox.data.QueryReadStore" id="personPickerStoreId" formElementNamesToSend="anotherItemName"
    //  data-dojo-props="url:'../twoFactorUi/app/UiMain.personPicker'" data-dojo-id="personPickerStoreDojoId" style="display: inline"></div>
    //<input id="personPickerId" name="personPickerName"  searchDelay="500" value="10021368"
    //    data-dojo-props="store:personPickerStoreDojoId" class="claro" style="width: 40em"
    //    autoComplete="false" data-dojo-type="dijit/form/FilteringSelect" hasDownArrow="false" /><!-- 
    //     -18 for no down arrow, -36 for down arrow --><img 
    //    style="position: relative; top: 5px; left: -18px; display: none; " alt="busy..."  id="personPickerThrobberId"
    //    src="../assets/busy.gif" class="comboThrobber" />
    //<script>
    //dojo.ready(function(){
    //  dijit.byId('personPickerId').onChange = function(evt) {
    //    this.focusNode.setSelectionRange(0,0);
    //  }
    //});
    //</script>
    
    result.append("    <div style=\"display: inline; white-space: nowrap; \"><div data-dojo-type=\"dojox.data.QueryReadStore\" id=\"" + this.idBase + "StoreId\" ");
    if (!StringUtils.isBlank(this.additionalFormElementNames)) {
      result.append(" formElementNamesToSend=\"anotherItemName\" ");
    }
    result.append(" data-dojo-props=\"url:'" + this.filterOperation 
        + "'\" data-dojo-id=\"" + this.idBase + "StoreDojoId\" style=\"display: none\"></div>\n" );

    result.append("    <input id=\"" + this.idBase + "DisplayId\" name=\"" + this.idBase + "DisplayName\" type=\"hidden\" /> \n");
    
    result.append("    <input id=\"" + this.idBase + "Id\" name=\"" + this.idBase + "Name\"  searchDelay=\"" 
        + this.searchDelayProcessed() + "\" ");
    
    result.append(" oninput=\"$('#" + this.idBase + "DisplayId').val(dijit.byId('" + this.idBase + "Id').get('displayedValue'))\" ");
    
    if (!StringUtils.isBlank(this.value)) {
      result.append(" value=\"" + this.value + "\" ");
    }
    
    result.append(" required=\"false\" data-dojo-props=\"store:" + this.idBase + "StoreDojoId\" ");
    
    if (!StringUtils.isBlank(this.classCss)) {
      result.append(" class=\"" + this.classCss + "\" ");
    }
    
    if (!StringUtils.isBlank(this.style)) {
      result.append(" style=\"" + this.style + "\" \n");
    }
    
    result.append("    autocomplete=\"false\" data-dojo-type=\"dijit/form/FilteringSelect\" hasDownArrow=\"" + this.hasDownArrowProcessed() + "\" />");
    
    //note, no whitespace between input and throbber
    // <!-- 
    //     -18 for no down arrow, -36 for down arrow --><img 
    //    style="position: relative; top: 5px; left: -18px; display: none; " alt="busy..."  id="personPickerThrobberId"
    //    src="../assets/busy.gif" class="comboThrobber" />
    result.append("<img \n");
    result.append("     style=\"position: relative; top: 5px; left: " + (this.hasDownArrowProcessed() ? "-36" : "-18") + "px; display: none; \" alt=\"busy...\"  id=\"" + this.idBase + "ThrobberId\"\n");
    result.append("     src=\"../../assets/busy.gif\" class=\"comboThrobber\" />\n");
    
    //<script>
    //dojo.ready(function(){
    //  dijit.byId('personPickerId').onChange = function(evt) {
    //    this.focusNode.setSelectionRange(0,0);
    //  }
    //});
    //</script>
    result.append("    <script>\n");
    result.append("      dojo.ready(function(){\n");
    result.append("        dijit.byId('" + this.idBase + "Id').onChange = function(evt) {\n");
    result.append("          this.focusNode.setSelectionRange(0,0);\n");
    result.append("        }\n");
    // clear out value so a quick submit doesnt break it
    result.append("        $('#" + this.idBase + "Id').val('');\n");
    result.append("      });\n");
    result.append("    </script></div>\n"); //TODO add delete button
    
    this.getJspContext().getOut().print(result.toString());
  }

}
