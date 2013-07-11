
/**
 * 
 */
package org.openTwoFactor.server.status;

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openTwoFactor.server.config.TwoFactorServerConfig;



/**
 * see if the server is out of memory
 * @author mchyzer
 *
 */
public class DiagnosticMemoryTest extends DiagnosticTask {

  /**
   * 
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DiagnosticMemoryTest;
  }
  
  /**
   * 
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder().toHashCode();
  }

  /**
   * @see org.openTwoFactor.server.status.DiagnosticsTask#doTask()
   */
  @Override
  protected boolean doTask() {
    
    int bytesToAllocate = bytesToAllocate();
    
    @SuppressWarnings("unused")
    byte[] someArray = new byte[bytesToAllocate];
    
    this.appendSuccessTextLine("Allocating " + bytesToAllocate() + " bytes to an array to make sure not out of memory");
    
    return true;
    
  }

  /**
   * bytes to allocate
   * @return bytes
   */
  private int bytesToAllocate() {
    return TwoFactorServerConfig.retrieveConfig().propertyValueInt("status.diagnostics.bytesToAllocate", 100000);
  }

  /**
   * @see org.openTwoFactor.server.status.DiagnosticsTask#retrieveName()
   */
  @Override
  public String retrieveName() {
    
    return "memoryTest";
  }

  /**
   * @see org.openTwoFactor.server.status.DiagnosticsTask#retrieveNameFriendly()
   */
  @Override
  public String retrieveNameFriendly() {
    return "Memory test";
  }

}
