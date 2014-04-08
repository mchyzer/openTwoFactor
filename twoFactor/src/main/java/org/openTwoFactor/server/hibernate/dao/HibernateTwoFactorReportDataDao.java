/**
 * @author mchyzer
 * $Id: HibernateTwoFactorUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.hibernate.dao;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openTwoFactor.server.dao.TwoFactorReportDataDao;
import org.openTwoFactor.server.hibernate.ByHqlStatic;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfHibUtils;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import edu.internet2.middleware.subject.Subject;



/**
 * hibernate implementation of dao
 */
public class HibernateTwoFactorReportDataDao implements TwoFactorReportDataDao {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    System.out.println(TwoFactorDaoFactory.getFactory().getTwoFactorReportData().retrieveTotalCountByReportSystemNames(
        TwoFactorServerUtils.toList("9142", "9147")));
    System.out.println(TwoFactorDaoFactory.getFactory().getTwoFactorReportData().retrieveOptedInCountByReportSystemNames(
        TwoFactorServerUtils.toList("9142", "9147")));

    List<String> userList = TwoFactorDaoFactory.getFactory()
        .getTwoFactorReportData().retrieveUsersNotOptedInPageByReportSystemNames(
            TwoFactorServerUtils.toList("9142", "9147"));

    System.out.println(TwoFactorServerUtils.toStringForLog(
        userList));
    
    Map<String, Subject> subjectMap = TfSourceUtils.retrieveSubjectsByIdsOrIdentifiers(TfSourceUtils.mainSource(), userList, true);
    for (Subject subject : subjectMap.values()) {
      System.out.println(subject.getDescription());
    }
    
    System.out.println(TwoFactorServerUtils.toStringForLog(TwoFactorDaoFactory.getFactory().getTwoFactorReportData().retrieveReportNameSystems()));
    
  }
  
  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDataDao#retrieveTotalCountByReportSystemNames(java.util.Collection)
   */
  public int retrieveTotalCountByReportSystemNames(Collection<String> reportSystemNames) {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select count(tfrd.loginid) " 
        + " from TwoFactorReport as tfr, "
        + " TwoFactorReportData tfrd where tfr.reportNameSystem = tfrd.reportNameSystem "
        + " and tfrd.reportNameSystem in ( ");

    sql.append(TfHibUtils.convertToInClause(reportSystemNames, byHqlStatic));

    sql.append(" ) ");

    
    long count = byHqlStatic.createQuery(sql.toString())
      .uniqueResult(Long.class);
    return (int)count;

  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDataDao#retrieveOptedInCountByReportSystemNames(java.util.Collection)
   */
  public int retrieveOptedInCountByReportSystemNames(Collection<String> reportSystemNames) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select count(tfrd.loginid) " 
        + " from TwoFactorReport as tfr, "
        + " TwoFactorReportData tfrd where tfr.reportNameSystem = tfrd.reportNameSystem "
        + " and tfrd.reportNameSystem in ( ");

    sql.append(TfHibUtils.convertToInClause(reportSystemNames, byHqlStatic));

    sql.append(" ) and exists (select 1 from TwoFactorUserView as tfuv where tfuv.loginid = tfrd.loginid and tfuv.optedIn = 'T')  ");
    
    long count = byHqlStatic.createQuery(sql.toString())
      .uniqueResult(Long.class);
    return (int)count;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDataDao#retrieveUsersNotOptedInPageByReportSystemNames(java.util.Collection)
   */
  public List<String> retrieveUsersNotOptedInPageByReportSystemNames(Collection<String> reportSystemNames) {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select distinct(tfrd.loginid) " 
        + " from TwoFactorReport as tfr, "
        + " TwoFactorReportData tfrd where tfr.reportNameSystem = tfrd.reportNameSystem "
        + " and tfrd.reportNameSystem in ( ");

    sql.append(TfHibUtils.convertToInClause(reportSystemNames, byHqlStatic));

    sql.append(" ) and not exists (select 1 from TwoFactorUserView as tfuv where tfuv.loginid = tfrd.loginid and tfuv.optedIn = 'T')  ");
    
    List<String> loginids = byHqlStatic.createQuery(sql.toString())
      .list(String.class);
    return loginids;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportDataDao#retrieveReportNameSystems()
   */
  public List<String> retrieveReportNameSystems() {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select distinct(tfrd.reportNameSystem) " 
        + " from TwoFactorReportData tfrd order by tfrd.reportNameSystem ");
    
    List<String> reportSystemNames = byHqlStatic.createQuery(sql.toString())
      .list(String.class);
    return reportSystemNames;
  }


}
