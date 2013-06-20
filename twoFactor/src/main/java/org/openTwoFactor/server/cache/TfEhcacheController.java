
package org.openTwoFactor.server.cache;
import java.io.File;
import java.net.URL;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Statistics;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.util.TwoFactorServerUtils;





/**
 * Base class for common cache operations.
 * @version $Id: TfEhcacheController.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
public class TfEhcacheController implements TfCacheController {

  /**
   * singleton cache controller
   */
  private static TfEhcacheController ehcacheController = null;

  /**
   * utility cache controller if you dont want to create your own...
   * @return ehcache controller
   */
  public static TfEhcacheController ehcacheController() {
    if (ehcacheController == null) {
      ehcacheController = new TfEhcacheController();
    }
    return ehcacheController;
  }

  /**
   * manager
   */
  private CacheManager mgr;

  /**
   * @see java.lang.Object#finalize()
   */
  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    this.stop();
  }

  /**
   */
  public void stop() {
    if (this.mgr != null) {
      synchronized(CacheManager.class) {
        this.mgr.shutdown();
      }
    }
  }

 
  /**
   * not public since we only want one of these...
   * Initialize caching.
   */
  private TfEhcacheController() {
  }



  /**
   * Flush all caches.
   */
  public void flushCache() {
    if (this.mgr != null) {
      this.mgr.clearAll(); 
    }
  }

  /**
   * Retrieve a cache (like a generic Map)
   * @param name should be unique, prefix with fully qualified classname
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   */
  public TwoFactorCache getTwoFactorCache(String name) 
    throws  IllegalStateException { 
    //dont use defaults
    return getTwoFactorCache(name, false, -1, false, -1, -1, false);
  }
  
  /**
   * Retrieve a TwoFactorCache which is a generic Map cache.  Note the defaults are only used
   * in the first invocation of the cache retrieval.
   * @param name should be unique, prefix with fully qualified classname
   * @param useDefaultIfNotInConfigFile use the defaults if not in the config file
   * @param defaultMaxElementsInMemory if not in config file, this is max elements in memory
   * @param defaultEternal if not in config file,  true to never expire stuff
   * @param defaultTimeToIdleSeconds  if not in config file, time where if not accessed, will expire
   * @param defaultTimeToLiveSeconds  if not in config file, time where even if accessed, will expire
   * @param defaultOverflowToDisk  if not in config file, if it should go to disk in overflow
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   */
  public TwoFactorCache getTwoFactorCache(String name, boolean useDefaultIfNotInConfigFile,
      int defaultMaxElementsInMemory, 
      boolean defaultEternal, int defaultTimeToIdleSeconds, 
      int defaultTimeToLiveSeconds, boolean defaultOverflowToDisk) 
    throws  IllegalStateException { 
    return new TwoFactorCache(this.getCache(name, useDefaultIfNotInConfigFile, 
        defaultMaxElementsInMemory, defaultEternal, defaultTimeToIdleSeconds, 
        defaultTimeToLiveSeconds, defaultOverflowToDisk));
  }
  
  /**
   * Note, this might be better to be used from TwoFactorCache
   * @param name should be unique, prefix with fully qualified classname
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   * @since   1.2.1
   */
  public Cache getCache(String name) 
    throws  IllegalStateException { 
    //dont use defaults
    return getCache(name, false, -1, false, -1, -1, false);
  }
  
  /**
   * Note, this might be better to be used from TwoFactorCache
   * @param name should be unique, prefix with fully qualified classname
   * @param useDefaultIfNotInConfigFile use the defaults if not in the config file
   * @param defaultMaxElementsInMemory if not in config file, this is max elements in memory
   * @param defaultEternal if not in config file,  true to never expire stuff
   * @param defaultTimeToIdleSeconds  if not in config file, time where if not accessed, will expire
   * @param defaultTimeToLiveSeconds  if not in config file, time where even if accessed, will expire
   * @param defaultOverflowToDisk  if not in config file, if it should go to disk in overflow
   * @return  Cache <i>name</i>.
   * @throws  IllegalStateException if cache not found.
   */
  public Cache getCache(String name, boolean useDefaultIfNotInConfigFile,
      int defaultMaxElementsInMemory, 
      boolean defaultEternal, int defaultTimeToIdleSeconds, 
      int defaultTimeToLiveSeconds, boolean defaultOverflowToDisk) 
    throws  IllegalStateException { 
    this.initialize();
    if (this.mgr.cacheExists(name) ) {
      return this.mgr.getCache(name);
    }
    if (useDefaultIfNotInConfigFile) {
      if (LOG != null) {
        LOG.info("cache not configured explicitly: " + name + ", to override default values, " +
        		"configure in the resource /ehcache.xml.  Default values are:" +
        		"maxElementsInMemory: " + defaultMaxElementsInMemory + ", eternal: " + defaultEternal
        		+ ", timeToIdleSeconds: " + defaultTimeToIdleSeconds + ", timeToLiveSeconds: " 
        		+ defaultTimeToLiveSeconds + ", overFlowToDisk: " + defaultOverflowToDisk);
      }
      Cache cache = new Cache(name, defaultMaxElementsInMemory, defaultOverflowToDisk, 
          defaultEternal, defaultTimeToLiveSeconds, defaultTimeToIdleSeconds);
      this.mgr.addCache(cache);
      return cache;
    }
    
    throw new IllegalStateException("cache not found: " + name + " make sure the cache" +
        " config is correct, the resource: /ehcache.xml");
  }

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TfEhcacheController.class);

  /**
   * @param cache 
   * @return  ehcache statistics for <i>cache</i>.
   * @since   1.2.1
   */
  public TfCacheStats getStats(String cache) {
    //not sure if we need to initialize, since no stats will be found...
    this.initialize();
    Cache c = this.getCache(cache);
    c.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_GUARANTEED);
    return new TfEhcacheStats( c.getStatistics() );
  }

  /** 
   * Initialize privilege cache.
   * @since   1.2.1
   */
  public void initialize() {
    if (this.mgr == null) {
      synchronized(TfEhcacheController.class) {
        if (this.mgr == null) {
          URL url = this.getClass().getResource("/ehcache.xml");
          if (url == null) {
            throw new RuntimeException("Cant find resource /ehcache.xml, " +
                "make sure it is on the classpath");
          }
          
          //trying to avoid warning of using the same dir
          String tmpDir = TwoFactorServerUtils.tmpDir();
          try {
            String newTmpdir = StringUtils.trimToEmpty(tmpDir);
            if (!newTmpdir.endsWith("\\") && !newTmpdir.endsWith("/")) {
              newTmpdir += File.separator;
            }
            newTmpdir += "twoFactor_ehcache_auto_" + TwoFactorServerUtils.uniqueId();
            System.setProperty(TwoFactorServerUtils.JAVA_IO_TMPDIR, newTmpdir);
            
            synchronized(CacheManager.class) {
              //now it should be using a unique directory
              this.mgr = new CacheManager(url);
            }
          } finally {
            
            //put tmpdir back
            if (tmpDir == null) {
              System.clearProperty(TwoFactorServerUtils.JAVA_IO_TMPDIR);
            } else {
              System.setProperty(TwoFactorServerUtils.JAVA_IO_TMPDIR, tmpDir);
            }
          }
        }
      }
    }
  }
  
}

