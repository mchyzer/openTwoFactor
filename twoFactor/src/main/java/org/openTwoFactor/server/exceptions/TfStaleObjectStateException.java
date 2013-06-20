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
 * $Id: TfStaleObjectStateException.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.exceptions;


/**
 * specific way to say that someone else has edited this object,
 * user should refresh object state and make changes again
 */
@SuppressWarnings("serial")
public class TfStaleObjectStateException extends RuntimeException {

  /**
   * 
   */
  public TfStaleObjectStateException() {
  }

  /**
   * @param message
   */
  public TfStaleObjectStateException(String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public TfStaleObjectStateException(Throwable cause) {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public TfStaleObjectStateException(String message, Throwable cause) {
    super(message, cause);
  }

}
