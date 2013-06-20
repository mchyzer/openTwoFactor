/*
 * Copyright 2002-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openTwoFactor.clientExt.net.sf.json.processors;

import org.openTwoFactor.clientExt.net.sf.json.JSONException;
import org.openTwoFactor.clientExt.net.sf.json.JSONObject;
import org.openTwoFactor.clientExt.net.sf.json.JsonConfig;

/**
 * Base interface for custom serialization per Bean.
 *
 * @author Andres Almiray <aalmiray@users.sourceforge.net>
 */
public interface JsonBeanProcessor {
   /**
    * Processes the bean an returns a suitable JSONObject representation.
    *
    * @param bean the input bean
    * @param jsonConfig the current configuration environment
    * @return a JSONObject that represents the input bean
    * @throws JSONException if an error occurs during transformation
    */
   JSONObject processBean( Object bean, JsonConfig jsonConfig );
}