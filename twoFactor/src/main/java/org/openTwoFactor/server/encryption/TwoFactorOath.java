/**
 * @author mchyzer
 * $Id: TwoFactorOath.java,v 1.1 2013/06/20 06:02:51 mchyzer Exp $
 */
package org.openTwoFactor.server.encryption;

import java.io.File;
import java.io.FileWriter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.openTwoFactor.server.TwoFactorLogicInterface;
import org.openTwoFactor.server.config.TwoFactorServerConfig;
import org.openTwoFactor.server.util.TwoFactorPassResult;
import org.openTwoFactor.server.util.TwoFactorServerUtils;



/**
 *
 */
public class TwoFactorOath {

  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {

    File result = new File("c:/temp/secrets.txt");
    FileWriter fileWriter = new FileWriter(result);
    
    
    
    for (int i=0; i < 10000000; i++) {
      fileWriter.write(TwoFactorOath.twoFactorGenerateTwoFactorPass() + "\n");
      if ((i+1)%1000 == 0) {
        System.out.println(i);
      }
    }
    
    fileWriter.close();
    
    //String loginid = "mchyzer";
    //String pass = "801195";
    //
    //TwoFactorUser twoFactorUser = TwoFactorUser.retrieveByLoginid(TwoFactorDaoFactory.getFactory(), loginid);
    //String secret = twoFactorUser.getTwoFactorSecretUnencrypted();
    //TwoFactorPassResult twoFactorPassResult = TwoFactorOath.twoFactorCheckPassword(
    //    secret, pass, twoFactorUser.getSequentialPassIndex(), 
    //    twoFactorUser.getLastTotpTimestampUsed(), twoFactorUser.getLastTotp60TimestampUsed(), twoFactorUser.getTokenIndex(), 
    //    twoFactorUser.getPhoneCodeUnencryptedIfNotExpired());
    //
    //System.out.println("Pass correct: " + twoFactorPassResult.isPasswordCorrect());
    //System.out.println("30 time period: " + twoFactorPassResult.getLastTotp30TimestampUsed());
    //System.out.println("60 time period: " + twoFactorPassResult.getLastTotp60TimestampUsed());
    //System.out.println("HOTP index: " + twoFactorPassResult.getNextHotpIndex());
    //System.out.println("token index: " + twoFactorPassResult.getNextTokenIndex());
    //System.out.println("Phone pass: " + twoFactorPassResult.isPhonePass());
  }

  /**
   * valid pass chars
   */
  private static final char[] twoFactorValidChars = new char[]{
    'A', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 
    'N', 'P', 'Q', 'R', 'T', 'U', 'V', 'W', 'X', 'Y', '3', '4', '7'
  };
  
  /**
   * invalid pass chars, look confusing, like other chars
   */
  private static final char[] twoFactorInvalidPassChars = new char[]{};
      
//      new char[]{
//    '0', 'O', '1', 'I', '5', 'S', 'B', '8', 'Z', '2', '6'
//  };

  
  
  /**
   * generate a two factor secret
   * @return the two factor secret in base32
   */
  public static String twoFactorGenerateTwoFactorPass() {
    int bytesInSecret = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.bytesInSecret", 10);
    
    String pass = TwoFactorServerConfig.retrieveConfig().twoFactorLogic().generateBase32secret(bytesInSecret);
    
    for (char theChar : twoFactorInvalidPassChars) {
      //Dont do i's or o's since they are confusing 
      pass = pass.replace(theChar, twoFactorValidChars[
        new Random().nextInt(twoFactorValidChars.length)]);
    }
    return pass;
  }

  /** two factor one pass pattern */
  private static final Pattern twoFactorOnePassPattern = Pattern.compile("^\\s*([0-9]+)\\s*$");
  
  /**
   * result from totp check
   */
  public static class TwoFactorTotpResult {
    
    /**
     * if the two factor pass was correct
     */
    private boolean twoFactorCorrect;
    
    /**
     * totp time period if was correct millis div 30k
     */
    private Long totpTimePeriod;

    
    /**
     * if the two factor pass was correct
     * @return the twoFactorCorrect
     */
    public boolean isTwoFactorCorrect() {
      return this.twoFactorCorrect;
    }

    
    /**
     * if the two factor pass was correct
     * @param twoFactorCorrect1 the twoFactorCorrect to set
     */
    public void setTwoFactorCorrect(boolean twoFactorCorrect1) {
      this.twoFactorCorrect = twoFactorCorrect1;
    }

    
    /**
     * totp time period if was correct millis div 30k
     * @return the totpTimePeriod
     */
    public Long getTotpTimePeriod() {
      return this.totpTimePeriod;
    }

    
    /**
     * totp time period if was correct millis div 30k
     * @param totpTimePeriod1 the totpTimePeriod to set
     */
    public void setTotpTimePeriod(Long totpTimePeriod1) {
      this.totpTimePeriod = totpTimePeriod1;
    }
    
  }
  
