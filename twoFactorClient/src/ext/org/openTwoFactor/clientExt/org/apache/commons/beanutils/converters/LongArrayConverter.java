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


package org.openTwoFactor.clientExt.org.apache.commons.beanutils.converters;


import java.util.List;

import org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException;



/**
 * <p>Standard {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.Converter} implementation that converts an incoming
 * String into a primitive array of long.  On a conversion failure, returns
 * a specified default value or throws a {@link ConversionException} depending
 * on how this instance is constructed.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.1 $ $Date: 2013/06/20 06:16:03 $
 * @since 1.4
 * @deprecated Replaced by the new {@link ArrayConverter} implementation
 */

public final class LongArrayConverter extends AbstractArrayConverter {


    // ----------------------------------------------------------- Constructors


    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.Converter} that will throw
     * a {@link ConversionException} if a conversion error occurs.
     */
    public LongArrayConverter() {

        this.defaultValue = null;
        this.useDefault = false;

    }


    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.Converter} that will return
     * the specified default value if a conversion error occurs.
     *
     * @param defaultValue The default value to be returned
     */
    public LongArrayConverter(Object defaultValue) {

        this.defaultValue = defaultValue;
        this.useDefault = true;

    }


    // ------------------------------------------------------- Static Variables


    /**
     * <p>Model object for type comparisons.</p>
     */
    private static final long[] MODEL = new long[0];


    // --------------------------------------------------------- Public Methods


    /**
     * Convert the specified input object into an output object of the
     * specified type.
     *
     * @param type Data type to which this value should be converted
     * @param value The input value to be converted
     * @return the converted value
     *
     * @exception ConversionException if conversion cannot be performed
     *  successfully
     */
    public Object convert(Class type, Object value) {

        // Deal with a null value
        if (value == null) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException("No value specified");
            }
        }

        // Deal with the no-conversion-needed case
        if (MODEL.getClass() == value.getClass()) {
            return (value);
        }

        // Deal with input value as a String array
        if (strings.getClass() == value.getClass()) {
            try {
                String[] values = (String[]) value;
                long[] results = new long[values.length];
                for (int i = 0; i < values.length; i++) {
                    results[i] = Long.parseLong(values[i]);
                }
                return (results);
            } catch (Exception e) {
                if (useDefault) {
                    return (defaultValue);
                } else {
                    throw new ConversionException(value.toString(), e);
                }
            }
        }

        // Parse the input value as a String into elements
        // and convert to the appropriate type
        try {
            List list = parseElements(value.toString());
            long[] results = new long[list.size()];
            for (int i = 0; i < results.length; i++) {
                results[i] = Long.parseLong((String) list.get(i));
            }
            return (results);
        } catch (Exception e) {
            if (useDefault) {
                return (defaultValue);
            } else {
                throw new ConversionException(value.toString(), e);
            }
        }

    }


}
