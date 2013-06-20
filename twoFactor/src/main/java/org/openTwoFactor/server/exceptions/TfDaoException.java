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
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package org.openTwoFactor.server.exceptions;

/**
 * Generic DAO exception.
 * @version $Id: TfDaoException.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
public class TfDaoException extends RuntimeException {
  
  /**
   * 
   */
  private static final long serialVersionUID = -7856283917603254749L;
  
  /**
   */
  public TfDaoException() { 
    super(); 
  }
  
  /**
   * @param msg 
   */
  public TfDaoException(String msg) { 
    super(msg); 
  }
  
  /**
   * @param msg 
   * @param cause 
   */
  public TfDaoException(String msg, Throwable cause) { 
    super(msg, cause); 
  }
  
  /**
   * @param cause 
   */
  public TfDaoException(Throwable cause) { 
    super(cause); 
  }

} 

