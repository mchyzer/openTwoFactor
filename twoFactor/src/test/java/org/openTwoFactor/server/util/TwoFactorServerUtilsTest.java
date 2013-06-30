package org.openTwoFactor.server.util;

import junit.framework.TestCase;
import junit.textui.TestRunner;

import org.openTwoFactor.server.config.TwoFactorServerConfig;



/**
 *
 */
public class TwoFactorServerUtilsTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
    TestRunner.run(new TwoFactorServerUtilsTest("testObscureName"));
    
//    int secretSize = 10;
//    int numOfScratchCodes = 5;
//    int scratchCodeSize = 5;
//    
//    // Allocating the buffer
//    byte[] buffer =
//      new byte[secretSize + numOfScratchCodes * scratchCodeSize];
//
//    // Filling the buffer with random numbers.
//    // Notice: you want to reuse the same random generator
//    // while generating larger random number sequences.
//    new Random().nextBytes(buffer);
//
//    buffer = new byte[] { 'H', 'e', 'l', 'l', 'o', '!', (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };
//    
//    //Now we want to extract the bytes corresponding to the secret key and encode it using the Base32 encoding. 
//    //I'm using the Apache Common Codec library to get a codec implementation:
//
//    // Getting the key and converting it to Base32
//    Base32 codec = new Base32();
//    byte[] secretKey = Arrays.copyOf(buffer, secretSize);
//    byte[] bEncodedKey = codec.encode(secretKey);
//    String encodedKey = new String(bEncodedKey);
//    System.out.println(encodedKey);
    
//    byte[] theBytes = new byte[10];
//    SecureRandom secureRandom = new SecureRandom();
//    secureRandom.nextBytes(theBytes);
//    Base32 codec = new Base32();
//    byte[] encoded = codec.encode(theBytes);
//    @SuppressWarnings("unused")
//    String secret = new String(encoded);

    
    //System.out.println(secret);
    
    //V7I7VOGZ4L24OC3J

  }
  
  /**
   * 
   */
  public TwoFactorServerUtilsTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public TwoFactorServerUtilsTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testGenerateRandomKey() {
    for (int i=0;i<30;i++) {
      @SuppressWarnings("unused")
      String generateBase32secret = TwoFactorServerConfig.retrieveConfig().twoFactorLogic().generateBase32secret(10);
      //System.out.println(generateBase32secret);
    }
  }
  
  /**
   * 
   */
  public void testObscureName() {
    
    //* if name is Chris Hyzer   then convert to    Ch*** H****
    assertEquals("Ch*** H****", TwoFactorServerUtils.obscureName("Chris Hyzer"));
    
    //* if name is Dr. Jim R. Hopkins   then convert to    Dr. Ji* R. H******
    assertEquals("Dr. Ji* R. H******", TwoFactorServerUtils.obscureName("Dr. Jim R. Hopkins"));

    
  }
  
}