  /**
   * see if time based password
   * @param twoFactorLogic 
   * @param secret
   * @param passwordInt
   * @param totpPeriodsInPast 
   * @param totpPeriodsInFuture 
   * @param secondsPassChanges 
   * @return true if the two factor pass is correct for totp
   */
  public static TwoFactorTotpResult twoFactorCheckPasswordTotp(TwoFactorLogicInterface twoFactorLogic, 
      byte[] secret, int passwordInt, int totpPeriodsInPast, int totpPeriodsInFuture, int secondsPassChanges) {

    TwoFactorTotpResult twoFactorTotpResult = new TwoFactorTotpResult();

    //check totp first
    int timeOverSeconds = (int)(System.currentTimeMillis() / (secondsPassChanges * 1000));

    //check the current time period
    if (passwordInt == twoFactorLogic.totpPassword(secret, timeOverSeconds)) {
      twoFactorTotpResult.setTotpTimePeriod(Long.valueOf(timeOverSeconds));
      twoFactorTotpResult.setTwoFactorCorrect(true);
      return twoFactorTotpResult;
    }

    //check the previous time period(s)
    for (int i=0;i<totpPeriodsInPast;i++) {
      int timePeriod = timeOverSeconds-(i+1);
      int totpPassword = twoFactorLogic.totpPassword(secret, timePeriod);

      if (passwordInt == totpPassword) {
        twoFactorTotpResult.setTotpTimePeriod(Long.valueOf(timePeriod));
        twoFactorTotpResult.setTwoFactorCorrect(true);
        return twoFactorTotpResult;
      }
    }

    //check the next time period(s)
    for (int i=0;i<totpPeriodsInFuture;i++) {
      int timePeriod = timeOverSeconds+(i+1);
      if (passwordInt == twoFactorLogic.totpPassword(secret, timePeriod)) {
        twoFactorTotpResult.setTotpTimePeriod(Long.valueOf(timePeriod));
        twoFactorTotpResult.setTwoFactorCorrect(true);
        return twoFactorTotpResult;
      }
    }
    twoFactorTotpResult.setTwoFactorCorrect(false);
    return twoFactorTotpResult;
  }
  
  
  
