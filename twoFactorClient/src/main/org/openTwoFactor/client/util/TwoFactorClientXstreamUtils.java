/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id: TwoFactorClientXstreamUtils.java,v 1.1 2013/06/20 06:15:22 mchyzer Exp $
 */
package org.openTwoFactor.client.util;

import java.util.List;
import java.util.Map;

import org.openTwoFactor.client.ws.TfClientRestClassLookup;
import org.openTwoFactor.clientExt.com.thoughtworks.xstream.XStream;
import org.openTwoFactor.clientExt.com.thoughtworks.xstream.io.xml.DomDriver;
import org.openTwoFactor.clientExt.com.thoughtworks.xstream.mapper.MapperWrapper;
import org.openTwoFactor.clientExt.org.apache.commons.logging.Log;



/**
 *
 */
public class TwoFactorClientXstreamUtils {

  /**
   * logger
   */
  static Log log = TwoFactorClientUtils.retrieveLog(TwoFactorClientXstreamUtils.class);

  /**
   * get xstream with all client aliases intact
   * @return xstream
   */
  public static XStream retrieveXstream() {
    return retrieveXstream(TfClientRestClassLookup.getAliasClassMap());
  }
  
  /**
   * 
   * @param aliasClassMap for xstream
   * @return xstream, configured to use
   */
  public static XStream retrieveXstream(Map<String, Class<?>> aliasClassMap) {
    boolean ignoreExtraneousFields = true;
    
    XStream xStream = null;
    
    if (ignoreExtraneousFields) {
    
      xStream = new XStream(new DomDriver()) {

        /**
         * 
         * @see org.openTwoFactor.clientExt.com.thoughtworks.xstream.XStream#wrapMapper(org.openTwoFactor.clientExt.com.thoughtworks.xstream.mapper.MapperWrapper)
         */
        @Override
        protected MapperWrapper wrapMapper(MapperWrapper next) {
          return new MapperWrapper(next) {
  
            /**
             * 
             * @see org.openTwoFactor.clientExt.com.thoughtworks.xstream.mapper.MapperWrapper#shouldSerializeMember(java.lang.Class, java.lang.String)
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
    //dont try to get fancy
    xStream.setMode(XStream.NO_REFERENCES);

    for (String key : TwoFactorClientUtils.nonNull(aliasClassMap).keySet()) {
      xStream.alias(key, aliasClassMap.get(key));
    }

    xStream.autodetectAnnotations(true);

    //see if omitting fields
    String fieldsToOmit = TwoFactorClientConfig.retrieveConfig().propertyValueString("grouper.webService.omitXmlProperties");
    if (!TwoFactorClientUtils.isBlank(fieldsToOmit)) {
      List<String> fieldsToOmitList = TwoFactorClientUtils.splitTrimToList(fieldsToOmit, ",");
      for (String fieldToOmit: fieldsToOmitList) {
        if (!TwoFactorClientUtils.isBlank(fieldToOmit)) {
          try {
            int dotIndex = fieldToOmit.lastIndexOf('.');
            String className = fieldToOmit.substring(0, dotIndex);
            String propertyName = fieldToOmit.substring(dotIndex+1, fieldToOmit.length());
            Class<?> theClass = TwoFactorClientUtils.forName(className);
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
