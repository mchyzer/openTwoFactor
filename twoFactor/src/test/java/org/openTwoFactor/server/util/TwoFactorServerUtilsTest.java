package org.openTwoFactor.server.util;

import java.util.Date;

import junit.framework.TestCase;
import junit.textui.TestRunner;
import net.sf.json.JSONObject;

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
    
    TestRunner.run(new TwoFactorServerUtilsTest("testHistogramDaysAutoFalloff"));
    
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
  
  /**
   * 
   */
  public void testHistogramDaysIncrement() {
    
    String histogramJson = null;
    
    assertEquals(0, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000101"), histogramJson));
    
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000101"), histogramJson);

    assertEquals(1, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000101"), histogramJson));
    
    JSONObject jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(1, jsonObject.size());

    //month is 0 indexed
    assertEquals(1, jsonObject.getInt("0001"));

    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000101"), histogramJson);

    assertEquals(2, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000101"), histogramJson));
    
    jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(1, jsonObject.size());

    assertEquals(2, jsonObject.getInt("0001"));
  
  
  }
  
  
  /**
   * 
   */
  public void testHistogramDaysAutoFalloff() {
    
    String histogramJson = null;
    
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000101"), histogramJson);
    JSONObject jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(1, jsonObject.size());
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000102"), histogramJson);
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000102"), histogramJson);
    jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(2, jsonObject.size());
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000103"), histogramJson);
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000103"), histogramJson);
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000103"), histogramJson);
    jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(3, jsonObject.size());
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000104"), histogramJson);
    jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(4, jsonObject.size());
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000105"), histogramJson);
    jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(5, jsonObject.size());
    histogramJson = TwoFactorServerUtils.histogramIncrementForDate(TwoFactorServerUtils.toTimestamp("20000106"), histogramJson);
    jsonObject = JSONObject.fromObject( histogramJson );
    assertEquals(5, jsonObject.size());

    assertFalse(jsonObject.containsKey("0001"));
    assertTrue(jsonObject.containsKey("0002"));
    assertTrue(jsonObject.containsKey("0003"));
    assertTrue(jsonObject.containsKey("0004"));
    assertTrue(jsonObject.containsKey("0005"));
    assertTrue(jsonObject.containsKey("0006"));
    
    assertEquals(2, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000102"), histogramJson));
    assertEquals(3, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000103"), histogramJson));
    assertEquals(1, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000104"), histogramJson));
    assertEquals(1, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000105"), histogramJson));
    assertEquals(1, TwoFactorServerUtils.histogramValueForDate(TwoFactorServerUtils.toTimestamp("20000106"), histogramJson));
    
    //month is 0 indexed
    assertEquals(2, jsonObject.getInt("0002"));
    assertEquals(3, jsonObject.getInt("0003"));
    assertEquals(1, jsonObject.getInt("0004"));
    assertEquals(1, jsonObject.getInt("0005"));
    assertEquals(1, jsonObject.getInt("0006"));

  }

}
