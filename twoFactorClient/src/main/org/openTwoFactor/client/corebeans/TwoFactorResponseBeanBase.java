package org.openTwoFactor.client.corebeans;



/**
 * base class that beans extends
 * @author mchyzer
 */
public abstract class TwoFactorResponseBeanBase {

  /**
   * "debug" input param: true|false If true or if a param is set on the server, 
   * then a debug message will be returned
   */
  private String debugMessage = null;
  
  /**
   * code which doesnt change which can be parsed about the state of the result
   */
  private String resultCode;
  
  
  /**
   * code which doesnt change which can be parsed about the state of the result
   * @return the resultCode
   */
  public String getResultCode() {
    return this.resultCode;
  }

  
  /**
   * code which doesnt change which can be parsed about the state of the result
   * @param resultCode1 the resultCode to set
   */
  public void setResultCode(String resultCode1) {
    this.resultCode = resultCode1;
  }

  /**
   * friendly message that could change, this is for logging and shouldnt be parsed
   * e.g. User is authenticated"
   */
  private String responseMessage;

  /**
   * friendly message that could change, this is for logging and shouldnt be parsed
   * e.g. User is authenticated"
   * @return the responseMessage
   */
  public String getResponseMessage() {
    return this.responseMessage;
  }
  
  /**
   * friendly message that could change, this is for logging and shouldnt be parsed
   * e.g. User is authenticated"
   * @param responseMessage1 the responseMessage to set
   */
  public void setResponseMessage(String responseMessage1) {
    this.responseMessage = responseMessage1;
  }

  /** 
   * if there are warnings, they will be there
   */
  private String warning = null;

  /**
   * if there are warnings, they will be there
   * @return any warnings
   */
  public String getWarning() {
    return this.warning;
  }


  /**
   * @param resultWarnings1 the resultWarnings to set
   */
  public void setWarning(String resultWarnings1) {
    this.warning = resultWarnings1;
  }

  /**
   * if the request was valid and processed correctly
   */
  private Boolean success;
  /**
   * if there is an error (not success), this is the error message)
   */
  private String errorMessage;

  /**
   * 
   */
  public TwoFactorResponseBeanBase() {
  }

  /**
   * if the request was valid and processed correctly
   * @return if success
   */
  public Boolean getSuccess() {
    return this.success;
  }

  /**
   * if the request was valid and processed correctly
   * @param success1
   */
  public void setSuccess(Boolean success1) {
    this.success = success1;
  }

  /**
   * if there is an error (not success), this is the error message)
   * @return error
   */
  public String getErrorMessage() {
    return this.errorMessage;
  }

  /**
   * if there is an error (not success), this is the error message)
   * @param errorMessage1
   */
  public void setErrorMessage(String errorMessage1) {
    this.errorMessage = errorMessage1;
  }

  /**
   * if there are debug messages, they will be there
   * @return any debugs
   */
  public String getDebugMessage() {
    return this.debugMessage;
  }

  /**
   * @param debugMessage1 the debug to set
   */
  public void setDebugMessage(String debugMessage1) {
    this.debugMessage = debugMessage1;
  }


  
}
