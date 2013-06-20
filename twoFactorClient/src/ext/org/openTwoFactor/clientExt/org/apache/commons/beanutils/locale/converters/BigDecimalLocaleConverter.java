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

package org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.converters;

import java.util.Locale;
import java.math.BigDecimal;
import java.text.ParseException;

import org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException;


/**
 * <p>Standard {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
 * implementation that converts an incoming
 * locale-sensitive String into a <code>java.math.BigDecimal</code> object,
 * optionally using a default value or throwing a 
 * {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException}
 * if a conversion error occurs.</p>
 *
 * @author Yauheny Mikulski
 */

public class BigDecimalLocaleConverter extends DecimalLocaleConverter {

    // ----------------------------------------------------------- Constructors

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will throw a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException}
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine and an unlocalized pattern is used
     * for the convertion.
     *
     */
    public BigDecimalLocaleConverter() {

        this(false);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will throw a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException}
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine.
     *
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public BigDecimalLocaleConverter(boolean locPattern) {

        this(Locale.getDefault(), locPattern);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will throw a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException}
     * if a conversion error occurs. An unlocalized pattern is used for the convertion.
     *
     * @param locale        The locale
     */
    public BigDecimalLocaleConverter(Locale locale) {

        this(locale, false);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will throw a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException}
     * if a conversion error occurs.
     *
     * @param locale        The locale
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public BigDecimalLocaleConverter(Locale locale, boolean locPattern) {

        this(locale, (String) null, locPattern);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will throw a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException}
     * if a conversion error occurs. An unlocalized pattern is used for the convertion.
     *
     * @param locale        The locale
     * @param pattern       The convertion pattern
     */
    public BigDecimalLocaleConverter(Locale locale, String pattern) {

        this(locale, pattern, false);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will throw a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.ConversionException}
     * if a conversion error occurs.
     *
     * @param locale        The locale
     * @param pattern       The convertion pattern
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public BigDecimalLocaleConverter(Locale locale, String pattern, boolean locPattern) {

        super(locale, pattern, locPattern);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will return the specified default value
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine and an unlocalized pattern is used
     * for the convertion.
     *
     * @param defaultValue  The default value to be returned
     */
    public BigDecimalLocaleConverter(Object defaultValue) {

        this(defaultValue, false);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will return the specified default value
     * if a conversion error occurs. The locale is the default locale for
     * this instance of the Java Virtual Machine.
     *
     * @param defaultValue  The default value to be returned
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public BigDecimalLocaleConverter(Object defaultValue, boolean locPattern) {

        this(defaultValue, Locale.getDefault(), locPattern);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will return the specified default value
     * if a conversion error occurs. An unlocalized pattern is used for the convertion.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     */
    public BigDecimalLocaleConverter(Object defaultValue, Locale locale) {

        this(defaultValue, locale, false);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will return the specified default value
     * if a conversion error occurs.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public BigDecimalLocaleConverter(Object defaultValue, Locale locale, boolean locPattern) {

        this(defaultValue, locale, null, locPattern);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will return the specified default value
     * if a conversion error occurs. An unlocalized pattern is used for the convertion.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     * @param pattern       The convertion pattern
     */
    public BigDecimalLocaleConverter(Object defaultValue, Locale locale, String pattern) {

        this(defaultValue, locale, pattern, false);
    }

    /**
     * Create a {@link org.openTwoFactor.clientExt.org.apache.commons.beanutils.locale.LocaleConverter} 
     * that will return the specified default value
     * if a conversion error occurs.
     *
     * @param defaultValue  The default value to be returned
     * @param locale        The locale
     * @param pattern       The convertion pattern
     * @param locPattern    Indicate whether the pattern is localized or not
     */
    public BigDecimalLocaleConverter(Object defaultValue, Locale locale, String pattern, boolean locPattern) {

        super(defaultValue, locale, pattern, locPattern);
    }

    /**
     * Convert the specified locale-sensitive input object into an output object of
     * BigDecimal type.
     *
     * @param value The input object to be converted
     * @param pattern The pattern is used for the convertion
     * @return The converted value
     *
     * @exception ConversionException if conversion cannot be performed
     *  successfully
     * @throws ParseException if an error occurs parsing a String to a Number
     * @since 1.8.0
     */
    protected Object parse(Object value, String pattern) throws ParseException {

        Object result = super.parse(value, pattern);

        if (result == null || result instanceof BigDecimal) {
            return result;
        }

        try {
            return new BigDecimal(result.toString());
        }
        catch (NumberFormatException ex) {
            throw new ConversionException("Suplied number is not of type BigDecimal: " + result);
        }

    }

}
