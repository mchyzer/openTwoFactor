/**
 * @author mchyzer
 * $Id: Functions.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.functions;



/**
 * jsp EL functions
 */
public class Functions {

  /**
   * escape single quotes
   * @param input
   * @return the escaped string
   */
  public static String escapeSingleQuotes(String input) {
    if (input == null) return "";
    return input.replace("'", "\'");
  }
  
}
