/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openTwoFactor.clientExt.org.apache.commons.beanutils;

import java.lang.reflect.InvocationTargetException;

/**
 * <p>Implementation of <code>DynaBean</code> that wraps a standard JavaBean
 * instance, so that DynaBean APIs can be used to access its properties,
 * though this implementation allows type conversion to occur when properties are set.
 * This means that (say) Strings can be passed in as values in setter methods and
 * this DynaBean will convert them to the correct primitive data types.</p>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - This implementation does not
 * support the <code>contains()</code> and <code>remove()</code> methods.</p>
 *
 * @author James Strachan
 * @version $Revision: 1.1 $ $Date: 2013/06/20 06:16:03 $
 */

public class ConvertingWrapDynaBean extends WrapDynaBean {



    /**
     * Construct a new <code>DynaBean</code> associated with the specified
     * JavaBean instance.
     *
     * @param instance JavaBean instance to be wrapped
     */
    public ConvertingWrapDynaBean(Object instance) {

        super(instance);

    }


    /**
     * Set the value of the property with the specified name
     * performing any type conversions if necessary. So this method
     * can accept String values for primitive numeric data types for example.
     *
     * @param name Name of the property whose value is to be set
     * @param value Value to which this property is to be set
     *
     * @exception IllegalArgumentException if there are any problems
     *            copying the property.
     */
    public void set(String name, Object value) {

        try {
            BeanUtils.copyProperty(instance, name, value);
        } catch (InvocationTargetException ite) {
            Throwable cause = ite.getTargetException();
            throw new IllegalArgumentException
                    ("Error setting property '" + name +
                              "' nested exception - " + cause);
        } catch (Throwable t) {
            IllegalArgumentException iae = new IllegalArgumentException
                    ("Error setting property '" + name +
                              "', exception - " + t);
            BeanUtils.initCause(iae, t);
            throw iae;
        }

    }
}