  /**
   * check a password
   * @param secretString is base32 encoded string
   * @param password
   * @param sequentialPassIndexAvailable the index that is available for this user
   * @param lastTotp30TimestampUsed 
   * @param lastTotp60TimestampUsed 
   * @param tokenIndexAvailable if not null check token hotp password
   * @param phonePass
   * @return true if the password is correct
   */
  public static TwoFactorPassResult twoFactorCheckPassword(String secretString, String password, 
      Long sequentialPassIndexAvailable, Long lastTotp30TimestampUsed, 
      Long lastTotp60TimestampUsed, Long tokenIndexAvailable, String phonePass) {
    
    sequentialPassIndexAvailable = TwoFactorServerUtils.defaultIfNull(sequentialPassIndexAvailable, 1L);
    
    Base32 base32 = new Base32();
    byte[] secret = base32.decode(secretString);
    
    TwoFactorPassResult twoFactorPassResult = new TwoFactorPassResult();
    
    //lets get the password
    if (password == null) {
      return twoFactorPassResult;
    }
    Matcher onePasswordMatcher = twoFactorOnePassPattern.matcher(password);
    int passwordInt = -1;
    
    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();

    if (onePasswordMatcher.matches()) {
      password = onePasswordMatcher.group(1);
      passwordInt = Integer.parseInt(password);
      
      //try phone password
      if (!StringUtils.isBlank(phonePass)) {
        try {
          int phonePasswordInt = Integer.parseInt(phonePass);
          if (phonePasswordInt == passwordInt) {
            twoFactorPassResult.setPasswordCorrect(true);
            twoFactorPassResult.setPhonePass(true);
            return twoFactorPassResult;
          }
        } catch (Exception e) {
          LOG.error("Error with phone pass: '" + phonePass + "'" );
        }
      }
      
      //check TOTP periods for time periods 
      String timePeriods = TwoFactorServerConfig.retrieveConfig().propertyValueString("twoFactorServer.totpTimePeriodsInSeconds");
      
      for (String timePeriodSecondsString : TwoFactorServerUtils.nonNull(TwoFactorServerUtils.splitTrim(timePeriods, ","), String.class)) {
        
        int timePeriodSeconds = TwoFactorServerUtils.intValue(timePeriodSecondsString);
        
        int totpPeriodsInPast = TwoFactorServerConfig.retrieveConfig().propertyValueIntRequired("twoFactorServer.totp" + timePeriodSeconds + "PeriodsInPast");
        int totpPeriodsInFuture = TwoFactorServerConfig.retrieveConfig().propertyValueIntRequired("twoFactorServer.totp" + timePeriodSeconds + "PeriodsInFuture");

        TwoFactorTotpResult twoFactorTotpResult = twoFactorCheckPasswordTotp(twoFactorLogic, secret, passwordInt, totpPeriodsInPast, totpPeriodsInFuture, timePeriodSeconds);
        if (twoFactorTotpResult.isTwoFactorCorrect()) {
          
          //if totp passwords can be used only once
          boolean totpPassesCanBeUsedOnlyOnce = TwoFactorServerConfig.retrieveConfig()
            .propertyValueBoolean("twoFactorServer.totpPassesCanBeUsedOnlyOnce", true);
          
          Long lastTotpTimestampUsed = null;
          
          switch(timePeriodSeconds) {
            case 30:
              lastTotpTimestampUsed = lastTotp30TimestampUsed;
              break;
              
            case 60:
              lastTotpTimestampUsed = lastTotp60TimestampUsed;
              break;
            default:
              throw new RuntimeException("Invalid time period: " + timePeriodSeconds);
          }
          
          //lets see if time period is ok
          if (!totpPassesCanBeUsedOnlyOnce || lastTotpTimestampUsed == null || lastTotpTimestampUsed < twoFactorTotpResult.getTotpTimePeriod()) {
            
            switch(timePeriodSeconds) {
              case 30:
                twoFactorPassResult.setLastTotp30TimestampUsed(twoFactorTotpResult.getTotpTimePeriod());
                break;
                
              case 60:
                twoFactorPassResult.setLastTotp60TimestampUsed(twoFactorTotpResult.getTotpTimePeriod());
                break;
              default:
                throw new RuntimeException("Invalid time period: " + timePeriodSeconds);
            }
            twoFactorPassResult.setPasswordCorrect(true);
            return twoFactorPassResult;
          }
        }
      }
        
    }
      

    //first try token hotp passes
    {
      if (tokenIndexAvailable != null) {
        int hotpIndexesInFuture = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.hotpTokenIndexesInFuture", 20);
        int hotpSecretsMultipleIndexInFuture = TwoFactorServerConfig.retrieveConfig().propertyValueInt(
            "twoFactorServer.hotpTokenSecretsMultipleIndexInFuture", 200);
        TwoFactorPassResult twoFactorPassResultTemp = hotpPasswordHelper(hotpIndexesInFuture, 
            hotpSecretsMultipleIndexInFuture, tokenIndexAvailable, password, passwordInt, secret, true);
        if (twoFactorPassResultTemp != null) {
          return twoFactorPassResultTemp;
        }
      }
    }
    
    //then try printed hotp codes
    {
      if (sequentialPassIndexAvailable != null) {
        
        int hotpIndexesInFuture = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.hotpIndexesInFuture", 1);
        int hotpSecretsMultipleIndexInFuture = TwoFactorServerConfig.retrieveConfig().propertyValueInt(
            "twoFactorServer.hotpSecretsMultipleIndexInFuture", 20);
        TwoFactorPassResult twoFactorPassResultTemp = hotpPasswordHelper(hotpIndexesInFuture, 
            hotpSecretsMultipleIndexInFuture, sequentialPassIndexAvailable, password, passwordInt, secret, false);
        if (twoFactorPassResultTemp != null) {
          return twoFactorPassResultTemp;
        }
      }
    }
        
    return twoFactorPassResult;
  }
  
