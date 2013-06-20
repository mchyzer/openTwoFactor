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
package org.openTwoFactor.clientExt.org.apache.commons.jexl2;

/**
 * Manages variables which can be referenced in a JEXL expression.
 *
 *  @since 1.0
 *  @version $Id: JexlContext.java,v 1.1 2013/06/20 06:16:07 mchyzer Exp $
 */
public interface JexlContext {
    /**
     * Gets the value of a variable.
     * @param name the variable's name
     * @return the value
     */
    Object get(String name);

    /**
     * Sets the value of a variable.
     * @param name the variable's name
     * @param value the variable's value
     */
    void set(String name, Object value);

    /**
     * Checks whether a variable is defined in this context.
     * <p>A variable may be defined with a null value; this method checks whether the
     * value is null or if the variable is undefined.</p>
     * @param name the variable's name
     * @return true if it exists, false otherwise
     */
    boolean has(String name);
}
