package org.openTwoFactor.server.poc;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

/**
 * 
 */
public class GoogleAuthPoc3 {
  
  /**
   * //   These are used to calculate the check-sum digits.
   * //                                0  1  2  3  4  5  6  7  8  9
   */
  private static final int[] doubleDigits =
  { 0, 2, 4, 6, 8, 1, 3, 5, 7, 9 };

  /**
   * Calculates the checksum using the credit card algorithm.
   * This algorithm has the advantage that it detects any single
   * mistyped digit and any single transposition of
   * adjacent digits.
   *
   * @param num the number to calculate the checksum for
   * @param digits number of significant places in the number
   *
   * @return the checksum of num
   */
  public static int calcChecksum(long num, int digits) {
    boolean doubleDigit = true;
    int     total = 0;
    while (0 < digits--) {
      int digit = (int) (num % 10);
      num /= 10;
      if (doubleDigit) {
        digit = doubleDigits[digit];
      }
      total += digit;
      doubleDigit = !doubleDigit;
    }
    int result = total % 10;
    if (result > 0) {
      result = 10 - result;
    }
    return result;
  }

  /**
   * This method uses the JCE to provide the HMAC-SHA-1
   * algorithm.
   * HMAC computes a Hashed Message Authentication Code and
   * in this case SHA1 is the hash algorithm used.
   *
   * @param keyBytes   the bytes to use for the HMAC-SHA-1 key
   * @param text       the message or text to be authenticated.
   *
   * @throws NoSuchAlgorithmException if no provider makes
   *       either HmacSHA1 or HMAC-SHA-1
   *       digest algorithms available.
   * @throws InvalidKeyException
   *       The secret provided was not a valid HMAC-SHA-1 key.
   * @return the byte array
   */
  public static byte[] hmac_sha1(byte[] keyBytes, byte[] text)
  throws NoSuchAlgorithmException, InvalidKeyException
  {
    //        try {
    Mac hmacSha1;
    try {
      hmacSha1 = Mac.getInstance("HmacSHA1");
    } catch (NoSuchAlgorithmException nsae) {
      hmacSha1 = Mac.getInstance("HMAC-SHA-1");
    }
    SecretKeySpec macKey =
      new SecretKeySpec(keyBytes, "RAW");
    hmacSha1.init(macKey);
    return hmacSha1.doFinal(text);
    //        } catch (GeneralSecurityException gse) {
    //            throw new UndeclaredThrowableException(gse);
    //        }
  }

  /**
   * 
   */
  private static final int[] DIGITS_POWER
  // 0 1  2   3    4     5      6       7        8
  = {1,10,100,1000,10000,100000,1000000,10000000,100000000};

  /**
   * This method generates an OTP value for the given
   * set of parameters.
   *
   * @param secret       the shared secret
   * @param movingFactor the counter, time, or other value that
   *                     changes on a per use basis.
   * @param codeDigits   the number of digits in the OTP, not
   *                     including the checksum, if any.
   * @param addChecksum  a flag that indicates if a checksum digit
   *                     should be appended to the OTP.
   * @param truncationOffset the offset into the MAC result to
   *                     begin truncation.  If this value is out of
   *                     the range of 0 ... 15, then dynamic
   *                     truncation  will be used.
   *                     Dynamic truncation is when the last 4
   *                     bits of the last byte of the MAC are
   *                     used to determine the start offset.
   * @throws NoSuchAlgorithmException if no provider makes
   *                     either HmacSHA1 or HMAC-SHA-1
   *                     digest algorithms available.
   * @throws InvalidKeyException
   *                     The secret provided was not
   *                     a valid HMAC-SHA-1 key.
   *
   * @return A numeric String in base 10 that includes
   * digits plus the optional checksum
   * digit if requested.
   */
  static public String generateOTP(byte[] secret,
      long movingFactor,
      int codeDigits,
      boolean addChecksum,
      int truncationOffset)
  throws NoSuchAlgorithmException, InvalidKeyException
  {
    // put movingFactor value into text byte array
    String result = null;
    int digits = addChecksum ? (codeDigits + 1) : codeDigits;
    
    byte[] text = new byte[8];
    for (int i = text.length - 1; i >= 0; i--) {
      text[i] = (byte) (movingFactor & 0xff);
      movingFactor >>= 8;
    }

    // compute hmac hash
    byte[] hash = hmac_sha1(secret, text);

    // put selected bytes into result int
    int offset = hash[hash.length - 1] & 0xf;
    
    if ( (0<=truncationOffset) &&
        (truncationOffset<(hash.length-4)) ) {
      offset = truncationOffset;
    }
    int binary =
      ((hash[offset] & 0x7f) << 24)
      | ((hash[offset + 1] & 0xff) << 16)
      | ((hash[offset + 2] & 0xff) << 8)
      | (hash[offset + 3] & 0xff);

    int otp = binary % DIGITS_POWER[codeDigits];
    if (addChecksum) {
      otp =  (otp * 10) + calcChecksum(otp, codeDigits);
    }
    result = Integer.toString(otp);
    while (result.length() < digits) {
      result = "0" + result;
    }
    return result;
  }
  
