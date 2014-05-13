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

    int batch = Integer.parseInt(args[0]);
    
    for (int i=0;i<20000;i++) {

      List<String> theList = HibernateSession.bySqlStatic().listSelect(String.class,
          "SELECT uuid FROM two_factor_audit tfa where tfa.the_Timestamp < 1397966400000 and tfa.action = 'AUTHN_NOT_OPTED_IN' and rownum < 30000", null);

      System.out.println("Selected : " + i + ": " + new Date());
//      for (String uuid : theList) {
//        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
//        String sql = "delete from TwoFactorAudit where uuid = :theUuid";
//        byHqlStatic.createQuery(sql).setString("theUuid", uuid).executeUpdate();
//      }      

      for (int j=0;j<10;j++) {
        try {
          List<String> batchUuids = TwoFactorServerUtils.batchList(theList, 100, batch*30+j);
          ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
          String sql = "delete from TwoFactorAudit where uuid in (" + TfHibUtils.convertToInClause(batchUuids, byHqlStatic) + ")";
          byHqlStatic.createQuery(sql).executeUpdate();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      System.out.println("Done " + i + ": " + new Date());
    }
    
  }

}
