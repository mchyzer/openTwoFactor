/**
 * 
 */
package org.openTwoFactor.server;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.encryption.EncryptionKey;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;


/**
 * two factor logic default built in
 * @author mchyzer
 *
 */
public class TwoFactorLogic implements TwoFactorLogicInterface {

  /**
   * for debugging
   * @param secret 
   * @param sequentialPassIndex 
   * @param tokenIndex 
   * @param currentTimeMillis 
   * @param isHex 
   * 
   */
  public static void printPasswordsForSecret(String secret, Long sequentialPassIndex, Long tokenIndex, Long currentTimeMillis, boolean isHex) {
    
    Base32 codec = new Base32();
    byte[] plainText = codec.decode(secret);
    
    if (isHex) {
      try {
        plainText = Hex.decodeHex(secret.toCharArray());
      } catch (DecoderException de) {
        throw new RuntimeException("Bad hex", de);
      }
    }
    
    if (sequentialPassIndex == null) {
      sequentialPassIndex = 500001L;
    }
    
    if (tokenIndex == null) {
      tokenIndex = 0L;
    }
    
    System.out.println("HOTP (printed)");
    for (int i=-50;i<50;i++) {
      String label = "now";
      if (i < 0) {
        label = "" + i;
      }
      if (i > 0) {
        label = "+" + i;
      }
      System.out.println(label + ": " + new TwoFactorLogic().hotpPassword(plainText, sequentialPassIndex + i));
    }
    
    System.out.println("\nHOTP (token)");
    for (int i=-50;i<5000;i++) {
      String label = "now";
      if (i < 0) {
        label = "" + i;
      }
      if (i > 0) {
        label = "+" + i;
      }
      System.out.println(label + ": " + new TwoFactorLogic().hotpPassword(plainText, tokenIndex + i));
    }

    System.out.println("\nTOTP 60");
    
    currentTimeMillis = currentTimeMillis == null ? System.currentTimeMillis() : currentTimeMillis;

    for (int i=-50;i<50;i++) {
      String label = "now";
      if (i < 0) {
        label = "" + i;
      }
      if (i > 0) {
        label = "+" + i;
      }
      System.out.println(label + ": " + new TwoFactorLogic().totpPassword(plainText, (currentTimeMillis/60000) + i));
    }
    
    System.out.println("\nTOTP 30");
    
    for (int i=-50;i<50;i++) {
      String label = "now";
      if (i < 0) {
        label = "" + i;
      }
      if (i > 0) {
        label = "+" + i;
      }
      System.out.println(label + ": " + new TwoFactorLogic().totpPassword(plainText, (currentTimeMillis/30000) + i));
    }
    

//    for (int i=0;i<50000;i++) {
//      String label = "now";
//      if (i < 0) {
//        label = "" + i;
//      }
//      if (i > 0) {
//        label = "+" + i;
//      }
//      System.out.println(label + ": " + new TwoFactorLogic().totpPassword(plainText, i));
//    }
    

  }

