
package org.openTwoFactor.server.cache;


/**
 * Cache statistics interface.
 * @version $Id: TfCacheStats.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
public interface TfCacheStats {

  /**
   * @return  Number of cache hits.
   */
  long getHits();

  /**
   * @return  Number of cache misses.
   */
  long getMisses();

  /**
   * @return  Number of objects in cache.
   */
  long getSize();

}

