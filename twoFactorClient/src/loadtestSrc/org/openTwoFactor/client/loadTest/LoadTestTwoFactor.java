/**
 * @author mchyzer
 * $Id: LoadTestTwoFactor.java,v 1.1 2013/06/20 06:19:35 mchyzer Exp $
 */
package org.openTwoFactor.client.loadTest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import org.openTwoFactor.client.api.TfValidatePassword;
import org.openTwoFactor.client.corebeans.TfCheckPasswordResponse;
import org.openTwoFactor.client.util.TwoFactorClientUtils;
import org.openTwoFactor.clientExt.org.apache.commons.lang.StringUtils;


/**
 *
 */
public class LoadTestTwoFactor {

  /**
   * @param args
   */
  public static void main(String[] args) {

    final int sleepMillis = 20;
    final int threadRecords = 2000;
    final int threadCount = 10;
    final int printMod = Math.min(threadRecords / 10, 10);
    final int printMoreThanMillis = 1000;

    final List<Long> times = Collections.synchronizedList(new ArrayList<Long>());
    
    Thread[] threads = new Thread[threadCount];
    
    for (int i = 0; i<threads.length; i++) {
      
      final int I = i;
      threads[i] = new Thread(new Runnable() {
        
        @Override
        public void run() {
          
          for (int j=0;j<threadRecords;j++) {
            
            long start = System.nanoTime();
            
            try {
              
              int userIndex = new Random().nextInt(1000);
              
              TfCheckPasswordResponse tfCheckPasswordResponse = new TfValidatePassword().assignUsername("jsmith"+userIndex)
                .assignTwoFactorPass(StringUtils.leftPad(
                  new Random().nextInt(1000) + "" + userIndex, 6, '0')).execute();
                  
              if (tfCheckPasswordResponse.getSuccess() == null || !tfCheckPasswordResponse.getSuccess()) {
                System.out.println("Error!");
              }
              
              long tookMs = (System.nanoTime() - start) / 1000000;
              if (j>3) {
                times.add(tookMs);
              }
              
              if (tookMs > printMoreThanMillis || (threadRecords % printMod) + 1 == 0) {
                System.out.println("Thread: " + I + " index: " + j + " took " + tookMs + " ms");
              }
              
            } catch (Exception e) {
              e.printStackTrace();
            }
            int printInterval = Math.min(threadRecords / 10, 100);
            
            if ((j+1)%printInterval == 0) {
              System.out.println("Thread " + I + " on record: " + j);
            }
            
            if (sleepMillis > 0) {  
              TwoFactorClientUtils.sleep(new Random().nextInt(sleepMillis));
            }
          }
        }
      });
      
      threads[i].start();
      
    }

    for (int i=0;i<threads.length;i++) {
      try {
        threads[i].join();
      } catch (Exception e) {
        throw new RuntimeException("What?", e);
      }
    }
    
    //analyze
    double[] millis = new double[times.size()];
    int i=0;
    for (Long milli : times) {
      millis[i] = milli.doubleValue();
      i++;
    }
    
    DescriptiveStatistics descriptiveStatistics = new DescriptiveStatistics(millis);
    
    DecimalFormat df = new DecimalFormat("#.##");
    
    System.out.println("Total records: " + millis.length);
    System.out.println("Total time: " + df.format(descriptiveStatistics.getSum()) + " ms");
    System.out.println("Mean: " + df.format(descriptiveStatistics.getMean()) + " ms");
    System.out.println("Min: " + df.format(descriptiveStatistics.getMin()) + " ms");
    System.out.println("Max: " + df.format(descriptiveStatistics.getMax()) + " ms");
    System.out.println("Standard deviation: " + df.format(descriptiveStatistics.getStandardDeviation()) + " ms");
    
  }

}
