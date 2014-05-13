package org.openTwoFactor.server.poc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
    System.out.println(new Date(1380194743444L));
    
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
    System.out.println(simpleDateFormat.parse("2013/8/20 00:00:00").getTime());
    
    Calendar calendar = new GregorianCalendar();
    calendar.setTimeInMillis(simpleDateFormat.parse("2013/8/20 00:00:00").getTime());
    
    Date endDate = new Date(simpleDateFormat.parse("2014/4/15 00:00:00").getTime());
    
    while (true) {
      
      System.out.println("delete from two_factor_audit tfa where TFA.THE_TIMESTAMP < " + calendar.getTimeInMillis() + " and TFA.ACTION = 'AUTHN_NOT_OPTED_IN';");
      System.out.println("commit;");
       
      calendar.add(Calendar.DAY_OF_YEAR, 1);

      if (calendar.getTimeInMillis() > endDate.getTime()) {
        break;
      }
    }
    
    
  }

}