  /**
   * 
   * @param hex
   * @return the byte array
   */
  public static byte[] hexStringToByte(String hex) {
      int len = (hex.length() / 2);
      byte[] result = new byte[len];
      char[] achar = hex.toCharArray();
      for (int i = 0; i < len; i++) {
       int pos = i * 2;
       result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
      }
      return result;
  }

  /**
   * 
   * @param c
   * @return the byte
   */
  private static byte toByte(char c) {
      byte b = (byte) "0123456789ABCDEF".indexOf(c);
      return b;
  }
  /**
   * @param args
   * @throws UnsupportedEncodingException
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   */
  public static void main(String[] args) throws 
  UnsupportedEncodingException,
  NoSuchAlgorithmException, InvalidKeyException {
    
    if (args == null || args.length == 0) {
      System.out.println("Usage:");
      System.out.println("java testOTP share_secret count");
    }
    
//    byte[] plainText = args[0].getBytes("UTF-8");
    
    byte[] plainText = hexStringToByte("8F9811E09306A1EACEDFE2AC2545940A2512E328");
    /*for (int j = 0; j < 40; j++) {
        System.out.println(Integer.toHexString(plainText[j]));
        //System.out.println("count: " + i + ", otp: " + HOTPAlgorithm.generateOTP(text, i, 6,false,16));
    }*/
    
//    String   temp1 = "12345678901234567890";    
//    byte[]   plainText   =   temp1.getBytes("UTF-8");
    
    //long   count = 1014;//Long.parseLong(args[1].trim());
    boolean addChecksum = false;
    
    //String   mactext = generateOTP(plainText, count, 6, addChecksum,16);
    for (int i = 1014; i < 1024; i++) {
        System.out.println("count: " + i + ", otp: " + generateOTP(plainText, 
            i, 6, addChecksum,16));
        //System.out.println("count: " + i + ", otp: " + HOTPAlgorithm.generateOTP(text, i, 6,false,16));
    }
    
    
    String secret = "MWEK YAWE NYMK TEPU ";
    Base32 codec = new Base32();
    plainText = codec.decode(secret);
    
    StringBuffer hexString = new StringBuffer();
    for (int i=0;i<plainText.length;i++) {
        hexString.append(Integer.toHexString(0xFF & plainText[i]));
        }
    System.out.println("Plaintext: " + hexString);
    
    //starts at 1
    for (int i = 1; i < 10; i++) {
      System.out.println("count: " + i + ", otp: " + generateOTP(plainText, i, 6, addChecksum,16));
      //System.out.println("count: " + i + ", otp: " + HOTPAlgorithm.generateOTP(text, i, 6,false,16));
  }
    
    
//    System.out.println("Share Secret: "+args[0]);
//    System.out.println("Count: "+ count);
//    System.out.println("OTP: "+ mactext);
    
//    System.out.println("Original Text:"+args[0]);
//    System.out.println("Original Text:"+plainText);
    

    
//    System.out.println("\n" + mac.getProvider().getInfo());
//    System.out.println("\nMAC: ");
//    System.out.println(new String(mac.doFinal(), "UTF-8"));
  }
}