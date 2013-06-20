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
package org.openTwoFactor.clientExt.org.apache.commons.beanutils.expression;

/**
 * Default Property Name Expression {@link Resolver} Implementation.
 * <p>
 * This class assists in resolving property names in the following five formats,
 * with the layout of an identifying String in parentheses:
 * <ul>
 * <li><strong>Simple (<code>name</code>)</strong> - The specified
 *     <code>name</code> identifies an individual property of a particular
 *     JavaBean.  The name of the actual getter or setter method to be used
 *     is determined using standard JavaBeans instrospection, so that (unless
 *     overridden by a <code>BeanInfo</code> class, a property named "xyz"
 *     will have a getter method named <code>getXyz()</code> or (for boolean
 *     properties only) <code>isXyz()</code>, and a setter method named
 *     <code>setXyz()</code>.</li>
 * <li><strong>Nested (<code>name1.name2.name3</code>)</strong> The first
 *     name element is used to select a property getter, as for simple
 *     references above.  The object returned for this property is then
 *     consulted, using the same approach, for a property getter for a
 *     property named <code>name2</code>, and so on.  The property value that
 *     is ultimately retrieved or modified is the one identified by the
 *     last name element.</li>
 * <li><strong>Indexed (<code>name[index]</code>)</strong> - The underlying
 *     property value is assumed to be an array, or this JavaBean is assumed
 *     to have indexed property getter and setter methods.  The appropriate
 *     (zero-relative) entry in the array is selected.  <code>List</code>
 *     objects are now also supported for read/write.  You simply need to define
 *     a getter that returns the <code>List</code></li>
 * <li><strong>Mapped (<code>name(key)</code>)</strong> - The JavaBean
 *     is assumed to have an property getter and setter methods with an
 *     additional attribute of type <code>java.lang.String</code>.</li>
 * <li><strong>Combined (<code>name1.name2[index].name3(key)</code>)</strong> -
 *     Combining mapped, nested, and indexed references is also
 *     supported.</li>
 * </ul>
 *
 * @version $Revision: 1.1 $ $Date: 2013/06/20 06:16:07 $
 * @since 1.8.0
 */
public class DefaultResolver implements Resolver {

    private static final char NESTED        = '.';
    private static final char MAPPED_START  = '(';
    private static final char MAPPED_END    = ')';
    private static final char INDEXED_START = '[';
    private static final char INDEXED_END   = ']';

    /**
     * Default Constructor.
     */
    public DefaultResolver() {
    }

    /**
     * Return the index value from the property expression or -1.
     *
     * @param expression The property expression
     * @return The index value or -1 if the property is not indexed
     * @throws IllegalArgumentException If the indexed property is illegally
     * formed or has an invalid (non-numeric) value.
     */
    public int getIndex(String expression) {
        if (expression == null || expression.length() == 0) {
            return -1;
        }
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == NESTED || c == MAPPED_START) {
                return -1;
            } else if (c == INDEXED_START) {
                int end = expression.indexOf(INDEXED_END, i);
                if (end < 0) {
                    throw new IllegalArgumentException("Missing End Delimiter");
                }
                String value = expression.substring(i + 1, end);
                if (value.length() == 0) {
                    throw new IllegalArgumentException("No Index Value");
                }
                int index = 0;
                try {
                    index = Integer.parseInt(value, 10);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid index value '"
                            + value + "'");
                }
                return index;
            }
        }
        return -1;
    }

    /**
     * Return the map key from the property expression or <code>null</code>.
     *
     * @param expression The property expression
     * @return The index value
     * @throws IllegalArgumentException If the mapped property is illegally formed.
     */
    public String getKey(String expression) {
        if (expression == null || expression.length() == 0) {
            return null;
        }
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == NESTED || c == INDEXED_START) {
                return null;
            } else if (c == MAPPED_START) {
                int end = expression.indexOf(MAPPED_END, i);
                if (end < 0) {
                    throw new IllegalArgumentException("Missing End Delimiter");
                }
                return expression.substring(i + 1, end);
            }
        }
        return null;
    }

    /**
     * Return the property name from the property expression.
     *
     * @param expression The property expression
     * @return The property name
     */
    public String getProperty(String expression) {
        if (expression == null || expression.length() == 0) {
            return expression;
        }
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == NESTED) {
                return expression.substring(0, i);
            } else if (c == MAPPED_START || c == INDEXED_START) {
                return expression.substring(0, i);
            }
        }
        return expression;
    }

    /**
     * Indicates whether or not the expression
     * contains nested property expressions or not.
     *
     * @param expression The property expression
     * @return The next property expression
     */
    public boolean hasNested(String expression) {
        if (expression == null || expression.length() == 0) {
            return false;
        } else {
            return (remove(expression) != null);
        }
    }

    /**
     * Indicate whether the expression is for an indexed property or not.
     *
     * @param expression The property expression
     * @return <code>true</code> if the expresion is indexed,
     *  otherwise <code>false</code>
     */
    public boolean isIndexed(String expression) {
        if (expression == null || expression.length() == 0) {
            return false;
        }
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == NESTED || c == MAPPED_START) {
                return false;
            } else if (c == INDEXED_START) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicate whether the expression is for a mapped property or not.
     *
     * @param expression The property expression
     * @return <code>true</code> if the expresion is mapped,
     *  otherwise <code>false</code>
     */
    public boolean isMapped(String expression) {
        if (expression == null || expression.length() == 0) {
            return false;
        }
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == NESTED || c == INDEXED_START) {
                return false;
            } else if (c == MAPPED_START) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract the next property expression from the
     * current expression.
     *
     * @param expression The property expression
     * @return The next property expression
     */
    public String next(String expression) {
        if (expression == null || expression.length() == 0) {
            return null;
        }
        boolean indexed = false;
        boolean mapped  = false;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (indexed) {
                if (c == INDEXED_END) {
                    return expression.substring(0, i + 1);
                }
            } else if (mapped) {
                if (c == MAPPED_END) {
                    return expression.substring(0, i + 1);
                }
            } else {
                if (c == NESTED) {
                    return expression.substring(0, i);
                } else if (c == MAPPED_START) {
                    mapped = true;
                } else if (c == INDEXED_START) {
                    indexed = true;
                }
            }
        }
        return expression;
    }

    /**
     * Remove the last property expresson from the
     * current expression.
     *
     * @param expression The property expression
     * @return The new expression value, with first property
     * expression removed - null if there are no more expressions
     */
    public String remove(String expression) {
        if (expression == null || expression.length() == 0) {
            return null;
        }
        String property = next(expression);
        if (expression.length() == property.length()) {
            return null;
        }
        int start = property.length();
        if (expression.charAt(start) == NESTED) {
            start++;
        }
        return expression.substring(start);
    }
}
