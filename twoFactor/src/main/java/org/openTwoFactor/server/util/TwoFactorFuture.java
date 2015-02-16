/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server.util;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;


/**
 * like a normal future but keeps a reference to the callable, 
 * and exceptions are wrapped in RuntimeException
 * @param <T> type of return
 */
public class TwoFactorFuture<T> implements Future<T> {

  /**
   * enclosed future
   */
  private Future<T> future;
  
  /**
   * callable for this future
   */
  private Callable callable;

  /**
   * logger 
   */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorFuture.class);
  
  /**
   * @param theFuture 
   * @param theCallable 
   */
  public TwoFactorFuture(Future theFuture, Callable theCallable) {
    this.future = theFuture;
    this.callable = theCallable;
  }
  
  /**
   * @return the grouperCallable
   */
  public Callable getCallable() {
    return this.callable;
  }

  /**
   * if grouper callable, this is a convenience method for getting that type
   * @return the grouperCallable
   */
  public TwoFactorCallable getTwoFactorCallable() {
    if (!(this.callable instanceof TwoFactorCallable)) {
      throw new RuntimeException("Not TwoFactorCallable! " + (this.callable == null ? null : this.callable.getClass()));
    }
    return (TwoFactorCallable)this.callable;
  }

  /**
   * @see java.util.concurrent.Future#cancel(boolean)
   */
  public boolean cancel(boolean mayInterruptIfRunning) {
    return this.future.cancel(mayInterruptIfRunning);
  }

  /**
   * @see java.util.concurrent.Future#isCancelled()
   */
  public boolean isCancelled() {
    return this.future.isCancelled();
  }

  /**
   * @see java.util.concurrent.Future#isDone()
   */
  public boolean isDone() {
    return this.future.isDone();
  }

  /**
   * @see java.util.concurrent.Future#get()
   */
  public T get() {
    try {
      return this.future.get();
    } catch (Exception e) {
      TwoFactorCallable.throwRuntimeException(e);
    }
    throw new RuntimeException("shouldnt get here");
  }

  /**
   * @see java.util.concurrent.Future#get(long, java.util.concurrent.TimeUnit)
   */
  public T get(long timeout, TimeUnit unit) {
    try {
      return this.future.get(timeout, unit);
    } catch (Exception e) {
      TwoFactorCallable.throwRuntimeException(e);
    }
    throw new RuntimeException("shouldnt get here");
  }

  /**
   * relies on the callable being a TwoFactorCallable.  make sure there arent more threads than the max.
   * pass in 0 to wait for all.
   * @param futures
   * @param threadPoolSize
   * @param callablesWithProblems pass in a list to capture which jobs had problems.  if null, then jsut throw
   * exceptions as they happen
   */
  public static void waitForJob(List<TwoFactorFuture> futures, int threadPoolSize, List<TwoFactorCallable> callablesWithProblems)  {
    OUTER: while (futures.size() > threadPoolSize) {
      
      int futureToRemove = -1;
      TwoFactorFuture grouperFuture = null;

      //0 means remove all, dont care which is first
      if (threadPoolSize == 0) {
        futureToRemove = 0;
        grouperFuture = futures.get(0);
      } else {
        //find one thats done
        for (int i=0;i<futures.size();i++) {
          grouperFuture = futures.get(i);
          if (grouperFuture.isDone()) {
            futureToRemove = i;
            break;
          }
        }
        
        //didnt find a job hat done?  wait a bit and try again
        if (futureToRemove == -1) {
          TwoFactorServerUtils.sleep(5);
          continue OUTER;
        }
      }
      
      try {
        //this will throw exception if necessary and wait if hadnt waited
        grouperFuture.get();
        
      } catch (RuntimeException e) {

        //decorate some info...
        TwoFactorCallable grouperCallable = grouperFuture.getTwoFactorCallable();
        TwoFactorServerUtils.injectInException(e, "Problem in job: " + grouperCallable.getLogLabel());

        if (callablesWithProblems != null) {
          LOG.warn("Non fatal problem with callable.  Will try again not in thread", e);
          callablesWithProblems.add(grouperFuture.getTwoFactorCallable());
        } else {
          //not capturing, just throw
          throw e;
        }

      }
      futures.remove(futureToRemove);
    }
  }


}