  /**
   * try hotp passwords (either the right one, one in the future, or multiple together in future)
   * @param hotpIndexesInFuture
   * @param hotpSecretsMultipleIndexInFuture
   * @param sequentialPassIndexAvailable
   * @param password 
   * @param passwordInt 
   * @param secret 
   * @param isTokenPassInsteadOfPrinted true if token, false if printed
   * @return the result if it is ok, null if not
   */
  public static TwoFactorPassResult hotpPasswordHelper(int hotpIndexesInFuture, int hotpSecretsMultipleIndexInFuture, 
      long sequentialPassIndexAvailable, String password, int passwordInt, byte[] secret, boolean isTokenPassInsteadOfPrinted) {

    TwoFactorLogicInterface twoFactorLogic = TwoFactorServerConfig.retrieveConfig().twoFactorLogic();
    TwoFactorPassResult twoFactorPassResult = new TwoFactorPassResult();
    
    int hotpSecretsAccepted = TwoFactorServerConfig.retrieveConfig().propertyValueInt("twoFactorServer.hotpSecretsAccepted", 2);

    int hotpCalculateInFuture = Math.max(hotpIndexesInFuture, hotpSecretsMultipleIndexInFuture + hotpSecretsAccepted);
    
    int[] hotpPasses = new int[hotpCalculateInFuture];
    
    //try sequential pass(es).  Add one since you need to check the current one
    for (int i=0;i<hotpCalculateInFuture;i++) {
      long sequentialPassIndexToCheck = sequentialPassIndexAvailable + i;
      int hotpPassword = twoFactorLogic.hotpPassword(secret, (int)sequentialPassIndexToCheck);
      hotpPasses[i] = hotpPassword;
      
      if (i<=hotpIndexesInFuture && passwordInt != -1 && passwordInt == hotpPassword) {
        twoFactorPassResult.setPasswordCorrect(true);
        if (isTokenPassInsteadOfPrinted) {
          twoFactorPassResult.setNextTokenIndex(sequentialPassIndexToCheck + 1);
        } else {
          twoFactorPassResult.setNextHotpIndex(sequentialPassIndexToCheck + 1);
        }
        return twoFactorPassResult;
      }
      
    }
    //check multiple passes (note, for multiple passes, dont do totp)!
    if (hotpSecretsAccepted > 1 && passwordInt == -1) {
      
      //lets get the secrets
      int[] secrets = new int[hotpSecretsAccepted];
      boolean foundPasswords = false;
      switch(hotpSecretsAccepted) {
        case 2:
          
          Matcher matcher = otpTwoPasswordPattern.matcher(password);
          
          if (matcher.matches()) {
            secrets[0] = Integer.parseInt(matcher.group(1));
            secrets[1] = Integer.parseInt(matcher.group(2));
            foundPasswords = true;
          } else {
          
            matcher = otpTwoPasswordTogetherPattern.matcher(password);
            
            if (matcher.matches()) {
              secrets[0] = Integer.parseInt(matcher.group(1));
              secrets[1] = Integer.parseInt(matcher.group(2));
              foundPasswords = true;
            }
          }
          
          break;
        case 3:
          
          matcher = otpThreePasswordPattern.matcher(password);
          
          if (matcher.matches()) {
            secrets[0] = Integer.parseInt(matcher.group(1));
            secrets[1] = Integer.parseInt(matcher.group(2));
            secrets[2] = Integer.parseInt(matcher.group(3));
            foundPasswords = true;
          } else {
            matcher = otpThreePasswordTogetherPattern.matcher(password);
            
            if (matcher.matches()) {
              secrets[0] = Integer.parseInt(matcher.group(1));
              secrets[1] = Integer.parseInt(matcher.group(2));
              secrets[2] = Integer.parseInt(matcher.group(3));
              foundPasswords = true;
            } 
          }
          
          break;
        default:
          throw new RuntimeException("twoFactorServer.hotpSecretsAccepted in properties file must be less than 4");
      }
      //if the submission is a set of passwords
      if (foundPasswords) {
        //loop through all passwords in future to check
        HOTP_INDEX: for (int hotpIndex=0;hotpIndex<hotpSecretsMultipleIndexInFuture;hotpIndex++) {
          //look through the set of passwords submitted
          for (int submittedPassIndex=0;submittedPassIndex<hotpSecretsAccepted;submittedPassIndex++) {
            //see if ok
            if (secrets[submittedPassIndex] != hotpPasses[hotpIndex + submittedPassIndex]) {
              continue HOTP_INDEX;
            }
          }
          //all the passwords matched
          twoFactorPassResult.setPasswordCorrect(true);
          if (isTokenPassInsteadOfPrinted) {
            twoFactorPassResult.setNextTokenIndex(sequentialPassIndexAvailable + hotpIndex + hotpSecretsAccepted);
          } else {
            twoFactorPassResult.setNextHotpIndex(sequentialPassIndexAvailable + hotpIndex + hotpSecretsAccepted);
          }
          return twoFactorPassResult;
        }
        
      }
    }
    return null;

  }

  /**
   * 
   */
  private static final Pattern otpThreePasswordPattern = Pattern.compile("^\\s*(\\d+)\\D+(\\d+)\\D+(\\d+)\\s*$");

  /**
   * 
   */
  private static final Pattern otpThreePasswordTogetherPattern = Pattern.compile("^\\s*(\\d{6})(\\d{6})(\\d{6})\\s*$");

  /**
   * 
   */
  private static final Pattern otpTwoPasswordPattern = Pattern.compile("^\\s*(\\d+)\\D+(\\d+)\\s*$");

  /**
   * 
   */
  private static final Pattern otpTwoPasswordTogetherPattern = Pattern.compile("^\\s*(\\d{6})(\\d{6})\\s*$");

  /** logger */
  private static final Log LOG = TwoFactorServerUtils.getLog(TwoFactorOath.class);
  

}
