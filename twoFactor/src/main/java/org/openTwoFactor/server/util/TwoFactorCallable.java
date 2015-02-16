/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.util;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.subject.Subject;


/**
 * @param <T>
 */
public abstract class TwoFactorCallable<T> implements Callable<T> {

  
  /**
   * describes the callable
   * @return the logLabel
   */
  public String getLogLabel() {
    return this.logLabel;
  }

  /**
   * note, call this in your grouper session
   * @param callablesWithProblems
   */
  public static void tryCallablesWithProblems(Collection<TwoFactorCallable> callablesWithProblems) {
    
    RuntimeException problem = null;

    int callablesWithProblemsCount = 0;
    
    for (TwoFactorCallable grouperCallable : TwoFactorServerUtils.nonNull(callablesWithProblems)) {
      
      try {
        grouperCallable.callLogic();
      } catch (RuntimeException re) {
        
        callablesWithProblemsCount++;
        
        if (problem == null) {
          problem = re;
        } else {
          LOG.error("Problem with callable: " + grouperCallable.getLogLabel());
        }
      }
      
    }
    
    if (problem != null) {
      TwoFactorServerUtils.injectInException(problem, callablesWithProblemsCount 
          + " callables out of problem count " + callablesWithProblems.size() + " had problems running outside of threads!");
      throw problem;
    }
  }
  
  /**
   * convert exception
   * @param throwable
   */
  public static void throwRuntimeException(Throwable throwable) {

    //this isnt good, exception
    if (throwable instanceof ExecutionException) {
      //unwrap it
      ExecutionException executionException = (ExecutionException)throwable;
      if (executionException.getCause() != null) {
        //the underlying exception is here... might be runtime
        throwable = executionException.getCause();
      }
    }
    if (throwable instanceof RuntimeException) {
      throw (RuntimeException)throwable;
    }
    throw new RuntimeException(throwable);

  }
  
  /**
   * logger 
   */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorCallable.class);

  /**
   * grouper session subject or null if none.  keep subject and not session in case it gets stopped in another thread
   */
  private Subject grouperSessionSubject;
  
  /** loglabel */
  private String logLabel;

  /**
   * 
   */
  static int numberOfThreads = 0;
  
  /**
   * @see java.util.concurrent.Callable#call()
   */
  public final T call() throws Exception {
    
    long subStartNanos = -1;
    try {
      if (LOG.isDebugEnabled()) {
        subStartNanos = System.nanoTime();
        synchronized (TwoFactorCallable.class) {
          numberOfThreads++;
        }
      }
      return this.callLogic();
    } finally {
      if (LOG.isDebugEnabled()) {
        synchronized (TwoFactorCallable.class) {
          long nanos = System.nanoTime() - subStartNanos;
          long millis = nanos / 1000000;
          LOG.debug("Threads: " + numberOfThreads + ", " + this.logLabel + ", time in millis: " + millis);
          numberOfThreads--;
        }
      }
    }
    
  }

  /**
   * Computes a result
   *
   * @return computed result
   */
  public abstract T callLogic();

  /**
   * construct with log label, use the static session if it exists
   * @param theLogLabel
   */
  public TwoFactorCallable(String theLogLabel) {
    this.logLabel = theLogLabel;
  }

}
