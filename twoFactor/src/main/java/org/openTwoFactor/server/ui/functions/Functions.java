/**
 * @author mchyzer
 * $Id: Functions.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.ui.functions;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



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
 
  /**
   * escape single quotes
   * @param input
   * @return the escaped string
   */
  public static String escapeSingleQuotesAndXml(String input) {
    if (input == null) return "";
    input = TwoFactorServerUtils.xmlEscape(input, true);
    return input.replace("'", "\'");
  }
  
  /**
   * if its 11 digits, starting with 1, then 123-456-7890
   * if its 10 digits, then 123-456-6789
   * otherwise just the number
   * @param input
   * @return the escaped string
   */
  public static String formatPhone(String input) {
    if(StringUtils.isBlank(input)) {
      return input;
    }
    int[] number = new int[10];
    boolean firstChar = true;
    int digitCount = 0;
    for (char theChar : input.toCharArray()) {
      if (digitCount >= 10) {
        return input;
      }
      if (!Character.isDigit(theChar)) {
        continue;
      }
      if (theChar == '1' && firstChar) {
        firstChar = false;
        continue;
      }
      firstChar = false;
      
      number[digitCount++] = theChar - '0';
      
    }
    if (digitCount != 10) {
      return input;
    }
    return number[0] + "" + number[1] + "" + number[2] + "-" + number[3] + "" + number[4] + "" + number[5] + "-" + number[6] + "" + number[7] + "" + number[8] + "" + number[9]; 
  }
 
  
}
