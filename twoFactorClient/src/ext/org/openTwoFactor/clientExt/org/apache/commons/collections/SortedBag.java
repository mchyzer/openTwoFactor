/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openTwoFactor.clientExt.org.apache.commons.collections;

import java.util.Comparator;

/**
 * Defines a type of <code>Bag</code> that maintains a sorted order among
 * its unique representative members.
 *
 * @since Commons Collections 2.0
 * @version $Revision: 1.1 $ $Date: 2013/06/20 06:16:03 $
 * 
 * @author Chuck Burdick
 */
public interface SortedBag extends Bag {

    /**
     * Returns the comparator associated with this sorted set, or null
     * if it uses its elements' natural ordering.
     * 
     * @return the comparator in use, or null if natural ordering
     */
    public Comparator comparator();

    /**
     * Returns the first (lowest) member.
     * 
     * @return the first element in the sorted bag
     */
    public Object first();

    /**
     * Returns the last (highest) member.
     * 
     * @return the last element in the sorted bag
     */
    public Object last();
    
}
