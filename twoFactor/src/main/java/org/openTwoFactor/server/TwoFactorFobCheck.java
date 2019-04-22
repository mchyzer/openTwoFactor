/**
 * @author mchyzer
 * $Id$
 */
package org.openTwoFactor.server;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.openTwoFactor.server.beans.TwoFactorDeviceSerial;
import org.openTwoFactor.server.beans.TwoFactorUser;
import org.openTwoFactor.server.hibernate.TwoFactorDaoFactory;
import org.openTwoFactor.server.util.TfSourceUtils;
import org.openTwoFactor.server.util.TwoFactorServerUtils;


/**
 *
 */
public class TwoFactorFobCheck {

  /**
   * 
   */
  public TwoFactorFobCheck() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    if (args.length != 3) {
      throw new RuntimeException("Send in the fob serial, and 2 codes");
    }
    
    String fobSerial = args[0];
    String code0 = args[1];
    String code1 = args[2];
    
    //lets pull up the fob
    TwoFactorDeviceSerial twoFactorDeviceSerial = TwoFactorDaoFactory.getFactory().getTwoFactorDeviceSerial().retrieveBySerial(fobSerial);
    
    if (twoFactorDeviceSerial == null) {
      System.out.println("Cant find serial");
      return;
    }
    
    String secret = twoFactorDeviceSerial.getTwoFactorSecretUnencrypted();
    
    final String userUuid = twoFactorDeviceSerial.getUserUuid();
    
    System.out.println("Fob is registered to userUuid: " + userUuid);
    
    TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByUuid(TwoFactorDaoFactory.getFactory(), userUuid);
    twoFactorUser.setSubjectSource(TfSourceUtils.mainSource());
    System.out.println("User: " + twoFactorUser.getDescription());
    if (!twoFactorUser.isOptedIn()) {
      System.out.println("User is not opted in");
    } else {
      String userSecret = twoFactorUser.getTwoFactorSecretUnencrypted();
     
      System.out.println("User secret matches fob: " + StringUtils.equals(userSecret, secret));

      System.out.println("Current fob index in system for user: " + twoFactorUser.getSequentialPassIndex());
      System.out.println("Current printed index in system for user: " + twoFactorUser.getSeqPassIndexGivenToUser());

    }

    int matchingIndex = findTimePeriodFob(secret, TwoFactorServerUtils.intValue(code0), TwoFactorServerUtils.intValue(code1));

    if (matchingIndex == -1) {
      matchingIndex = 0;
    }
    printPasswordsForSecret(secret, matchingIndex, TwoFactorServerUtils.intValue(code0), TwoFactorServerUtils.intValue(code1));
  }

  /**
   * for debugging
   * @param secret 
   * @param tokenIndex 
   * @param firstPass 
   * @param secondPass 
   * 
   */
  public static void printPasswordsForSecret(String secret, int tokenIndex, int firstPass, int secondPass) {
    
    Base32 codec = new Base32();
    byte[] plainText = codec.decode(secret);
    
    System.out.println("\nHOTP (token)");
    for (int i=0;i<tokenIndex + 50;i++) {
      String label = "0";
      if (i > 0) {
        label = "+" + i;
      }
      String suffix = "";
      int currentPass = new TwoFactorLogic().hotpPassword(plainText, tokenIndex + i);
      if (currentPass == firstPass || currentPass == secondPass) {
        suffix = "  <<----";
      }
      System.out.println(StringUtils.leftPad(label + ": ", 6, ' ') + StringUtils.leftPad(Integer.toString(currentPass), 6, '0') + suffix);
    }

  }

  
  /**
   * @param secret
   * @param firstPass 
   * @param secondPass 
   * @return index that matches
   */
  private static int findTimePeriodFob(String secret, int firstPass, int secondPass) {
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
    
    // PRINTED
    long sequentialPassIndex = 500001L;
    
    int password = new TwoFactorLogic().hotpPassword(plainText, sequentialPassIndex);

    for (int i=0;i<20000;i++) {
      
      int nextPassword = new TwoFactorLogic().hotpPassword(plainText, sequentialPassIndex + i + 1);

      String label = "+" + (i+1);
      String label2 = "+" + (i+2);

      if (password == firstPass && nextPassword == secondPass) {
        System.out.println("HOTP(printed)!!!!! " + label + ": " + password);
        System.out.println("HOTP(printed)!!!!! " + label2 + ": " + nextPassword);
        return i;
      }

      password = nextPassword;
    }

    // FOB
    sequentialPassIndex = 0L;
    
    password = new TwoFactorLogic().hotpPassword(plainText, sequentialPassIndex);

    for (int i=0;i<20000;i++) {
      
      int nextPassword = new TwoFactorLogic().hotpPassword(plainText, sequentialPassIndex + i + 1);

      String label = "0";
      if (i > 0) {
        label = "+" + i;
      }
      String label2 = "+" + (i+1);

      if (password == firstPass && nextPassword == secondPass) {
        System.out.println("HOTP(fob)!!!!! " + label + ": " + password);
        System.out.println("HOTP(fob)!!!!! " + label2 + ": " + nextPassword);
        return -1;
      }
      password = nextPassword;
    }
    
    long currentIndex = (System.currentTimeMillis() / 30) -50;
    
    // TOTP 30 reset
    password = new TwoFactorLogic().totpPassword(plainText, currentIndex);
    
    for (int i=0;i<100;i++) {
      String label = "now";
      String label2 = null;
      int offset = -50 + i;
      if (offset < 0) {
        label = "" + offset;
      }
      if (offset > 0) {
        label = "+" + offset;
      }
      if (offset+1 < 0) {
        label2 = "" + (offset+1);
      }
      if (offset+1 > 0) {
        label2 = "+" + (offset+1);
      }
      
      int nextPassword = new TwoFactorLogic().totpPassword(plainText, currentIndex + i+1);
      if (password == firstPass && nextPassword == secondPass) {
        System.out.println("TOTP(30)!!!!! " + label + ": " + password);
        System.out.println("TOTP(30)!!!!! " + label2 + ": " + nextPassword);
        if (offset == 0) {
          System.out.println("Time is set correctly");
        } else {
          System.out.println("Time is off by " + (offset * 30) + " seconds");
        }
        System.out.println();
        return i;
      }
      password = nextPassword;
      
    }
    
    currentIndex = (System.currentTimeMillis() / 60) -50;
    
    // TOTP 60 reset
    password = new TwoFactorLogic().totpPassword(plainText, currentIndex);
    
    for (int i=0;i<100;i++) {
      String label = "now";
      String label2 = null;
      int offset = -50 + i;
      if (offset < 0) {
        label = "" + offset;
      }
      if (offset > 0) {
        label = "+" + offset;
      }
      if (offset+1 < 0) {
        label2 = "" + (offset+1);
      }
      if (offset+1 > 0) {
        label2 = "+" + (offset+1);
      }
      
      int nextPassword = new TwoFactorLogic().totpPassword(plainText, currentIndex + i+1);
      if (password == firstPass && nextPassword == secondPass) {
        System.out.println("TOTP(60)!!!!! " + label + ": " + password);
        System.out.println("TOTP(60)!!!!! " + label2 + ": " + nextPassword);
        if (offset == 0) {
          System.out.println("Time is set correctly");
        } else {
          System.out.println("Time is off by " + (offset) + " minutes");
        }
        return i;
      }
      password = nextPassword;
      
    }
    return -1;
  }

}
