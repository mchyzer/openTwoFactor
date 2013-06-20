
/*
 * @author mchyzer
 * $Id: TfHibUtilsMapping.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Value;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * metadata and other methods for hibernate
 */
public class TfHibUtilsMapping {

  /**
   * see if this object would be an insert for hibernate, or if it would be an update.
   * Note this only works if we dont have assigned keys...
   * @param object
   * @return true if insert
   */
  public static boolean isInsert(Object object) {
//    Session session = hibernateSession.getSession();
//    AbstractSessionImpl abstractSessionImpl = (AbstractSessionImpl)session;
//    EntityEntry entityEntry = abstractSessionImpl.getPersistenceContext().getEntry(object);
//    entityEntry.isExistsInDatabase()
    Serializable id = primaryKeyCurrentValue(object);
    
    if (id == null) {
      return true;
    }
    
    //see if object is hibernate versionable
    if (object instanceof TwoFactorHibernateBeanBase) {
      return ((TwoFactorHibernateBeanBase)object).getVersionNumber() < 0;
    }
    
    //if null, then see if it is an assigned key
    Value identifierValue = primaryKeyValue(object.getClass());
    String generator = null;
    if (identifierValue instanceof SimpleValue) {
      generator = ((SimpleValue)identifierValue).getIdentifierGeneratorStrategy();
      if (StringUtils.equals("uuid.hex", generator)) {
        return false;
      }
    }
    //then how do we know???
    throw new RuntimeException("Cant tell if insert if assigned key! " 
        + TwoFactorServerUtils.className(object) + ", " + generator);
  }

  /**
   * get the hibernate mapping property of a mapped class
   * 
   * @param clazz
   * @return the property
   */
  public static Property primaryKeyProperty(Class clazz) {
    clazz = TwoFactorServerUtils.unenhanceClass(clazz);
    Configuration configuration = TwoFactorDaoFactory.getFactory().getConfiguration();
    PersistentClass persistentClass = 
          configuration.getClassMapping(clazz.getName());
    Property property = persistentClass.getIdentifierProperty();
    return property;
  }

  /**
   * get the hibernate primary key property Value object
   * @param clazz
   * @return the value
   */
  public static Value primaryKeyValue(Class clazz) {
    Property primaryKeyProperty = primaryKeyProperty(clazz);
    Value value = primaryKeyProperty.getValue();
    return value;
  }
  
  /**
   * get the hibernate primary key property value (current value)
   * @param object
   * @return the value
   */
  public static Serializable primaryKeyCurrentValue(Object object) {
    Property primaryKeyProperty = primaryKeyProperty(object.getClass());
    String propertyName = primaryKeyProperty.getName();
    Serializable value = (Serializable)TwoFactorServerUtils.propertyValue(object, propertyName);
    return value;
  }
  
  
}
