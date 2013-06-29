package org.openTwoFactor.server.poc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * decode times
 * @author mchyzer
 *
 */
public class TimePoc {

  /**
   * @param args
   */
  public static void main(String[] args) throws ParseException {
    
    System.out.println(new Date(1372437208564L));
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    System.out.println(simpleDateFormat.parse("2013/06/26 12:00:00").getTime());
    
  }

}
