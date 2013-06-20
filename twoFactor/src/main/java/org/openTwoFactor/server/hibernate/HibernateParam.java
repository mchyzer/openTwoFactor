
/**
 * 
 */
package org.openTwoFactor.server.hibernate;

/**
 * param for a query
 * @author mchyzer
 *
 */
class HibernateParam {

  /** name of param */
  private String name;
  
  /** value of param */
  private Object value;
  
  /** type of param */
  private Class type;

  /**
   * name or param
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * name or param
   * @param name1 the name to set
   */
  public void setName(String name1) {
    this.name = name1;
  }

  /**
   * value of param
   * @return the value
   */
  public Object getValue() {
    return this.value;
  }

  /**
   * value of param
   * @param value1 the value to set
   */
  public void setValue(Object value1) {
    this.value = value1;
  }

  /**
   * type of param
   * @return the type
   */
  public Class getType() {
    return this.type;
  }

  /**
   * to string
   * @return the string
   */
  @Override
  public String toString() {
    return "Param (" + this.type + "): '" + this.name + "'->'" + this.value + "'"; 
  }
  /** no arg constructor */
  public HibernateParam() {
    
  }
  
  /**
   * constructor with fields
   * @param name1 name
   * @param value1 value
   * @param type1 type
   */
  public HibernateParam(String name1, Object value1, Class type1) {
    this.name = name1;
    this.value = value1;
    this.type = type1;
  }

  /**
   * type of param
   * @param type1 the type to set
   */
  public void setType(Class type1) {
    this.type = type1;
  }
  
  
}
