package org.openTwoFactor.server.poc;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;

/**
 * 
 */
public class GoogleAuthPoc2 {

  /**
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {

    
    // Allocating the buffer
//    byte[] buffer =
//      new byte[secretSize + numOfScratchCodes * scratchCodeSie];
//
//    // Filling the buffer with random numbers.
//    // Notice: you want to reuse the same random generator
//    // while generating larger random number sequences.
//    new Random().nextBytes(buffer);
//
//
//    // Getting the key and converting it to Base32
//    Base32 codec = new Base32();
//    byte[] secretKey = Arrays.copyOf(buffer, secretSize);
//    byte[] bEncodedKey = codec.encode(secretKey);
//    String encodedKey = new String(bEncodedKey);
    
    String secret = "3PNT RF3J NWTL EKEU ";
    secret = StringUtils.strip(secret, " ");
    Base32 codec = new Base32();
    codec.decode(secret);

    Calendar testCalendar = new GregorianCalendar();
    testCalendar.setTimeInMillis(System.currentTimeMillis());
    testCalendar.set(Calendar.MILLISECOND, 0);
    testCalendar.set(Calendar.SECOND, 0);

    Calendar testCalendar30 = new GregorianCalendar();
    testCalendar30.setTimeInMillis(System.currentTimeMillis());
    testCalendar30.set(Calendar.MILLISECOND, 0);
    testCalendar30.set(Calendar.SECOND, 30);

    System.out.println(check_code(secret, 363727, System.currentTimeMillis()/30000));
  }

  /**
   * 
   * @param secret
   * @param code
   * @param t
   * @return boolean
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   */
  private static boolean check_code(
    String secret,
    long code,
    long t)
      throws NoSuchAlgorithmException,
        InvalidKeyException {
    Base32 codec = new Base32();
    byte[] decodedKey = codec.decode(secret);


    // Window is used to check codes generated in the near past.
    // You can use this value to tune how far you're willing to go. 
    int window = 3;
    for (int i = -window; i <= window; ++i) {
      long hash = verify_code(decodedKey, t + i);

      System.out.println(hash);
      if (hash == code) {
        return true;
      }
    }


    // The validation code is invalid.
    return false;
  }

  /**
   * 
   * @param key
   * @param t
   * @return int
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   */
  private static int verify_code(
    byte[] key,
    long t)
    throws NoSuchAlgorithmException,
      InvalidKeyException {
    byte[] data = new byte[8];
    long value = t;
    for (int i = 8; i-- > 0; value >>>= 8) {
      data[i] = (byte) value;
    }


    SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(signKey);
    byte[] hash = mac.doFinal(data);


    int offset = hash[20 - 1] & 0xF;
    
    // We're using a long because Java hasn't got unsigned int.
    long truncatedHash = 0;
    for (int i = 0; i < 4; ++i) {
      truncatedHash <<= 8;
      // We are dealing with signed bytes:
      // we just keep the first byte.
      truncatedHash |= (hash[offset + i] & 0xFF);
    }


    truncatedHash &= 0x7FFFFFFF;
    truncatedHash %= 1000000;


    return (int) truncatedHash;
  }


}
