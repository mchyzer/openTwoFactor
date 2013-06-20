/**
 * @author mchyzer
 * $Id: TfHtmlFilter.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;


/**
 * filter HTML
 */
public interface TfHtmlFilter {

  /**
   * filter html from a string
   * @param html
   * @return the html to filter
   */
  public String filterHtml(String html);
  
}
