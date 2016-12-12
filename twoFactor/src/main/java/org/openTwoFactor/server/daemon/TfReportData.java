/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.daemon;

import java.util.List;


/**
 * data from a report
 */
public class TfReportData {

  /**
   * 
   */
  public TfReportData() {
  }

  /**
   * @param headers1
   * @param data1
   */
  public TfReportData(List<String> headers1, List<String[]> data1) {
    this.headers = headers1;
    this.data = data1;
  }

  /**
   * report headers
   */
  private List<String> headers = null;
  
  /**
   * report data
   */
  private List<String[]> data = null;

  
  /**
   * @return the headers
   */
  public List<String> getHeaders() {
    return this.headers;
  }

  
  /**
   * @param headers1 the headers to set
   */
  public void setHeaders(List<String> headers1) {
    this.headers = headers1;
  }

  
  /**
   * @return the data
   */
  public List<String[]> getData() {
    return this.data;
  }

  
  /**
   * @param data1 the data to set
   */
  public void setData(List<String[]> data1) {
    this.data = data1;
  }

  
  
}
