import java.util.Date;
import java.util.List;

import org.openTwoFactor.server.hibernate.ByHqlStatic;
import org.openTwoFactor.server.hibernate.HibernateSession;
import org.openTwoFactor.server.hibernate.TfHibUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 * @author mchyzer
 * $Id$
 */

/**
 *
 */
public class DeleteAudit {

  /**
   * 
   */
  public DeleteAudit() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    
    long since = System.currentTimeMillis() - (1000*60*60*24*15);
    System.out.println("Records before " + since);
    
    if (true) {
      return;
    }
    
    for (int i=0;i<100000;i++) {

      long now = System.currentTimeMillis();
      
      List<String> theList = HibernateSession.bySqlStatic().listSelect(String.class,
          "SELECT uuid FROM two_factor_audit tfa where tfa.the_Timestamp < " 
              + since + " and tfa.action = 'AUTHN_NOT_OPTED_IN' and rownum < 400", null);

      System.out.println("Selected : " + i + ": " + new Date() + ", " + theList.size());

      if (theList.size() == 0) {
        break;
      }
      
//      for (String uuid : theList) {
//        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
//        String sql = "delete from TwoFactorAudit where uuid = :theUuid";
//        byHqlStatic.createQuery(sql).setString("theUuid", uuid).executeUpdate();
//      }      

      for (int j=0;j<TwoFactorServerUtils.batchNumberOfBatches(theList, 100);j++) {
        try {
          List<String> batchUuids = TwoFactorServerUtils.batchList(theList, 100, j);
          ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
          String sql = "delete from TwoFactorAudit where uuid in (" + TfHibUtils.convertToInClause(batchUuids, byHqlStatic) + ")";
          byHqlStatic.createQuery(sql).executeUpdate();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      
      //sleep a minute
      long millisToSleep = 60000 - (System.currentTimeMillis() - now);

      System.out.println("Done " + i + ": " + new Date() + ", sleeping " + millisToSleep + "ms");

      TwoFactorServerUtils.sleep(millisToSleep);
      
    }
    
  }

}
