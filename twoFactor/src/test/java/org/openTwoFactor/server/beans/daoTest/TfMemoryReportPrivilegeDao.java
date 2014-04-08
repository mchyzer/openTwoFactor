/**
 * @author mchyzer
 * $Id: TfMemoryUserDao.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package org.openTwoFactor.server.beans.daoTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorReportPrivilege;
import org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 * 
 */
public class TfMemoryReportPrivilegeDao implements TwoFactorReportPrivilegeDao {

  /**
   * users
   */
  public static List<TwoFactorReportPrivilege> reportPrivileges = Collections.synchronizedList(new ArrayList<TwoFactorReportPrivilege>());

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#delete(TwoFactorReportPrivilege)
   */
  @Override
  public void delete(TwoFactorReportPrivilege twoFactorReportPrivilege) {
    Iterator<TwoFactorReportPrivilege> iterator = reportPrivileges.iterator();
    while (iterator.hasNext()) {
      TwoFactorReportPrivilege current = iterator.next();
      if (current == twoFactorReportPrivilege || StringUtils.equals(twoFactorReportPrivilege.getUuid(), current.getUuid())) {
        iterator.remove();
      }
    }
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#store(TwoFactorReportPrivilege)
   */
  @Override
  public void store(TwoFactorReportPrivilege twoFactorReportPrivilege) {
    if (StringUtils.isBlank(twoFactorReportPrivilege.getUuid())) {
      throw new RuntimeException("uuid is blank");
    }

    delete(twoFactorReportPrivilege);
    reportPrivileges.add(twoFactorReportPrivilege);
  }

  /**
   * 
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#retrieveByUuid(java.lang.String)
   */
  @Override
  public TwoFactorReportPrivilege retrieveByUuid(String uuid) {
    for (TwoFactorReportPrivilege twoFactorReportPrivilege : TwoFactorServerUtils.nonNull(reportPrivileges)) {
      if (StringUtils.equals(uuid, twoFactorReportPrivilege.getUuid())) {
        return twoFactorReportPrivilege;
      }
    }
    return null;
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#retrieveAll()
   */
  @Override
  public List<TwoFactorReportPrivilege> retrieveAll() {
    return new ArrayList<TwoFactorReportPrivilege>(reportPrivileges);
  }

  /**
   * @see org.openTwoFactor.server.dao.TwoFactorReportPrivilegeDao#retrieveByUserUuid(java.lang.String)
   */
  @Override
  public List<TwoFactorReportPrivilege> retrieveByUserUuid(String userUuid) {
    List<TwoFactorReportPrivilege> result = new ArrayList<TwoFactorReportPrivilege>();
    for (TwoFactorReportPrivilege twoFactorReportPrivilege : reportPrivileges) {
      if (StringUtils.equals(userUuid, twoFactorReportPrivilege.getUserUuid())) {
        result.add(twoFactorReportPrivilege);
      }
    }
    return result;
  }

}