  /**
   * for debugging
   * @param username
   */
  public static void printPasswordsForUser(String username) {
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), username);
    System.out.println(twoFactorUser.getTwoFactorSecretUnencrypted());
    printPasswordsForSecret(twoFactorUser.getTwoFactorSecretUnencrypted(), 
        twoFactorUser.getSequentialPassIndex(), twoFactorUser.getTokenIndex(), null, false);
  }
  
  /**
   * for debugging
   * @param args
   */
  public static void main(String[] args) {

//    if (true) {
//      System.out.println(EncryptionKey.decrypt("1473868252981__ar3DWJXXXXXXXXXXXXXXXXXXXXXX"));
//      return;
//    }
    
    if (true) {
      
      String secret = "";
      //System.out.println(new Date(23820173L*60));
      
      //findTimePeriodFob(secret, 551960, 305783);
      
      printPasswordsForSecret(secret, 
          null, null, 0L, false);
      return;
    }
    
    if (true) {
    
      @SuppressWarnings("unused")
      String netId = "mchyzer";
      
      String userName = "10021368";
      //userName = TfSourceUtils.resolveSubjectId(TfSourceUtils.mainSource(), netId, true);
      if (args.length > 0) {
        userName = args[0];
      }

      if (true) {
        printPasswordsForUser(userName);
        System.exit(0);
      }
      

    }
    
    String serial = "70000502";
    TwoFactorDeviceSerial twoFactorDeviceSerial = TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveBySerial(serial);
    
    String secret = twoFactorDeviceSerial.getTwoFactorSecretUnencrypted();
    
    System.out.println(secret);
    
    secret = "9f9360922509e02c15ffb14fbxxxxxxxxxxx";
    
//    printPasswordsForSecret(secret, null, null, null, true);
    
    
    System.out.println(new Date(23820173L*60));
    
    findTimePeriodFob(secret, 456154, 497306);
    
    
//    String secret = "GMUP LNR7 EFSY HEYZ";
//    System.out.println(secret);
//    Base32 codec = new Base32();
//    byte[] plainText = codec.decode(secret);
//    
//    String hexSecret = new String(Hex.encodeHex(plainText));
//    
//    System.out.println(hexSecret);
//    
//    printPasswordsForSecret(secret, null, null, null, false);
    
//    printPasswordsForUser(userName);
    
   // printPasswordsForSecret("", null, null, null, true);
   // printPasswordsForSecret("U7HT WEKC 4VQX KC3C", null, null);
    
    //printPasswordsForSecret("DUUE TNCV DGNK DXUL", null, null, 1391019502150L);

    
    
//    for (int i=0;i<100;i++) {
//      String secret = new TwoFactorLogic().generateBase32secret(10);
//      System.out.println(secret);
//    }
//    secret = "KJARPPQYTM3E7QYR";
//    System.out.println(secret);
//    Base32 codec = new Base32();
//    byte[] plainText = codec.decode(secret);
//
//    System.out.println("HOTP");
//    System.out.println(new TwoFactorLogic().hotpPassword(plainText, 1));
//    System.out.println(new TwoFactorLogic().hotpPassword(plainText, 2));
//    System.out.println(new TwoFactorLogic().hotpPassword(plainText, 3));
//    System.out.println("TOTP");
//    System.out.println("now: " + new TwoFactorLogic().totpPassword(plainText, System.currentTimeMillis()/30000));
//    System.out.println("-1: " + new TwoFactorLogic().totpPassword(plainText, (System.currentTimeMillis()/30000)-1));
//    System.out.println("+1: " + new TwoFactorLogic().totpPassword(plainText, (System.currentTimeMillis()/30000)+1));
//    System.out.println("+2: " + new TwoFactorLogic().totpPassword(plainText, (System.currentTimeMillis()/30000)+2));
//    System.out.println("+3: " + new TwoFactorLogic().totpPassword(plainText, (System.currentTimeMillis()/30000)+3));
//    System.out.println("+4: " + new TwoFactorLogic().totpPassword(plainText, (System.currentTimeMillis()/30000)+4));
//    System.out.println("+5: " + new TwoFactorLogic().totpPassword(plainText, (System.currentTimeMillis()/30000)+5));
//    System.out.println(new TwoFactorLogic().totpPassword(plainText, (System.currentTimeMillis()/30000)+6));

  }

  /**
   * @param secret
   * @param firstPass 
   * @param secondPass 
   */
  private static void findTimePeriodFob(String secret, int firstPass, int secondPass) {
    byte[] plainText = null;
    
    try {
      plainText = Hex.decodeHex(secret.toCharArray());
    } catch (DecoderException de) {
      try {
        Base32 codec = new Base32();
        plainText = codec.decode(secret);
      } catch (Exception e) {
        throw new RuntimeException("Bad secret", de);
      }
    }
    
    int totpPassword = new TwoFactorLogic().totpPassword(plainText, 0);
    
    for (int i=0;i<100000000;i++) {
      String label = "now";
      if (i < 0) {
        label = "" + i;
      }
      if (i > 0) {
        label = "+" + i;
      }
      
      int nextTotpPassword = new TwoFactorLogic().totpPassword(plainText, i+1);
      if (totpPassword == firstPass && nextTotpPassword == secondPass) {
        System.out.println(label + ": " + totpPassword);
      }
      totpPassword = nextTotpPassword;
      
      if ((i+1) % 10000000 == 0) {
        System.out.println(i);
      }
    }
  }
  
  /**
   * 
   */
  private static final int[] DIGITS_POWER
  // 0 1  2   3    4     5      6       7        8
  = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000 };

  /**
   * //   These are used to calculate the check-sum digits.
   * //                                0  1  2  3  4  5  6  7  8  9
   */
  private static final int[] doubleDigits =
  { 0, 2, 4, 6, 8, 1, 3, 5, 7, 9 };

  /**
   * @see TwoFactorLogicInterface#generateQrFile(String, File, int)
   */
  @Override
  public void generateQrFile(String data, File imageFile, int height) {
    QRCodeWriter writer = new QRCodeWriter();
    BitMatrix bitMatrix = null;
    try {
      bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, height, height);
      MatrixToImageWriter.writeToFile(bitMatrix, "gif", imageFile);
    } catch (Exception e) {
      throw new RuntimeException("Problem with imageFile: " + imageFile, e);
    }
  }

  /**
   * generate a new base 32 secret
   * @param secretSizeInBytes 
   * @return the secret
   */
  public String generateBase32secret(int secretSizeInBytes) {
    byte[] buffer =
        new byte[secretSizeInBytes];

    // Filling the buffer with random numbers.
    // Notice: you want to reuse the same random generator
    // while generating larger random number sequences.
    new SecureRandom().nextBytes(buffer);

    // Getting the key and converting it to Base32
    Base32 codec = new Base32();
    buffer = codec.encode(buffer);
    try {
      String secret = new String(buffer, "UTF-8");
      return secret;
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("UTF-8");
    }
  }

  /**
   * @see TwoFactorLogicInterface#totpPassword(byte[], long)
   */
  @Override
  public int totpPassword(byte[] secret, long timeDiv30000) {
    try {
      byte[] data = new byte[8];
      long value = timeDiv30000;
      for (int i = 8; i-- > 0; value >>>= 8) {
        data[i] = (byte) value;
      }
  
  
      SecretKeySpec signKey = new SecretKeySpec(secret, "HmacSHA1");
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
  
  
      return (int)truncatedHash;
    } catch (Exception e) {
      throw new RuntimeException("Problem generating TOTP!", e);
    }
  }

  /**
   * @see TwoFactorLogicInterface#hotpPassword(byte[], long)
   */
  @Override
  public int hotpPassword(byte[] secret, long movingFactor) {
    try {
      int codeDigits = 6;
      int truncationOffset = 16;
      boolean addChecksum = false;

      // put movingFactor value into text byte array
      //String result = null;
      //int digits = addChecksum ? (codeDigits + 1) : codeDigits;

      byte[] text = new byte[8];
      for (int i = text.length - 1; i >= 0; i--) {
        text[i] = (byte) (movingFactor & 0xff);
        movingFactor >>= 8;
      }

      // compute hmac hash
      byte[] hash = hmac_sha1(secret, text);

      // put selected bytes into result int
      int offset = hash[hash.length - 1] & 0xf;

      if ((0 <= truncationOffset) &&
          (truncationOffset < (hash.length - 4))) {
        offset = truncationOffset;
      }
      int binary =
          ((hash[offset] & 0x7f) << 24)
              | ((hash[offset + 1] & 0xff) << 16)
              | ((hash[offset + 2] & 0xff) << 8)
              | (hash[offset + 3] & 0xff);

      int otp = binary % DIGITS_POWER[codeDigits];
      if (addChecksum) {
        otp = (otp * 10) + calcChecksum(otp, codeDigits);
      }
      return otp;
    } catch (Exception e) {
      throw new RuntimeException("Problem with hotp!", e);
    }
  }

  /**
   * Calculates the checksum using the credit card algorithm.
   * This algorithm has the advantage that it detects any single
   * mistyped digit and any single transposition of
   * adjacent digits.  Used for HOTP
   *
   * @param num the number to calculate the checksum for
   * @param digits number of significant places in the number
   *
   * @return the checksum of num
   */
  public static int calcChecksum(long num, int digits) {
    boolean doubleDigit = true;
    int total = 0;
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
   * algorithm.  Used for HOTP
   * HMAC computes a Hashed Message Authentication Code and
   * in this case SHA1 is the hash algorithm used.
   *
   * @param keyBytes   the bytes to use for the HMAC-SHA-1 key
   * @param text       the message or text to be authenticated.
   * @return the byte array
   *
   * @throws NoSuchAlgorithmException if no provider makes
   *       either HmacSHA1 or HMAC-SHA-1
   *       digest algorithms available.
   * @throws InvalidKeyException
   *       The secret provided was not a valid HMAC-SHA-1 key.
   *
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

}
