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
 * $Header: /var/cvs/isc/projects/twoFactorClient/src/ext/org/openTwoFactor/clientExt/org/apache/commons/httpclient/CircularRedirectException.java,v 1.1 2013/06/20 06:16:04 mchyzer Exp $
 * $Revision: 1.1 $
 * $Date: 2013/06/20 06:16:04 $
 *
 * ====================================================================
 *
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.openTwoFactor.clientExt.org.apache.commons.httpclient;

/**
 * Signals a circular redirect
 * 
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 * 
 * @since 3.0
 */
public class CircularRedirectException extends RedirectException {

    /**
     * Creates a new CircularRedirectException with a <tt>null</tt> detail message. 
     */
    public CircularRedirectException() {
        super();
    }

    /**
     * Creates a new CircularRedirectException with the specified detail message.
     * 
     * @param message The exception detail message
     */
    public CircularRedirectException(String message) {
        super(message);
    }

    /**
     * Creates a new CircularRedirectException with the specified detail message and cause.
     * 
     * @param message the exception detail message
     * @param cause the <tt>Throwable</tt> that caused this exception, or <tt>null</tt>
     * if the cause is unavailable, unknown, or not a <tt>Throwable</tt>
     */
    public CircularRedirectException(String message, Throwable cause) {
        super(message, cause);
    }
}
