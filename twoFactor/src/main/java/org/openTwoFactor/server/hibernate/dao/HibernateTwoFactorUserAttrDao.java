/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserAttrDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.openTwoFactor.server.beans.TwoFactorUserAttr;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.dao.TwoFactorUserAttrDao;
import org.openTwoFactor.server.exceptions.TfDaoException;
import org.openTwoFactor.server.hibernate.BySql;
import org.openTwoFactor.server.hibernate.HibernateHandler;
import org.openTwoFactor.server.hibernate.HibernateHandlerBean;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfAuditControl;
import org.openTwoFactor.server.hibernate.TfQueryOptions;
import org.openTwoFactor.server.hibernate.TwoFactorTransactionType;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorUserAttrDao implements TwoFactorUserAttrDao {

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserDao#delete(org.openTwoFactor.server.beans.TwoFactorUser)
   */
  @Override
  public void delete(TwoFactorUserAttr twoFactorUserAttr) {
    if (twoFactorUserAttr == null) {
      throw new NullPointerException("twoFactorUserAttr is null");
    }
    
    HibernateSession.byObjectStatic().delete(twoFactorUserAttr);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveByUser(java.lang.String)
   */
  @Override
  public Set<TwoFactorUserAttr> retrieveByUser(String userUuid) {
    if (TwoFactorServerUtils.isBlank(userUuid)) {
      throw new RuntimeException("Why is userUuid blank? ");
    }
    
    List<TwoFactorUserAttr> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfua from TwoFactorUserAttr as tfua where tfua.userUuid = :theUserUuid")
        .setString("theUserUuid", userUuid).list(TwoFactorUserAttr.class);
    return new TreeSet<TwoFactorUserAttr>(theList);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveByUserAndAttributeName(java.lang.String, java.lang.String)
   */
  @Override
  public TwoFactorUserAttr retrieveByUserAndAttributeName(String userUuid,
      String attributeName) {
    if (TwoFactorServerUtils.isBlank(userUuid)) {
      throw new RuntimeException("Why is userUuid blank? ");
    }
    
    List<TwoFactorUserAttr> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfu from TwoFactorUserAttr as tfua where tfua.userUuid = :theUserUuid and tfua.attributeName = :theAttributeName")
        .setString("theUserUuid", userUuid)
        .setString("theAttributeName", attributeName)
        .list(TwoFactorUserAttr.class);
    return TwoFactorServerUtils.listPopOne(theList);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveByAttributeName(java.lang.String)
   */
  @Override
  public List<TwoFactorUserAttr> retrieveByAttributeName(String attributeName) {
    if (TwoFactorServerUtils.isBlank(attributeName)) {
      throw new RuntimeException("Why is attributeName blank? ");
    }
    
    List<TwoFactorUserAttr> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfua from TwoFactorUserAttr as tfua where tfua.attributeName = :theAttributeName order by tfua.uuid")
        .setString("theAttributeName", attributeName)
        .list(TwoFactorUserAttr.class);
    return theList;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#store(org.openTwoFactor.server.beans.TwoFactorUserAttr)
   */
  @Override
  public void store(final TwoFactorUserAttr twoFactorUserAttr) {
    if (twoFactorUserAttr == null) {
      throw new NullPointerException("twoFactorUser is null");
    }

    if (HibernateSession.isReadonlyMode()) {
      return;
    }
    try {

      HibernateSession.callbackHibernateSession(TwoFactorTransactionType.READ_WRITE_OR_USE_EXISTING, TfAuditControl.WILL_NOT_AUDIT, new HibernateHandler() {
        
        public Object callback(HibernateHandlerBean hibernateHandlerBean) throws TfDaoException {
  
          HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
  
          boolean isUpdateDirect = false;
          
          if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("tfUserAttrUseUpdateStatement", true)) {
  
            //if we know its an update
            isUpdateDirect = twoFactorUserAttr.getVersionNumber() != null && twoFactorUserAttr.getVersionNumber() != -1;
            
            if (!isUpdateDirect) {
              
              hibernateSession.misc().flush();

              //see if its there
              List<TwoFactorUserAttr> theList = HibernateSession.byHqlStatic().createQuery(
                  "select tfu from TwoFactorUserAttr as tfua where tfua.userUuid = :theUserUuid and tfua.attributeName = :theAttributeName")
                  .setString("theUserUuid", twoFactorUserAttr.getUserUuid())
                  .setString("theAttributeName", twoFactorUserAttr.getAttributeName())
                  .list(TwoFactorUserAttr.class);
              TwoFactorUserAttr twoFactorUserAttrCurrent =  TwoFactorServerUtils.listPopOne(theList);
                
              
              if (twoFactorUserAttrCurrent != null) {
                
                isUpdateDirect = true;
                
              }
            }
            if (isUpdateDirect) {

              BySql bySql = hibernateSession.bySql();
              String sql = "update two_factor_user_attr tfua "
                  + "set attribute_value_string = ?, "
                  + "attribute_value_integer = ?, "
                  + "last_updated = ?, "
                  + "version_number = ? "
                  + "where uuid = ?" ;
              int rows = bySql.executeSql(sql, 
                  TwoFactorServerUtils.toListObject(twoFactorUserAttr.getAttributeValueString(), 
                      twoFactorUserAttr.getAttributeValueInteger(),
                      System.currentTimeMillis(), twoFactorUserAttr.getVersionNumber()+1,
                      twoFactorUserAttr.getUuid()));
              if (rows != 1) {
                throw new RuntimeException("Why is rows not 1? " + rows + ", uuid: " + twoFactorUserAttr.getUuid() + ", userUuid: " + twoFactorUserAttr.getUserUuid() + ", attributeName: " + twoFactorUserAttr.getAttributeName());
              }

            }
                      
          }
          
          if (!isUpdateDirect) {
            //this will insert or update with hibernate using optimistic locking
            HibernateSession.byObjectStatic().saveOrUpdate(twoFactorUserAttr);
          }
          
          if (TwoFactorServerConfig.retrieveConfig().propertyValueBoolean("tfUserAttrFlushAfter", true)) {
            hibernateSession.misc().flush();
          }
          return null;
        }
      });
    } catch (RuntimeException e) {
      TwoFactorServerUtils.injectInException(e, "Exception in attr: user_uuid" + twoFactorUserAttr.getUserUuid() + ", attrName: " + twoFactorUserAttr.getAttributeName()
          + ", valueString: '" + twoFactorUserAttr.getAttributeValueString() + "', valueNumber: " + twoFactorUserAttr.getAttributeValueInteger());
      throw e;
    }
        
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorUserAttrDao#retrieveDeletedOlderThanAge(long)
   */
  @Override
  public List<TwoFactorUserAttr> retrieveDeletedOlderThanAge(long selectBeforeThisMilli) {
    List<TwoFactorUserAttr> theList = HibernateSession.byHqlStatic().createQuery(
        "select tfua from TwoFactorUserAttr as tfua where tfua.deletedOn is not null and tfua.deletedOn < :selectBeforeThisMilli ")
        .setLong("selectBeforeThisMilli", selectBeforeThisMilli)
        .options(new TfQueryOptions().paging(1000, 1,false))
        .list(TwoFactorUserAttr.class);
    return theList;

  }

}
