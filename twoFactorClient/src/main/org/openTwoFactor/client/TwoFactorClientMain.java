/**
 * @author mchyzer
 * $Id: TwoFactorClientMain.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client;


/**
 *
 */
public class TwoFactorClientMain {

  /**
   * results
   */
  private static final StringBuilder results = new StringBuilder();
  
  /**
   * append something to the results
   * @param string
   */
  private static synchronized void appendToResults(String string) {
    results.append(string);
  }
  
  /**
   * 
   * @param args
   * @throws InterruptedException
   */
  public static void main(String[] args) throws InterruptedException {
    
    TwoFactorClient.exitOnError=false;
    
    //TwoFactorClient.main(new String[]{"--operation=validatePasswordWs", "--twoFactorPass=489384", "--username=mchyzer",
    //   "--format=xml", "--debug=true", "--userBrowserUuid=0bb2368adb294254841d9db7a12a720c" });
    
    Thread[] threads = new Thread[100];
    for (int i=0;i<threads.length;i++) {
      final int I = i;
      threads[i] = new Thread(new Runnable() {

        public void run() {
          long start = System.nanoTime();
          try {
            TwoFactorClient.main(new String[]{"--operation=validatePasswordWs", "--username=mchyzer", 
              "--format=xml",  "--debug=true" /*,  "--userBrowserUuid=b3032d7a1c3946beaba49f7b3b703659" */ });
          } catch (Exception e) {
            System.out.println("Problem in thread: " + I);
            e.printStackTrace();
          } finally {
            TwoFactorClientMain.appendToResults("Thread " + I + " finished in " + ((System.nanoTime() - start) / 1000000000) + " seconds\n");
          }
        }
        
      });
      try {
        threads[i].start();
      } catch (Exception e) {
        System.out.println("Problem in thread: " + I);
        e.printStackTrace();
      }
    }
    for (int i=0;i<threads.length;i++) {
      threads[i].join();
    }
    System.out.println(TwoFactorClientMain.results);
  }
  
}
