/*
 * @author mchyzer
 * $Id: TwoFactorServerXstreamUtils.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.openTwoFactor.server.cache.TfEhcacheController;
import org.openTwoFactor.server.config.TwoFactorServerConfig;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.javabean.JavaBeanConverter;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;


/**
 *
 */
public class TwoFactorServerXstreamUtils {

  /** logger */
  private static final Log log = TwoFactorServerUtils.getLog(TfEhcacheController.class);

  /**
   * get xstream with all client aliases intact
   * @return xstream
   */
  public static XStream retrieveXstream() {
    return retrieveXstream(TfRestClassLookup.getAliasClassMap());
  }
  
  /**
   * 
   * @param aliasClassMap for xstream
   * @return xstream, configured to use
   */
  @SuppressWarnings("deprecation")
  public static XStream retrieveXstream(Map<String, Class<?>> aliasClassMap) {
    boolean ignoreExtraneousFields = true;
    
    XStream xStream = null;
    
    if (ignoreExtraneousFields) {
    
      xStream = new XStream(new DomDriver()) {

        /**
         * 
         */
        @Override
        protected MapperWrapper wrapMapper(MapperWrapper next) {
          return new MapperWrapper(next) {
  
            /**
             * 
             * @see edu.internet2.middleware.grouperClientExt.com.thoughtworks.xstream.mapper.MapperWrapper#shouldSerializeMember(java.lang.Class, java.lang.String)
             */
            @Override
            public boolean shouldSerializeMember(Class definedIn, String fieldName) {
              boolean definedInNotObject = definedIn != Object.class;
              if (definedInNotObject) {
                return super.shouldSerializeMember(definedIn, fieldName);
              }
  
              log.debug("Cant find field: " + fieldName);
              return false;
            }
  
          };
        }
      };
    } else {
      xStream = new XStream(new DomDriver());
    }
    
    //xStream.registerConverter(new JavaBeanConverter(xStream.getMapper()));
    xStream.registerConverter(new
        JavaBeanConverter(xStream.getMapper(), "class"), -20);
    
    //dont try to get fancy
    xStream.setMode(XStream.NO_REFERENCES);

    for (String key : TwoFactorServerUtils.nonNull(aliasClassMap).keySet()) {
      xStream.alias(key, aliasClassMap.get(key));
    }

    xStream.autodetectAnnotations(true);

    //see if omitting fields
    String fieldsToOmit = TwoFactorServerConfig.retrieveConfig().propertyValueString("grouper.webService.omitXmlProperties");
    if (!TwoFactorServerUtils.isBlank(fieldsToOmit)) {
      List<String> fieldsToOmitList = TwoFactorServerUtils.splitTrimToList(fieldsToOmit, ",");
      for (String fieldToOmit: fieldsToOmitList) {
        if (!TwoFactorServerUtils.isBlank(fieldToOmit)) {
          try {
            int dotIndex = fieldToOmit.lastIndexOf('.');
            String className = fieldToOmit.substring(0, dotIndex);
            String propertyName = fieldToOmit.substring(dotIndex+1, fieldToOmit.length());
            Class<?> theClass = TwoFactorServerUtils.forName(className);
            xStream.omitField(theClass, propertyName);
          } catch (Exception e) {
            throw new RuntimeException("Problem with grouper.webService.omitXmlProperties: " + fieldsToOmit + ", " + e.getMessage(), e);
          }
        }
      }
    }
    
    return xStream;
  }
  
}
