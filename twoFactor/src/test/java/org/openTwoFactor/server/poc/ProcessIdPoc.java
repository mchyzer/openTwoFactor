package org.openTwoFactor.server.poc;

import java.lang.management.ManagementFactory;

/**
 * 
 * @author mchyzer
 *
 */
public class ProcessIdPoc {

  /**
   * @param args
   */
  public static void main(String[] args) {

    System.out.println(ManagementFactory.getRuntimeMXBean().getName()); 
    
  }

}
