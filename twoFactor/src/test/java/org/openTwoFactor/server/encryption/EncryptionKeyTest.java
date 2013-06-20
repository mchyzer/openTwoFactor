/**
 * @author mchyzer
 * $Id: EncryptionKeyTest.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.encryption;

import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.encryption.EncryptionKey;
import org.openTwoFactor.server.util.TwoFactorServerUtils;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class EncryptionKeyTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run((new EncryptionKeyTest("testEncrypt")));
  }
  
  /**
   * @param name
   */
  public EncryptionKeyTest(String name) {
    super(name);
  }

  /**
   * Test method for {@link org.openTwoFactor.server.encryption.EncryptionKey#encrypt(java.lang.String)}.
   */
  public void testEncrypt() {
    
    String data = "asdfghjkl;";
    String encryptedData = EncryptionKey.encrypt(data);
    assertTrue(encryptedData.contains("__"));
    assertEquals(data, EncryptionKey.decrypt(encryptedData));
    
    TwoFactorServerUtils.sleep(100);
    
    String newEncryptedData = EncryptionKey.encrypt(data);
    
    assertTrue(!StringUtils.equals(encryptedData, newEncryptedData));
    
    assertEquals(data, EncryptionKey.decrypt(newEncryptedData));
    assertEquals(data, EncryptionKey.decrypt(encryptedData));
    
  }

}
