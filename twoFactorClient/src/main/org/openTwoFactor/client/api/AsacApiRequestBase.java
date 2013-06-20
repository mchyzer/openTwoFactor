package org.openTwoFactor.client.api;

import org.openTwoFactor.client.contentType.TfClientRestContentType;

/**
 * base class of method chaining objects which are used to execute operations
 * @author mchyzer
 *
 */
public class AsacApiRequestBase {

  /**
   * if the server should indent the response.  This is generally only for testing
   */
  private boolean indent = false;

  /**
   * content type of the response.  This is generally only for testing
   */
  private TfClientRestContentType contentType = TfClientRestContentType.json;
  
  /**
   * if the server should indent the response.  This is generally only for testing
   * @return the indent
   */
  public boolean isIndent() {
    return this.indent;
  }

  
  /**
   * if the server should indent the response.  This is generally only for testing
   * @param indent1 the indent to set
   * @return this for chaining
   */
  public AsacApiRequestBase assignIndent(boolean indent1) {
    this.indent = indent1;
    return this;
  }

  
  /**
   * content type of the response.  This is generally only for testing
   * @return the contentType
   */
  public TfClientRestContentType getContentType() {
    return this.contentType;
  }

  
  /**
   * @param contentType1 the contentType to set
   */
  public void setContentType(TfClientRestContentType contentType1) {
    this.contentType = contentType1;
  }
  
}
