
package org.openTwoFactor.server.hibernate;

import java.io.Serializable;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * hibernate should be able to tell if an assigned key and version
 * are insert or update, but it cant, so tell it
 */
public class TfSessionInterceptor extends EmptyInterceptor implements Serializable {

  /**
   * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
   */
  @Override
  public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState,
      Object[] previousState, String[] propertyNames, Type[] types) {
    if (entity instanceof TfVersioned) {
      //find the current version
      long newVersion = (Long)TfHibUtils.propertyValue(currentState, propertyNames, 
          TwoFactorHibernateBeanBase.FIELD_VERSION_NUMBER) + 1;
      //increment by one, granted this should always by 0
      TfHibUtils.assignProperty(currentState, propertyNames, 
          TwoFactorHibernateBeanBase.FIELD_VERSION_NUMBER, newVersion);
      //assign back to object since might not flush in time
      TwoFactorServerUtils.assignField(entity, TwoFactorHibernateBeanBase.FIELD_VERSION_NUMBER, newVersion);
      //edited it
      return true;
    }
    //not edited
    return false;
  }

  /**
   * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
   */
  @Override
  public boolean onSave(Object entity, Serializable id, Object[] state,
      String[] propertyNames, Type[] types) {
    if (entity instanceof TfVersioned) {
      //find the current version
      long newVersion = (Long)TfHibUtils.propertyValue(state, propertyNames, 
          TwoFactorHibernateBeanBase.FIELD_VERSION_NUMBER) + 1;
      //increment by one, granted this should always by 0
      TfHibUtils.assignProperty(state, propertyNames, 
          TwoFactorHibernateBeanBase.FIELD_VERSION_NUMBER, newVersion);
      //assign back to object since might not flush in time
      TwoFactorServerUtils.assignField(entity, TwoFactorHibernateBeanBase.FIELD_VERSION_NUMBER, newVersion);
      //edited it
      return true;
    }
    //not edited
    return false;
  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * @see org.hibernate.EmptyInterceptor#isTransient(java.lang.Object)
   */
  @Override
  public Boolean isTransient(Object entity) {
    if (entity instanceof TfVersioned) {
      return ((TwoFactorHibernateBeanBase)entity).getVersionNumber() < 0;
    }
    return super.isTransient(entity);
  }

}
